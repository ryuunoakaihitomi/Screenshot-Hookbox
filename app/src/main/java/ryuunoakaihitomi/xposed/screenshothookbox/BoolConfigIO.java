package ryuunoakaihitomi.xposed.screenshothookbox;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by ZQY on 2017/11/11.
 * Boolean Value I/O interface For Xposed.
 */

class BoolConfigIO {
    
    private static String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/ryuunoakaihitomi.xposed.screenshothookbox/files/";

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
