package com.sdwfqin.cbt.receiver;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.sdwfqin.cbt.utils.CbtLogs;
import com.sdwfqin.cbt.callback.BaseConfigCallback;
import com.sdwfqin.cbt.model.DeviceModel;
import com.sdwfqin.cbt.utils.CbtConstant;

/**
 * 描述：监听蓝牙广播
 *
 * @author zhangqin
 * @date 2018/5/30
 */
public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "BroadcastReceiver";
    private BaseConfigCallback mCallback;
    private BroadcastReceiver mReceiver;

    public BluetoothReceiver(Context context, BaseConfigCallback callback) {
        mCallback = callback;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙开关状态
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙搜索结束

        filter.addAction(BluetoothDevice.ACTION_FOUND);//蓝牙发现新设备(未配对)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对状态改变
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//设备建立连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//设备断开连接

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); //BluetoothHeadset连接状态
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp连接状态
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;
        switch (action) {
            /**
             * 蓝牙开关状态
             * int STATE_OFF = 10; //蓝牙关闭
             * int STATE_ON = 12; //蓝牙打开
             * int STATE_TURNING_OFF = 13; //蓝牙正在关闭
             * int STATE_TURNING_ON = 11; //蓝牙正在打开
             */
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                mCallback.onStateSwitch(state);
                break;
            /**
             * 蓝牙开始搜索
             */
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                Log.e(TAG,"蓝牙开始搜索");
                break;
            /**
             * 蓝牙搜索结束
             */
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                Log.e(TAG,"蓝牙扫描结束");
                mCallback.onScanStop();
                break;
            /**
             * 发现新设备
             */
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                DeviceModel deviceModel = new DeviceModel(dev.getName(), dev.getAddress());
                mCallback.onFindDevice(deviceModel);
                Log.e(TAG,"蓝牙发现新设备"+dev.fetchUuidsWithSdp());
                break;
            /**
             * 设备配对状态改变
             */
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                CbtLogs.i("STATE: " + intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0));
                CbtLogs.i("BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                if (dev.getBondState() == BluetoothDevice.BOND_BONDING) {
                    mCallback.onConn(CbtConstant.PAIR);
                } else if (dev.getBondState() == BluetoothDevice.BOND_BONDED) {
                    mCallback.onConn(CbtConstant.PAIR_SUCCESS);
                } else if (dev.getBondState() == BluetoothDevice.BOND_NONE) {
                    mCallback.onConn(CbtConstant.PAIR_UNSUCCESSFUL);
                }
                break;
            /**
             * 设备建立连接
             */
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                CbtLogs.i("BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;

            /**
             * 设备断开连接
             */
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                CbtLogs.i("BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;

            /**
             * 本地蓝牙适配器
             * BluetoothAdapter连接状态
             */
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                CbtLogs.i("STATE: " + intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0));
                CbtLogs.i("BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;

            /**
             * 提供用于手机的蓝牙耳机支持
             * BluetoothHeadset连接状态
             */
            case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                CbtLogs.i("STATE: " + intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0));
                CbtLogs.i("BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;
            /**
             * 定义高质量音频可以从一个设备通过蓝牙连接传输到另一个设备
             * BluetoothA2dp连接状态
             */
            case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                CbtLogs.i("STATE: " + intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0));
                CbtLogs.i("BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;
        }
    }
}