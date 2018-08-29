package mobi.zapzap.mediapicker.callbacks;

import android.support.annotation.NonNull;
import android.view.View;

import mobi.zapzap.mediapicker.models.Album;

/**
 * Created by Wade Morris on 2018/08/29.
 */
public interface OnAlbumSelectionListener {

    void onClick(@NonNull Album album, @NonNull View view, int position);

}
