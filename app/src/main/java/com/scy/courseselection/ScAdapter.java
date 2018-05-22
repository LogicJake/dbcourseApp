package com.scy.courseselection;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ScAdapter extends BaseAdapter implements View.OnClickListener {
    private Context context;
    List<HashMap<String, Object>> list;
    private boolean exist;
    private Callback mCallback;

    /**
     * 自定义接口，用于回调按钮点击事件到Activity
     * @author Ivan Xu
     * 2014-11-26
     */
    public interface Callback {
        public void click(View v);
    }

    public ScAdapter(Context context, List<HashMap<String, Object>> list,boolean exist,Callback callback) {
        this.context = context;
        this.list = list;
        this.exist = exist;
        mCallback = callback;
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
            convertView = View.inflate(context, R.layout.item_list_sc, null);
            holder = new ViewHolder();
            holder.sno = (TextView) convertView.findViewById(R.id.item_sno);
            holder.sname = (TextView) convertView.findViewById(R.id.item_sname);
            holder.cname = (TextView) convertView.findViewById(R.id.item_cname);
            holder.credit = (TextView) convertView.findViewById(R.id.item_credit);
            holder.grade = (TextView) convertView.findViewById(R.id.item_grade);
            holder.button = (Button) convertView.findViewById(R.id.item_button);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.sno.setText((String)list.get(position).get("sno"));
        holder.sname.setText((String)list.get(position).get("sname"));
        holder.cname.setText((String)list.get(position).get("cname"));
        holder.credit.setText((String)list.get(position).get("credit"));
        holder.grade.setText((String)list.get(position).get("grade"));
        if (exist){
            holder.button.setOnClickListener(this);
            holder.button.setTag(position);
            holder.button.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public class ViewHolder {
        private TextView sno;
        private TextView sname;
        private TextView cname;
        private TextView credit;
        private TextView grade;
        private Button button;
    }

    //响应按钮点击事件,调用子定义接口，并传入View
    @Override
    public void onClick(View v) {
        mCallback.click(v);
    }
}
