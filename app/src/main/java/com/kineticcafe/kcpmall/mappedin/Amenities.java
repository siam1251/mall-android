package com.kineticcafe.kcpmall.mappedin;

import android.app.Activity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.annotations.SerializedName;
import com.kineticcafe.kcpmall.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kay on 2016-07-25.
 */
public class Amenities {

    @SerializedName("amenities")
    private List<Amenity> amenityList = new ArrayList<Amenity>();

    public List<Amenity> getAmenityList (){
        return amenityList;
    }

    public class Amenity {

        @SerializedName("type")
        private String type;

        @SerializedName("enabled")
        private boolean enabled;

        @SerializedName("title")
        private String title;

        @SerializedName("externalIds")
        private String[] externalIds;

        public String getType() {
            return type;
        }

        public boolean isEnabled(){
            return enabled;
        }

        public String getTitle() {
            return title;
        }

        public String[] getExternalIds() {
            return externalIds;
        }

    }


    public static class AmenityLayout {
        private View mView;
        private View.OnClickListener mOnClickListener;
        private SwitchCompat swAmenity;
        public String title;
        private TextView tvAmenity;

        public AmenityLayout(Activity activity, ViewGroup parentView, int layout, String title, View.OnClickListener onClickListener) {
            mView = activity.getLayoutInflater().inflate(
                    layout,
                    parentView,
                    false);
            this.title = title;
            tvAmenity = (TextView) mView.findViewById(R.id.tvAmenity);
            swAmenity = (SwitchCompat) mView.findViewById(R.id.swAmenity);
            tvAmenity.setText(title);
            mOnClickListener = onClickListener;
            swAmenity.setOnClickListener(mOnClickListener);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swAmenity.performClick();
                }
            });
        }

        public View getView(){
            return mView;
        }
    }


}
