package com.example.renren.bletransferdemo.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.View;

public interface OnItemClickLisenter {
    void onItemClick(View view,int position,BluetoothDevice bluetoothDevice);

    void onItemLongClick(View view, int position, BluetoothDevice bluetoothDevice);
}
