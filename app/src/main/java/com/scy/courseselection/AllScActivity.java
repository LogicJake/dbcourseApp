package com.scy.courseselection;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.widget.SpringView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

import static com.scy.courseselection.MainActivity.api_url;

public class AllScActivity extends AppCompatActivity implements SpringView.OnFreshListener{
    private Context context = this;
    private SpringView sv;
    private int page = 1;
    private boolean is_done = true;
    private ListView mlistview;
    List<HashMap<String, Object>> mListData  = new ArrayList<HashMap<String, Object>>();;
    private ScAdapter scAdapter;
    private static final String TAG = "AllStuActivity";

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    System.out.println(mListData);

                    scAdapter = new ScAdapter(context, mListData);
                    mlistview.setAdapter(scAdapter);
                    setListViewHeightBasedOnChildren(mlistview);
                    scAdapter.notifyDataSetChanged();
                    sv.onFinishFreshAndLoad();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_sc);

        setTitle("学生名单");

        sv = (SpringView) findViewById(R.id.sv);//sv
        sv.setHeader(new DefaultHeader(this));
        sv.setFooter(new DefaultFooter(this));
        sv.setListener(this);

        mlistview = (ListView)findViewById(R.id.booklist) ;
        mlistview.setDividerHeight(5 );

        getData();
    }

    @Override
    public void onRefresh() {
        mListData.clear();
        page = 1;       //重新从第一页开始
        getData();
    }

    @Override
    public void onLoadmore() {
        if(!is_done) {
            page++;
            getData();
        }
        else {
            Toast.makeText(AllScActivity.this, "没有更多选课信息", Toast.LENGTH_SHORT).show();
            sv.onFinishFreshAndLoad();
        }
    }


    public static void setListViewHeightBasedOnChildren(ListView listView) {
        //获取listview的适配器
        ListAdapter listAdapter = listView.getAdapter(); //item的高度
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); //计算子项View 的宽高 //统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight()+listView.getDividerHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

    public void getData(){
        Intent intent = getIntent();
        if (intent != null) {
            String cno = intent.getStringExtra("cno");
            String sno = intent.getStringExtra("sno");
            if(cno!=null)
                getDataByCno(cno);
            else if (sno!=null)
                getDataBySno(sno);
            return;
        }
    }

    public void getDataByCno(String cno){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectScByCno")
                .addParams("page", Integer.toString(page))
                .addParams("cno",cno)
                .build()
                .execute(new StringCallback()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(context,"网络错误",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "onResponse: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray result = jsonObject.getJSONObject("data").getJSONArray("data");
                            is_done = jsonObject.getJSONObject("data").getBoolean("finished");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = (JSONObject) result.get(i);
                                HashMap map = new HashMap<String,Object>();
                                map.put("sno",temp.getString("sno"));
                                map.put("sname", temp.getString("sname"));
                                map.put("cname", temp.getString("cname"));
                                map.put("credit", temp.getString("credit"));
                                map.put("grade", temp.getString("grade"));
                                mListData.add(map);
                            }
                            Message msg = new Message();
                            msg.what = 0;
                            handler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"服务器错误",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getDataBySno(String sno){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectScBySno")
                .addParams("page", Integer.toString(page))
                .addParams("sno",sno)
                .build()
                .execute(new StringCallback()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(context,"网络错误",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "onResponse: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray result = jsonObject.getJSONObject("data").getJSONArray("data");
                            is_done = jsonObject.getJSONObject("data").getBoolean("finished");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = (JSONObject) result.get(i);
                                HashMap map = new HashMap<String,Object>();
                                map.put("sno",temp.getString("sno"));
                                map.put("sname", temp.getString("sname"));
                                map.put("cname", temp.getString("cname"));
                                map.put("credit", temp.getString("credit"));
                                map.put("grade", temp.getString("grade"));
                                mListData.add(map);
                            }
                            Message msg = new Message();
                            msg.what = 0;
                            handler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"服务器错误",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
