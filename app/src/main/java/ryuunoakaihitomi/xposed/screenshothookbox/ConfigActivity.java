package ryuunoakaihitomi.xposed.screenshothookbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by ZQY on 2017/11/11.
 * Config UI
 */

public class ConfigActivity extends Activity {

    //Menu
    static final String JPG = "isJPG";
    static final String CAP = "isCAP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        getRightEmoji(CAP) + getString(R.string.use_screencap)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                BoolConfigIO.set(JPG, !BoolConfigIO.get(JPG));
                                break;
                            case 1:
                                BoolConfigIO.set(CAP, !BoolConfigIO.get(CAP));
                                break;
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
