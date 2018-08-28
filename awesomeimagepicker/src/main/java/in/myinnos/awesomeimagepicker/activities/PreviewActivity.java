package in.myinnos.awesomeimagepicker.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.text.emoji.widget.EmojiAppCompatEditText;
import android.support.text.emoji.widget.EmojiEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import in.myinnos.awesomeimagepicker.R;
import in.myinnos.awesomeimagepicker.adapter.ImagePreviewAdapter;
import in.myinnos.awesomeimagepicker.callbacks.OnSelectionListener;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;

import static in.myinnos.awesomeimagepicker.R.anim.abc_fade_in;
import static in.myinnos.awesomeimagepicker.R.anim.abc_fade_out;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";

    private ArrayList<Uri> imageUris;
    private ArrayList<Image> selectedImages;
    private ImagePreviewAdapter imageAdapter;
    private RecyclerView imageRecyclerView;
    private PhotoView imgPreview;
    private EmojiconEditText edtCaption;
    private ImageButton btnAdd;
    private FloatingActionButton btnSend;

    private Image currentImage;

    private final OnSelectionListener onSelectionListener = new OnSelectionListener() {

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
        }

    };

    public static Intent createIntent(@NonNull Context context, @NonNull Image image) {

        Intent intent = new Intent(context, PreviewActivity.class);
        intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGE, image);
        return intent;
    }

    public static Intent createIntent(@NonNull Context context, @NonNull ArrayList<Image> images) {

        Intent intent = new Intent(context, PreviewActivity.class);
        intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIST_IMAGES, images);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        if (getIntent() != null) {

            if (getIntent().hasExtra(ConstantsCustomGallery.INTENT_EXTRA_LIST_IMAGES)) {
                selectedImages = getIntent().getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_LIST_IMAGES);
            } else if (getIntent().hasExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGE)){

                selectedImages = new ArrayList<Image>();
                Image image = getIntent().getParcelableExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGE);
                if (image != null) {
                    selectedImages.add(image);
                }
            }
        }
        if (selectedImages != null && !selectedImages.isEmpty()) {

            imageUris = new ArrayList<Uri>();
            imageRecyclerView = (RecyclerView) findViewById(R.id.rv_selected_items);
            imgPreview = (PhotoView) findViewById(R.id.img_preview);
            edtCaption = (EmojiconEditText) findViewById(R.id.edt_caption);
            btnAdd = (ImageButton) findViewById(R.id.btn_add_attachment);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), AlbumSelectActivity.class);
                    intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, ConstantsCustomGallery.DEFAULT_LIMIT);
                    intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_MULTI_SELECTION, true);
                    startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
                }
            });
            btnSend = (FloatingActionButton) findViewById(R.id.btn_send_attachment);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntent();
                }
            });
            if (selectedImages.size() > 1) {

                imageRecyclerView.setVisibility(View.VISIBLE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                imageRecyclerView.setLayoutManager(linearLayoutManager);
                imageAdapter = new ImagePreviewAdapter(PreviewActivity.this);
                imageAdapter.addImageList(selectedImages);
                imageAdapter.addOnSelectionListener(onSelectionListener);
                imageRecyclerView.setAdapter(imageAdapter);
            } else {

                imageRecyclerView.setVisibility(View.GONE);
            }
            Image firstImage = selectedImages.get(0);
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
        intent.putParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_LIST_IMAGES, selectedImages);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(abc_fade_in, abc_fade_out);
    }

}
