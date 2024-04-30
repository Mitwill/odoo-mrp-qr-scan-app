package com.mitwill.mrp.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mitwill.mrp.R;
import com.mitwill.mrp.models.WorkCenter;

import java.util.List;
import java.util.Map;

public class WorkCenterAdapter extends RecyclerView.Adapter<WorkCenterAdapter.MyViewHolder> {

    private Context context;
    private List<Map<String, String>> workCenterMapList;

    private ItemClickListener itemClickListener;

    // variable to track event time
    private long mLastClickTime = 0;

    public WorkCenterAdapter(Context c, List<Map<String, String>> workCenterList, ItemClickListener itemClickListener) {
        this.context = c;
        this.workCenterMapList = workCenterList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View listItem = layoutInflater.inflate(R.layout.recyclerview_row, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.textView.setText(workCenterMapList.get(position).get(WorkCenter.WORK_CENTER_NAME));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                itemClickListener.onItemClicked(holder, workCenterMapList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workCenterMapList.size();
    }

    public interface ItemClickListener {

        void onItemClicked(MyViewHolder vh, List<Map<String, String>> item, int pos);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.tvWorkCenter);
        }
    }
}
