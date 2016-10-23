package com.example.philip.gltest2;

import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;

class GrayScaleRenderer extends RendererBase {

    GrayScaleRenderer(Context context) {
        super(context);
    }

    void onSurfaceCreated(EGLConfig config) {
        int program;

        String vertexShader = resourceToString(R.raw.vertexshader);
        String grayscaleShader = resourceToString(R.raw.grayscale);

        program = createProgram(vertexShader, grayscaleShader);
        if (program == 0)
            return;

        currentProgram = program;
    }
}

