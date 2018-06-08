package com.scy.courseselection.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.widget.SpringView;
import com.scy.courseselection.R;
import com.scy.courseselection.adapter.StuAdapter;
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

public class SelectStuActivity extends AppCompatActivity implements SpringView.OnFreshListener,View.OnClickListener{
    private static final String TAG = "SelectStuActivity";
    private ListView mlistview;
    private Context context = this;
    private Button search,filter,clear;
    private boolean is_done = false;
    List<HashMap<String, Object>> mListData  = new ArrayList<HashMap<String, Object>>();;
    private StuAdapter stuAdapter;
    private int page = 1;
    private SpringView sv;
    private String filter_content = "";
    private FloatingActionButton floatingActionButton;
    private TextView tv_filter;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    System.out.println(mListData);
                    stuAdapter = new StuAdapter(context, mListData);
                    mlistview.setAdapter(stuAdapter);
                    setListViewHeightBasedOnChildren(mlistview);
                    stuAdapter.notifyDataSetChanged();
                    sv.onFinishFreshAndLoad();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_stu);
        setTitle("学生名单");
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

        mlistview = (ListView)findViewById(R.id.booklist) ;
        mlistview.setDividerHeight(5 );
        mlistview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 0, 0, "修改");
                contextMenu.add(0, 1, 0, "删除");
            }
        });

        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab: {
                View dialog_view = getLayoutInflater().inflate(R.layout.dialog_view_add_stu, null);
                final EditText no = (EditText) dialog_view.findViewById(R.id.dialog_sno);
                final EditText name = (EditText) dialog_view.findViewById(R.id.dialog_sname);
                final EditText sex = (EditText) dialog_view.findViewById(R.id.dialog_sex);
                final EditText age = (EditText) dialog_view.findViewById(R.id.dialog_age);
                final EditText dept = (EditText) dialog_view.findViewById(R.id.dialog_dept);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("录入学生信息")
                        .setView(dialog_view)
                        .setCancelable(false)
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content_no = no.getText().toString();
                                String content_name = name.getText().toString();
                                String content_sex = sex.getText().toString();
                                String content_age = age.getText().toString();
                                String content_dept = dept.getText().toString();
                                addStu(content_no, content_name, content_sex, content_age, content_dept);
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
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
        View dialog_view = getLayoutInflater().inflate(R.layout.filter_stu, null);
        final EditText no = (EditText) dialog_view.findViewById(R.id.dialog_sno);
        final EditText name = (EditText) dialog_view.findViewById(R.id.dialog_sname);
        final EditText age = (EditText) dialog_view.findViewById(R.id.dialog_age);
        final EditText dept = (EditText) dialog_view.findViewById(R.id.dialog_dept);
        final CheckBox cb_sname = (CheckBox) dialog_view.findViewById(R.id.cb_sname);
        final CheckBox cb_sdept = (CheckBox) dialog_view.findViewById(R.id.cb_sdept);
        final CheckBox cb_male = (CheckBox) dialog_view.findViewById(R.id.male);
        final CheckBox cb_female = (CheckBox) dialog_view.findViewById(R.id.female);
        final Spinner re = (Spinner) dialog_view.findViewById(R.id.re);

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
                            fil = fil + "Sno = "+no.getText().toString();
                            first = false;
                        }
                        if (name.getText().toString().length() != 0)
                        {
                            if (first)
                                first = false;
                            else
                                fil = fil + " AND ";
                            if (cb_sname.isChecked())
                                fil = fil + "Sname like '%"+name.getText().toString()+"%'";
                            else
                                fil = fil + "Sname = '"+name.getText().toString()+"'";
                        }
                        if (cb_male.isChecked() && !cb_female.isChecked()){
                            if (first)
                                first = false;
                            else
                                fil = fil + " AND ";
                            fil = fil + "Ssex = '男'";
                        }
                        if (cb_female.isChecked() && !cb_male.isChecked()){
                            if (first)
                                first = false;
                            else
                                fil = fil + " AND ";
                            fil = fil + "Ssex = '女'";
                        }
                        if (re.getSelectedItemPosition()!=0 && age.getText().toString().length() != 0){
                            if (first)
                                first = false;
                            else
                                fil = fil + " AND ";
                            switch (re.getSelectedItemPosition()){
                                case 1:
                                    fil = fil + "Sage > "+age.getText().toString();
                                    break;
                                case 2:
                                    fil = fil + "Sage < "+age.getText().toString();
                                    break;
                                case 3:
                                    fil = fil + "Sage = "+age.getText().toString();
                                    break;
                            }
                        }
                        if (dept.getText().toString().length() != 0)
                        {
                            if (first)
                                first = false;
                            else
                                fil = fil + " AND ";
                            if (cb_sdept.isChecked())
                                fil = fil + "Sdept like '%"+dept.getText().toString()+"%'";
                            else
                                fil = fil + "Sdept = "+dept.getText().toString();
                            first = false;
                        }
                        filter_content = "WHERE "+fil;
                        tv_filter.setText(fil);
                        System.out.println(fil);
                    }
                })
                .show();
    }

    public void addStu(final String no, final String name, final String sex, final String age, final String dept){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "insertStu")
                .addParams("sno", no)
                .addParams("sname", name)
                .addParams("ssex",sex)
                .addParams("sage",age)
                .addParams("sdept",dept)
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
                                Toast.makeText(context, "添加成功", Toast.LENGTH_LONG).show();
                            }
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
        String sno = (String) mListData.get(i).get("no");
        switch (item.getItemId()) {
            case 1:
                deleteStu(i,sno);
                break;
            case 0:
                String old_no = (String)mListData.get(i).get("no");
                String old_name = (String)mListData.get(i).get("name");
                String old_sex = (String)mListData.get(i).get("sex");
                String old_age = (String)mListData.get(i).get("age");
                String old_dept = (String)mListData.get(i).get("dept");
                View dialog_view = getLayoutInflater().inflate(R.layout.dialog_view_add_stu, null);

                final EditText no = (EditText) dialog_view.findViewById(R.id.dialog_sno);
                final EditText name = (EditText) dialog_view.findViewById(R.id.dialog_sname);
                final EditText sex = (EditText) dialog_view.findViewById(R.id.dialog_sex);
                final EditText age = (EditText) dialog_view.findViewById(R.id.dialog_age);
                final EditText dept = (EditText) dialog_view.findViewById(R.id.dialog_dept);

                no.setText(old_no);
                name.setText(old_name);
                sex.setText(old_sex);
                age.setText(old_age);
                dept.setText(old_dept);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("更新学生信息")
                        .setView(dialog_view)
                        .setCancelable(false)
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content_no = no .getText().toString();
                                String content_name = name .getText().toString();
                                String content_sex = sex .getText().toString();
                                String content_age = age .getText().toString();
                                String content_dept = dept .getText().toString();
                                updateStu(i,content_no,content_name,content_sex,content_age,content_dept);

                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void getData(){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectStu")
                .addParams("key", filter_content)
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
                            int num = jsonObject.getJSONObject("data").getInt("num");
                            Toast.makeText(context,"总共查询到："+num+"条记录",Toast.LENGTH_LONG).show();
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = (JSONObject) result.get(i);
                                HashMap map = new HashMap<String,Object>();
                                map.put("name",temp.getString("name"));
                                map.put("no", temp.getString("no"));
                                map.put("age", temp.getString("age"));
                                map.put("sex",temp.getString("sex"));
                                map.put("dept",temp.getString("dept"));
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
            Toast.makeText(SelectStuActivity.this, "没有更多信息", Toast.LENGTH_SHORT).show();
            sv.onFinishFreshAndLoad();
        }
    }

    public void deleteStu(final int i,final String sno){
        new AlertDialog.Builder(this)
                .setTitle("确定")
                .setMessage("是否确认删除该学生")
                .setCancelable(false)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OkHttpUtils
                                .get()
                                .url(api_url)
                                .addParams("_action", "deleteStu")
                                .addParams("sno", sno)
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
                                });                    }
                })
                .create().show();
    }

    public void updateStu(final int i,final String no,final String name,final String sex,final String age,final String dept){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "updateStu")
                .addParams("sno", no)
                .addParams("sname", name)
                .addParams("ssex", sex)
                .addParams("sage", age)
                .addParams("sdept", dept)
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
                                HashMap map = new HashMap<String,Object>();
                                map.put("name",name);
                                map.put("no", no);
                                map.put("age", age);
                                map.put("sex",sex);
                                map.put("dept",dept);
                                mListData.set(i,map);
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                                Toast.makeText(context, "更新成功", Toast.LENGTH_LONG).show();
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

}
