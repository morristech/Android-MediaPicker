package mobi.zapzap.mediapicker.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;
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

import mobi.zapzap.mediapicker.MediaPickerConstants;
import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.callbacks.SectionIndexer;
import mobi.zapzap.mediapicker.models.Image;
import mobi.zapzap.mediapicker.widget.HeaderItemDecoration;

/**
 * Created by Wade Morris on 2018/08/27.
 */
public class ImageGridAdapter extends ImagesAdapter implements HeaderItemDecoration.StickyHeaderInterface, SectionIndexer {

    private static final String TAG = "ImageGridAdapter";

    public ImageGridAdapter(@NonNull ArrayList<Image> images) {
        this.images = images;
    }

    @Override
    public int getItemViewType(int position) {

        Image i = images.get(position);
        return (i.getContentPath().equalsIgnoreCase("")) ? MediaPickerConstants.VIEW_TYPE_HEADER : MediaPickerConstants.VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return images.get(position).getContentPath().hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MediaPickerConstants.VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_grid_header, parent, false));
        } else {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Image image = images.get(position);
        if (image != null) {

            if (holder instanceof ImageViewHolder) {

                ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                Uri uri = Uri.fromFile(new File(image.getPath()));
                if (uri != null) {

                    RequestOptions options = new RequestOptions().placeholder(R.color.zz_media_picker_default_placeholder).transform(new CenterCrop()).transform(new FitCenter());
                    Glide.with(holder.itemView).load(uri).apply(options).into(imageViewHolder.imgThumbnail);
                }
                imageViewHolder.selection.setVisibility(image.isSelected() ? View.VISIBLE : View.GONE);
            } else if (holder instanceof HeaderViewHolder) {

                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.header.setText(image.getHeaderDate());
            }
        }
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {

        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return R.layout.layout_grid_header;
    }

    @Override
    public void bindHeaderData(@NonNull View header, int headerPosition) {

        Image image = images.get(headerPosition);
        ((TextView) header.findViewById(R.id.txt_header)).setText(image.getHeaderDate());
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return getItemViewType(itemPosition) == MediaPickerConstants.VIEW_TYPE_HEADER;
    }

    @Override
    public String getSectionText(int position) {
        return images.get(position).getHeaderDate();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView imgThumbnail;
        View selection;

        ImageViewHolder(@NonNull View itemView) {

            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            selection = itemView.findViewById(R.id.img_alpha);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int id = this.getLayoutPosition();
            if (onSelectionListener != null) {
                onSelectionListener.onClick(images.get(id), view, id);
            }
        }

        @Override
        public boolean onLongClick(View view) {

            int id = this.getLayoutPosition();
            onSelectionListener.onLongClick(images.get(id), view, id);
            return true;
        }

    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView header;

        HeaderViewHolder(View itemView) {

            super(itemView);
            header = itemView.findViewById(R.id.txt_header);
        }
    }

}
