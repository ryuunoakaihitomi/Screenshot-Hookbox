package ryuunoakaihitomi.xposed.screenshothookbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Created by ZQY on 2017/10/31.
 * Superuser Shell Tools.
 */

public class SU extends BroadcastReceiver {

    private static OutputStream os;

    //Execute shell in one outputstream.
    static void exec(String cmd) {
        try {
            if (os == null) os = Runtime.getRuntime().exec("su").getOutputStream();
            os.write((cmd + "\n").getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(X.TAG, "SU.exec:" + cmd);
        }
    }

    //AutoRun.Initialize the outputstream.
    @Override
    public void onReceive(Context context, Intent intent) {
        //intent.getAction() may return null.
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
            exec("echo init");
    }

    //UtilTool:check root permission.
    static synchronized boolean isRoot() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                assert process != null;
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
