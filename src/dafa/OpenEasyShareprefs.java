package dafa;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by lixi on 2017/4/9.
 */
public class OpenEasyShareprefs extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        SettingDialog settingDialog = new SettingDialog();
        settingDialog.setVisible(true);
    }
}
