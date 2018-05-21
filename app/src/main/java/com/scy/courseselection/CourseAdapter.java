package com.scy.courseselection;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class CourseAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String, Object>> list;

    public CourseAdapter(Context context, List<HashMap<String, Object>> list) {
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
            convertView = View.inflate(context, R.layout.item_list_course, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_name);
            holder.no = (TextView) convertView.findViewById(R.id.item_no);
            holder.credit = (TextView) convertView.findViewById(R.id.item_credit);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText((String)list.get(position).get("name"));
        holder.no.setText((String)list.get(position).get("no"));
        holder.credit.setText((String)list.get(position).get("credit"));
        return convertView;
    }

    public class ViewHolder {
        private TextView no;
        private TextView name;
        private TextView credit;
    }
}
