package com.example.renren.bletransferdemo.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.renren.bletransferdemo.R;

import java.util.List;

public class FoundDeviceAdapter extends RecyclerView.Adapter<FoundDeviceAdapter.ViewHolder> {
    private List<BluetoothDevice> deviceList;
    private OnItemClickLisenter onItemClickLisenter;

    public void setOnItemClickLisenter(OnItemClickLisenter onItemClickLisenter) {
        this.onItemClickLisenter = onItemClickLisenter;
    }

    public FoundDeviceAdapter(List<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    public void addDevice(BluetoothDevice device) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BluetoothDevice bluetoothDevice = deviceList.get(position);
        holder.tvName.setText(bluetoothDevice.getName());
        holder.tvAddress.setText(bluetoothDevice.getAddress());
        if (onItemClickLisenter != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickLisenter.onItemClick(v, position, bluetoothDevice);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickLisenter.onItemLongClick(v, position, bluetoothDevice);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvAddress = itemView.findViewById(R.id.tv_item_address);
        }
    }
}
