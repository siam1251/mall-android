package com.kineticcafe.kcpmall.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kineticcafe.kcpandroidsdk.logger.Logger;
import com.kineticcafe.kcpandroidsdk.models.KcpContentPage;
import com.kineticcafe.kcpandroidsdk.models.KcpPlaces;
import com.kineticcafe.kcpmall.R;
import com.kineticcafe.kcpmall.adapters.CategoryStoreRecyclerViewAdapter;
import com.kineticcafe.kcpmall.adapters.NewsRecyclerViewAdapter;
import com.kineticcafe.kcpmall.adapters.SocialFeedDetailRecyclerViewAdapter;
import com.kineticcafe.kcpmall.factory.KcpContentTypeFactory;
import com.kineticcafe.kcpmall.fragments.DealsRecyclerViewAdapter;
import com.kineticcafe.kcpmall.fragments.HomeFragment;
import com.kineticcafe.kcpmall.interfaces.FavouriteInterface;
import com.kineticcafe.kcpmall.managers.FavouriteManager;
import com.kineticcafe.kcpmall.views.ActivityAnimation;
import com.kineticcafe.kcpmall.views.DealRecyclerItemDecoration;
import com.kineticcafe.kcpmall.views.NewsRecyclerItemDecoration;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.util.ArrayList;

/**
 * Created by Kay on 2016-07-05.
 */
public class SocialDetailActivity extends AppCompatActivity {
    protected final Logger logger = new Logger(getClass().getName());
    private int mItemType;
    private RecyclerView rv;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

        Bundle bundle = getIntent().getExtras();
        mItemType = bundle.getInt(Constants.ARG_ACTIVITY_TYPE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        String pageTitle = "";
        if (mItemType == KcpContentTypeFactory.ITEM_TYPE_INSTAGRAM){
            pageTitle = "@" + Constants.INSTAGRAM_USER_NAME;
        } else if(mItemType == KcpContentTypeFactory.ITEM_TYPE_TWITTER) {
            pageTitle = "@" + Constants.TWITTER_SCREEN_NAME;
            toolbar.setBackgroundColor(getResources().getColor(R.color.twitter_theme_color));
        }
        getSupportActionBar().setTitle(pageTitle);


        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setNestedScrollingEnabled(true);
        rv.setBackgroundColor(Color.WHITE);

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        if (mItemType == KcpContentTypeFactory.ITEM_TYPE_INSTAGRAM){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            rv.setLayoutManager(linearLayoutManager);
            SocialFeedDetailRecyclerViewAdapter socialFeedDetailRecyclerViewAdapter = new SocialFeedDetailRecyclerViewAdapter (
                    this,
                    HomeFragment.sInstaFeedList, null);
            rv.setAdapter(socialFeedDetailRecyclerViewAdapter);
        } else if(mItemType == KcpContentTypeFactory.ITEM_TYPE_TWITTER) {
            rv.setVisibility(View.GONE);
            ListView lvTwitter = (ListView) findViewById(R.id.lvTwitter);
            lvTwitter.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lvTwitter.setNestedScrollingEnabled(true);
            }

            final UserTimeline userTimeline = new UserTimeline.Builder()
                    .screenName(Constants.TWITTER_SCREEN_NAME)
                    .build();

            final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(this)
                    .setTimeline(userTimeline)
                    .build();

            lvTwitter.setAdapter(adapter);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onFinish(int resultCode){
        setResult(resultCode, new Intent());
        onBackPressed();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityAnimation.exitActivityAnimation(this);
    }

}
