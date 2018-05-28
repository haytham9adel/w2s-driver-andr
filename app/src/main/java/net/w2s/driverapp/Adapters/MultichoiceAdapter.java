package net.w2s.driverapp.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import net.w2s.driverapp.Beans.ParentBean;
import net.w2s.driverapp.R;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 12/12/2016.
 */

public class MultichoiceAdapter extends BaseAdapter {

    private ArrayList<ParentBean> list;
    private ArrayList<Boolean> listCheck;
    private LayoutInflater layoutInflater;
    private ViewHolder viewHolder;
    private Context context;

    public MultichoiceAdapter(Context context, ArrayList<ParentBean> list, ArrayList<Boolean> listCheck) {
        this.list = list;
        this.listCheck = listCheck;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.multichoice_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position != 0) {
            viewHolder.checkBox.setText(list.get(position).getParent_fname() + " " + list.get(position).getParent_family_name());
            viewHolder.text.setText(list.get(position).getParent_number() );
        } else {
            viewHolder.checkBox.setText(context.getString(R.string.all_parent));
            viewHolder.text.setVisibility(View.GONE);
        }


     /*   viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                           @Override
                                                           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                               if (position == 0) {
                                                                   for (int i = 0; i < listCheck.size(); i++) {
                                                                       listCheck.set(i, isChecked);
                                                                   }
                                                                   notifyDataSetChanged();
                                                               } else {
                                                                   listCheck.set(0, false);
                                                                   listCheck.set(position, isChecked);
                                                               }
                                                           }
                                                       }

        );*/
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    boolean vari = false;
                    if (!listCheck.get(0)) {
                        vari = true;
                    } else {
                        vari = false;
                    }
                    for (int i = 0; i < listCheck.size(); i++) {
                        listCheck.set(i, vari);
                    }
                    notifyDataSetChanged();
                } else {
                    listCheck.set(0, false);
                    listCheck.set(position, !listCheck.get(position));
                    notifyDataSetChanged();
                }
            }
        });

        viewHolder.checkBox.setChecked(listCheck.get(position));

        if ((position % 2) != 1) {
            convertView.setBackgroundColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }
        return convertView;
    }

    private class ViewHolder {
        private CheckBox checkBox;
        private TextView text;
    }
}
