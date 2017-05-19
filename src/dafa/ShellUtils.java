package dafa;

import com.android.ddmlib.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by lixi on 16/7/6.
 */
public class ShellUtils {

    private IDevice iDevice;
    private IDevice[] deviceList;
    public AndroidDebugBridge adb = null;
    private String pcIp;

    public ShellUtils(AdbCallback callback) {


        AndroidDebugBridge.initIfNeeded(false);

        adb = AndroidDebugBridge.getBridge();
        if (adb == null) {
            adb = AndroidDebugBridge.createBridge();
        }

        DdmPreferences.setTimeOut(10000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                waitDeviceList(adb, callback);
            }
        }).start();


    }

    public void initDevice() {
        if (adb != null) {
            deviceList = adb.getDevices();
            if (deviceList != null && deviceList.length > 0) {
                iDevice = deviceList[0];
            } else {

            }
        }
    }


    private void waitDeviceList(AndroidDebugBridge bridge, AdbCallback adbCallback) {
        int count = 0;
        while (bridge.hasInitialDeviceList() == false) {
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException e) {
            }
            if (count > 100) {
                adbCallback.OnFail();
                break;
            }
        }
        adbCallback.OnFinish();
    }

    public void runCommand(String command, CommondCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                GenericReceiver genericReceiver = new GenericReceiver();
                try {
                    if (iDevice == null) {
                        initDevice();
                    }
                    if (iDevice == null){
                        if (callback!=null){
                            callback.onFail();
                        }
                        return;
                    }
                    iDevice.executeShellCommand(command, genericReceiver, 15l, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (AdbCommandRejectedException e) {
                    e.printStackTrace();
                } catch (ShellCommandUnresponsiveException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } finally {

                }

                if (callback != null) {
                    if (genericReceiver != null && genericReceiver.getAdbOutputLines() != null && genericReceiver.getAdbOutputLines().size() > 0) {
                        for (String s : genericReceiver.getAdbOutputLines()) {
                            callback.onRunning(s);
                        }
                    }
                    callback.onFinish();
                }
            }
        }).start();


    }


    public void openDataFolder(String packageName, CommondCallback callback) {
        runCommand("run-as " + packageName + " find shared_prefs/ -type f -name   \"*.xml\"   -print", callback);
    }

    public void watchPerf(String packageName, String perfName, CommondCallback callback) {
        runCommand("run-as " + packageName + " cat " + perfName, callback);
    }

    public void getPackage(CommondCallback callback){
        runCommand("pm list package -3",callback);
    }


    interface AdbCallback {
        void OnSuccess();

        void OnFail();

        void OnFinish();

        void OnRunning(String s);
    }

    interface CommondCallback {
        void onSuccess(ArrayList<String> output);

        void onFail();

        void onFinish();

        void onRunning(String s);
    }
}
