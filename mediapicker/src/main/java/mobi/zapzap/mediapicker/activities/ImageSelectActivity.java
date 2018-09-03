package mobi.zapzap.mediapicker.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
import java.util.concurrent.atomic.AtomicBoolean;

import mobi.zapzap.mediapicker.MediaPickerConstants;
import mobi.zapzap.mediapicker.MediaPickerUtil;
import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.adapter.ImageGridAdapter;
import mobi.zapzap.mediapicker.adapter.ImageLinearAdapter;
import mobi.zapzap.mediapicker.adapter.ImagesAdapter;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.models.Image;
import mobi.zapzap.mediapicker.widget.GridMarginDecoration;
import mobi.zapzap.mediapicker.widget.HeaderItemDecoration;

import static android.provider.MediaStore.Images.Media;
import static mobi.zapzap.mediapicker.R.anim.abc_fade_in;
import static mobi.zapzap.mediapicker.R.anim.abc_fade_out;

/**
 * Created by Zapper Development on 03-11-2016.
 */
public class ImageSelectActivity extends MediaPickerActivity {

    private ArrayList<Image> images;
    private String albumName;

    private TextView txtErrorDisplay;
    private TextView tvProfile;
    private TextView tvAdd;
    private TextView txtSelectCount;
    private LinearLayout liFinish;

    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private ImagesAdapter imagesAdapter;
    private RecyclerView imagesRecycler;

    private int countSelected = 0;

    private AtomicBoolean isMultiSelection;
    private AtomicBoolean sortAscending;
    private AtomicBoolean showListView;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private static final String[] PROJECTION = new String[]{
            Media._ID,
            Media.DISPLAY_NAME,
            Media.DATA,
            Media.DATE_ADDED
    };

