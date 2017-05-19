package dafa;

import dafa.ShellUtils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class SettingDialog extends JDialog {
    private JPanel contentPane;
    private JScrollPane scroll_pane;
    private JList file_list;
    private JTextArea perf_content;
    private JComboBox package_cobBox;
    private JLabel status_lb;
    private JLabel title_lb;
    private JLabel link_lb;
    private JButton buttonOK;
    private ShellUtils shellUtils;
    private String pName = "me.ele.napos";
    private ArrayList<String> currentList = new ArrayList<>();
    private int lastClick = -1;

    public SettingDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        status_lb.setText("loading packages,please wait");
        shellUtils = new ShellUtils(new ShellUtils.AdbCallback() {
            @Override
            public void OnSuccess() {
            }

            @Override
            public void OnFail() {

            }

            @Override
            public void OnFinish() {
                shellUtils.initDevice();
                getData();
            }

            @Override
            public void OnRunning(String s) {

            }
        });
        setSize(700, 500);

        scroll_pane.setSize(200, 500);

        Insets insets = new Insets(5, 5, 5, 5);

        perf_content.setMargin(insets);

        setLocationRelativeTo(null);

        link_lb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                URI l = URI.create("https://github.com/w22ee/EasySharedprefs");
                try {
                    Desktop.getDesktop().browse(l);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }


    private void getData() {
        getPackageNames();
    }

    private void getPackageNames() {
        shellUtils.getPackage(new ShellUtils.CommondCallback() {
            ArrayList<String> outPutlist = new ArrayList<>();

            @Override
            public void onSuccess(ArrayList<String> output) {
                DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
                for (String packageName : output) {
                    defaultComboBoxModel.addElement(packageName);
                }
                status_lb.setText("select your debug package name");
                package_cobBox.setModel(defaultComboBoxModel);
                package_cobBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (package_cobBox.getSelectedIndex() != -1) {
                            String p = output.get(package_cobBox.getSelectedIndex());
                            if (p != null) {
                                pName = p;
                                getFolderList();
                            }
                        }
                    }
                });
            }

            @Override
            public void onFail() {
                status_lb.setText("device not found");
            }

            @Override
            public void onFinish() {
                this.onSuccess(outPutlist);
            }

            @Override
            public void onRunning(String s) {
                if (s != null && !s.isEmpty()) {
                    String[] ps = s.split(":");
                    outPutlist.add(ps[1]);
                }
            }
        });
    }

    private void getFolderList() {
        shellUtils.openDataFolder(pName, new ShellUtils.CommondCallback() {

            private boolean fail = false;

            ArrayList<String> xmlList = new ArrayList<>();

            @Override
            public void onSuccess(ArrayList<String> output) {
                if (output != null && !output.isEmpty()) {
                    currentList = output;
                    setFileList();
                }
            }

            @Override
            public void onFail() {

            }

            @Override
            public void onFinish() {
                if (!fail) {
                    this.onSuccess(xmlList);
                }
            }

            @Override
            public void onRunning(String s) {
                if (s != null) {
                    if (s.endsWith("is unknown")) {
                        this.onFail();
                    } else if (s.endsWith("is not debuggable")) {
                        this.onFail();
                    } else {
                        xmlList.add(s);
                    }
                }
            }

        });
    }

    private void setFileList() {
        if (currentList != null) {
            file_list.removeAll();
            perf_content.setText("");
            lastClick = -1;
            ArrayList<String> showList = new ArrayList<>();
            for (int i = 0; i < currentList.size(); i++) {
                String curString = currentList.get(i);
                if (!curString.isEmpty() && curString.contains("shared_prefs/")) {
                    showList.add(curString.substring(curString.lastIndexOf('/')+1));
                }
            }
            file_list.setListData(showList.toArray(new String[showList.size()]));
            file_list.addMouseListener(new MouseInputAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    int i = file_list.getSelectedIndex();

                    if (i == lastClick) {
                        return;
                    }

                    lastClick = i;

                    String fileName = currentList.get(i);
                    perf_content.setText("");

                    shellUtils.watchPerf(pName, fileName, new ShellUtils.CommondCallback() {
                        @Override
                        public void onSuccess(ArrayList<String> output) {

                        }

                        @Override
                        public void onFail() {

                        }

                        @Override
                        public void onFinish() {

                        }

                        @Override
                        public void onRunning(String s) {
                            if (s != null) {
                                perf_content.append(s + "\r\n");
                            }
                        }
                    });
                }
            });
        }
    }


}
