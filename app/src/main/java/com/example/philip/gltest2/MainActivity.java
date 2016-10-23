package com.example.philip.gltest2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private OpenGLPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pickImageButton = (Button) findViewById(R.id.pickImage);
        pickImageButton.setOnClickListener(v -> {
            Intent gallery =
                    new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, PICK_IMAGE);
        });


        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new OpenGLPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            mPagerAdapter.setImage(imageUri);
        }
    }

}
