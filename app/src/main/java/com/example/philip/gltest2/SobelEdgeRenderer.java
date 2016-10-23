package com.example.philip.gltest2;

import android.content.Context;
import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGLConfig;

class SobelEdgeRenderer extends RendererBase {

    SobelEdgeRenderer(Context context) {
        super(context);
    }

    void onSurfaceCreated(EGLConfig config) {

        int program;

        String vertexShader = resourceToString(R.raw.vertexshader);
        String sobelEdgeShader = resourceToString(R.raw.sobeledgeshader);

        program = createProgram(vertexShader, sobelEdgeShader);

        if (program == 0)
            return;

        currentProgram = program;
    }

    @Override
    public void onDrawFrame() {
        int offsetId = GLES20.glGetUniformLocation(currentProgram, "offset");
        checkGlError("glGetUniformLocation offset");
        if (offsetId != -1) {
            GLES20.glUniform2fv(offsetId, 9, offsetBuffer);
            checkGlError("glUniform2fv offsetId");
        }
    }
}
