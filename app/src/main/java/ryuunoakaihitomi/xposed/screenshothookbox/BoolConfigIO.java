package ryuunoakaihitomi.xposed.screenshothookbox;

import java.io.File;
import java.io.IOException;

/**
 * Created by ZQY on 2017/11/11.
 * Boolean Value I/O interface For Xposed.
 */

class BoolConfigIO {
    //Use fixed paths to avoid using context and speeding up execution.In xposed,Environment.getExternalStorageDirectory() will return null.
    private static String path = "/sdcard/Android/data/ryuunoakaihitomi.xposed.screenshothookbox/files/";
    //if !exists
    static {
        SU.exec("mkdir -p " + path);
    }

    //setter and getter.
    static void set(String key, boolean value) {
        File file = new File(path + key);
        if (value)
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            file.delete();
    }

    static boolean get(String key) {
        return new File(path + key).exists();
    }
}
