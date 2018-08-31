package mobi.zapzap.mediapicker.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.byteunits.DecimalByteUnit;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import mobi.zapzap.mediapicker.MediaPickerConstants;
import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.callbacks.SectionIndexer;
import mobi.zapzap.mediapicker.models.Image;
import mobi.zapzap.mediapicker.widget.HeaderItemDecoration;

/**
 * Created by Wade Morris on 2018/08/29.
 */
public class ImageLinearAdapter extends ImagesAdapter implements HeaderItemDecoration.StickyHeaderInterface, SectionIndexer {

    private static final String TAG = "ImageLinearAdapter";

    public ImageLinearAdapter(@NonNull ArrayList<Image> images) {
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
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_row, parent, false));
        } else {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Image image = images.get(position);
        if (image != null) {

            if (holder instanceof ImageLinearAdapter.ImageViewHolder) {

                ImageLinearAdapter.ImageViewHolder imageViewHolder = (ImageLinearAdapter.ImageViewHolder) holder;
                // TODO: 2018/08/31 Handle file references for all supported versions of android
                File imageFile = new File(image.getPath());
                Uri uri = Uri.fromFile(imageFile);
                if (uri != null) {

                    String fileSize = DecimalByteUnit.format(imageFile.length());
                    String displayDate = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date(image.getTimestamp()));
                    Log.d(TAG, "onBindViewHolder: File - Name: " + imageFile.getName() + ", Size: " + fileSize);
                    imageViewHolder.txtName.setText(imageFile.getName());
                    imageViewHolder.txtDateAdded.setText(displayDate);
                    imageViewHolder.txtSizeOnDisk.setText(fileSize);
                    RequestOptions options = new RequestOptions().placeholder(R.color.zz_media_picker_default_placeholder).transform(new CenterCrop()).transform(new FitCenter());
                    Glide.with(holder.itemView).load(uri).apply(options).into(imageViewHolder.imgThumbnail);
                }
                imageViewHolder.selection.setVisibility(image.isSelected() ? View.VISIBLE : View.GONE);
            } else if (holder instanceof ImageLinearAdapter.HeaderViewHolder) {

                ImageLinearAdapter.HeaderViewHolder headerViewHolder = (ImageLinearAdapter.HeaderViewHolder) holder;
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
        return R.layout.header_row;
    }

    @Override
    public void bindHeaderData(@NonNull View header, int headerPosition) {

        Image image = images.get(headerPosition);
        ((TextView) header.findViewById(R.id.txt_header)).setText(image.getHeaderDate());
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return getItemViewType(itemPosition) == 1;
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
        TextView txtName;
        TextView txtDateAdded;
        TextView txtSizeOnDisk;
        View selection;

        ImageViewHolder(@NonNull View itemView) {

            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            txtName = (TextView) itemView.findViewById(R.id.txt_image_name);
            txtDateAdded = (TextView) itemView.findViewById(R.id.txt_image_date_added);
            txtSizeOnDisk = (TextView) itemView.findViewById(R.id.txt_image_size);
            selection = itemView.findViewById(R.id.img_alpha);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int id = this.getLayoutPosition();
            onSelectionListener.onClick(images.get(id), view, id);
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
