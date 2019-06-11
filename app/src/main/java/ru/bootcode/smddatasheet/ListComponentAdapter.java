package ru.bootcode.smddatasheet;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Администратор on 26.07.2018.
 */

public class ListComponentAdapter extends BaseAdapter {
    private Context mContext;
    private List<Component> mComponentList;

    public ListComponentAdapter(Context mContext, List<Component> mComponentList) {
        this.mContext = mContext;
        this.mComponentList = mComponentList;
    }

    @Override
    public int getCount() {
        return mComponentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mComponentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mComponentList.get(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.item, null);

        // Черезстрочно закрашиваем выводимый список -----------------------------------------------
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.linlayBackground);
        if ((position % 2) == 0) {
            layout.setBackgroundColor(0xFFFFFFFF);
        } else {
            layout.setBackgroundColor(0xFFEEEEEE);
        }

        // Вывод в список информации по компонентам ------------------------------------------------
        TextView tvCode     = (TextView)v.findViewById(R.id.tvCode);
        TextView tvMarker   = (TextView)v.findViewById(R.id.tvMarker);
        TextView tvNote     = (TextView)v.findViewById(R.id.tvNote);
        TextView tvName     = (TextView)v.findViewById(R.id.tvName);

        tvCode.setText(mComponentList.get(position).getCode());
        tvMarker.setText(" ["+mComponentList.get(position).getMarker()+"] ");
        tvNote.setText(mComponentList.get(position).getNote()+" ("+mComponentList.get(position).getProd()+")");
        tvName.setText(mComponentList.get(position).getName());

        // Код выводит картинку из ресурсов приложения, --------------------------------------------
        // заменяем - на _ так как - нельзя испеользовать в файлах
        String sCode = mComponentList.get(position).getCode().replace("-","_").toLowerCase();
        ImageView  mImageView = (ImageView) v.findViewById(R.id.ivCode);
        int id = mContext.getResources().getIdentifier("ru.bootcode.smddatasheet:drawable/" + sCode, null, null);
        mImageView.setImageResource(id);

        return v;
    }
}