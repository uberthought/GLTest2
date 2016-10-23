package com.example.philip.gltest2;

import javax.microedition.khronos.egl.EGLConfig;


public class CopyPagerFragment extends BasePagerFragment {

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
