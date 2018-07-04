package com.n00blife.lockit.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n00blife.lockit.R;

public class IntroFragment extends Fragment {

    private static String POSITION = "position";

    public IntroFragment() {

    }

    public static IntroFragment getInstance(int position) {
        IntroFragment fragment = new IntroFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutResId = R.layout.intro_final_slide;

        switch (getArguments().getInt(POSITION)) {
            case 0:
                layoutResId = R.layout.intro_slide1;
                break;
            case 1:
                layoutResId = R.layout.intro_slide2;
                break;
            case 2:
                layoutResId = R.layout.intro_slide3;
                break;
            case 3:
                layoutResId = R.layout.intro_final_slide;
                break;
        }

        View view = LayoutInflater.from(getContext()).inflate(layoutResId, container, false);

        view.setTag(getArguments().getInt(POSITION));

        return view;
    }

}
