package com.kineticcafe.kcpmall.factory;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kineticcafe.kcpmall.R;
import com.kineticcafe.kcpmall.utility.Utility;

/**
 * Created by Kay on 2016-05-06.
 */
public class GlideFactory {

    public void glideWithDefaultRatio(Context context, int drawable, ImageView imageView){
        Glide.with(context)
                .load(drawable)
                .centerCrop()
                .override(Utility.getScreenWidth(context), (int) (Utility.getScreenWidth(context) / Utility.getFloat(context, R.dimen.ancmt_image_ratio)))
                .crossFade() //TODO: necessary?
                .into(imageView);

    }

    public void glideWithDefaultRatio(Context context, String url, ImageView imageView){
        Glide.with(context)
                .load(url)
//                .centerCrop()
                .override(Utility.getScreenWidth(context), (int) (Utility.getScreenWidth(context) / Utility.getFloat(context, R.dimen.ancmt_image_ratio)))
                .crossFade() //TODO: necessary?
                .into(imageView);
    }

    /**
     * This will first check whether the image is available at the url. if not, it will just set the errorDrawable. The reason for not using .error(errorDrawable)
     * is because it takes longer for glide to figure whether the image is available and decide to use the errorDrawable.
     * @param context
     * @param url
     * @param imageView
     * @param errorDrawable errorDrawable is passed
     */
    public void glideWithDefaultRatio(Context context, String url, ImageView imageView, int errorDrawable){
        if(Utility.existsInServer(url)){
            Glide.with(context)
                    .load(url)
//                    .centerCrop()
                    .override(Utility.getScreenWidth(context), (int) (Utility.getScreenWidth(context) / Utility.getFloat(context, R.dimen.ancmt_image_ratio)))
                    .crossFade() //TODO: necessary?
                    .into(imageView);
        } else {
            imageView.setImageResource(errorDrawable);
        }





    }

}
