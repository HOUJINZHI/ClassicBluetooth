package com.sdwfqin.cbt;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.sdwfqin.cbt.callback.ConnectDeviceCallBack;
import com.sdwfqin.cbt.callback.ScanCallback;
import com.sdwfqin.cbt.connect.BluetoothPair;
import com.sdwfqin.cbt.utils.CbtLogs;
import com.sdwfqin.cbt.callback.BaseConfigCallback;
import com.sdwfqin.cbt.callback.StateSwitchCallback;
import com.sdwfqin.cbt.model.DeviceModel;
import com.sdwfqin.cbt.receiver.BluetoothReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：Android经典蓝牙
 *
 * @author zhangqin
 * @date 2018/5/30
 */
public class CbtManager implements BaseConfigCallback {

    private static final String TAG = "BaseConfigCallback";
    private Application mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothReceiver mBluetoothReceiver;

    private StateSwitchCallback mStateSwitchCallback;
    private ScanCallback mScanCallback;

    private List<DeviceModel> mDeviceList = new ArrayList<>();
    private ConnectDeviceCallBack mConnCallBack;
    private BluetoothPair mBluetoothPair;
    private BluetoothDevice btDev;

    public static CbtManager getInstance() {
        return CbtManagerHolder.sBleManager;
    }

    @Override
    public void onStateSwitch(int state) {
        if (mStateSwitchCallback == null)
            return;
        if (state == BluetoothAdapter.STATE_ON) {
            mStateSwitchCallback.onStateChange(true);
        } else if (state == BluetoothAdapter.STATE_OFF) {
            mStateSwitchCallback.onStateChange(false);
        }
    }

    @Override
    public void onScanStop() {
        if (mScanCallback == null)
            return;
        mScanCallback.onScanStop(mDeviceList);
    }

    @Override
    public void onFindDevice(DeviceModel deviceModel) {
        if (mScanCallback == null)
            return;
        // 排除相同设备
        for (int i = 0; i < mDeviceList.size(); i++) {
            if (deviceModel.getAddress().equals(mDeviceList.get(i).getAddress())) {
                return;
            }
        }
        mScanCallback.onFindDevice(deviceModel);
        mDeviceList.add(deviceModel);
    }

    @Override
    public void onConn(int conn_type) {
        mConnCallBack.connectDevice(conn_type);

    }

    private static class CbtManagerHolder {
        private static final CbtManager sBleManager = new CbtManager();
    }

    /**
     * 初始化
     *
     * @param app
     * @return
     */
    public CbtManager init(Application app) {
        if (mContext == null && app != null) {
            mContext = app;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothReceiver = new BluetoothReceiver(mContext, this);
        }
        return this;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 是否打印日志
     * <p>
     * 默认打印
     *
     * @param isEnable
     * @return BleManager
     */
    public CbtManager enableLog(boolean isEnable) {
        CbtLogs.Config config = CbtLogs.getConfig();
        config.setLogSwitch(isEnable)
                .setConsoleSwitch(isEnable);
        return this;
    }

    /**
     * 开启蓝牙
     */
    public void enableBluetooth(StateSwitchCallback callback) {

        mStateSwitchCallback = callback;

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            } else {
                mStateSwitchCallback.onStateChange(true);
            }
        }
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetooth(StateSwitchCallback callback) {
        mStateSwitchCallback = callback;
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        }
    }

    /**
     * 开始搜索
     * <p>
     * TODO: 自行判断是否开启蓝牙
     */
    public void scan(ScanCallback scanCallback) {
        mScanCallback = scanCallback;
        mDeviceList.clear();
        if (mBluetoothAdapter != null) {
            mScanCallback.onScanStart(mBluetoothAdapter.startDiscovery());
        }

    }

    /**
     * 设备配对
     * @param callBack
     */
    public void pair(ConnectDeviceCallBack callBack){
        mConnCallBack = callBack;
        DeviceModel deviceModel = mConnCallBack.getConnectDevice();
        if (deviceModel!=null){
            btDev = mBluetoothAdapter.getRemoteDevice(deviceModel.getAddress());
            Log.e(TAG, btDev.getName()+"---->"+ btDev.getAddress());
            //配对蓝牙
            mBluetoothPair = new BluetoothPair(mBluetoothAdapter, btDev);
            mBluetoothPair.start();

        }
    }
    /**
     * 关闭服务
     */
    public void onDestroy() {
        if (mBluetoothPair!=null) {
            mBluetoothPair.cancel();
        }
        mContext.unregisterReceiver(mBluetoothReceiver);
    }
}
