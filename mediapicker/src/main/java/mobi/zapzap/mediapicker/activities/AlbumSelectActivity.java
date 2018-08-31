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
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import mobi.zapzap.mediapicker.MediaPickerConstants;
import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.adapter.AlbumGridAdapter;
import mobi.zapzap.mediapicker.callbacks.OnAlbumSelectionListener;
import mobi.zapzap.mediapicker.models.Album;
import mobi.zapzap.mediapicker.widget.GridMarginDecoration;

import static mobi.zapzap.mediapicker.R.anim.abc_fade_in;
import static mobi.zapzap.mediapicker.R.anim.abc_fade_out;

/**
 * Created by Zapper Development on 03-11-2016.
 */
public class AlbumSelectActivity extends MediaPickerActivity implements OnAlbumSelectionListener {

    private ArrayList<Album> albums;

    private TextView errorDisplay;
    private TextView tvProfile;
    private LinearLayout liFinish;

    private ActionBar actionBar;

    private RecyclerView gridView;
    private AlbumGridAdapter albumGridAdapter;
    private GridLayoutManager gridLayoutManager;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private int selectionLimit = MediaPickerConstants.DEFAULT_SELECTION_LIMIT;
    private boolean multiSelectEnabled = false;

    private static final String[] PROJECTION = new String[]{
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);
        setView(findViewById(R.id.layout_album_select));

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        selectionLimit = intent.getIntExtra(MediaPickerConstants.INTENT_EXTRA_LIMIT, MediaPickerConstants.DEFAULT_SELECTION_LIMIT);
        multiSelectEnabled = intent.getBooleanExtra(MediaPickerConstants.INTENT_EXTRA_SELECTION_MODE, false);
        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        tvProfile = (TextView) findViewById(R.id.tvProfile);
        tvProfile.setText(R.string.album_view);
        liFinish = (LinearLayout) findViewById(R.id.liFinish);

        gridView = (RecyclerView) findViewById(R.id.grid_view_album);
        gridView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(AlbumSelectActivity.this, MediaPickerConstants.ALBUM_GRID_SPAN_COUNT);

        liFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    case MediaPickerConstants.PERMISSION_GRANTED: {
                        loadAlbums();
                        break;
                    }
                    case MediaPickerConstants.FETCH_STARTED: {
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case MediaPickerConstants.FETCH_COMPLETED: {
                        if (albumGridAdapter == null) {

                            albumGridAdapter = new AlbumGridAdapter(AlbumSelectActivity.this);
                            albumGridAdapter.addAll(albums);
                            gridView.setLayoutManager(gridLayoutManager);
                            gridView.addItemDecoration(new GridMarginDecoration(AlbumSelectActivity.this, 2, 2, 2, 2));
                            gridView.setAdapter(albumGridAdapter);
                            gridView.setVisibility(View.VISIBLE);
                        } else {
                            albumGridAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                    case MediaPickerConstants.ERROR: {
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
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
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
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
        }
        albums = null;
        if (albumGridAdapter != null) {
            //adapter.releaseResources();
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        setResult(RESULT_CANCELED);
        overridePendingTransition(abc_fade_in, abc_fade_out);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MediaPickerConstants.REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private void loadAlbums() {
        startThread(new AlbumLoaderRunnable());
    }

    @Override
    public void onClick(@NonNull Album album, @NonNull View view, int position) {

        if (album.getName().equals(getResources().getString(R.string.capture_photo))) {
            //HelperClass.displayMessageOnScreen(getApplicationContext(), "HMM!", false);
        } else {

            Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
            intent.putExtra(MediaPickerConstants.INTENT_EXTRA_ALBUM_NAME, album.getName());
            intent.putExtra(MediaPickerConstants.INTENT_EXTRA_SELECTION_MODE, multiSelectEnabled);
            startActivityForResult(intent, MediaPickerConstants.REQUEST_CODE);
        }
    }

    private final class AlbumLoaderRunnable implements Runnable {

        @Override
        public void run() {

            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (albumGridAdapter == null) {
                sendMessage(MediaPickerConstants.FETCH_STARTED);
            }
            Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION, null, null, MediaStore.Images.Media.DATE_MODIFIED);
            if (cursor == null) {
                sendMessage(MediaPickerConstants.ERROR);
                return;
            }
            ArrayList<Album> temp = new ArrayList<>(cursor.getCount());
            HashSet<Long> albumSet = new HashSet<>();
            File file;
            if (cursor.moveToLast()) {

                do {
                    if (Thread.interrupted()) {
                        cursor.close();
                        return;
                    }
                    long albumId = cursor.getLong(cursor.getColumnIndex(PROJECTION[0]));
                    String album = cursor.getString(cursor.getColumnIndex(PROJECTION[1]));
                    String image = cursor.getString(cursor.getColumnIndex(PROJECTION[2]));
                    long albumTimestamp = cursor.getLong(cursor.getColumnIndex(PROJECTION[3]));
                    String displayDate = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date(albumTimestamp));
                    if (!albumSet.contains(albumId)) {
                        /*
                        It may happen that some image file paths are still present in cache,
                        though image file does not exist. These last as long as media
                        scanner is not run again. To avoid get such image file paths, check
                        if image file exists.
                         */
                        file = new File(image);
                        if (file.exists()) {

                            // TODO: 2018/08/28 Complete implementation
                            temp.add(new Album(album, image, displayDate, R.drawable.ic_folder));
                            /*if (!album.equals("Hiding particular folder")) {
                                temp.add(new Album(album, image));
                            }*/
                            albumSet.add(albumId);
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (albums == null) {
                albums = new ArrayList<>();
            }
            albums.clear();
            // adding taking photo from camera option!
            /*albums.add(new Album(getString(R.string.capture_photo),
                    "https://image.freepik.com/free-vector/flat-white-camera_23-2147490625.jpg"));*/
            albums.addAll(temp);

            sendMessage(MediaPickerConstants.FETCH_COMPLETED);
        }
    }

    private void startThread(Runnable runnable) {

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

        if (handler == null) {
            return;
        }
        Message message = handler.obtainMessage();
        message.what = what;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {

        Message message = handler.obtainMessage();
        message.what = MediaPickerConstants.PERMISSION_GRANTED;
        message.sendToTarget();
    }

    @Override
    protected void hideViews() {

        gridView.setVisibility(View.INVISIBLE);
    }

}
