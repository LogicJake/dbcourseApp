package com.scy.courseselection.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.widget.SpringView;
import com.scy.courseselection.adapter.CourseStatusAdapter;
import com.scy.courseselection.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

import static com.scy.courseselection.activity.SelectScActivity.setListViewHeightBasedOnChildren;
import static com.scy.courseselection.activity.MainActivity.api_url;

public class SelectCourseSatusActivity extends AppCompatActivity implements SpringView.OnFreshListener,AdapterView.OnItemClickListener,View.OnClickListener{
    private Context context = this;
    private SpringView sv;
    private int page = 1;
    private boolean is_done = true;
    private ListView mlistview;
    List<HashMap<String, Object>> mListData  = new ArrayList<HashMap<String, Object>>();;
    private CourseStatusAdapter courseStatusAdapter;
    private static final String TAG = "SelectCourseSatus";
    private SharedPreferences sharedPreferences;
    private TextView tv_filter;
    private String filter_content = "";
    private Button search,filter,clear;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    courseStatusAdapter = new CourseStatusAdapter(context, mListData);
                    mlistview.setAdapter(courseStatusAdapter);
                    setListViewHeightBasedOnChildren(mlistview);
                    courseStatusAdapter.notifyDataSetChanged();
                    sv.onFinishFreshAndLoad();
                    break;
                case 1:
                    courseStatusAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_course_satus);

        setTitle("选课");
        search = (Button)findViewById(R.id.searech);
        filter = (Button)findViewById(R.id.filter);
        clear = (Button)findViewById(R.id.clear);
        filter.setOnClickListener(this);
        clear.setOnClickListener(this);
        search.setOnClickListener(this);

        tv_filter = (TextView)findViewById(R.id.tv_filter);
        sv = (SpringView) findViewById(R.id.sv);//sv
        sv.setHeader(new DefaultHeader(this));
        sv.setFooter(new DefaultFooter(this));
        sv.setListener(this);

        mlistview = (ListView)findViewById(R.id.booklist) ;
        mlistview.setDividerHeight(5 );

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
            Toast.makeText(SelectCourseSatusActivity.this, "没有更多课程信息", Toast.LENGTH_SHORT).show();
            sv.onFinishFreshAndLoad();
        }
    }

    public void getData(){
        sharedPreferences = getSharedPreferences("stu",Context.MODE_PRIVATE);
        String sno = sharedPreferences.getString("no",null);

        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectCourseStatus")
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
                                map.put("name",temp.getString("name"));
                                map.put("no", temp.getString("no"));
                                map.put("credit", temp.getString("credit"));
                                map.put("status", temp.getString("status"));
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        String status = (String) mListData.get(i).get("status");
        final String cno = (String) mListData.get(i).get("no");
        if (status.equals("已选课")){
            Toast.makeText(context,"已选课",Toast.LENGTH_SHORT).show();
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle("确定")
                    .setMessage("是否确认选课")
                    .setCancelable(false)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            chooseCourse(i,sharedPreferences.getString("no",null),cno);
                        }
                    })
                    .create().show();
        }
    }

    public void chooseCourse(final int i,String sno,String cno){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "insertSc")
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
                            JSONObject res = new JSONObject(response).getJSONObject("data");
                            int status = res.getInt("status");
                            if (status == 1) {
                                HashMap map = mListData.get(i);
                                map.put("status", "已选课");
                                mListData.set(i,map);
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                                Toast.makeText(context, "选课成功", Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(context,"选课失败",Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
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
        View dialog_view = getLayoutInflater().inflate(R.layout.filter_course, null);
        final EditText no = (EditText) dialog_view.findViewById(R.id.dialog_cno);
        final EditText name = (EditText) dialog_view.findViewById(R.id.dialog_cname);
        final EditText credit = (EditText) dialog_view.findViewById(R.id.dialog_credit);
        final CheckBox cb_cname = (CheckBox) dialog_view.findViewById(R.id.cb_cname);
        final CheckBox cb_cno = (CheckBox) dialog_view.findViewById(R.id.cb_cno);
        final Spinner re = (Spinner) dialog_view.findViewById(R.id.re);
        System.out.println("11111");
        new AlertDialog.Builder(this)
                .setTitle("筛选条件")
                .setView(dialog_view)
                .setCancelable(false)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Boolean first = true;
                        String fil = "";
                        if (no.getText().toString().length() != 0)
                        {
                            if (cb_cno.isChecked())
                                fil = fil + "Cno like '%"+name.getText().toString()+"%'";
                            else
                                fil = fil + "Cno = '"+name.getText().toString()+"'";
                            first = false;
                        }
                        if (name.getText().toString().length() != 0)
                        {
                            if (first)
                                first = false;
                            else
                                fil = fil + " AND ";
                            if (cb_cname.isChecked())
                                fil = fil + "Cname like '%"+name.getText().toString()+"%'";
                            else
                                fil = fil + "Cname = '"+name.getText().toString()+"'";
                        }
                        if (re.getSelectedItemPosition()!=0 && credit.getText().toString().length() != 0){
                            if (first)
                                first = false;
                            else
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
                        filter_content = "WHERE "+fil;
                        tv_filter.setText(fil);
                        System.out.println(fil);
                    }
                })
                .show();
    }
}
