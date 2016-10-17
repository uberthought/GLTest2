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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    private static String TAG = "OpenGLRenderer";
    private final float[] _squareVertices = {
            -1.0f, 1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
    };
    private final float[] _textureVertices = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };
    private float[] viewMatrix = new float[16];
    private Size viewportSize;
    private Size bitmapSize;

    private FloatBuffer _squareVerticesBuffer;
    private FloatBuffer _textureVerticesBuffer;
    private int _program;
    private int _programSobelEdge;
    private int _currentProgram;
    private int _textureId;
    private int _aPositionHandle;
    private int _aTextureHandle;
    private Context _context;

    OpenGLRenderer(Context context) {
        _context = context;
        _squareVerticesBuffer = ByteBuffer.allocateDirect(_squareVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        _squareVerticesBuffer.put(_squareVertices).position(0);
        _textureVerticesBuffer = ByteBuffer.allocateDirect(_textureVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        _textureVerticesBuffer.put(_textureVertices).position(0);
    }

    void useCopyShader() {
        _currentProgram = _program;
    }

    void useSobelEdgeShader() {
        _currentProgram = _programSobelEdge;
    }

    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(_currentProgram);
        checkGlError("glUseProgram");

        int modelViewMatId = GLES20.glGetUniformLocation(_currentProgram, "modelViewMat");
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

    private String resourceToString(int resourceId) {
        try {
            Resources res = _context.getResources();
            InputStream in_s = res.openRawResource(resourceId);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return new String(b);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        String vertexShader = resourceToString(R.raw.vertexshader);
        String fragmentShader = resourceToString(R.raw.fragmentshader);

        _program = createProgram(vertexShader, fragmentShader);
        if (_program == 0) {
            return;
        }

        _aPositionHandle = GLES20.glGetAttribLocation(_program, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (_aPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        _aTextureHandle = GLES20.glGetAttribLocation(_program, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (_aTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        _currentProgram = _program;

        String sobelEdgeShader = resourceToString(R.raw.sobeledgeshader);
        _programSobelEdge = createProgram(vertexShader, sobelEdgeShader);
        if (_programSobelEdge == 0) {
            return;
        }

        _aPositionHandle = GLES20.glGetAttribLocation(_programSobelEdge, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (_aPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        _aTextureHandle = GLES20.glGetAttribLocation(_programSobelEdge, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (_aTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        // Create our texture. This has to be done each time the surface is created.
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        _textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.testimage);
        setBitmap(bitmap);
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
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    void setImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(_context.getContentResolver(), imageUri);
            setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setBitmap(Bitmap bitmap) {
        bitmapSize = new Size(bitmap.getWidth(), bitmap.getHeight());

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap.recycle();

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
        FloatBuffer offsetBuffer = ByteBuffer.allocateDirect(offsets.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        offsetBuffer.put(offsets).position(0);
        int offsetId = GLES20.glGetUniformLocation(_programSobelEdge, "offset");
        checkGlError("glGetUniformLocation offset");
        if (offsetId != -1) {
            GLES20.glUseProgram(_programSobelEdge);
            GLES20.glUniform2fv(offsetId, 9, offsetBuffer);
            checkGlError("glUniform2fv offsetId");
        }
    }
}

