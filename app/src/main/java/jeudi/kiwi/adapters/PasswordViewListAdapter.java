package jeudi.kiwi.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import jeudi.kiwi.R;

public class PasswordViewListAdapter extends RecyclerView.Adapter<PasswordViewListAdapter.ViewHolder> {

    private ArrayList<CustomAppItem> apps;
    private LayoutInflater mInflater;

    public PasswordViewListAdapter(Context nC, ArrayList<CustomAppItem> nApps){
        apps = nApps;
        mInflater = (LayoutInflater) nC.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }


    // inflates the row layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PasswordViewListAdapter.ViewHolder viewHolder;
        View convertView = mInflater.inflate(R.layout.item_app, parent, false);
            viewHolder = new PasswordViewListAdapter.ViewHolder(convertView);
        viewHolder.image = convertView.findViewById(R.id.app_icon1);
        return viewHolder;
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        if(i>=getItemCount())return;
        apps.get(i).putIconInto(viewHolder.image);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return apps.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

}