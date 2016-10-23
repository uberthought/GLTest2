package com.example.philip.gltest2;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.HashMap;
import java.util.Map;

class OpenGLPagerAdapter extends FragmentStatePagerAdapter {

    private final Map<Integer, OpenGLPagerFragment> mFragments = new HashMap<>();

    OpenGLPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        OpenGLPagerFragment fragment = mFragments.get(position);
        if (fragment == null) {
            fragment = new OpenGLPagerFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("program", shaderFromPosition(position).ordinal());
            fragment.setArguments(bundle);

            mFragments.put(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (shaderFromPosition(position)) {
            default:
            case Copy:
                return "Original";
            case SobelEdge:
                return "Sobel Edge";
            case GrayScale:
                return "Gray Scale";
        }
    }

    private GrayScaleRenderer.Shader shaderFromPosition(int position) {
        switch (position) {
            default:
            case 0:
                return RendererBase.Shader.Copy;
            case 1:
                return RendererBase.Shader.SobelEdge;
            case 2:
                return RendererBase.Shader.GrayScale;
        }
    }

    void setImage(Uri imageUri) {
        for (OpenGLPagerFragment fragment : mFragments.values()) {
            fragment.setImage(imageUri);
        }
    }
}