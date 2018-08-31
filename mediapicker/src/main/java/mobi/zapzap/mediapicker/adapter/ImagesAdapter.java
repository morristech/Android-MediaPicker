package mobi.zapzap.mediapicker.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.callbacks.SectionIndexer;
import mobi.zapzap.mediapicker.models.Image;
import mobi.zapzap.mediapicker.widget.HeaderItemDecoration;

/**
 * Created by Wade Morris on 2018/08/31.
 */
public abstract class ImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements HeaderItemDecoration.StickyHeaderInterface, SectionIndexer {

    protected ArrayList<Image> images;
    protected OnImageSelectionListener onSelectionListener;

    public ImagesAdapter() {
        this.images = new ArrayList<Image>();
    }

    public void addOnSelectionListener(@NonNull OnImageSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    public Image getItem(int position) {
        return images.get(position);
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

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    public void sortList(boolean assending) {

        if (assending) {

            Collections.sort(images, new Comparator<Image>() {
                @Override
                public int compare(Image a, Image b) {
                    return (a.getTimestamp() < b.getTimestamp()) ? -1 : (
                            (a.getTimestamp() == b.getTimestamp()) ? 0 : 1);
                }
            });
        } else {

            Collections.sort(images, new Comparator<Image>() {
                @Override
                public int compare(Image a, Image b) {
                    return (b.getTimestamp() < a.getTimestamp()) ? -1 : (
                            (b.getTimestamp() == a.getTimestamp()) ? 0 : 1);
                }
            });
        }
        notifyDataSetChanged();
    }

}
