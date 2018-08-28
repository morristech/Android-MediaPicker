package in.myinnos.awesomeimagepicker.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import in.myinnos.awesomeimagepicker.Utility;
import in.myinnos.awesomeimagepicker.R;
import in.myinnos.awesomeimagepicker.adapter.MainImageAdapter;
import in.myinnos.awesomeimagepicker.callbacks.OnSelectionListener;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.helpers.HeaderItemDecoration;
import in.myinnos.awesomeimagepicker.helpers.ItemOffsetDecoration;
import in.myinnos.awesomeimagepicker.models.Image;

import static in.myinnos.awesomeimagepicker.R.anim.abc_fade_in;
import static in.myinnos.awesomeimagepicker.R.anim.abc_fade_out;

/**
 * Created by MyInnos on 03-11-2016.
 */
public class ImageSelectActivity extends HelperActivity {

    private ArrayList<Image> images;
    private String albumName;

    private TextView errorDisplay;
    private TextView tvProfile;
    private TextView tvAdd;
    private TextView tvSelectCount;
    private LinearLayout liFinish;

    private ProgressBar loader;
    //private GridView gridView;
    //private CustomImageSelectAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView gridView;
    private MainImageAdapter adapter;

    private int countSelected;
    private boolean multiSelect = false;
    private boolean sortAssending = false;
    private boolean showListView = false;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN};

    private final OnSelectionListener onSelectionListener = new OnSelectionListener() {

        @Override
        public void onClick(@NonNull Image img, @NonNull View view, int position) {

            if (multiSelect) {

                toggleSelection(position);
                //actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                tvSelectCount.setText(countSelected + " " + getResources().getString(R.string.selected));
                tvSelectCount.setVisibility(View.VISIBLE);
                tvAdd.setVisibility(View.VISIBLE);
                tvProfile.setVisibility(View.GONE);
                if (countSelected == 0) {
                    //actionMode.finish();
                    tvSelectCount.setVisibility(View.GONE);
                    tvAdd.setVisibility(View.GONE);
                    tvProfile.setVisibility(View.VISIBLE);
                }
            } else {
                // TODO: 2018/08/28
            }
        }

        @Override
        public void onLongClick(@NonNull Image img, @NonNull View view, int position) {

            multiSelect = !multiSelect;
            toggleSelection(position);
            //actionMode.setTitle(countSelected + " " + getString(R.string.selected));
            tvSelectCount.setText(countSelected + " " + getResources().getString(R.string.selected));
            tvSelectCount.setVisibility(View.VISIBLE);
            tvAdd.setVisibility(View.VISIBLE);
            tvProfile.setVisibility(View.GONE);
            if (countSelected == 0) {
                //actionMode.finish();
                tvSelectCount.setVisibility(View.GONE);
                tvAdd.setVisibility(View.GONE);
                tvProfile.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        setView(findViewById(R.id.layout_image_select));

        tvProfile = (TextView) findViewById(R.id.tvProfile);
        tvAdd = (TextView) findViewById(R.id.tvAdd);
        tvSelectCount = (TextView) findViewById(R.id.tvSelectCount);
        tvProfile.setText(R.string.image_view);
        liFinish = (LinearLayout) findViewById(R.id.liFinish);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        albumName = intent.getStringExtra(ConstantsCustomGallery.INTENT_EXTRA_ALBUM_NAME);
        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        loader = (ProgressBar) findViewById(R.id.loader);
//        gridView = (GridView) findViewById(R.id.grid_view_image_select);
        gridView = (RecyclerView) findViewById(R.id.grid_view_image_select);
        gridLayoutManager = new GridLayoutManager(ImageSelectActivity.this, MainImageAdapter.SPAN_COUNT);
        linearLayoutManager = new LinearLayoutManager(ImageSelectActivity.this);

        liFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tvSelectCount.getVisibility() == View.VISIBLE) {
                    deselectAll();
                } else {
                    finish();
                    overridePendingTransition(abc_fade_in, abc_fade_out);
                }
            }
        });

        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendIntent();
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {

                    case ConstantsCustomGallery.PERMISSION_GRANTED: {
                        loadImages();
                        break;
                    }
                    case ConstantsCustomGallery.FETCH_STARTED: {
                        loader.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case ConstantsCustomGallery.FETCH_COMPLETED: {
                        /*
                        If adapter is null, this implies that the loaded images will be shown
                        for the first time, hence send FETCH_COMPLETED message.
                        However, if adapter has been initialised, this thread was run either
                        due to the activity being restarted or content being changed.
                         */
                        if (adapter == null) {
                            //adapter = new CustomImageSelectAdapter(ImageSelectActivity.this, getApplicationContext(), images);
                            //gridView.setAdapter(adapter);
                            adapter = new MainImageAdapter(ImageSelectActivity.this);
                            adapter.addImageList(images);
                            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                @Override
                                public int getSpanSize(int position) {

                                    if (adapter.getItemViewType(position) == MainImageAdapter.HEADER) {
                                        return MainImageAdapter.SPAN_COUNT;
                                    }
                                    return 1;
                                }
                            });
                            gridView.setLayoutManager(gridLayoutManager);
                            adapter.addOnSelectionListener(onSelectionListener);
                            gridView.setAdapter(adapter);
                            gridView.addItemDecoration(new HeaderItemDecoration(ImageSelectActivity.this, gridView, adapter));
                            gridView.addItemDecoration(new ItemOffsetDecoration(ImageSelectActivity.this, R.dimen.image_grid_item_offset));

                            loader.setVisibility(View.GONE);
                            gridView.setVisibility(View.VISIBLE);
                        } else {

                            adapter.notifyDataSetChanged();
                            /*
                            Some selected images may have been deleted
                            hence update action mode title
                             */
                            countSelected = msg.arg1;
                            //actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                            tvSelectCount.setText(countSelected + " " + getString(R.string.selected));
                            tvSelectCount.setVisibility(View.VISIBLE);
                            tvAdd.setVisibility(View.VISIBLE);
                            tvProfile.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case ConstantsCustomGallery.ERROR: {

                        loader.setVisibility(View.GONE);
                        errorDisplay.setVisibility(View.VISIBLE);
                        break;
                    }
                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                loadImages();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
        checkPermission();
    }

    @Override
    protected void onStop() {

        super.onStop();
        stopThread();
        getContentResolver().unregisterContentObserver(observer);
        observer = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        images = null;
        if (adapter != null) {
            //adapter.releaseResources();
        }
        //gridView.setOnItemClickListener(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {

        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        if (adapter != null) {
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 3 : metrics.widthPixels / 5;
            //adapter.setLayoutParams(size);
        }
        if (!showListView) {
            gridLayoutManager.setSpanCount(orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_image_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_image_select_sort) {
            //invalidateOptionsMenu();
            if (adapter != null) {

                sortAssending = !sortAssending;
                adapter.sortList(sortAssending);
                return true;
            }
            return false;
        } else if (item.getItemId() == R.id.menu_image_list_view) {

            if (gridView != null) {

                showListView = !showListView;
                adapter.showListView(showListView);
                gridView.setLayoutManager(showListView ? linearLayoutManager : gridLayoutManager);
                gridView.setAdapter(adapter);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private void toggleSelection(int position) {

        if (!images.get(position).isSelected() && countSelected >= ConstantsCustomGallery.limit) {
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.limit_exceeded), ConstantsCustomGallery.DEFAULT_LIMIT), Toast.LENGTH_SHORT) .show();
            return;
        }
        images.get(position).setSelected(!images.get(position).isSelected());
        if (images.get(position).isSelected()) {
            countSelected++;
        } else {
            countSelected--;
        }
        adapter.notifyDataSetChanged();
    }

    private void deselectAll() {

        tvProfile.setVisibility(View.VISIBLE);
        tvAdd.setVisibility(View.GONE);
        tvSelectCount.setVisibility(View.GONE);
        for (int i = 0, l = images.size(); i < l; i++) {
            images.get(i).setSelected(false);
        }
        countSelected = 0;
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Image> getSelected() {

        ArrayList<Image> selectedImages = new ArrayList<>();
        for (int i = 0, l = images.size(); i < l; i++) {
            if (images.get(i).isSelected()) {
                selectedImages.add(images.get(i));
            }
        }
        return selectedImages;
    }

    private void sendIntent() {

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES, getSelected());
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(abc_fade_in, abc_fade_out);
    }

    private void loadImages() {
        startThread(new ImageLoaderRunnable());
    }

    private class ImageLoaderRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            /*
            If the adapter is null, this is first time this activity's view is
            being shown, hence send FETCH_STARTED message to show progress bar
            while images are loaded from phone
             */
            if (adapter == null) {
                sendMessage(ConstantsCustomGallery.FETCH_STARTED);
            }
            File file;
            HashSet<Long> selectedImages = new HashSet<>();
            if (images != null) {

                Image image;
                for (int i = 0, l = images.size(); i < l; i++) {
                    image = images.get(i);
                    file = new File(image.getPath());
                    if (file.exists() && image.isSelected()) {
                        selectedImages.add(image.getId());
                    }
                }
            }
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{albumName}, MediaStore.Images.Media.DATE_ADDED);
            if (cursor == null) {
                sendMessage(ConstantsCustomGallery.ERROR);
                return;
            }

            /*
            In case this runnable is executed to onChange calling loadImages,
            using countSelected variable can result in a race condition. To avoid that,
            tempCountSelected keeps track of number of selected images. On handling
            FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
             */
            int tempCountSelected = 0;
            ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
            String header = "";
            Calendar calendar;
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        cursor.close();
                        return;
                    }
                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    long capturedTimestamp = cursor.getLong(cursor.getColumnIndex(projection[3]));
                    Uri contentPath = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(cursor.getColumnIndex(projection[0])));
                    boolean isSelected = selectedImages.contains(id);
                    if (isSelected) {
                        tempCountSelected++;
                    }
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(capturedTimestamp);
                    String dateDifference = Utility.getDateDifference(ImageSelectActivity.this, calendar);
                    if (!header.equalsIgnoreCase("" + dateDifference)) {
                        header = "" + dateDifference;
                        temp.add(new Image(-1, "", dateDifference, "", "", capturedTimestamp, isSelected));
                    }
                    temp.add(new Image(id, header, name, contentPath.toString(), path, capturedTimestamp, isSelected));
                } while (cursor.moveToPrevious());
            }
            cursor.close();
            if (images == null) {
                images = new ArrayList<>();
            } else {
                images.clear();
            }
            images.addAll(temp);
            sendMessage(ConstantsCustomGallery.FETCH_COMPLETED, tempCountSelected);
        }
    }

    private void startThread(@NonNull Runnable runnable) {

        stopThread();
        thread = new Thread(runnable);
        thread.start();
    }

    private void stopThread() {

        if (thread == null || !thread.isAlive()) {
            return;
        }
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int what) {
        sendMessage(what, 0);
    }

    private void sendMessage(int what, int arg1) {

        if (handler == null) {
            return;
        }
        Message message = handler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {
        sendMessage(ConstantsCustomGallery.PERMISSION_GRANTED);
    }

    @Override
    protected void hideViews() {

        loader.setVisibility(View.GONE);
        gridView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {

        if (tvSelectCount.getVisibility() == View.VISIBLE) {
            deselectAll();
        } else {
            super.onBackPressed();
            overridePendingTransition(abc_fade_in, abc_fade_out);
            finish();
        }
    }

}
