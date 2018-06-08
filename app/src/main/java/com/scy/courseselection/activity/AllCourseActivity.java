package com.scy.courseselection.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.widget.SpringView;
import com.scy.courseselection.adapter.CourseAdapter;
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

import static com.scy.courseselection.activity.AllScActivity.setListViewHeightBasedOnChildren;
import static com.scy.courseselection.activity.MainActivity.api_url;

public class AllCourseActivity extends AppCompatActivity implements SpringView.OnFreshListener,View.OnClickListener {
    private Context context = this;
    private SpringView sv;
    private int page = 1;
    private boolean is_done = true;
    private ListView mlistview;
    List<HashMap<String, Object>> mListData  = new ArrayList<HashMap<String, Object>>();;
    private CourseAdapter courseAdapter;
    private static final String TAG = "AllCourseActivity";
    private FloatingActionButton floatingActionButton;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    System.out.println(mListData);
                    courseAdapter = new CourseAdapter(context, mListData);
                    mlistview.setAdapter(courseAdapter);
                    setListViewHeightBasedOnChildren(mlistview);
                    courseAdapter.notifyDataSetChanged();
                    sv.onFinishFreshAndLoad();
                    break;
                case 1:
                    courseAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_course);
        setTitle("课程列表");

        sv = (SpringView) findViewById(R.id.sv);//sv
        sv.setHeader(new DefaultHeader(this));
        sv.setFooter(new DefaultFooter(this));
        sv.setListener(this);

        mlistview = (ListView)findViewById(R.id.booklist) ;
        mlistview.setDividerHeight(5 );
        mlistview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 0, 0, "修改");
                contextMenu.add(0, 1, 0, "删除");
                contextMenu.add(0, 2, 0, "查看学生名单");
            }
        });
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

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
            Toast.makeText(AllCourseActivity.this, "没有更多课程信息", Toast.LENGTH_SHORT).show();
            sv.onFinishFreshAndLoad();
        }
    }

    public void getData(){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectCourseAll")
                .addParams("page", Integer.toString(page))
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:{
                View dialog_view = getLayoutInflater().inflate(R.layout.dialog_view_add_course, null);
                final EditText cno = (EditText) dialog_view.findViewById(R.id.dialog_cno);
                final EditText cname = (EditText) dialog_view.findViewById(R.id.dialog_cname);
                final EditText credit = (EditText) dialog_view.findViewById(R.id.dialog_ccredit);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("添加课程")
                        .setView(dialog_view)
                        .setCancelable(false)
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content_cno = cno .getText().toString();
                                String content_cname = cname .getText().toString();
                                String content_credit = credit .getText().toString();
                                addCourse(content_cno,content_cname,content_credit);
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
        }
    }

    public void addCourse(String cno,String cname,String credit){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "insertCourse")
                .addParams("cno", cno)
                .addParams("cname", cname)
                .addParams("ccredit",credit)
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
                            if(status == 1)
                                Toast.makeText(context,"添加成功",Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(context,"添加失败",Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"服务器错误",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        final int i = (int) info.id;
        String cno = (String) mListData.get(i).get("no");
        switch (item.getItemId()) {
            case 1:
                deleteCourse(i,cno);
                break;
            case 0:
            {
                String old_no = (String)mListData.get(i).get("no");
                String old_name = (String)mListData.get(i).get("name");
                String old_credit = (String)mListData.get(i).get("credit");
                View dialog_view = getLayoutInflater().inflate(R.layout.dialog_view_add_course, null);
                final EditText no = (EditText) dialog_view.findViewById(R.id.dialog_cno);
                no.setText(old_no);
                final EditText cname = (EditText) dialog_view.findViewById(R.id.dialog_cname);
                cname.setText(old_name);
                final EditText credit = (EditText) dialog_view.findViewById(R.id.dialog_ccredit);
                credit.setText(old_credit);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("更新课程信息")
                        .setView(dialog_view)
                        .setCancelable(false)
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content_cno = no .getText().toString();
                                String content_cname = cname .getText().toString();
                                String content_credit = credit .getText().toString();
                                updateCourse(i,content_cno,content_cname,content_credit);
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
            case 2:
                Intent intent = new  Intent(AllCourseActivity.this,AllScActivity.class);
                intent.putExtra("cno",cno);
                startActivity(intent);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void updateCourse(final int i,final String cno,final String cname,final String credit){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "updateCourse")
                .addParams("cno", cno)
                .addParams("cname", cname)
                .addParams("ccredit", credit)
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
                            if(status == 1){
                                HashMap<String ,Object> map = new HashMap<>();
                                map.put("name",cname);
                                map.put("no", cno);
                                map.put("credit",credit);
                                mListData.set(i,map);
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                                Toast.makeText(context,"更新成功",Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(context,"更新失败",Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"服务器错误",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void deleteCourse(final int i, final String cno){
        new AlertDialog.Builder(this)
                .setTitle("确定")
                .setMessage("是否确认删除该课程")
                .setCancelable(false)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OkHttpUtils
                                .get()
                                .url(api_url)
                                .addParams("_action", "deleteCourse")
                                .addParams("cno", cno)
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
                                            if(status == 1) {
                                                mListData.remove(i);
                                                Message message = new Message();
                                                message.what = 1;
                                                handler.sendMessage(message);
                                                Toast.makeText(context, "删除成功", Toast.LENGTH_LONG).show();
                                            }
                                            else
                                                Toast.makeText(context,"删除失败",Toast.LENGTH_LONG).show();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Toast.makeText(context,"服务器错误",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                })
                .create().show();
    }

}
