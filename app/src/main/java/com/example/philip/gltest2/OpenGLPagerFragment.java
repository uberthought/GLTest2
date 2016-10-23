package com.example.philip.gltest2;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OpenGLPagerFragment extends Fragment {

    private RendererBase mOpenGLRenderer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.screen_slide_pager_fragment, container, false);

        Bundle bundle = getArguments();
        GrayScaleRenderer.Shader program = RendererBase.Shader.values()[bundle.getInt("program")];

        switch (program) {
            default:
            case Copy:
                mOpenGLRenderer = new CopyRenderer(getContext());
                break;
            case SobelEdge:
                mOpenGLRenderer = new SobelEdgeRenderer(getContext());
                break;
            case GrayScale:
                mOpenGLRenderer = new GrayScaleRenderer(getContext());
                break;
        }

        GLSurfaceView glSurfaceView = new GLSurfaceView(getContext());
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(mOpenGLRenderer);

        rootView.addView(glSurfaceView);

        return rootView;
    }

    void setImage(Uri imageUri) {
        mOpenGLRenderer.setImage(imageUri);
    }
}
