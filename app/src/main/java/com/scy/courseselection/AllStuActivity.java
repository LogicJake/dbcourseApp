package com.scy.courseselection;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
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

public class AllStuActivity extends AppCompatActivity implements SpringView.OnFreshListener,View.OnClickListener {
    private Context context = this;
    private SpringView sv;
    private int page = 1;
    private boolean is_done = true;
    private ListView mlistview;
    List<HashMap<String, Object>> mListData  = new ArrayList<HashMap<String, Object>>();;
    private StuAdapter stuAdapter;
    private static final String TAG = "AllStuActivity";
    private FloatingActionButton floatingActionButton;

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
                case 1:
                    stuAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_stu);
        setTitle("学生名单");

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
            Toast.makeText(AllStuActivity.this, "没有更多学生信息", Toast.LENGTH_SHORT).show();
            sv.onFinishFreshAndLoad();
        }
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
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "selectStuAll")
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:{
                View dialog_view = getLayoutInflater().inflate(R.layout.dialog_view_add_stu, null);
                final EditText no = (EditText) dialog_view.findViewById(R.id.dialog_sno);
                final EditText name = (EditText) dialog_view.findViewById(R.id.dialog_sname);
                final EditText sex = (EditText) dialog_view.findViewById(R.id.dialog_sex);
                final EditText age = (EditText) dialog_view.findViewById(R.id.dialog_age);
                final EditText dept = (EditText) dialog_view.findViewById(R.id.dialog_dept);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("添加课程")
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
                                addStu(content_no,content_name,content_sex,content_age,content_dept);
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
        }
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
                                HashMap map = new HashMap<String,Object>();
                                map.put("name",name);
                                map.put("no", no);
                                map.put("age", age);
                                map.put("sex",sex);
                                map.put("dept",dept);
                                mListData.add(map);
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
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

}
