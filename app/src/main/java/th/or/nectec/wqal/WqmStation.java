package th.or.nectec.wqal;

import android.util.Log;

import java.util.ArrayList;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by Mink on 11/14/2017.
 */

public class WqmStation {
    private static final WqmStation ourInstance = new WqmStation();
    private static final ArrayList<String> mChatLog = new ArrayList<String>();
    private OnWqmStationDataReceivedListener mWqmStationDataReceivedListener;
    private OnWqmStationCmdResponseListener mWqmStationCmdResponseListener;

    public static WqmStation getInstance() {
        return ourInstance;
    }

    static BluetoothSPP mBt;

    static float mPh;
    static float mDo;
    static float mEc;
    static float mTm;

    static float mVPh;
    static float mVDo;
    static float mVEc;
    static float mVTm;


    static ArrayList<String> mCmdList = new ArrayList<String>();
    static String mLastCommand = new String("");

    static final String CMD_GET_EC = "$ wqalarm get 0";
    static final String CMD_GET_DO = "$ wqalarm get 1";
    static final String CMD_GET_PH = "$ wqalarm get 2";
    static final String CMD_GET_TM = "$ wqalarm get 3";
    static final String CMD_GET_VEC = "$ atod get 3301";
    static final String CMD_GET_VDO = "$ atod get 2502";
    static final String CMD_GET_VPH = "$ atod get 2503";
    static final String CMD_GET_VTM = "$ atod get 3304";

    static final String RES_GET_WQALARM = "1000 wqalarm";
    static final String RES_GET_ADC = "1000 atod";


    private WqmStation() {
    }

    public interface OnWqmStationDataReceivedListener {
        public void onWqmStationDataReceived(byte[] data, String message);
    }

    public void setOnWqmStationDataReceivedListener(OnWqmStationDataReceivedListener listener) {
        mWqmStationDataReceivedListener = listener;
    }


    public interface OnWqmStationCmdResponseListener {
        public void onWqmStationCmdReponse();
    }

    public void setOnWqmStationCmdResponseListener(OnWqmStationCmdResponseListener listener) {
        mWqmStationCmdResponseListener = listener;
    }

    public void addCommand(String cmd) {

        if (mBt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            mCmdList.add(cmd);

            if (mCmdList.size() == 1) {
                send(mCmdList.get(0), true);
            }
        }
    }

    public void finishCommand() {

        if (mCmdList.size() > 0) {
            mCmdList.remove(0);
        }
        if (mCmdList.size() > 0) {
            send(mCmdList.get(0), true);
        } else {
            if (mWqmStationCmdResponseListener != null)
                mWqmStationCmdResponseListener.onWqmStationCmdReponse();
        }
    }

    public void send(String msg, boolean CRLF) {
        mLastCommand = msg;
        Log.i("send", "send " + mLastCommand);
        mBt.send(msg, CRLF);

    }


    public void setBt(BluetoothSPP bt) {

        mBt = bt;

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                //textRead.append(message + "\n");
                mWqmStationDataReceivedListener.onWqmStationDataReceived(data, message);
                onStationDataReceive(message);

                //String[] splited = message.split("\\s+");
            }
        });


    }


    public void onStationDataReceive(String msg) {

        Boolean valid_response = false;
        appendChat(0, msg);

        Log.i("data_recv", "lastcmd " + mLastCommand);
        Log.i("data_recv", "response " + msg);
        interpretResponse(msg);
    }


    public void appendChat(int direction, String msg) {

    }

    public void update() {

        addCommand(CMD_GET_DO);
        addCommand(CMD_GET_PH);
        addCommand(CMD_GET_EC);
        addCommand(CMD_GET_TM);
//        Log.d("Last Value", String.valueOf(mVEc)+" "+String.valueOf(mVDo)+" "
//                +String.valueOf(mVPh)+" "+String.valueOf(mVDo)+" "+String.valueOf(mVTm)+" ");
    }

    public void updateRaw() {
        addCommand(CMD_GET_VDO);
        addCommand(CMD_GET_VPH);
        addCommand(CMD_GET_VEC);
        addCommand(CMD_GET_VTM);
    }

    public void updateDo() {
        addCommand(CMD_GET_VDO);
        addCommand(CMD_GET_DO);
    }


    public void updatePh() {
        addCommand(CMD_GET_VPH);
        addCommand(CMD_GET_PH);
    }

    public void updateEc() {
        addCommand(CMD_GET_VEC);
        addCommand(CMD_GET_EC);
    }


    public float getPh() {
        return mPh;
    }

    public float getDo() {
        return mDo;
    }

    public float getEc() {
        return mEc;
    }

    public float getTemp() {
        return mTm;
    }

    public float getVDo() {
        return mVDo;
    }

    public float getVEc() {
        return mVEc;
    }

    public float getVPh() {
        return mVPh;
    }

    public float getVTm() {
        return mVTm;
    }


    private float getValueFromResponse(int index, String res) {
        String[] splited = res.split("\\s+");
        return Float.parseFloat(splited[index]);
    }

    private void interpretResponse(String msg) {
        boolean valid_response;
        valid_response = false;

        if (mLastCommand.contains(CMD_GET_EC)) {
            if (msg.contains("1000 wqalarm")) {
                mEc = getValueFromResponse(2, msg);
                valid_response = true;
            }
        } else if (mLastCommand.contains(CMD_GET_DO)) {
            if (msg.contains("1000 wqalarm")) {
                mDo = getValueFromResponse(2, msg);
                valid_response = true;
            }
        } else if (mLastCommand.contains(CMD_GET_PH)) {
            if (msg.contains("1000 wqalarm")) {
                mPh = getValueFromResponse(2, msg);
                valid_response = true;
            }
        } else if (mLastCommand.contains(CMD_GET_TM)) {
            if (msg.contains("1000 wqalarm")) {
                mTm = getValueFromResponse(2, msg);
                valid_response = true;
            }
        } else if (mLastCommand.contains(CMD_GET_VEC)) {
            if (msg.contains(RES_GET_ADC)) {
                mVEc = getValueFromResponse(2, msg);
                valid_response = true;
            }
        } else if (mLastCommand.contains(CMD_GET_VDO)) {
            if (msg.contains(RES_GET_ADC)) {
                mVDo = getValueFromResponse(2, msg);
                valid_response = true;
            }
        } else if (mLastCommand.contains(CMD_GET_VPH)) {
            if (msg.contains(RES_GET_ADC)) {
                mVPh = getValueFromResponse(2, msg);
                valid_response = true;
            }
        } else if (mLastCommand.contains(CMD_GET_VTM)) {
            if (msg.contains(RES_GET_ADC)) {
                mVTm = getValueFromResponse(2, msg);
                valid_response = true;
            }
        }

        if (valid_response) {
            finishCommand();
        }
    }
}