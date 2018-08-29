package mobi.zapzap.mediapicker.activities;

import android.content.Intent;
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
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.Utility;
import mobi.zapzap.mediapicker.adapter.ImageGridAdapter;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.Constants;
import mobi.zapzap.mediapicker.models.Image;
import mobi.zapzap.mediapicker.widget.GridMarginDecoration;
import mobi.zapzap.mediapicker.widget.HeaderItemDecoration;

import static mobi.zapzap.mediapicker.R.anim.abc_fade_in;
import static mobi.zapzap.mediapicker.R.anim.abc_fade_out;

/**
 * Created by Zapper Development on 03-11-2016.
 */
public class ImageSelectActivity extends HelperActivity {

    private ArrayList<Image> images;
    private String albumName;

    private TextView errorDisplay;
    private TextView tvProfile;
    private TextView tvAdd;
    private TextView tvSelectCount;
    private LinearLayout liFinish;

    private GridLayoutManager gridLayoutManager;
    private RecyclerView gridView;
    private ImageGridAdapter adapter;

    private int countSelected;
    private boolean multiSelectEnabled = false;
    private boolean sortAscending = false;
    private boolean showListView = false;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private final String[] projection = new String[]{MediaStore.Images.Media._ID,
                                                     MediaStore.Images.Media.DISPLAY_NAME,
                                                     MediaStore.Images.Media.DATA,
                                                     MediaStore.Images.Media.DATE_TAKEN};

    private final OnImageSelectionListener onSelectionListener = new OnImageSelectionListener() {

        @Override
        public void onClick(@NonNull Image img, @NonNull View view, int position) {

            if (multiSelectEnabled) {

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

                startActivity(PreviewActivity.createIntent(view.getContext(), img));
                finish();
                overridePendingTransition(abc_fade_in, abc_fade_out);
            }
        }

        @Override
        public void onLongClick(@NonNull Image img, @NonNull View view, int position) {

            multiSelectEnabled = !multiSelectEnabled;
            toggleSelection(position);

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

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        albumName = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM_NAME);
        multiSelectEnabled = intent.getBooleanExtra(Constants.INTENT_EXTRA_MULTI_SELECTION, false);
        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        gridView = (RecyclerView) findViewById(R.id.grid_view_image);
        gridView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(ImageSelectActivity.this, Constants.IMAGE_GRID_SPAN_COUNT);

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
                //sendIntent();
                startActivity(PreviewActivity.createIntent(v.getContext(), getSelected()));
                finish();
                overridePendingTransition(abc_fade_in, abc_fade_out);
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

                    case Constants.PERMISSION_GRANTED: {
                        loadImages();
                        break;
                    }
                    case Constants.FETCH_STARTED: {
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case Constants.FETCH_COMPLETED: {
                        /*
                        If adapter is null, this implies that the loaded images will be shown
                        for the first time, hence send FETCH_COMPLETED message.
                        However, if adapter has been initialised, this thread was run either
                        due to the activity being restarted or content being changed.
                         */
                        if (adapter == null) {

                            adapter = new ImageGridAdapter(ImageSelectActivity.this, onSelectionListener);
                            adapter.addImageList(images);
                            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                                @Override
                                public int getSpanSize(int position) {

                                    if (adapter.getItemViewType(position) == Constants.VIEW_TYPE_HEADER) {
                                        return Constants.IMAGE_GRID_SPAN_COUNT;
                                    }
                                    return 1;
                                }
                            });
                            gridView.setLayoutManager(gridLayoutManager);
                            gridView.addItemDecoration(new HeaderItemDecoration(ImageSelectActivity.this, adapter));
                            gridView.addItemDecoration(new GridMarginDecoration(ImageSelectActivity.this, 2, 2, 2, 2));
                            gridView.setAdapter(adapter);
                            gridView.setVisibility(View.VISIBLE);
                        } else {

                            adapter.notifyDataSetChanged();
                            /* Some selected images may have been deleted hence update action mode title */
                            countSelected = msg.arg1;
                            //actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                            tvSelectCount.setText(countSelected + " " + getString(R.string.selected));
                            tvSelectCount.setVisibility(View.VISIBLE);
                            tvAdd.setVisibility(View.VISIBLE);
                            tvProfile.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case Constants.ERROR: {

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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_image_selection, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_image_select_sort) {
            supportInvalidateOptionsMenu();
            if (adapter != null) {

                sortAscending = !sortAscending;
                adapter.sortList(sortAscending);
                return true;
            }
            return false;
        } else if (item.getItemId() == R.id.menu_image_list_view) {
            supportInvalidateOptionsMenu();
            if (gridView != null) {

                showListView = !showListView;
                gridView.setLayoutManager(gridLayoutManager);
                gridView.setAdapter(adapter);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private void toggleSelection(int position) {

        if (!images.get(position).isSelected() && countSelected >= Constants.limit) {
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.limit_exceeded), Constants.DEFAULT_LIMIT), Toast.LENGTH_SHORT).show();
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
        intent.putParcelableArrayListExtra(Constants.INTENT_EXTRA_LIST_IMAGES, getSelected());
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

            if (!TextUtils.isEmpty(albumName)) {

                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            /*
            If the adapter is null, this is first time this activity's view is
            being shown, hence send FETCH_STARTED message to show progress bar
            while images are loaded from phone
             */
                if (adapter == null) {
                    sendMessage(Constants.FETCH_STARTED);
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
                    sendMessage(Constants.ERROR);
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
                            temp.add(new Image(-1, "", dateDifference, "", "", "", capturedTimestamp, isSelected));
                        }
                        temp.add(new Image(id, header, name, contentPath.toString(), path, "", capturedTimestamp, isSelected));
                    } while (cursor.moveToPrevious());
                }
                cursor.close();
                if (images == null) {
                    images = new ArrayList<>();
                } else {
                    images.clear();
                }
                images.addAll(temp);
                sendMessage(Constants.FETCH_COMPLETED, tempCountSelected);
            }
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

        sendMessage(Constants.PERMISSION_GRANTED);
    }

    @Override
    protected void hideViews() {
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
