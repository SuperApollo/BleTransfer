package com.example.renren.bletransferdemo.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.renren.bletransferdemo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.DEVICE_NAME;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_DEVICE_NAME;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_READ_DONE;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_READ_START;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_STATE_CHANGE;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_TOAST;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_WRITE_DONE;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_WRITE_PROGRESS;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.MESSAGE_WRITE_START;
import static com.example.renren.bletransferdemo.bluetooth.BluetoothService.TOAST;

public class BluetoothDeviceActivity extends Activity implements View.OnClickListener, OnItemClickLisenter {
    private static final String TAG = BluetoothDeviceActivity.class.getSimpleName();

    private RecyclerView mRecyclerPaired;
    private RecyclerView mRecyclerFound;
    private FloatingActionButton mBtnSearch;
    private BluetoothAdapter mBleAdapter;
    private final int REQUEST_ENABLE_BT = 0x1000;
    private final int PERMISSION_REQUEST_CODE = 0x2000;
    private List<BluetoothDevice> pairedList = new ArrayList<>();
    private List<BluetoothDevice> foundList = new ArrayList<>();
    private PairedDeviceAdapter pairedDeviceAdapter;
    private FoundDeviceAdapter foundDeviceAdapter;
    private ProgressBar mProgressBar;
    private MyHandler mMyHandler = new MyHandler();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(BluetoothDevice.ACTION_FOUND, action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundDeviceAdapter.addDevice(bluetoothDevice);
                Log.i("apollo", "找到设备：" + bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress());
            }
        }
    };
    private BluetoothService mBleService;
    private String mConnectedDeviceName;
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.i("apollo", "connected to " + mConnectedDeviceName);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.i("apollo", "connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.i("apollo", "not connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE_START:
                    Toast.makeText(BluetoothDeviceActivity.this, "开始发送文件...", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_WRITE_DONE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    Log.i("apollo", "write: " + writeBuf.length);
                    Toast.makeText(BluetoothDeviceActivity.this, "成功发送文件！", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ_START:
                    Toast.makeText(BluetoothDeviceActivity.this, "开始接收收据...", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ_DONE:
                    int length = (int) msg.obj;
                    Log.i("apollo", "成功收到数据: " + length);

                    Toast.makeText(BluetoothDeviceActivity.this, "成功接收文件！", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "成功连接到 "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_WRITE_PROGRESS:
                    try {
                        String progressStr = (String) msg.obj;
                        String[] split = progressStr.split("/");
                        String progress = split[0];
                        String total = split[1];

                        mProgressBarTrans.setVisibility(View.VISIBLE);
                        mProgressBarTrans.setMax(Integer.parseInt(total));
                        mProgressBarTrans.setProgress(Integer.parseInt(progress));

                        if (mProgressBarTrans.getProgress() / mProgressBarTrans.getMax() == 1) {
                            mProgressBarTrans.setVisibility(View.GONE);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    private FloatingActionButton mBtnSend;
    private ProgressBar mProgressBarTrans;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device);

        mRecyclerPaired = findViewById(R.id.recycler_paired);
        mRecyclerFound = findViewById(R.id.recycler_found);
        mBtnSearch = findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(this);
        mBtnSend = findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);
        mProgressBar = findViewById(R.id.progress);
        mProgressBarTrans = findViewById(R.id.progress_trans);

        LinearLayoutManager pairedManager = new LinearLayoutManager(this);
        LinearLayoutManager foundManager = new LinearLayoutManager(this);
        mRecyclerPaired.setLayoutManager(pairedManager);
        mRecyclerFound.setLayoutManager(foundManager);
        //添加Android自带的分割线
        mRecyclerPaired.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerFound.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        pairedDeviceAdapter = new PairedDeviceAdapter(pairedList);
        foundDeviceAdapter = new FoundDeviceAdapter(foundList);
        pairedDeviceAdapter.setOnItemClickLisenter(this);
        foundDeviceAdapter.setOnItemClickLisenter(this);
        mRecyclerPaired.setAdapter(pairedDeviceAdapter);
        mRecyclerFound.setAdapter(foundDeviceAdapter);

        initBroadcustReceiver();
        initBlueTooth();

        scanBle();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBleAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mBleService == null) {
                mBleService = new BluetoothService(BluetoothDeviceActivity.this, mHandler);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBleService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBleService.getState() == mBleService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBleService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        cancelScanBle();

        if (mBleService != null) {
            mBleService.stop();
        }
    }


    /**
     * 初始化扫描蓝牙设备结果的接收者
     */
    private void initBroadcustReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    /**
     * 开始扫描蓝牙设备
     */
    private void scanBle() {
        if (mBleAdapter.isDiscovering()) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        //获取已经配对过的设备
        Set<BluetoothDevice> bondedDevices = mBleAdapter.getBondedDevices();
        if (bondedDevices != null && bondedDevices.size() > 0) {
            pairedList.clear();
            pairedList.addAll(bondedDevices);
            pairedDeviceAdapter.notifyDataSetChanged();
        }

        mMyHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelScanBle();
            }
        }, 10000);
        boolean startDicovery = mBleAdapter.startDiscovery();//开始异步搜索
        Log.i("apollo", "startDiscovery: " + startDicovery);

    }

    /**
     * 停止扫描
     */
    private void cancelScanBle() {
        mProgressBar.setVisibility(View.GONE);
        if (mBleAdapter != null && mBleAdapter.isDiscovering()) {
            boolean cancelDiscovery = mBleAdapter.cancelDiscovery();
            Log.i("apollo", "cancelDiscovery: " + cancelDiscovery);
        }
    }

    /**
     * 设置本地蓝牙可见
     *
     * @param timeMillis
     */
    private void setDiscoverable(int timeMillis) {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeMillis > 0 ? timeMillis : 300);
        startActivity(discoverableIntent);
    }

    /**
     * 初始化蓝牙相关
     */
    private void initBlueTooth() {
        //6.0以上动态判断权限
        checkPermission(new String[]{Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });


        //初始化蓝牙相关
        mBleAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBleAdapter == null) {
            Log.e("apollo", "not support bluetooth=========");
            return;
        }

    }

    /**
     * 动态权限检查
     */
    private void checkPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                            permissions[i]
                    }, PERMISSION_REQUEST_CODE);
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                scanBle();
                break;
            case R.id.btn_send:
                sendFile();
                break;
        }
    }

    /**
     * 使用蓝牙发送文件
     */
    private void sendFile() {
        mBleService.write(FileUtils.getFilePath("TestBleTrans.csv"));
//        mBleService.write(FileUtils.getFilePath("TestBle.mp4"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            Toast.makeText(this, "蓝牙开启成功", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(View view, int position, BluetoothDevice bluetoothDevice) {
        connect(bluetoothDevice);
    }

    /**
     * 连接蓝牙
     *
     * @param bluetoothDevice
     */
    private void connect(BluetoothDevice bluetoothDevice) {
        Toast.makeText(this, "正在连接" + bluetoothDevice.getName() + "...", Toast.LENGTH_SHORT).show();
        // Get the device MAC address
        String address = bluetoothDevice.getAddress();
        // Get the BluetoothDevice object
        BluetoothDevice device = mBleAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBleService.connect(device, true);

    }

    @Override
    public void onItemLongClick(View view, int position, BluetoothDevice bluetoothDevice) {

    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("apollo", grantResults[0] + " 权限已获得");
                }
                break;
        }
    }

}
