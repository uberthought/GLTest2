package com.example.philip.gltest2;

import javax.microedition.khronos.egl.EGLConfig;

public class GrayScalePagerFragment extends BasePagerFragment {
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