    private final OnImageSelectionListener onSelectionListener = new OnImageSelectionListener() {

        @Override
        public void onClick(@NonNull Image img, @NonNull View view, int position) {

            if (isMultiSelection.get()) {

                toggleSelection(position);
                //actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                txtSelectCount.setText(countSelected + " " + getResources().getString(R.string.media_picker_selected_label));
                txtSelectCount.setVisibility(View.VISIBLE);
                tvAdd.setVisibility(View.VISIBLE);
                tvProfile.setVisibility(View.GONE);
                if (countSelected == 0) {
                    //actionMode.finish();
                    txtSelectCount.setVisibility(View.GONE);
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

            isMultiSelection.set(!isMultiSelection.get());
            toggleSelection(position);

            txtSelectCount.setText(getResources().getString(R.string.media_picker_selected_label, countSelected));
            txtSelectCount.setVisibility(View.VISIBLE);
            tvAdd.setVisibility(View.VISIBLE);
            tvProfile.setVisibility(View.GONE);
            if (countSelected == 0) {
                //actionMode.finish();
                txtSelectCount.setVisibility(View.GONE);
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

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        isMultiSelection = new AtomicBoolean(false);
        sortAscending = new AtomicBoolean(false);
        showListView = new AtomicBoolean(false);
        albumName = intent.getStringExtra(MediaPickerConstants.INTENT_EXTRA_ALBUM_NAME);
        isMultiSelection.set(intent.getBooleanExtra(MediaPickerConstants.INTENT_EXTRA_SELECTION_MODE, false));

        tvProfile = (TextView) findViewById(R.id.tvProfile);
        tvAdd = (TextView) findViewById(R.id.tvAdd);
        txtSelectCount = (TextView) findViewById(R.id.tvSelectCount);
        liFinish = (LinearLayout) findViewById(R.id.liFinish);
        txtErrorDisplay = (TextView) findViewById(R.id.text_view_error);
        txtErrorDisplay.setVisibility(View.INVISIBLE);

        imagesRecycler = (RecyclerView) findViewById(R.id.grid_view_image);
        imagesRecycler.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(ImageSelectActivity.this, MediaPickerConstants.IMAGE_GRID_SPAN_COUNT);
        linearLayoutManager = new LinearLayoutManager(ImageSelectActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        liFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txtSelectCount.getVisibility() == View.VISIBLE) {
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

    @Override @SuppressLint("HandlerLeak")
    protected void onStart() {

        super.onStart();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {

                    case MediaPickerConstants.PERMISSION_GRANTED: {
                        loadImages();
                        break;
                    }
                    case MediaPickerConstants.FETCH_STARTED: {
                        imagesRecycler.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case MediaPickerConstants.FETCH_COMPLETED: {
                        /*
                        If imageGridAdapter is null, this implies that the loaded images will be shown
                        for the first time, hence send FETCH_COMPLETED message.
                        However, if imageGridAdapter has been initialised, this thread was run either
                        due to the activity being restarted or content being changed.
                         */
                        if (imagesAdapter == null) {

                            if (showListView.get()) {

                                imagesAdapter = new ImageLinearAdapter(images);
                                imagesRecycler.setLayoutManager(linearLayoutManager);
                            } else {

                                imagesAdapter = new ImageGridAdapter(images);
                                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                                    @Override
                                    public int getSpanSize(int position) {

                                        if (imagesAdapter.getItemViewType(position) == MediaPickerConstants.VIEW_TYPE_HEADER) {
                                            return MediaPickerConstants.IMAGE_GRID_SPAN_COUNT;
                                        }
                                        return 1;
                                    }
                                });
                                imagesRecycler.setLayoutManager(gridLayoutManager);
                                imagesRecycler.addItemDecoration(new HeaderItemDecoration(ImageSelectActivity.this, imagesAdapter));
                                imagesRecycler.addItemDecoration(new GridMarginDecoration(ImageSelectActivity.this, 2, 2, 2, 2));
                            }
                            imagesAdapter.addOnSelectionListener(onSelectionListener);
                            imagesRecycler.setAdapter(imagesAdapter);
                            imagesRecycler.setVisibility(View.VISIBLE);
                        } else {

                            adapterDataSetChanged();
                            /* Some selected images may have been deleted hence update action mode title */
                            countSelected = msg.arg1;
                            //actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                            txtSelectCount.setText(getResources().getString(R.string.media_picker_selected_label, countSelected));
                            txtSelectCount.setVisibility(View.VISIBLE);
                            tvAdd.setVisibility(View.VISIBLE);
                            tvProfile.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case MediaPickerConstants.ERROR: {

                        txtErrorDisplay.setVisibility(View.VISIBLE);
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
        getContentResolver().registerContentObserver(Media.EXTERNAL_CONTENT_URI, false, observer);
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
        if (imagesAdapter != null) {
            //imageGridAdapter.releaseResources();
        }
        images = null;
        //imagesRecycler.setOnItemClickListener(null);
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
            sortAscending.set(!sortAscending.get());
            if (imagesRecycler != null) {

                imagesAdapter.sortList(sortAscending.get());
                return true;
            }
            return false;
        } else if (item.getItemId() == R.id.menu_image_list_view) {

            supportInvalidateOptionsMenu();
            showListView.set(!showListView.get());
            if (showListView.get()) {

                imagesAdapter = new ImageLinearAdapter(images);
                imagesRecycler.setLayoutManager(linearLayoutManager);
            } else {

                imagesAdapter = new ImageGridAdapter(images);
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                    @Override
                    public int getSpanSize(int position) {

                        if (imagesAdapter.getItemViewType(position) == MediaPickerConstants.VIEW_TYPE_HEADER) {
                            return MediaPickerConstants.IMAGE_GRID_SPAN_COUNT;
                        }
                        return 1;
                    }
                });
                imagesRecycler.setLayoutManager(gridLayoutManager);
            }
            imagesRecycler.setAdapter(imagesAdapter);
            return true;
        } else {
            return false;
        }
    }

    private void toggleSelection(int position) {

        if (images != null) {

            Image image = images.get(position);
            if (image != null) {

                if (!image.isSelected() && countSelected >= MediaPickerConstants.DEFAULT_SELECTION_LIMIT) {
                    Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.media_picker_selection_limit_exceeded), MediaPickerConstants.DEFAULT_SELECTION_LIMIT), Toast.LENGTH_SHORT).show();
                    return;
                }
                image.setSelected(!image.isSelected());
                if (image.isSelected()) {
                    countSelected++;
                } else {
                    countSelected--;
                }
                adapterDataSetChanged();
            }
        }
    }

    private void deselectAll() {

        tvProfile.setVisibility(View.VISIBLE);
        tvAdd.setVisibility(View.GONE);
        txtSelectCount.setVisibility(View.GONE);
        for (int i = 0, l = images.size(); i < l; i++) {
            images.get(i).setSelected(false);
        }
        countSelected = 0;
        adapterDataSetChanged();
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
        intent.putParcelableArrayListExtra(MediaPickerConstants.INTENT_EXTRA_LIST_IMAGES, getSelected());
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(abc_fade_in, abc_fade_out);
    }

    private void loadImages() {
        startThread(new ImageLoaderRunnable());
    }

    private void adapterDataSetChanged() {

        if (imagesAdapter != null) {
            imagesAdapter.notifyDataSetChanged();
        }
    }

    private class ImageLoaderRunnable implements Runnable {

        @Override
        public void run() {

            if (!TextUtils.isEmpty(albumName)) {

                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            /*
            If the imageGridAdapter is null, this is first time this activity's view is
            being shown, hence send FETCH_STARTED message to show progress bar
            while images are loaded from phone
             */
                if (imagesAdapter == null) {
                    sendMessage(MediaPickerConstants.FETCH_STARTED);
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
                Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, PROJECTION,
                        Media.BUCKET_DISPLAY_NAME + " =?", new String[]{albumName},
                        Media.DATE_ADDED);
                if (cursor == null) {
                    sendMessage(MediaPickerConstants.ERROR);
                    return;
                }
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
                        long id = cursor.getLong(cursor.getColumnIndex(PROJECTION[0]));
                        String name = cursor.getString(cursor.getColumnIndex(PROJECTION[1]));
                        String path = cursor.getString(cursor.getColumnIndex(PROJECTION[2]));
                        long capturedTimestamp = cursor.getLong(cursor.getColumnIndex(PROJECTION[3]));
                        Uri contentPath = Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(cursor.getColumnIndex(PROJECTION[0])));
                        boolean isSelected = selectedImages.contains(id);
                        if (isSelected) {
                            tempCountSelected++;
                        }
                        calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(capturedTimestamp);
                        String dateDifference = MediaPickerUtil.getDateDifference(ImageSelectActivity.this, calendar);
                        if (!header.equalsIgnoreCase("" + dateDifference)) {
                            header = "" + dateDifference;
                            temp.add(new Image(-1, "", dateDifference, "", "", "", capturedTimestamp, isSelected));
                        }
                        temp.add(new Image(id, header, name, contentPath.toString(), path, "", capturedTimestamp, isSelected));
                    } while (cursor.moveToPrevious());
                }
                cursor.close();
                if (images == null) {
                    images = new ArrayList<Image>();
                } else {
                    images.clear();
                }
                images.addAll(temp);
                sendMessage(MediaPickerConstants.FETCH_COMPLETED, tempCountSelected);
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
        sendMessage(MediaPickerConstants.PERMISSION_GRANTED);
    }

    @Override
    protected void hideViews() {

        if (imagesRecycler != null) {
            imagesRecycler.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {

        if (txtSelectCount.getVisibility() == View.VISIBLE) {
            deselectAll();
        } else {

            super.onBackPressed();
            overridePendingTransition(abc_fade_in, abc_fade_out);
            finish();
        }
    }

}
