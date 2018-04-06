package ryuunoakaihitomi.xposed.screenshothookbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by ZQY on 2017/11/11.
 * Config UI
 */

public class ConfigActivity extends Activity {

    //Menu
    static final String JPG = "isJPG";
    static final String CAP = "isCAP";
    //The Man Who Changed China
    static final String DEL = "XuYiMiao";

    //Hook entry
    static boolean isXposedRunning() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Clear status bar's black.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        BoolConfigIO.init(this);
        if (!SU.isRoot()) {
            Toast.makeText(getApplicationContext(), getString(R.string.root), Toast.LENGTH_LONG).show();
            finish();
        }
        new AlertDialog.Builder(ConfigActivity.this, android.R.style.Theme_Material_Dialog)
                .setTitle(getString(R.string.config_dialog_title))
                //Icon:like a notebook.
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setItems(new String[]{
                        getRightEmoji(JPG) + getString(R.string.save_jpg),
                        getRightEmoji(CAP) + getString(R.string.use_screencap),
                        getRightEmoji(DEL) + getString(R.string.delay_1s),
                        getString(R.string.donate)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                BoolConfigIO.set(JPG, !BoolConfigIO.get(JPG));
                                break;
                            case 1:
                                if (isXposedRunning())
                                    Toast.makeText(getApplicationContext(), getString(R.string.xposed), Toast.LENGTH_LONG).show();
                                else
                                    BoolConfigIO.set(CAP, !BoolConfigIO.get(CAP));
                                break;
                            case 2:
                                BoolConfigIO.set(DEL, !BoolConfigIO.get(DEL));
                                break;
                            case 3:
                                Toast.makeText(getApplicationContext(), getString(R.string.donate_notice), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://ryuunoakaihitomi.info/donate/")));
                        }
                        finish();
                    }
                }).show();
    }

    //As CheckBox.
    String getRightEmoji(String key) {
        return BoolConfigIO.get(key) ? "✔" : "";
    }
}
