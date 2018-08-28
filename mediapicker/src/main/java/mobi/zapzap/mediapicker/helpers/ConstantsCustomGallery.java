package mobi.zapzap.mediapicker.helpers;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by MyInnos on 03-11-2016.
 */
public class ConstantsCustomGallery {
    public static final int PERMISSION_REQUEST_CODE = 1000;
    public static final int PERMISSION_GRANTED = 1001;
    public static final int PERMISSION_DENIED = 1002;

    public static final int REQUEST_CODE = 2000;
    public static final int IMAGE_SELECTION_MODE_SINGLE = 80807;
    public static final int IMAGE_SELECTION_MODE_MULTIPLE = 80808;

    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;
    public static final int ERROR = 2005;

    /**
     * Request code for permission has to be < (1 << 8)
     * Otherwise throws java.lang.IllegalArgumentException: Can only use lower 8 bits for requestCode
     */
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 23;

    public static final String INTENT_EXTRA_ALBUM_NAME = "album";
    public static final String INTENT_EXTRA_IMAGE = "image";
    public static final String INTENT_EXTRA_LIST_IMAGES = "list_images";
    public static final String INTENT_EXTRA_LIMIT = "limit";
    public static final String INTENT_EXTRA_MULTI_SELECTION = "selection_mode";
    public static final int DEFAULT_LIMIT = 10;

    //Maximum number of images that can be selected at a time
    public static int limit;

    public static final int sScrollbarAnimDuration = 300;
    public static String[] PROJECTION = new String[]{
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
    };
    public static Uri URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static String ORDERBY = MediaStore.Images.Media.DATE_TAKEN + " DESC";

}
