package mobi.zapzap.mediapicker.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import mobi.zapzap.mediapicker.Constants;
import mobi.zapzap.mediapicker.R;
import mobi.zapzap.mediapicker.adapter.ImagePreviewAdapter;
import mobi.zapzap.mediapicker.callbacks.OnImageSelectionListener;
import mobi.zapzap.mediapicker.models.Image;

import static mobi.zapzap.mediapicker.R.anim.abc_fade_in;
import static mobi.zapzap.mediapicker.R.anim.abc_fade_out;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";

    private ArrayList<Image> images;
    private ImagePreviewAdapter imageAdapter;
    private RecyclerView imageRecyclerView;
    private PhotoView imgPreview;
    private EmojiconEditText edtCaption;
    private ImageButton btnAdd;
    private FloatingActionButton btnSend;

    private Image currentImage;

    private final OnImageSelectionListener onSelectionListener = new OnImageSelectionListener() {

        @Override
        public void onClick(@NonNull Image img, @NonNull View view, int position) {

            currentImage = img;
            if (edtCaption != null && edtCaption.getText() != null) {

                if (img.getCaption() != null) {
                    edtCaption.setText(img.getCaption());
                }
            }
            if (imgPreview != null) {

                Uri uri = Uri.fromFile(new File(img.getPath()));
                imgPreview.setImageURI(uri);
            }
        }

        @Override
        public void onLongClick(@NonNull Image img, @NonNull View view, int position) {
            //Do nothing..
        }

    };

    public static Intent createIntent(@NonNull Context context, @NonNull Image image) {

        Intent intent = new Intent(context, PreviewActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_IMAGE, image);
        return intent;
    }

    public static Intent createIntent(@NonNull Context context, @NonNull ArrayList<Image> images) {

        Intent intent = new Intent(context, PreviewActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIST_IMAGES, images);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_preview_selection, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_preview_delete) {

            if (currentImage != null) {

                if (images != null) {
                    images.remove(currentImage);
                }
                if (imageAdapter != null) {
                    imageAdapter.remove(currentImage);
                }
                return true;
            }
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        if (getIntent() != null) {

            if (getIntent().hasExtra(Constants.INTENT_EXTRA_LIST_IMAGES)) {
                this.images = getIntent().getParcelableArrayListExtra(Constants.INTENT_EXTRA_LIST_IMAGES);
            } else if (getIntent().hasExtra(Constants.INTENT_EXTRA_IMAGE)){

                this.images = new ArrayList<Image>();
                Image image = getIntent().getParcelableExtra(Constants.INTENT_EXTRA_IMAGE);
                if (image != null) {
                    images.add(image);
                }
            }
        }
        if (images != null && !images.isEmpty()) {

            imageRecyclerView = (RecyclerView) findViewById(R.id.rv_selected_items);
            imgPreview = (PhotoView) findViewById(R.id.img_preview);
            edtCaption = (EmojiconEditText) findViewById(R.id.edt_caption);
            btnAdd = (ImageButton) findViewById(R.id.btn_add_attachment);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), AlbumSelectActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT);
                    intent.putExtra(Constants.INTENT_EXTRA_MULTI_SELECTION, true);
                    startActivityForResult(intent, Constants.REQUEST_CODE);
                }
            });
            btnSend = (FloatingActionButton) findViewById(R.id.btn_send_attachment);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntent();
                }
            });
            if (images.size() > 1) {

                imageRecyclerView.setVisibility(View.VISIBLE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                imageRecyclerView.setLayoutManager(linearLayoutManager);
                imageAdapter = new ImagePreviewAdapter(images);
                imageAdapter.addOnSelectionListener(onSelectionListener);
                imageRecyclerView.setAdapter(imageAdapter);
            } else {

                imageRecyclerView.setVisibility(View.GONE);
            }
            Image firstImage = images.get(0);
            if (firstImage != null) {

                Uri uri = Uri.fromFile(new File(firstImage.getPath()));
                imgPreview.setImageURI(uri);
                if(firstImage.getCaption() != null) {
                    edtCaption.setText(firstImage.getCaption());
                }
            }
            edtCaption.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable input) {

                    if (currentImage != null && !TextUtils.isEmpty(input)) {
                        currentImage.setCaption(input.toString());
                    }
                }

            });
        }
    }

    private void sendIntent() {

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Constants.INTENT_EXTRA_LIST_IMAGES, images);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(abc_fade_in, abc_fade_out);
    }

}
