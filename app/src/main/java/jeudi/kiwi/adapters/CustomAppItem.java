package jeudi.kiwi.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

import jeudi.kiwi.R;

public class CustomAppItem {
    String info;
    String packageName;
    private int drawableId = -1;
    boolean locked = false;
    View rootView;
    Context context;
    public CustomAppItem(String nInfo, String nId, Context ctx){
        info = nInfo;
        packageName = nId;
        context = ctx;
    }

    public String getInfo() {
        return info;
    }

    public boolean isLocked() {
        return locked;
    }

    public CustomAppItem setSelected(boolean locked) {
        this.locked = locked;
        return this;
    }

    public void putIconInto(ImageView img){
        if(drawableId==-1) {
            File f = new File(context.getFilesDir(), packageName + ".jpg");
            Picasso.get().load(f).into(img);
        }else Picasso.get().load(drawableId).into(img);
    }


    public View getRootView() {
        return rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public void updateView(){
        if(rootView != null) {
            try {
                updateIcon();
                updateInfoText();
                updateLockIcon();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateInfoText(){
        if(rootView!=null) {
            TextView title = rootView.findViewById(R.id.app_info);
            title.setText(getInfo());
        }
    }

    public void updateIcon(){
        ImageView image = rootView.findViewById(R.id.app_icon1);
        putIconInto(image);
    }

    public void updateLockIcon(){
        ImageView lockIcon = rootView.findViewById(R.id.locked);
        lockIcon.setVisibility(isLocked() ? View.VISIBLE : View.GONE);
    }
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
