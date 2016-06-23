package com.kineticcafe.kcpmall.views;

import android.app.Activity;
import android.content.Context;

import com.kineticcafe.kcpmall.R;

/**
 * Created by Kay on 2016-06-20.
 */
public class ActivityAnimation {

    public static void startActivityAnimation(Context context){
//        ((Activity)context).overridePendingTransition(R.anim.anim_slide_in_right, android.R.anim.fade_out); //shifts in from the left, remaining one stays
        ((Activity)context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void exitActivityAnimation(Context context){
//        ((Activity)context).overridePendingTransition(R.anim.splash_fake, R.anim.anim_slide_out_left); //shifts out to the left, remaining one stays
        ((Activity)context).overridePendingTransition(R.anim.splash_fake, R.anim.anim_slide_out_right); //shifts out to the left, remaining one stays
    }
}
