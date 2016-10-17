package com.example.philip.gltest2;

import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
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

        originalButton.setOnClickListener(v -> mOpenGLRenderer.useCopyShader());

        Button sobelEdgeButton = (Button) findViewById(R.id.sobelEdge);

        sobelEdgeButton.setOnClickListener(v -> mOpenGLRenderer.useSobelEdgeShader());

        Button pickImageButton = (Button) findViewById(R.id.pickImage);
        pickImageButton.setOnClickListener(v -> {
            Intent gallery =
                    new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
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
