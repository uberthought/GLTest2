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

    private OpenGLRenderer mOpenGLRenderer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.screen_slide_pager_fragment, container, false);

        Bundle bundle = getArguments();
        OpenGLRenderer.Shader program = OpenGLRenderer.Shader.values()[bundle.getInt("program")];

        mOpenGLRenderer = new OpenGLRenderer(getContext(), program);

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
