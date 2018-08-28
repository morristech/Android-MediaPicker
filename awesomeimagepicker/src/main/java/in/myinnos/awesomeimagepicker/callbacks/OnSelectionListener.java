package in.myinnos.awesomeimagepicker.callbacks;

import android.support.annotation.NonNull;
import android.view.View;

import in.myinnos.awesomeimagepicker.models.Image;

/**
 * Created by Wade Morris on 2018/08/27.
 */
public interface OnSelectionListener {
    void onClick(@NonNull Image img, @NonNull View view, int position);

    void onLongClick(@NonNull Image img, @NonNull View view, int position);
}
