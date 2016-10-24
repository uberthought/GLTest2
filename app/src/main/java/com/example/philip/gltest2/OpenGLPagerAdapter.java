package com.example.philip.gltest2;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.HashMap;
import java.util.Map;

class OpenGLPagerAdapter extends FragmentStatePagerAdapter {

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, BasePagerFragment> mFragments = new HashMap<>();
    OpenGLPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        BasePagerFragment fragment = mFragments.get(position);
        if (fragment == null) {
            switch (shaderFromPosition(position)) {
                default:
                case Copy:
                    fragment = new CopyPagerFragment();
                    break;
                case SobelEdge:
                    fragment = new SobelEdgePagerFragment();
                    break;
                case GrayScale:
                    fragment = new GrayScalePagerFragment();
                    break;
            }

            mFragments.put(position, fragment);
        }

//        Bundle bundle = new Bundle();
//        if (bitmapSource != null)
//            bundle.putString("bitmapSource", bitmapSource.toString());
//        else
//            bundle.putString("bitmapSource", null);
//        fragment.setArguments(bundle);
//
//        fragment.setImageLocation(bitmapSource);

        return fragment;
    }

    @Override
    public int getCount() {
        return Shader.values().length;
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

    private Shader shaderFromPosition(int position) {
        switch (position) {
            default:
            case 0:
                return Shader.Copy;
            case 1:
                return Shader.SobelEdge;
            case 2:
                return Shader.GrayScale;
        }
    }

    private enum Shader {
        Copy,
        SobelEdge,
        GrayScale
    }
}