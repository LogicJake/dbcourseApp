package com.scy.courseselection;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Spinner spinner;
    private Context context = this;
    private TextView role;
    private NavigationView navigationView;
    public static String api_url = "http://api.logicjake.xyz/db/";
    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        sharedPreferences = getSharedPreferences("stu",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);


        spinner = (Spinner)headerView.findViewById(R.id.role);
        role = (TextView)headerView.findViewById(R.id.roletext);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(pos == 0) {
                    role.setText("教务员");
                    editor.clear();
                    editor.commit();
                    navigationView.getMenu().setGroupVisible(R.id.student,false);
                    navigationView.getMenu().setGroupVisible(R.id.teacher,true);
                }
                else {
                    fillIn();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
    }

    public void fillIn(){
        View view = getLayoutInflater().inflate(R.layout.dialog_view, null);
        final EditText editText = (EditText) view.findViewById(R.id.dialog_edit);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("填写学号")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("取消",null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = editText .getText().toString();
                        checkStudent(content);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.s1) {
            Intent intent = new  Intent(MainActivity.this,AllCourseSatusActivity.class);
            startActivity(intent);
        } else if (id == R.id.s2) {
            String sno = sharedPreferences.getString("no",null);
            Intent intent = new  Intent(MainActivity.this,AllScActivity.class);
            intent.putExtra("sno",sno);
            startActivity(intent);
        }else if (id == R.id.s3){
            Intent intent = new Intent(context,AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.t1){
            Intent intent = new Intent(context,AllStuActivity.class);
            startActivity(intent);
        } else if (id == R.id.t2){
            Intent intent = new Intent(context,AllCourseActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkStudent(final String uid){
        OkHttpUtils
                .get()
                .url(api_url)
                .addParams("_action", "getStu")
                .addParams("sno", uid)
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
                            if(status==0){
                                Toast.makeText(context,"不存在该学生",Toast.LENGTH_LONG).show();
                                spinner.setSelection(0);
                            }
                            else if (status == -1) {
                                Toast.makeText(context, "数据库错误："+jsonObject.getJSONObject("data").getString("msg"), Toast.LENGTH_LONG).show();
                                spinner.setSelection(0);
                            }
                            else{

                                String name = jsonObject.getJSONObject("data").getJSONObject("data").getString("name");
                                String sex = jsonObject.getJSONObject("data").getJSONObject("data").getString("sex");
                                String age = jsonObject.getJSONObject("data").getJSONObject("data").getString("age");
                                String dept = jsonObject.getJSONObject("data").getJSONObject("data").getString("dept");
                                editor.putString("no",uid);
                                editor.putString("name",name);
                                editor.putString("sex",sex);
                                editor.putString("age",age);
                                editor.putString("dept",dept);
                                editor.commit();
                                role.setText(name);
                                navigationView.getMenu().setGroupVisible(R.id.student,true);
                                navigationView.getMenu().setGroupVisible(R.id.teacher,false);
                                Toast.makeText(context, "登陆成功："+jsonObject.getJSONObject("data").getString("msg"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
    }

}
