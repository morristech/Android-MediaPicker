package mobi.zapzap.mediapicker.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import mobi.zapzap.mediapicker.Constants;
import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.models.Image;

/**
 * Created by Wade Morris on 2018/08/28.
 */
public class ImagePreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Image> images;
    private OnImageSelectionListener onSelectionListener;

    public ImagePreviewAdapter(@NonNull ArrayList<Image> images) {
        this.images = images;
    }

    public void addOnSelectionListener(@NonNull OnImageSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    private void add(@NonNull Image item) {

        images.add(item);
        notifyItemInserted(images.size() - 1);
    }

    public void addImageList(@NonNull ArrayList<Image> images) {

        for (Image image : images) {
            add(image);
        }
    }

    public void remove(@NonNull Image item) {

        int position = images.indexOf(item);
        if (position > -1) {
            images.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {

        while (getItemCount() > 0) {
            remove(getImage(0));
        }
    }

    public Image getImage(int position){
        return images.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item_image, parent, false);
        if (viewType == Constants.VIEW_TYPE_HEADER) {
            return new EmptyViewHolder(view);
        } else {
            return new ImageViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (TextUtils.isEmpty(images.get(position).getContentPath())) ? Constants.VIEW_TYPE_HEADER : Constants.VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Image image = getImage(position);
        if (image != null) {

            if (holder instanceof ImageViewHolder) {

                ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                RequestOptions options = new RequestOptions().placeholder(R.color.zz_media_picker_default_placeholder).transform(new CenterCrop()).transform(new FitCenter());
                Glide.with(imageViewHolder.itemView).load(image.getContentPath()).apply(options).into(imageViewHolder.preview);
            } else {

                EmptyViewHolder noneHolder = (EmptyViewHolder) holder;
                noneHolder.itemView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnFocusChangeListener {

        ImageView preview;
        View selection;

        ImageViewHolder(@NonNull View itemView) {

            super(itemView);
            preview = itemView.findViewById(R.id.img_thumbnail);
            selection = itemView.findViewById(R.id.selection_alpha);
            itemView.setOnClickListener(this);
            itemView.setOnFocusChangeListener(this);
        }

        @Override
        public void onClick(View view) {

            int id = this.getLayoutPosition();
            if (onSelectionListener != null) {
                onSelectionListener.onClick(images.get(id), view, id);
            }
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {

            if (selection != null) {
                selection.setVisibility(hasFocus ? View.GONE : View.VISIBLE);
            }
        }

    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

}
