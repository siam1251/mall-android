package com.kineticcafe.kcpmall.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kineticcafe.kcpandroidsdk.models.KcpContentPage;
import com.kineticcafe.kcpandroidsdk.models.KcpNavigationPage;
import com.kineticcafe.kcpandroidsdk.models.KcpNavigationRoot;
import com.kineticcafe.kcpandroidsdk.service.ServiceFactory;
import com.kineticcafe.kcpandroidsdk.utils.Utility;
import com.kineticcafe.kcpandroidsdk.logger.Logger;
import com.kineticcafe.kcpmall.activities.Constants;
import com.kineticcafe.kcpmall.activities.MainActivity;
import com.kineticcafe.kcpmall.factory.HeaderFactory;
import com.kineticcafe.kcpmall.instagram.InstagramService;
import com.kineticcafe.kcpmall.instagram.model.Media;
import com.kineticcafe.kcpmall.instagram.model.Recent;
import com.kineticcafe.kcpmall.kcpData.KcpService;
import com.kineticcafe.kcpmall.R;
import com.kineticcafe.kcpmall.adapters.HomeTopViewPagerAdapter;
import com.kineticcafe.kcpmall.instagram.model.InstagramFeed;
import com.kineticcafe.kcpmall.twitter.TwitterAsyncTask;
import com.kineticcafe.kcpmall.twitter.model.TwitterTweet;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends BaseFragment {

    private NewsFragment mNewsFragment;
    private KcpService mKcpService;
    private int mTwitterDownloadCounter = 3;
    public static ArrayList<TwitterTweet> sTwitterTweets = new ArrayList<>();
    public static ArrayList<InstagramFeed> sInstagramFeeds = new ArrayList<>();


    private static HomeFragment sHomeFragment;
    public static HomeFragment getInstance(){
        if(sHomeFragment == null) sHomeFragment = new HomeFragment();
        return sHomeFragment;
    }

    private View.OnClickListener mDownloadDealOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            downloadNewsAndDeal();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ViewPager vpHome = (ViewPager) view.findViewById(R.id.vpHome);
        setupViewPager(vpHome);

        TabLayout tablayout = (TabLayout) view.findViewById(R.id.tlHome);
        tablayout.setupWithViewPager(vpHome);

        if(Utility.isNetworkAvailable(getActivity())){
            downloadNewsAndDeal();
        } else {
            mMainActivity.onDataDownloaded(); //TODO: error here when offline
            mMainActivity.showSnackBar(R.string.warning_no_internet_connection, R.string.warning_retry,  mDownloadDealOnClickListener);
        }
        return view;
    }


    private void setupViewPager(ViewPager viewPager) {
        HomeTopViewPagerAdapter hometopViewPagerAdapter = new HomeTopViewPagerAdapter(getChildFragmentManager());
        mNewsFragment = NewsFragment.getInstance();
        hometopViewPagerAdapter.addFrag(mNewsFragment, "NEWS");
        hometopViewPagerAdapter.addFrag(new OneFragment(), "DEALS");
        viewPager.setAdapter(hometopViewPagerAdapter);
    }


    public void downloadNewsAndDeal(){
        mKcpService = ServiceFactory.createRetrofitService(getActivity(), new HeaderFactory().getHeaders(), KcpService.class, Constants.URL_BASE);
        Call<KcpNavigationRoot> call = mKcpService.getNavigationRoot(Constants.URL_NAVIGATION_ROOT);
        call.enqueue(new Callback<KcpNavigationRoot>() {
            @Override
            public void onResponse(Call<KcpNavigationRoot> call, Response<KcpNavigationRoot> response) {
                KcpNavigationRoot kcpNavigationRoot = KcpNavigationRoot.getInstance();
                kcpNavigationRoot = response.body();
                for(int i = 0; i < kcpNavigationRoot.getNavigationPageListSize(); i++){
                    KcpNavigationPage kcpNavigationPage = kcpNavigationRoot.getNavigationPage(i);
                    String fullHref = kcpNavigationPage.getFullLink();
                    downloadNavigationPage(fullHref);
                }
            }

            @Override
            public void onFailure(Call<KcpNavigationRoot> call, Throwable t) {
                String a = "ej";
                mMainActivity.onDataDownloaded();
                mMainActivity.showSnackBar(R.string.warning_no_internet_connection, R.string.warning_retry,  mDownloadDealOnClickListener);
            }
        });

        downloadTwitterTweets();
        downloadInstagram();
    }

    public void downloadNavigationPage(String url){

        Call<KcpNavigationPage> call = mKcpService.getNavigationPage(url);
        call.enqueue(new Callback<KcpNavigationPage>() {
            @Override
            public void onResponse(Call<KcpNavigationPage> call, Response<KcpNavigationPage> response) {
                KcpNavigationPage kcpNavigationPageResponse = response.body();
                String externalCode = kcpNavigationPageResponse.getExternalCode();
                KcpNavigationRoot.getInstance().setNavigationpage(externalCode, kcpNavigationPageResponse);
                String fullHref = kcpNavigationPageResponse.getSelfLink() + Constants.URL_VIEW_ALL_CONTENT;
                downloadContents(fullHref);
            }

            @Override
            public void onFailure(Call<KcpNavigationPage> call, Throwable t) {

            }
        });
    }

    public void downloadContents(String url){
        Call<KcpContentPage> call = mKcpService.getContentPage(url);
        call.enqueue(new Callback<KcpContentPage>() {
            @Override
            public void onResponse(Call<KcpContentPage> call, Response<KcpContentPage> response) {
                if (response.isSuccessful()) {
                    try {
                        final KcpContentPage kcpContentPageResponse = response.body();
                        for (KcpNavigationPage kcpNavigationPage : KcpNavigationRoot.getInstance().getNavigationPage()) {
                            String navigationPageLink = kcpNavigationPage.getSelfLink(); //"https://dit-kcp.kineticcafetech.com/core/content_pages/473/view_all_content"
                            String kCPContentPageLink = kcpContentPageResponse.getSelfLink(); //"https://dit-kcp.kineticcafetech.com/core/content_pages/473/view_all_content?page=1"

                            if(kCPContentPageLink.contains(navigationPageLink)){
                                boolean isDataAdded = kcpNavigationPage.setKcpContentPage(kcpContentPageResponse);
                                if(isDataAdded) {
                                    mNewsFragment.mNewsAdapter.addData(kcpContentPageResponse.getContentPageList());
                                    mNewsFragment.mEndlessRecyclerViewScrollListener.onLoadDone();
                                } else {
                                    updateAdapter();
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                } else {}
            }

            @Override
            public void onFailure(Call<KcpContentPage> call, Throwable t) {
                logger.error(t);
            }
        });
    }

    public void downloadMoreFeeds(String navigationPageType){
        try {
            KcpNavigationPage kcpNavigationPage = KcpNavigationRoot.getInstance().getNavigationpage(navigationPageType);
            String nextUrl = kcpNavigationPage.getNextContentPageUrl();
            if(!nextUrl.equals("")){
                mNewsFragment.mNewsAdapter.prepareLoadingImage();
                downloadContents(nextUrl);
            } else {
                //TODO: should stop loading image
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void updateAdapter(){
        if(mOnRefreshListener != null) mOnRefreshListener.onRefresh();
        try {
            KcpNavigationPage kcpNavigationPage = KcpNavigationRoot.getInstance().getNavigationpage(Constants.EXTERNAL_CODE_FEED);
            if(kcpNavigationPage != null && kcpNavigationPage.getKcpContentPageList() != null && kcpNavigationPage.getKcpContentPageList() != null && mNewsFragment.mNewsAdapter != null){
                mMainActivity.onDataDownloaded();
                mNewsFragment.mNewsAdapter.updateData(kcpNavigationPage.getKcpContentPageList());
                if (mNewsFragment.mNewsAdapter.getSocialFeedViewPagerAdapter() != null) {
                    mNewsFragment.mNewsAdapter.getSocialFeedViewPagerAdapter().updateTwitterData(sTwitterTweets);
                    mNewsFragment.mNewsAdapter.getSocialFeedViewPagerAdapter().updateInstaData(sInstagramFeeds);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void downloadTwitterTweets() {
        Log.e("HomeFragment", "ATTEMPTING TW DOWNLOAD");
        if (getActivity() != null && Utility.isNetworkAvailable(getActivity())) {
            Log.e("HomeFragment", "STARTING TW DOWNLOAD");
            new TwitterAsyncTask(
                    Constants.NUMB_OF_TWEETS,
                    Constants.TWITTER_API_KEY,
                    Constants.TWITTER_API_SECRET,
                    new TwitterAsyncTask.OnTwitterFeedDownloadCompleteListener() {
                        @Override
                        public void onTwitterFeedDownloadComplete(ArrayList<TwitterTweet> twitterTweets) {
                            if(twitterTweets == null) {
                                logger.debug("TWITTER NOT DOWNLOADED");
                                Log.e("HomeFragment", "TWITTER NOT DOWNLOADED");
                                mTwitterDownloadCounter--;
                                if(mTwitterDownloadCounter > 0) {
                                    logger.debug("twitter download counter : " + mTwitterDownloadCounter);
                                    downloadTwitterTweets();
                                }
                            } else {
                                mTwitterDownloadCounter = 3;
                                if (mNewsFragment.mNewsAdapter.getSocialFeedViewPagerAdapter() != null) {
                                    mNewsFragment.mNewsAdapter.getSocialFeedViewPagerAdapter().updateTwitterData(twitterTweets);
                                } else {
                                    sTwitterTweets = twitterTweets;
                                }
                            }


                        }
                    }).execute(Constants.TWITTER_SCREEN_NAME, 5, this);
        }
    }

    public void downloadInstagram() {
        if (getActivity() != null && Utility.isNetworkAvailable(getActivity())) {
            InstagramService instagramService = ServiceFactory.createRetrofitService(getActivity(), InstagramService.class, Constants.INSTAGRAM_BASE_URL);;
            Call<Recent> recent = instagramService.getRecent(Constants.INSTAGRAM_USER_ID, Constants.INSTAGRAM_ACCESS_TOKEN, Constants.NUMB_OF_INSTA, null, null, null, null);
            recent.enqueue(new Callback<Recent>() {
                @Override
                public void onResponse(Call<Recent> call, Response<Recent> response) {
                    if (response.isSuccessful()) {
                        Recent recent = response.body();
                        int mediaSize = recent.getMediaList().size();

                        sInstagramFeeds.clear();
                        for (int i = 0; i < mediaSize; i++) {
                            Media media = recent.getMediaList().get(i);
                            sInstagramFeeds.add(new InstagramFeed(media.getImages().getStandardResolution().getUrl()));
                        }
                        if (mNewsFragment.mNewsAdapter.getSocialFeedViewPagerAdapter() != null) {
                            mNewsFragment.mNewsAdapter.getSocialFeedViewPagerAdapter().updateInstaData(sInstagramFeeds);
                        } else {
                            //                            sTwitterTweets = twitterTweets;
                        }
                    } else {
                        logger.debug("instagram download unsuccessful");
                    }
                }

                @Override
                public void onFailure(Call<Recent> call, Throwable t) {
                    logger.error(t);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapter();
    }

    private RefreshListener mOnRefreshListener;
    public void setOnRefreshListener(RefreshListener refreshListener){
        mOnRefreshListener = refreshListener;
    }
    public interface RefreshListener {
        void onRefresh();
    }
}
