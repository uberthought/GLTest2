package com.example.philip.gltest2;

import android.content.Context;
import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GrayScaleRenderer extends RendererBase {

    GrayScaleRenderer(Context context) {
        super(context);
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);

        int program;

        String vertexShader = resourceToString(R.raw.vertexshader);
        String grayscaleShader = resourceToString(R.raw.grayscale);

        program = createProgram(vertexShader, grayscaleShader);
        if (program == 0)
            return;

        _aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (_aPositionHandle == -1)
            throw new RuntimeException("Could not get attrib location for aPosition");

        _aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (_aTextureHandle == -1)
            throw new RuntimeException("Could not get attrib location for aTextureCoord");

        currentProgram = program;
    }
}

