package com.n00blife.lockit.util;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.n00blife.lockit.R;

public class IntroPagerTransformer implements ViewPager.PageTransformer {


    @Override
    public void transformPage(@NonNull View page, float position) {
        int pagePosition = (int) page.getTag();

        int pageWidth = page.getWidth();

        float heightTimesPosition = position * page.getWidth();

        float absPosition = Math.abs(position);

        if(position <= -1.0f || position >= 1.0f) {
            // Page is totally out of scope
        } else if(position == 0.0f) {
            // Page is completely visible
            // Actions that require the page to be selected can be done here
        } else {

            View title = page.findViewById(R.id.intro_title);
            title.setTranslationY(+heightTimesPosition);
            title.setAlpha(1.0f - absPosition);

            View description = page.findViewById(R.id.intro_description);
            description.setTranslationY(-heightTimesPosition);
            description.setAlpha(1.0f - absPosition);

            View image = page.findViewById(R.id.intro_image);
            image.setAlpha(1.0f - absPosition);

        }
    }
}
