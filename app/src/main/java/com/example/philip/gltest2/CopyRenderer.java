package com.example.philip.gltest2;

import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;

class CopyRenderer extends RendererBase {

    CopyRenderer(Context context) {
        super(context);
    }

    void onSurfaceCreated(EGLConfig config) {

        String vertexShader = resourceToString(R.raw.vertexshader);
        String fragmentShader = resourceToString(R.raw.fragmentshader);

        int program;
        program = createProgram(vertexShader, fragmentShader);

        if (program == 0)
            return;

        currentProgram = program;
    }
}
