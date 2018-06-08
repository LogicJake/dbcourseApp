package com.scy.courseselection.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.widget.SpringView;
import com.scy.courseselection.R;
import com.scy.courseselection.adapter.ScAdapter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

import static com.scy.courseselection.activity.MainActivity.api_url;

public class SelectScActivity extends AppCompatActivity implements SpringView.OnFreshListener,ScAdapter.Callback,AdapterView.OnItemClickListener,View.OnClickListener {
    private Context context = this;
    private ScAdapter.Callback callback = this;
    private SpringView sv;
    private int page = 1;
    private boolean is_done = true;
    private ListView mlistview;
    List<HashMap<String, Object>> mListData  = new ArrayList<HashMap<String, Object>>();;
    private ScAdapter scAdapter;
    private static final String TAG = "AllStuActivity";
    private TextView tv_filter;
    private String filter_content = "";
    private Button search,filter,clear;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    System.out.println(mListData);
                    Intent intent = getIntent();
                    String sno = intent.getStringExtra("sno");
                    if (sno!=null)
                        scAdapter = new ScAdapter(context, mListData,true,callback);
                    else {
                        scAdapter = new ScAdapter(context, mListData, false, callback);
                    }
                    mlistview.setAdapter(scAdapter);
                    setListViewHeightBasedOnChildren(mlistview);
                    scAdapter.notifyDataSetChanged();
                    sv.onFinishFreshAndLoad();
                    break;
                case 1:
                    scAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_sc);
        search = (Button)findViewById(R.id.searech);
        filter = (Button)findViewById(R.id.filter);
        clear = (Button)findViewById(R.id.clear);
        filter.setOnClickListener(this);
        clear.setOnClickListener(this);
        search.setOnClickListener(this);
        sv = (SpringView) findViewById(R.id.sv);//sv
        sv.setHeader(new DefaultHeader(this));
        sv.setFooter(new DefaultFooter(this));
        sv.setListener(this);
        tv_filter = (TextView)findViewById(R.id.tv_filter);

        mlistview = (ListView)findViewById(R.id.booklist) ;
        mlistview.setDividerHeight(5);
        getData();

        Intent intent = getIntent();
        String cno = intent.getStringExtra("cno");
        if (cno!=null)                  //教务处信息可以设置分数
            mlistview.setOnItemClickListener(this);
    }

    @Override
    public void onRefresh() {
        mListData.clear();
        page = 1;       //重新从第一页开始
        is_done = false;
        getData();
    }

    @Override
    public void onLoadmore() {
        if(!is_done) {
            page++;
            getData();
        }
        else {
            Toast.makeText(SelectScActivity.this, "没有更多选课信息", Toast.LENGTH_SHORT).show();
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

    public void getDataByCno(final String cno){
        setTitle("学生名单");
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectScByCno")
                .addParams("page", Integer.toString(page))
                .addParams("key", filter_content)
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
                            int num = jsonObject.getJSONObject("data").getInt("num");
                            Toast.makeText(context,"总共查询到："+num+"条记录",Toast.LENGTH_LONG).show();
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = (JSONObject) result.get(i);
                                HashMap map = new HashMap<String,Object>();
                                map.put("sno",temp.getString("sno"));
                                map.put("sname", temp.getString("sname"));
                                map.put("cname", temp.getString("cname"));
                                map.put("credit", temp.getString("credit"));
                                map.put("cno", cno);
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
        setTitle("修读课程");
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectScBySno")
                .addParams("page", Integer.toString(page))
                .addParams("key", filter_content)
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
                            int num = jsonObject.getJSONObject("data").getInt("num");
                            Toast.makeText(context,"总共查询到："+num+"条记录",Toast.LENGTH_LONG).show();
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = (JSONObject) result.get(i);
                                HashMap map = new HashMap<String,Object>();
                                map.put("sno",temp.getString("sno"));
                                map.put("sname", temp.getString("sname"));
                                map.put("cname", temp.getString("cname"));
                                map.put("credit", temp.getString("credit"));
                                map.put("cno", temp.getString("cno"));
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

    public void click(View v) {
        String sno = (String)mListData.get((Integer) v.getTag()).get("sno");
        String cno = (String)mListData.get((Integer) v.getTag()).get("cno");
        deleteSc((Integer) v.getTag(),sno,cno);
    }

    public void deleteSc(final int i,String sno,String cno){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "deleteSc")
                .addParams("cno", cno)
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
                            int status = jsonObject.getJSONObject("data").getInt("status");
                            if(status == 1) {
                                mListData.remove(i);
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                                Toast.makeText(context, "退选成功", Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(context,"退选失败",Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"服务器错误",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        final EditText editText = new EditText(context);
        if (!((String)mListData.get(i).get("grade")).equals("未录入"))
            editText.setText((String)mListData.get(i).get("grade"));
        new AlertDialog.Builder(this)
                .setTitle("修改分数")
                .setView(editText)
                .setCancelable(false)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: "+editText.getText().toString() );
                        String cno =(String) mListData.get(i).get("cno");
                        String sno =(String)  mListData.get(i).get("sno");
                        String grade = editText.getText().toString();
                        updateGrade(i,cno,sno,grade);
                    }
                })
                .create().show();
    }

    public void updateGrade(final int i , String cno, String sno, final String grade){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "updateGrade")
                .addParams("cno", cno)
                .addParams("sno",sno)
                .addParams("grade",grade)
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
                            int status = jsonObject.getJSONObject("data").getInt("status");
                            if(status == 1) {
                                HashMap map = mListData.get(i);
                                map.put("grade",grade);
                                mListData.set(i,map);
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                                Toast.makeText(context, "修改成功", Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(context,"修改失败",Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"服务器错误",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searech: {
                mListData.clear();      //清空数据
                page = 1;
                getData();
                break;
            }

            case R.id.clear:{
                mListData.clear();      //清空数据
                tv_filter.setText("无筛选条件");
                page = 1;
                filter_content = "";
                is_done = true;
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
                break;
            }

            case R.id.filter:{
                setFilter();
                break;
            }
        }
    }

    public void setFilter(){
        View dialog_view = getLayoutInflater().inflate(R.layout.filter_sc, null);
        final EditText cno = (EditText) dialog_view.findViewById(R.id.dialog_cno);
        final EditText cname = (EditText) dialog_view.findViewById(R.id.dialog_cname);
        final EditText sno = (EditText) dialog_view.findViewById(R.id.dialog_sno);
        final EditText sname = (EditText) dialog_view.findViewById(R.id.dialog_sname);
        final EditText credit = (EditText) dialog_view.findViewById(R.id.dialog_credit);
        final EditText grade = (EditText) dialog_view.findViewById(R.id.dialog_grade);
        final CheckBox cb_cname = (CheckBox) dialog_view.findViewById(R.id.cb_cname);
        final CheckBox cb_cno = (CheckBox) dialog_view.findViewById(R.id.cb_cno);
        final CheckBox cb_sname = (CheckBox) dialog_view.findViewById(R.id.cb_sname);
        final Spinner re = (Spinner) dialog_view.findViewById(R.id.re);
        final Spinner re2 = (Spinner) dialog_view.findViewById(R.id.re2);
        Intent intent = getIntent();
        final String ccno = intent.getStringExtra("cno");
        final String ssno = intent.getStringExtra("sno");
        if(ccno!=null){
            cno.setVisibility(View.GONE);
            cname.setVisibility(View.GONE);
            cb_cno.setVisibility(View.GONE);
            cb_cname.setVisibility(View.GONE);
            re.setVisibility(View.GONE);
            credit.setVisibility(View.GONE);
        }
        else if (ssno!=null){
            sno.setVisibility(View.GONE);
            sname.setVisibility(View.GONE);
            cb_sname.setVisibility(View.GONE);
        }
        new AlertDialog.Builder(this)
                .setTitle("筛选条件")
                .setView(dialog_view)
                .setCancelable(false)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fil = "";
                        if(ccno!=null){
                            if (sno.getText().toString().length() != 0) {
                                fil = fil + " AND ";
                                fil = fil + "sc.Sno = " + sno.getText().toString();
                            }
                            if (sname.getText().toString().length() != 0)
                            {
                                fil = fil + " AND ";
                                if (cb_sname.isChecked())
                                    fil = fil + "Sname like '%"+sname.getText().toString()+"%'";
                                else
                                    fil = fil + "Sname = '"+sname.getText().toString()+"'";
                            }
                            if (re2.getSelectedItemPosition()!=0 && grade.getText().toString().length() != 0){
                                fil = fil + " AND ";
                                switch (re2.getSelectedItemPosition()){
                                    case 1:
                                        fil = fil + "Grade > "+grade.getText().toString();
                                        break;
                                    case 2:
                                        fil = fil + "Grade < "+grade.getText().toString();
                                        break;
                                    case 3:
                                        fil = fil + "Grade = "+grade.getText().toString();
                                        break;
                                }
                            }
                        }
                        else if (ssno!=null){
                            if (cno.getText().toString().length() != 0) {
                                fil = fil + " AND ";
                                if (cb_cno.isChecked())
                                    fil = fil + "c.Cno like '%"+cno.getText().toString()+"%'";
                                else
                                    fil = fil + "c.Cno = '"+cno.getText().toString()+"'";
                            }
                            if (cname.getText().toString().length() != 0)
                            {
                                fil = fil + " AND ";
                                if (cb_cname.isChecked())
                                    fil = fil + "Cname like '%"+cname.getText().toString()+"%'";
                                else
                                    fil = fil + "Cname = '"+cname.getText().toString()+"'";
                            }
                            if (re.getSelectedItemPosition()!=0 && credit.getText().toString().length() != 0){
                                fil = fil + " AND ";
                                switch (re.getSelectedItemPosition()){
                                    case 1:
                                        fil = fil + "Ccredit > "+credit.getText().toString();
                                        break;
                                    case 2:
                                        fil = fil + "Ccredit < "+credit.getText().toString();
                                        break;
                                    case 3:
                                        fil = fil + "Ccredit = "+credit.getText().toString();
                                        break;
                                }
                            }
                            if (re2.getSelectedItemPosition()!=0 && grade.getText().toString().length() != 0){
                                fil = fil + " AND ";
                                switch (re2.getSelectedItemPosition()){
                                    case 1:
                                        fil = fil + "Grade > "+grade.getText().toString();
                                        break;
                                    case 2:
                                        fil = fil + "Grade < "+grade.getText().toString();
                                        break;
                                    case 3:
                                        fil = fil + "Grade = "+grade.getText().toString();
                                        break;
                                }
                            }
                        }
                        filter_content = fil;
                        tv_filter.setText(fil);
                        System.out.println(fil);
                    }
                })
                .show();
    }
}
