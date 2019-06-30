package ru.bootcode.smddatasheet;


import android.annotation.SuppressLint;
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

    private int selections;
    private List<Component> mComponentList;

    ListComponentAdapter(Context mContext, List<Component> mComponentList) {
        this.mContext = mContext;
        this.mComponentList = mComponentList;

        this.selections = -1;
    }

    void switchSelection(int position){
        this.selections = position;
        //оповещаем адаптер об изменениях, чтобы он обновил все элементы списка.
        notifyDataSetChanged();
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
        @SuppressLint("ViewHolder")
        View v = View.inflate(mContext, R.layout.item, null);

        // Черезстрочно закрашиваем выводимый список -----------------------------------------------
        LinearLayout layout = v.findViewById(R.id.linlayBackground);
        if ((position % 2) == 0) {
            layout.setBackgroundColor(0xFFFFFFFF);
        } else {
            layout.setBackgroundColor(0xFFEEEEEE);
        }

        //Ключевой момент - просто ставим цвет фона в зависимости от значения переменной selection
        if (this.selections == position){
            layout.setBackgroundColor(0xFFFF8800);
        }

        // Вывод в список информации по компонентам ------------------------------------------------
        TextView tvCode     = v.findViewById(R.id.tvCode);
        TextView tvMarker   = v.findViewById(R.id.tvMarker);
        TextView tvNote     = v.findViewById(R.id.tvNote);
        TextView tvName     = v.findViewById(R.id.tvName);

        tvCode.setText(mComponentList.get(position).getBody());
        tvMarker.setText(String.format(" [%s] ", mComponentList.get(position).getLabel()));
        tvNote.setText(String.format("%s (%s)", mComponentList.get(position).getFunc(), mComponentList.get(position).getProd()));
        tvName.setText(mComponentList.get(position).getName());

        // Код выводит картинку из ресурсов приложения, --------------------------------------------
        // заменяем - на _ так как - нельзя испеользовать в файлах
        String sCode = mComponentList.get(position).getBody().replace("-","_").toLowerCase();
        ImageView  mImageView = v.findViewById(R.id.ivCode);
        int id = mContext.getResources().getIdentifier("ru.bootcode.smddatasheet:drawable/" + sCode, null, null);
        mImageView.setImageResource(id);


        return v;
    }
}