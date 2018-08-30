package mobi.zapzap.mediapicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.callbacks.OnAlbumSelectionListener;
import mobi.zapzap.mediapicker.models.Album;

/**
 * Created by Wade Morris on 2018/08/29.
 */
public class AlbumGridAdapter extends RecyclerView.Adapter<AlbumGridAdapter.AlbumViewHolder>{

    private ArrayList<Album> albums;
    private OnAlbumSelectionListener onAlbumSelectedListener;

    public AlbumGridAdapter(@NonNull OnAlbumSelectionListener onAlbumSelectedListener) {

        this.albums = new ArrayList<Album>();
        this.onAlbumSelectedListener = onAlbumSelectedListener;
    }

    private void add(@NonNull Album album) {

        albums.add(album);
        notifyItemInserted(albums.size() - 1);
    }

    public void addAll(@NonNull ArrayList<Album> singleList) {

        for (Album album : singleList) {
            add(album);
        }
    }

    public void remove(@NonNull Album album) {

        int position = albums.indexOf(album);
        if (position > -1) {
            albums.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {

        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public Album getItem(int position){
        return albums.get(position);
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AlbumViewHolder holder, int position) {

        final Album album = albums.get(position);
        if (album != null) {

            Context context = holder.itemView.getContext();
            holder.txtName.setText(album.getName());
            holder.txtDate.setText(album.getDisplayDate());
            holder.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, album.getIconResId()));
            RequestOptions options = new RequestOptions().placeholder(R.color.zz_media_picker_default_placeholder).transform(new CenterCrop()).transform(new FitCenter());
            if (album.getName().equals("Take Photo")) {

                Glide.with(context).load(album.getCoverPath())
                        .apply(options)
                        .into(holder.imgCoverArt);
            } else {

                final Uri uri = Uri.fromFile(new File(album.getCoverPath()));
                Glide.with(context).load(uri)
                        .apply(options)
                        .into(holder.imgCoverArt);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int adapterPos = holder.getAdapterPosition();
                    if (adapterPos != RecyclerView.NO_POSITION) {

                        if (onAlbumSelectedListener != null) {
                            onAlbumSelectedListener.onClick(album, view, adapterPos);
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return albums != null ? albums.size() : 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCoverArt;
        TextView txtName;
        TextView txtDate;
        ImageView imgIcon;

        public AlbumViewHolder(@NonNull View itemView) {

            super(itemView);
            imgCoverArt = (ImageView) itemView.findViewById(R.id.img_album_cover);
            txtName = (TextView) itemView.findViewById(R.id.text_view_album_name);
            txtDate = (TextView) itemView.findViewById(R.id.txt_album_timestamp);
            imgIcon = (ImageView) itemView.findViewById(R.id.img_album_icon);
        }

    }
}
