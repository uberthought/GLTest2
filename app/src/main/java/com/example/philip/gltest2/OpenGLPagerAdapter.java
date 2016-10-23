package com.example.philip.gltest2;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.HashMap;
import java.util.Map;

class OpenGLPagerAdapter extends FragmentStatePagerAdapter {

    @SuppressLint("UseSparseArrays")
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
            switch (position) {
                default:
                case 0:
                    bundle.putInt("program", OpenGLRenderer.Shader.Copy.ordinal());
                    break;
                case 1:
                    bundle.putInt("program", OpenGLRenderer.Shader.SobelEdge.ordinal());
                    break;
            }
            fragment.setArguments(bundle);

            mFragments.put(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Position " + Integer.toString(position);
    }

    void setImage(Uri imageUri) {
        for (OpenGLPagerFragment fragment : mFragments.values()) {
            fragment.setImage(imageUri);
        }
    }
}