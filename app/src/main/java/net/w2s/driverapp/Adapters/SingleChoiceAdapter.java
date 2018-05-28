package net.w2s.driverapp.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.w2s.driverapp.Beans.ParentBean;
import net.w2s.driverapp.R;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 12/12/2016.
 */

public class SingleChoiceAdapter extends BaseAdapter {

    private ArrayList<ParentBean> list;
    private LayoutInflater layoutInflater;

    public SingleChoiceAdapter(Context context, ArrayList<ParentBean> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.single_choice_row, parent, false);

        TextView checkedTextView = (TextView) convertView.findViewById(R.id.text1);
        TextView checkedTextView2 = (TextView) convertView.findViewById(R.id.text2);
        try {
            checkedTextView.setText(list.get(position).getParent_fname() + " " + list.get(position).getParent_family_name());
            checkedTextView2.setText(list.get(position).getRelationship());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((position % 2) != 1) {
            convertView.setBackgroundColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }
        return convertView;
    }
}
