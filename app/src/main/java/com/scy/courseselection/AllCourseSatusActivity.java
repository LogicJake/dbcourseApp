package com.scy.courseselection;

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

import static com.scy.courseselection.AllScActivity.setListViewHeightBasedOnChildren;
import static com.scy.courseselection.MainActivity.api_url;

public class AllCourseSatusActivity extends AppCompatActivity implements SpringView.OnFreshListener,AdapterView.OnItemClickListener{
    private Context context = this;
    private SpringView sv;
    private int page = 1;
    private boolean is_done = true;
    private ListView mlistview;
    List<HashMap<String, Object>> mListData  = new ArrayList<HashMap<String, Object>>();;
    private CourseStatusAdapter courseStatusAdapter;
    private static final String TAG = "AllCourseSatusActivity";
    private SharedPreferences sharedPreferences;
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
        setContentView(R.layout.activity_all_course_satus);

        setTitle("选课");

        sv = (SpringView) findViewById(R.id.sv);//sv
        sv.setHeader(new DefaultHeader(this));
        sv.setFooter(new DefaultFooter(this));
        sv.setListener(this);

        mlistview = (ListView)findViewById(R.id.booklist) ;
        mlistview.setDividerHeight(5 );

        mlistview.setOnItemClickListener(this);

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
            Toast.makeText(AllCourseSatusActivity.this, "没有更多课程信息", Toast.LENGTH_SHORT).show();
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
}
