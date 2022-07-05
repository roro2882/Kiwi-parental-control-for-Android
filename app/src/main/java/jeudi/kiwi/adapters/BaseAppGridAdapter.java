package jeudi.kiwi.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import jeudi.kiwi.R;

public class BaseAppGridAdapter extends BaseAdapter{
    private ArrayList<CustomAppItem> apps;
    private LayoutInflater inflater;

    public BaseAppGridAdapter(Context nC, ArrayList<CustomAppItem> nApps){
        apps = nApps;
        inflater = (LayoutInflater) nC.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public CustomAppItem getItem(int i) {
        return apps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        CustomAppItem appItem = apps.get(i);
        if(convertView==null)convertView = inflater.inflate(R.layout.item_app,viewGroup,false);
         if (i >= getCount()) return convertView;
            appItem.setRootView(convertView);
            appItem.updateView();
            return convertView;
    }
}
