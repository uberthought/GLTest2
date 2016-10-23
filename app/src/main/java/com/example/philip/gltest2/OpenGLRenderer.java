package com.example.philip.gltest2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "OpenGLRenderer";
    private final Shader mCurrentShader;
    private final Map<Shader, Integer> mShaderIds = new HashMap<>();
    private final float[] viewMatrix = new float[16];
    private final FloatBuffer _squareVerticesBuffer;
    private final FloatBuffer _textureVerticesBuffer;
    private final int[] textures = new int[1];
    private final Context mContext;
    private final ConcurrentLinkedDeque<Runnable> runnableDeque = new ConcurrentLinkedDeque<>();
    private Size viewportSize;
    private Size bitmapSize;
    private Bitmap bitmap;
    private FloatBuffer offsetBuffer;
    private int _textureId;
    private int _aPositionHandle;
    private int _aTextureHandle;

    OpenGLRenderer(Context context, Shader shader) {
        mContext = context;
        mCurrentShader = shader;

        float[] squareVertices = {
                -1.0f, 1.0f,
                1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
        };
        _squareVerticesBuffer = ByteBuffer.allocateDirect(squareVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        _squareVerticesBuffer.put(squareVertices).position(0);

        float[] textureVertices = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };
        _textureVerticesBuffer = ByteBuffer.allocateDirect(textureVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        _textureVerticesBuffer.put(textureVertices).position(0);
    }

    public void onDrawFrame(GL10 glUnused) {
        while (!runnableDeque.isEmpty()) {
            Runnable runnable = runnableDeque.removeFirst();
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int currentProgram = mShaderIds.get(mCurrentShader);

        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(currentProgram);
        checkGlError("glUseProgram");

        int offsetId = GLES20.glGetUniformLocation(currentProgram, "offset");
        checkGlError("glGetUniformLocation offset");
        if (offsetId != -1) {
            GLES20.glUniform2fv(offsetId, 9, offsetBuffer);
            checkGlError("glUniform2fv offsetId");
        }

        int modelViewMatId = GLES20.glGetUniformLocation(currentProgram, "modelViewMat");
        checkGlError("glGetUniformLocation modelViewMat");
        if (modelViewMatId != -1) {
            GLES20.glUniformMatrix4fv(modelViewMatId, 1, false, viewMatrix, 0);
            checkGlError("glUniformMatrix4fv modelViewMatId");
        }

        GLES20.glVertexAttribPointer(_aPositionHandle, 2, GLES20.GL_FLOAT, false, 0, _squareVerticesBuffer);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(_aPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        GLES20.glVertexAttribPointer(_aTextureHandle, 2, GLES20.GL_FLOAT, false, 0, _textureVerticesBuffer);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(_aTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        viewportSize = new Size(width, height);
        updateViewMatrix();
    }

    private void updateViewMatrix() {
        Matrix.setIdentityM(viewMatrix, 0);

        float viewportAspect = (float) viewportSize.getWidth() / (float) viewportSize.getHeight();
        float bitmapAspect = (float) bitmapSize.getWidth() / (float) bitmapSize.getHeight();
        float aspectRatio = bitmapAspect / viewportAspect;
        if (aspectRatio < 1f)
            Matrix.scaleM(viewMatrix, 0, aspectRatio, 1f, 1f);
        else
            Matrix.scaleM(viewMatrix, 0, 1f, 1f / aspectRatio, 1f);
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        String vertexShader = resourceToString(R.raw.vertexshader);
        String fragmentShader = resourceToString(R.raw.fragmentshader);

        int program;
        program = createProgram(vertexShader, fragmentShader);

        if (program == 0)
            return;

        _aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (_aPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        _aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (_aTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        mShaderIds.put(Shader.Copy, program);


        String sobelEdgeShader = resourceToString(R.raw.sobeledgeshader);
        program = createProgram(vertexShader, sobelEdgeShader);
        if (program == 0) {
            return;
        }
        _aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (_aPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        _aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (_aTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        mShaderIds.put(Shader.SobelEdge, program);


        // Create our texture. This has to be done each time the surface is created.
        GLES20.glGenTextures(1, textures, 0);

        _textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.testimage);
            setBitmap(bitmap);
        }
    }

    void setImage(Uri imageUri) {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
            setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setBitmap(Bitmap bitmap) {

        executeOnOpenGLThread(() -> GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0));

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

    private int createProgram(String vertexSource, String fragmentSource) {
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

    private void checkGlError(String op) {
        int error;
        //noinspection LoopStatementThatDoesntLoop
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + GLUtils.getEGLErrorString(error));
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private String resourceToString(int resourceId) {
        try {
            Resources res = mContext.getResources();
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

    private void executeOnOpenGLThread(Runnable func) {
        runnableDeque.addLast(func);
    }

    enum Shader {
        Copy,
        SobelEdge
    }
}

