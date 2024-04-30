package com.mitwill.mrp.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mitwill.mrp.R;
import com.mitwill.mrp.adapters.WorkCenterAdapter;
import com.mitwill.mrp.core.rpc.helper.OArguments;
import com.mitwill.mrp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.mrp.core.rpc.listeners.IOdooResponse;
import com.mitwill.mrp.core.rpc.listeners.OdooError;
import com.mitwill.mrp.datas.OConstants;
import com.mitwill.mrp.models.WorkCenter;
import com.mitwill.mrp.services.ServiceHandler;
import com.mitwill.mrp.utils.Utils;
import com.mitwill.mrp.views.QRScanActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mitwill.mrp.datas.OConstants.WS_GET_WORKCENTERS;
import static com.mitwill.mrp.utils.Utils.openErrorDialog;

public class WorkCenterFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static String TAG = WorkCenterFragment.class.getSimpleName();
    List<Map<String, String>> mapList;
    ServiceHandler callService;
    WorkCenter workCenter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.txt_no_data)
    TextView txt_no_data;


    WorkCenterAdapter workCenterAdapter;
    WorkCenterAdapter.ItemClickListener itemClickListener = new WorkCenterAdapter.ItemClickListener() {
        @Override
        public void onItemClicked(WorkCenterAdapter.MyViewHolder vh, List<Map<String, String>> item, int pos) {
            if (item.get(pos) != null) {
                Intent intent = new Intent(getActivity(), QRScanActivity.class);
                intent.putExtra(OConstants.KEY_WORK_CENTER_DATA, (Serializable) item.get(pos));
                startActivity(intent);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_work_center, null);
        ButterKnife.bind(this, rootView);
        callService = new ServiceHandler();
        workCenter = new WorkCenter();
        mapList = new ArrayList<>();
        setHasSwipeRefreshView();
        callService();
        workCenterAdapter = new WorkCenterAdapter(getActivity(), mapList, itemClickListener);
        recyclerView.setAdapter(workCenterAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }


    private void callService() {

        if (Utils.netConnect(getActivity())) {
            String strModelName = WorkCenter.MODEL_NAME;
            OArguments arguments = new OArguments();
            HashMap<String, Object> data = new HashMap<>();

            callService.callMethod(strModelName, WS_GET_WORKCENTERS, arguments, data, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult response) {
                    try {
                        hideRefreshingProgress();
                        populateWCDataList(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(OdooError error) {
                    Log.d(TAG, "onError: " + error);
                    if (mapList.size() <= 0) {
                        txt_no_data.setVisibility(View.VISIBLE);
                    }
                    hideRefreshingProgress();
                    openErrorDialog(getActivity(), error.getMessage(), error.getServerTrace(), true);
                }
            });
        }
    }

    private void populateWCDataList(OdooResult response) throws JSONException {
        mapList.clear();
        String str = response.getString("result");
        JSONArray jsonarray = new JSONArray(str);

        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonObject = jsonarray.getJSONObject(i);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(WorkCenter.WORK_CENTER_ID, jsonObject.getString(WorkCenter.WORK_CENTER_ID));
            String name = jsonObject.getString(WorkCenter.WORK_CENTER_NAME);
            String nameUpperCase = name.substring(0, 1).toUpperCase() + name.substring(1);
            map.put(WorkCenter.WORK_CENTER_NAME, nameUpperCase);
            mapList.add(map);
        }
        if (mapList.size() <= 0) {
            txt_no_data.setVisibility(View.GONE);
        }
        workCenterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        setSwipeRefreshing(true);
        callService();
    }

    // Swipe refresh view
    public void setHasSwipeRefreshView() {
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeResources(R.color.android_blue,
                R.color.android_green,
                R.color.android_orange_dark,
                R.color.android_red);
    }

    public void setSwipeRefreshing(boolean refreshing) {
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(refreshing);
    }

    public void hideRefreshingProgress() {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(false);
        }
    }
}