package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.DrawerRowItem;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MyDrawerAdapter extends ArrayAdapter<DrawerRowItem> {

    private final int drawerLayout;
    private final DrawerRowItem[] data;
    private Context mContext;

    public MyDrawerAdapter(Context context, DrawerRowItem[] data) {
        super(context, R.layout.fragment_navigation_drawer, data);
        this.mContext = context;
        this.drawerLayout = R.layout.row_drawer;
        this.data = data;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DrawerRowItem drawerRowItem = data[position];
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(drawerLayout, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.listText);
            holder.imageView = (ImageView) convertView.findViewById(R.id.listIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtTitle.setText(drawerRowItem.getTitle());
        holder.imageView.setImageResource(drawerRowItem.getImageId());

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
    }
}
