package ryuunoakaihitomi.xposed.screenshothookbox;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by ZQY on 2017/11/11.
 * Boolean Value I/O interface For Xposed.
 */

class BoolConfigIO {
    //The hardcode for xposed. If we can, we should not do this.
    private static String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/ryuunoakaihitomi.xposed.screenshothookbox/files";

    //setter and getter.
    static void set(String key, boolean value) {
        File file = new File(path + "/" + key);
        if (value)
            try {
                Log.v(X.TAG, key + ",set_create_result:" + file.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            Log.v(X.TAG, key + ",set_delete_result:" + file.delete());
    }

    static boolean get(String key) {
        Log.v(X.TAG, "get_path:" + path);
        return new File(path + "/" + key).exists();
    }

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    static void init(Context context) {
        path = context.getExternalFilesDir(null).getPath();
        new File(path).mkdirs();
    }
}
