package com.example.philip.gltest2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

abstract public class BasePagerFragment extends Fragment implements GLSurfaceView.Renderer {
    private final ConcurrentLinkedDeque<Runnable> runnableDeque = new ConcurrentLinkedDeque<>();
    private final int[] mTextures = new int[1];
    private final float[] viewMatrix = new float[16];
    private final FloatBuffer mSquareVerticesBuffer;
    private final FloatBuffer mTextureVerticesBuffer;
    protected FloatBuffer offsetBuffer;
    String TAG = "BasePagerFragment";
    int currentProgram;
    FloatBuffer emptyBuffer;
    private String imageLocation;
    private Size viewportSize;
    private Size bitmapSize;
    private Bitmap bitmap = null;

    public BasePagerFragment() {
        float[] squareVertices = {
                -1.0f, 1.0f,
                1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
        };
        mSquareVerticesBuffer = ByteBuffer.allocateDirect(squareVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mSquareVerticesBuffer.put(squareVertices).position(0);

        float[] textureVertices = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };
        mTextureVerticesBuffer = ByteBuffer.allocateDirect(textureVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureVerticesBuffer.put(textureVertices).position(0);
    }

    public Uri getBitmapSource() {
        if (imageLocation == null)
            return null;
        return Uri.parse(imageLocation);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        MainActivity activity = (MainActivity) getActivity();
        imageLocation = activity.getImageLocation();
        updateImage();

        activity.setOnImageChangedListener(() -> {
            imageLocation = activity.getImageLocation();
            updateImage();
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        onSurfaceCreated(config);

        // Create our texture. This has to be done each time the surface is created.
        GLES20.glGenTextures(1, mTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

//        clearImage();
        if (bitmap == null) {
            if (imageLocation != null)
                updateImage();
            else
                clearImage();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        viewportSize = new Size(width, height);
        updateViewMatrix();
    }

    private void updateViewMatrix() {
        Matrix.setIdentityM(viewMatrix, 0);

        if (viewportSize != null && bitmapSize != null) {
            float viewportAspect = (float) viewportSize.getWidth() / (float) viewportSize.getHeight();
            float bitmapAspect = (float) bitmapSize.getWidth() / (float) bitmapSize.getHeight();
            float aspectRatio = bitmapAspect / viewportAspect;
            if (aspectRatio < 1f)
                Matrix.scaleM(viewMatrix, 0, aspectRatio, 1f, 1f);
            else
                Matrix.scaleM(viewMatrix, 0, 1f, 1f / aspectRatio, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        while (!runnableDeque.isEmpty()) {
            Runnable runnable = runnableDeque.removeFirst();
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        if (bitmap == null)
//            return;

        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(currentProgram);
        checkGlError("glUseProgram");

        onDrawFrame();

        int modelViewMatrixHandle = GLES20.glGetUniformLocation(currentProgram, "modelViewMat");
        checkGlError("glGetUniformLocation modelViewMat");
        if (modelViewMatrixHandle != -1) {
            GLES20.glUniformMatrix4fv(modelViewMatrixHandle, 1, false, viewMatrix, 0);
            checkGlError("glUniformMatrix4fv modelViewMatrixHandle");
        }

        int positionHandle = GLES20.glGetAttribLocation(currentProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, mSquareVerticesBuffer);
        checkGlError("glVertexAttribPointer positionHandle");
        GLES20.glEnableVertexAttribArray(positionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        int textureHandle = GLES20.glGetAttribLocation(currentProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureVerticesBuffer);
        checkGlError("glVertexAttribPointer textureHandle");
        GLES20.glEnableVertexAttribArray(textureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    void updateImage() {
        try {
            if (getContext() == null)
                return;

            Uri imageUri = getBitmapSource();

            if (imageUri == null)
                return;

            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);

            executeOnOpenGLThread(() -> {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
            });

            bitmapSize = new Size(bitmap.getWidth(), bitmap.getHeight());

            float stepWidth = 1.0f / bitmapSize.getWidth();
            float stepHeight = 1.0f / bitmapSize.getWidth();

            float[] offsets = {
                    -stepWidth, -stepHeight,
                    -stepWidth, 0.0f,
                    -stepWidth, stepHeight,
                    0.0f, -stepHeight,
                    0.0f, 0.0f,
                    0.0f, stepHeight,
                    stepWidth, -stepHeight,
                    stepWidth, 0.0f,
                    stepWidth, stepHeight,
            };

            offsetBuffer = ByteBuffer.allocateDirect(offsets.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            offsetBuffer.put(offsets).position(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void clearImage() {
        bitmapSize = new Size(1, 1);
        emptyBuffer = ByteBuffer.allocateDirect(bitmapSize.getWidth() * bitmapSize.getHeight() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        executeOnOpenGLThread(() -> {
            if (imageLocation == null)
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmapSize.getWidth(), bitmapSize.getHeight(), 0, GL10.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, emptyBuffer);
        });

        checkGlError("glTexImage2D");
        float stepWidth = 1.0f / bitmapSize.getWidth();
        float stepHeight = 1.0f / bitmapSize.getWidth();

        float[] offsets = {
                -stepWidth, -stepHeight,
                -stepWidth, 0.0f,
                -stepWidth, stepHeight,
                0.0f, -stepHeight,
                0.0f, 0.0f,
                0.0f, stepHeight,
                stepWidth, -stepHeight,
                stepWidth, 0.0f,
                stepWidth, stepHeight,
        };

        offsetBuffer = ByteBuffer.allocateDirect(offsets.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        offsetBuffer.put(offsets).position(0);
    }

    int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    protected String resourceToString(int resourceId) {
        try {
            Resources res = getContext().getResources();
            InputStream in_s = res.openRawResource(resourceId);

            byte[] b = new byte[in_s.available()];
            int count = in_s.read(b);
            if (count > 0)
                return new String(b);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + GLUtils.getEGLErrorString(error));
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private void executeOnOpenGLThread(Runnable func) {
        runnableDeque.addLast(func);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.screen_slide_pager_fragment, container, false);

        if (imageLocation != null)
            updateImage();

        GLSurfaceView glSurfaceView = new GLSurfaceView(getContext());
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(this);

        rootView.addView(glSurfaceView);

        return rootView;
    }

    abstract void onSurfaceCreated(EGLConfig config);

    public void onDrawFrame() {
    }

}
