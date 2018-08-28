package in.myinnos.awesomeimagepicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import in.myinnos.awesomeimagepicker.R;
import in.myinnos.awesomeimagepicker.models.Album;

/**
 * Created by MyInnos on 03-11-2016.
 */
public class CustomAlbumSelectAdapter extends CustomGenericAdapter<Album> {

    public CustomAlbumSelectAdapter(Activity activity, Context context, ArrayList<Album> albums) {
        super(activity, context, albums);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.grid_view_item_album, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgCoverArt = (ImageView) convertView.findViewById(R.id.image_view_album_image);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.text_view_album_name);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.txt_album_timestamp);
            viewHolder.imgIcon = (ImageView) convertView.findViewById(R.id.img_album_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imgCoverArt.getLayoutParams().width = size;
        viewHolder.imgCoverArt.getLayoutParams().height = size;
        Album album = arrayList.get(position);
        if (album != null) {

            viewHolder.txtName.setText(album.getName());
            viewHolder.txtDate.setText(album.getDisplayDate());
            viewHolder.imgIcon.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), album.getIconResId()));
            RequestOptions options = new RequestOptions().placeholder(R.color.colorAccent).override(200, 200).transform(new CenterCrop()).transform(new FitCenter());
            if (album.getName().equals("Take Photo")) {

                Glide.with(context).load(album.getCoverPath())
                        .apply(options)
                        .into(viewHolder.imgCoverArt);
            } else {

                final Uri uri = Uri.fromFile(new File(album.getCoverPath()));
                Glide.with(context).load(uri)
                        .apply(options)
                        .into(viewHolder.imgCoverArt);
            }
        }
        return convertView;
    }

    private static class ViewHolder {

        ImageView imgCoverArt;
        TextView txtName;
        TextView txtDate;
        ImageView imgIcon;
    }

}
