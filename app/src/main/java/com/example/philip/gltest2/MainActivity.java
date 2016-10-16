package com.example.philip.gltest2;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private OpenGLRenderer mOpenGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = (LinearLayout)findViewById(R.id.activity_main);

        mOpenGLRenderer = new OpenGLRenderer(this);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(mOpenGLRenderer);

        layout.addView(mGLSurfaceView);

        Button originalButton = (Button)findViewById(R.id.original);

        originalButton.setOnClickListener(v -> mOpenGLRenderer.useCopyShader());

        Button sobelEdgeButton = (Button)findViewById(R.id.sobelEdge);

        sobelEdgeButton.setOnClickListener(v -> mOpenGLRenderer.useSobelEdgeShader());
    }
}
