package mobi.zapzap.mediapicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.callbacks.SectionIndexer;
import mobi.zapzap.mediapicker.Constants;
import mobi.zapzap.mediapicker.widget.HeaderItemDecoration;
import mobi.zapzap.mediapicker.models.Image;

/**
 * Created by Wade Morris on 2018/08/27.
 */
public class ImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements HeaderItemDecoration.StickyHeaderInterface, SectionIndexer {

    private ArrayList<Image> list;
    private OnImageSelectionListener onSelectionListener;
    private RequestManager glide;

    public ImageGridAdapter(@NonNull Context context, @NonNull OnImageSelectionListener onSelectionListener) {

        this.list = new ArrayList<>();
        this.onSelectionListener = onSelectionListener;
        RequestOptions options = new RequestOptions().placeholder(R.color.colorAccent).transform(new CenterCrop()).transform(new FitCenter());
        glide = Glide.with(context);
        glide.applyDefaultRequestOptions(options);
    }

    public ArrayList<Image> getItemList() {
        return list;
    }

    public ImageGridAdapter addImage(@NonNull Image image) {

        list.add(image);
        notifyDataSetChanged();
        return this;
    }

    public void addOnSelectionListener(@NonNull OnImageSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    public void addImageList(@NonNull ArrayList<Image> images) {

        list.addAll(images);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        Image i = list.get(position);
        return (i.getContentPath().equalsIgnoreCase("")) ? Constants.VIEW_TYPE_HEADER : Constants.VIEW_TYPE_ITEM;
    }

    public void clearList() {
        list.clear();
    }

    public void select(boolean selection, int pos) {

        list.get(pos).setSelected(selection);
        notifyItemChanged(pos);
    }

    public void sortList(boolean assending) {

        if (assending) {

            Collections.sort(list, new Comparator<Image>() {

                @Override
                public int compare(Image a, Image b) {

                    return (a.getTimestamp() < b.getTimestamp()) ? -1 : (
                            (a.getTimestamp() == b.getTimestamp()) ? 0 : 1);
                }
            });
        } else {

            Collections.sort(list, new Comparator<Image>() {

                @Override
                public int compare(Image a, Image b) {

                    return (b.getTimestamp() < a.getTimestamp()) ? -1 : (
                            (b.getTimestamp() == a.getTimestamp()) ? 0 : 1);
                }
            });
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {

        return list.get(position).getContentPath().hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == Constants.VIEW_TYPE_HEADER) {
            return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_row, parent, false));
        } else {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Image image = list.get(position);
        if (holder instanceof Holder) {

            Holder imageHolder = (Holder) holder;
            Uri uri = Uri.fromFile(new File(image.getPath()));
            glide.load(uri).into(imageHolder.preview);
            imageHolder.selection.setVisibility(image.isSelected() ? View.VISIBLE : View.GONE);
        } else if (holder instanceof HeaderHolder) {

            HeaderHolder headerHolder = (HeaderHolder) holder;
            headerHolder.header.setText(image.getHeaderDate());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
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

        Image image = list.get(headerPosition);
        ((TextView) header.findViewById(R.id.txt_header)).setText(image.getHeaderDate());
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return getItemViewType(itemPosition) == 1;
    }

    @Override
    public String getSectionText(int position) {
        return list.get(position).getHeaderDate();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView preview;
        View selection;

        Holder(View itemView) {

            super(itemView);
            preview = itemView.findViewById(R.id.img_thumbnail);
            selection = itemView.findViewById(R.id.img_alpha);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            //preview.setLayoutParams(layoutParams);
        }

        @Override
        public void onClick(View view) {

            int id = this.getLayoutPosition();
            onSelectionListener.onClick(list.get(id), view, id);
        }

        @Override
        public boolean onLongClick(View view) {

            int id = this.getLayoutPosition();
            onSelectionListener.onLongClick(list.get(id), view, id);
            return true;
        }
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {

        TextView header;

        HeaderHolder(View itemView) {

            super(itemView);
            header = itemView.findViewById(R.id.txt_header);
        }
    }

}
