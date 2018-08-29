package mobi.zapzap.mediapicker.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.Constants;
import mobi.zapzap.mediapicker.models.Image;

/**
 * Created by Wade Morris on 2018/08/28.
 */
public class ImagePreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Image> images;
    private OnImageSelectionListener onSelectionListener;
    private RequestManager glide;
    private RequestOptions options;

    public ImagePreviewAdapter(@NonNull Context context) {

        this.context = context;
        this.images = new ArrayList<>();
        options = new RequestOptions().transform(new CenterCrop()).transform(new FitCenter());
        glide = Glide.with(context);
        glide.applyDefaultRequestOptions(options);
    }

    public void addOnSelectionListener(OnImageSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    public ImagePreviewAdapter addImage(@NonNull Image image) {

        images.add(image);
        notifyDataSetChanged();
        return this;
    }

    public ArrayList<Image> getItemList() {
        return images;
    }

    public void addImageList(@NonNull ArrayList<Image> images) {

        this.images.addAll(images);
        notifyDataSetChanged();
    }

    public void clearList() {
        images.clear();
    }

    public void select(boolean selection, int pos) {

        if (pos < 100) {

            images.get(pos).setSelected(selection);
            notifyItemChanged(pos);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == Constants.VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()). inflate(R.layout.preview_item_image, parent, false);
            return new HolderNone(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()). inflate(R.layout.preview_item_image, parent, false);
            return new Holder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (TextUtils.isEmpty(images.get(position).getContentPath())) ? Constants.VIEW_TYPE_HEADER : Constants.VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Image image = images.get(position);
        if (holder instanceof Holder) {

            Holder imageHolder = (Holder) holder;
            glide.load(image.getContentPath()).into(imageHolder.preview);
            imageHolder.selection.setVisibility(image.isSelected() ? View.GONE : View.VISIBLE);
        } else {

            HolderNone noneHolder = (HolderNone) holder;
            noneHolder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView preview;
        ImageView selection;

        Holder(View itemView) {

            super(itemView);
            preview = itemView.findViewById(R.id.img_thumbnail);
            selection = itemView.findViewById(R.id.img_selection);
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

    public static class HolderNone extends RecyclerView.ViewHolder {
        HolderNone(View itemView) {
            super(itemView);
        }
    }

}
