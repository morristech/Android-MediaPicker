package mobi.zapzap.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import mobi.zapzap.mediapicker.activities.AlbumSelectActivity;
import mobi.zapzap.mediapicker.helpers.Constants;
import mobi.zapzap.mediapicker.models.Image;

public class MainActivity extends AppCompatActivity {

    private static final int READ_STORAGE_PERMISSION = 4000;
    private static final int LIMIT = 10;
    private ImageView imageView;
    private TextView txImageSelects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txImageSelects = (TextView) findViewById(R.id.txImageSelects);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!Helper.checkPermissionForExternalStorage(MainActivity.this)) {
                        Helper.requestStoragePermission(MainActivity.this, READ_STORAGE_PERMISSION);
                    } else {
                        // opining custom gallery
                        Intent intent = new Intent(MainActivity.this, AlbumSelectActivity.class);
                        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, LIMIT);
                        startActivityForResult(intent, Constants.REQUEST_CODE);
                    }
                }else{
                    Intent intent = new Intent(MainActivity.this, AlbumSelectActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, LIMIT);
                    startActivityForResult(intent, Constants.REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_LIST_IMAGES);
            for (int i = 0; i < images.size(); i++) {

                Uri uri = Uri.fromFile(new File(images.get(i).getPath()));
                RequestOptions options = new RequestOptions().placeholder(mobi.zapzap.mediapicker.R.color.colorAccent).override(400, 400).transform(new CenterCrop()).transform(new FitCenter());
                Glide.with(this).load(uri)
                        .apply(options)
                        .into(imageView);
                txImageSelects.setText(txImageSelects.getText().toString().trim()
                        + "\n" +
                        String.valueOf(i + 1) + ". " + String.valueOf(uri));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zapper")));
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
