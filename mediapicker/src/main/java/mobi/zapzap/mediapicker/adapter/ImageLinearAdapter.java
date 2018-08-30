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
import java.util.Collections;
import java.util.Comparator;

import mobi.zapzap.mediapicker.Constants;
import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.callbacks.SectionIndexer;
import mobi.zapzap.mediapicker.models.Image;
import mobi.zapzap.mediapicker.widget.HeaderItemDecoration;

/**
 * Created by Wade Morris on 2018/08/29.
 */
public class ImageLinearAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements HeaderItemDecoration.StickyHeaderInterface, SectionIndexer {

    private ArrayList<Image> images;
    private OnImageSelectionListener onSelectionListener;

    public ImageLinearAdapter(@NonNull ArrayList<Image> images) {
        this.images = images;
    }

    public ArrayList<Image> getItemList() {
        return images;
    }

    private void add(@NonNull Image image) {

        images.add(image);
        notifyItemInserted(images.size() - 1);
    }

    public void addAll(@NonNull ArrayList<Image> newImages) {
        for (Image image : newImages) {
            add(image);
        }
    }

    public void remove(@NonNull Image image) {

        int position = images.indexOf(image);
        if (position > -1) {
            images.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public Image getItem(int position){
        return images.get(position);
    }

    public void addOnSelectionListener(@NonNull OnImageSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    @Override
    public int getItemViewType(int position) {

        Image i = images.get(position);
        return (i.getContentPath().equalsIgnoreCase("")) ? Constants.VIEW_TYPE_HEADER : Constants.VIEW_TYPE_ITEM;
    }

    public void sortList(boolean assending) {

        if (assending) {

            Collections.sort(images, new Comparator<Image>() {
                @Override
                public int compare(Image a, Image b) {
                    return (a.getTimestamp() < b.getTimestamp()) ? -1 : ((a.getTimestamp() == b.getTimestamp()) ? 0 : 1);
                }
            });
        } else {

            Collections.sort(images, new Comparator<Image>() {
                @Override
                public int compare(Image a, Image b) {
                    return (b.getTimestamp() < a.getTimestamp()) ? -1 : ((b.getTimestamp() == a.getTimestamp()) ? 0 : 1);
                }
            });
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return images.get(position).getContentPath().hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == Constants.VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_row, parent, false));
        } else {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Image image = images.get(position);
        if (image != null) {

            if (holder instanceof ImageGridAdapter.ImageViewHolder) {

                ImageGridAdapter.ImageViewHolder imageViewHolder = (ImageGridAdapter.ImageViewHolder) holder;
                Uri uri = Uri.fromFile(new File(image.getPath()));
                if (uri != null) {

                    RequestOptions options = new RequestOptions().placeholder(R.color.zz_media_picker_default_placeholder).transform(new CenterCrop()).transform(new FitCenter());
                    Glide.with(holder.itemView).load(uri).apply(options).into(imageViewHolder.preview);
                }
                imageViewHolder.selection.setVisibility(image.isSelected() ? View.VISIBLE : View.GONE);
            } else if (holder instanceof ImageGridAdapter.HeaderViewHolder) {

                ImageGridAdapter.HeaderViewHolder headerViewHolder = (ImageGridAdapter.HeaderViewHolder) holder;
                headerViewHolder.header.setText(image.getHeaderDate());
            }
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
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

        ImageView preview;
        View selection;

        ImageViewHolder(@NonNull View itemView) {

            super(itemView);
            preview = itemView.findViewById(R.id.img_thumbnail);
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
