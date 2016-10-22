package com.example.philip.gltest2;

import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private GLSurfaceView mGLSurfaceView;
    private OpenGLRenderer mOpenGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_main);

        mOpenGLRenderer = new OpenGLRenderer(this);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(mOpenGLRenderer);

        layout.addView(mGLSurfaceView);

        Button originalButton = (Button) findViewById(R.id.original);
        Button sobelEdgeButton = (Button) findViewById(R.id.sobelEdge);

        originalButton.setOnClickListener(v -> mOpenGLRenderer.useCopyShader());
        sobelEdgeButton.setOnClickListener(v -> mOpenGLRenderer.useSobelEdgeShader());

        Button pickImageButton = (Button) findViewById(R.id.pickImage);
        pickImageButton.setOnClickListener(v -> {
            Intent gallery =
                    new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, PICK_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            mOpenGLRenderer.setImage(imageUri);
        }
    }
}
