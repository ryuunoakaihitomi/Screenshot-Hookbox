package ryuunoakaihitomi.xposed.screenshothookbox;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.service.quicksettings.TileService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ZQY on 2017/10/31.
 * Quick Setting Tile Service
 */

@TargetApi(Build.VERSION_CODES.N)
public class QSTileSer extends TileService {
    //Root permission request while adding the tile.
    @Override
    public void onCreate() {
        SU.exec("echo init_service");
    }

    @Override
    public void onClick() {
        //collapse StatusBar.
        SU.exec("service call statusbar 2");
        //+1s
        int wait = 0;
        if (BoolConfigIO.get(ConfigActivity.DEL))
            wait = 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //shot
                if (BoolConfigIO.get(ConfigActivity.CAP))
                    //Use screencap in root environment.
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            String path = new File(
                                    //Copy from AOSP src.
                                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots"),
                                    String.format("Screenshot_%s.jpg",
                                            new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(System.currentTimeMillis())))).getAbsolutePath();
                            SU.exec("screencap -j " + path);
                            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
                        }
                    }, 1000);
                else
                /**
                 * Key code constant: System Request / Print Screen key.
                 */
                    //public static final int KEYCODE_SYSRQ = 120;
                    SU.exec("input keyevent 120");
            }
        },wait);
    }
}
