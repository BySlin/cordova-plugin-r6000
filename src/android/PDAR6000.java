package com.byslin.cordova.plugin;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cn.pda.serialport.Tools;

/**
 * This class echoes a string called from JavaScript.
 */
public class PDAR6000 extends CordovaPlugin {
    private static final String LOG_TAG = "PDAR6000";
    private UhfReader manager;
    private boolean runFlag = true;
    private boolean startFlag = false;
    private int power = 30;//rate of work
    private int area = 2;
    private CallbackContext callbackContext;
    private ArrayList<com.byslin.cordova.plugin.EPC> listEPC = new ArrayList<>();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Thread thread = new InventoryThread();
        thread.start();
        // init sound pool
        com.byslin.cordova.plugin.Util.initSoundPool(cordova.getContext());
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        manager = UhfReader.getInstance();
        if (manager == null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "连接RFID读卡器串口失败");
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manager.setOutputPower(power);
        manager.setWorkArea(area);
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        startFlag = false;
        manager.close();
    }

    @Override
    public void onDestroy() {
        startFlag = false;
        runFlag = false;
        if (manager != null) {
            manager.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "start":
                Log.d(LOG_TAG, "start inventory");
                startFlag = true;
                return true;
            case "stop":
                Log.d(LOG_TAG, "stop inventory");
                startFlag = false;
                return true;
            case "register":
                this.callbackContext = callbackContext;
                return true;
            case "setOutputPower":
                int power = args.getInt(0);
                if (manager != null) {
                    manager.setOutputPower(power);
                }
                return true;
            case "setWorkArea":
                int area = args.getInt(0);
                if (manager != null) {
                    manager.setWorkArea(area);
                }
            default:
                Log.d(LOG_TAG, "unknown action:" + action);
                return false;
        }
    }

    /**
     * Inventory EPC Thread
     */
    class InventoryThread extends Thread {
        private List<TagModel> tagList;

        @Override
        public void run() {
            super.run();
            while (runFlag) {
                if (startFlag) {
                    tagList = manager.inventoryRealTime(); //实时盘存
                    if (tagList != null && !tagList.isEmpty()) {
                        //播放提示音
                        com.byslin.cordova.plugin.Util.play(1, 0);
                        for (TagModel tag : tagList) {
                            if (tag == null) {
                                String epcStr = "";
//								String epcStr = new String(epc);
                                addToList(epcStr, (byte) -1);
                            } else {
                                String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);
//								String epcStr = new String(epc);
                                byte rssi = tag.getmRssi();
                                addToList(epcStr, rssi);
                            }

                        }
                    }
                    tagList = null;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // EPC add to LISTVIEW
    private void addToList(final String epc, final byte rssi) {
        // The epc for the first time
        if (listEPC.isEmpty()) {
            com.byslin.cordova.plugin.EPC epcTag = new com.byslin.cordova.plugin.EPC();
            epcTag.setEpc(epc);
            epcTag.setCount(1);
            epcTag.setRssi(rssi);
            listEPC.add(epcTag);
        } else {
            for (int i = 0; i < listEPC.size(); i++) {
                com.byslin.cordova.plugin.EPC mEPC = listEPC.get(i);
                // list contain this epc
                if (epc.equals(mEPC.getEpc())) {
                    mEPC.setCount(mEPC.getCount() + 1);
                    mEPC.setRssi(rssi);
                    listEPC.set(i, mEPC);
                    break;
                } else if (i == (listEPC.size() - 1)) {
                    // list doesn't contain this epc
                    com.byslin.cordova.plugin.EPC newEPC = new com.byslin.cordova.plugin.EPC();
                    newEPC.setEpc(epc);
                    newEPC.setCount(1);
                    newEPC.setRssi(rssi);
                    listEPC.add(newEPC);
                }
            }
        }
        // play sound
        com.byslin.cordova.plugin.Util.play(1, 0);
        PluginResult pluginResult = new JSONPluginResult(PluginResult.Status.OK, JSON.toJSONString(listEPC));
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    class JSONPluginResult extends PluginResult {

        public JSONPluginResult(Status status, String message) {
            super(status, message);
        }

        @Override
        public int getMessageType() {
            return 2;
        }

        @Override
        public String getMessage() {
            return this.getStrMessage();
        }
    }
}
