package ryuunoakaihitomi.xposed.screenshothookbox;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AndroidAppHelper;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.media.MediaActionSound;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by ZQY on 2017/10/28.
 * Hooker
 */
@SuppressLint("SimpleDateFormat")
public class X implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static final String TAG = "Screenshot_Hooklog";

    //_switch will come in handy if I start to hook String.format() method.
    private static boolean _switch;

    @Override
    public void initZygote(StartupParam startupParam) {
        //start.
        XposedBridge.log("//Logcat tag of \"Screenshot Hookbox\" is " + TAG);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        final String app = lpparam.packageName;
        //Disable the security mechanism.(1)Get the first parameter.(flag)
        Class<?> vwclz = XposedHelpers.findClass("android.view.Window", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(vwclz, "setFlags", int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int i = (Integer) param.args[0];
                        if (i == WindowManager.LayoutParams.FLAG_SECURE) {
                            param.args[0] = 0;
                            Log.d(TAG, "Window.setFlags(0,):" + app);
                        }
                    }

                }
        );
        //Disable the security mechanism.(2)
        Class<?> svclz = XposedHelpers.findClass("android.view.SurfaceView", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(svclz, "setSecure", Boolean.TYPE, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals(true)) {
                    param.args[0] = false;
                    Log.d(TAG, "SurfaceView.setSecure:" + app);
                }
            }
        });
        //Check xposed status.
        if (app.equals(X.class.getPackage().getName())) {
            XposedHelpers.findAndHookMethod("ryuunoakaihitomi.xposed.screenshothookbox.ConfigActivity", lpparam.classLoader, "isXposedRunning", XC_MethodReplacement.returnConstant(true));
        }
        //SystemUI is the operator.
        if (app.equals("com.android.systemui")) {
            //Mute during getting screenshot.
            XposedHelpers.findAndHookMethod(MediaActionSound.class, "play", int.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    //The logcat shows that this is the earliest record.
                    XposedBridge.log("A screenshot was taken.");
                    MediaActionSound mas = (MediaActionSound) param.thisObject;
                    mas.release();
                    Log.d(TAG, "MediaActionSound.release");
                    return null;
                }
            });
            //Reformat the fileName.(Entrance)Because there's only one place is "yyyyMMdd-HHmmss" in the whole aosp-mirror/platform_frameworks_base.
            XposedBridge.hookAllConstructors(SimpleDateFormat.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args[0].equals("yyyyMMdd-HHmmss")) {
                        _switch = true;
                        Log.d(TAG, "SimpleDateFormat(\"yyyyMMdd-HHmmss\")");
                    }
                }
            });
            //Reformat the fileName.(Execute)(Screenshot_time(Accurate to milliseconds)_packagename.png)
            //It will call several times in　SystemUI.
            XposedHelpers.findAndHookMethod(String.class, "format", String.class, Object[].class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (_switch) {
                        _switch = false;
                        //Change the file type to WEBP or JPG.(filename)
                        String fileExtension;
                        if (BoolConfigIO.get(ConfigActivity.JPG))
                            fileExtension = "jpg";
                        else
                            fileExtension = "webp";
                        param.setResult(String.format("Screenshot_%s_%s." + fileExtension,
                                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS").format(new Date(System.currentTimeMillis()))
                                , getShotObject()));
                        Log.d(TAG, "String.format");
                    }
                }
            });
            //Change the file type to WEBP or JPG.(type)
            XposedHelpers.findAndHookMethod(Bitmap.class, "compress", Bitmap.CompressFormat.class, int.class, OutputStream.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!BoolConfigIO.get(ConfigActivity.JPG))
                        param.args[0] = Bitmap.CompressFormat.WEBP;
                    else
                        param.args[0] = Bitmap.CompressFormat.JPEG;
                    Log.d(TAG, "Bitmap.compress");
                }
            });

            //Add EXIF in JPEG image.
            XposedHelpers.findAndHookMethod(ContentValues.class, "put", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args[0] == MediaStore.Images.ImageColumns.DATA) {
                        Log.d(TAG, "MediaStore.Images.ImageColumns.DATA");
                        if (BoolConfigIO.get(ConfigActivity.JPG) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Log.d(TAG, "mImageFilePath:" + param.args[1]);
                            final String mImageFilePath = (String) param.args[1];
                            //Add EXIF
                            try {
                                Log.d(TAG, "ExifInterface.setAttribute");
                                ExifInterface exifInterface = new ExifInterface(mImageFilePath);
                                exifInterface.setAttribute(ExifInterface.TAG_DATETIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
                                //Debug:Width & Length
                                WindowManager windowManager = (WindowManager) AndroidAppHelper.currentApplication().getSystemService(Context.WINDOW_SERVICE);
                                Display display = windowManager.getDefaultDisplay();
                                Point point = new Point();
                                display.getRealSize(point);
                                int w = point.x;
                                int l = point.y;
                                Log.d(TAG, "getRealSize:" + w + "*" + l);
                                exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(w));
                                exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(l));
                                //Device Info
                                exifInterface.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER);
                                exifInterface.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);
                                exifInterface.setAttribute(ExifInterface.TAG_SOFTWARE, Build.DISPLAY);
                                exifInterface.setAttribute(ExifInterface.TAG_MAKER_NOTE, "getShotObject:" + getShotObject());
                                //Others
                                exifInterface.setAttribute(ExifInterface.TAG_ARTIST, "SystemUI");
                                exifInterface.setAttribute(ExifInterface.TAG_COPYRIGHT, "EXIF:Screenshot Hookbox");
                                exifInterface.saveAttributes();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            return;
        }
        //android get the key event.As a messager.
        if (app.equals("android")) {
            //PhoneWindowManager:(at) hide
            Class<?> pwmclz = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager", lpparam.classLoader);

            //Set the delay waiting for screenshot to 0.
            XposedHelpers.findAndHookMethod(pwmclz, "getScreenshotChordLongPressDelay", XC_MethodReplacement.returnConstant(0L));
        }
    }

    //get the object on the top of the screen.(Top Activity's PackageName)
    private String getShotObject() {
        String ret = getLollipopRecentTask();
        String out;
        if (ret.isEmpty())
            out = "others";
        else
            out = ret;
        Log.d(TAG, "getShotObject:" + out);
        return out;
    }

    //Get foreground package name,it can work on lollipop or higher without PACKAGE_USAGE_STATS.(reflection.But it could't get the RecentsActivity from SystemUI.System permission needed)
    @SuppressWarnings("JavaReflectionMemberAccess")
    private String getLollipopRecentTask() {
        /** @hide Process is hosting the current top activities.  Note that this covers
         * all activities that are visible to the user. */
        final int PROCESS_STATE_TOP = 2;
        try {
            Field processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) AndroidAppHelper.currentApplication().getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (process.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.importanceReasonCode == 0) {
                    int state = processStateField.getInt(process);
                    if (state == PROCESS_STATE_TOP) {
                        String[] packname = process.pkgList;
                        return packname[0];
                    }
                }
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        return "";
    }
}
