package com.scy.courseselection.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scy.courseselection.R;

import java.util.HashMap;
import java.util.List;

public class StuAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String, Object>> list;

    public StuAdapter(Context context, List<HashMap<String, Object>> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_list_stu, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_name);
            holder.no = (TextView) convertView.findViewById(R.id.item_no);
            holder.sex = (TextView) convertView.findViewById(R.id.item_sex);
            holder.age = (TextView) convertView.findViewById(R.id.item_age);
            holder.dept = (TextView) convertView.findViewById(R.id.item_dept);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText((String)list.get(position).get("name"));
        holder.no.setText((String)list.get(position).get("no"));
        holder.age.setText((String)list.get(position).get("age"));
        holder.sex.setText((String)list.get(position).get("sex"));
        holder.dept.setText((String)list.get(position).get("dept"));
        return convertView;
    }

    public class ViewHolder {
        private TextView no;
        private TextView name;
        private TextView sex;
        private TextView age;
        private TextView dept;

    }

}
