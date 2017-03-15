package com.ivanhoecambridge.mall.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivanhoecambridge.kcpandroidsdk.models.KcpContentPage;
import com.ivanhoecambridge.kcpandroidsdk.utils.KcpUtility;
import com.ivanhoecambridge.mall.R;
import com.ivanhoecambridge.mall.activities.MainActivity;
import com.ivanhoecambridge.mall.activities.MyPagesActivity;
import com.ivanhoecambridge.mall.activities.ShowtimesActivity;
import com.ivanhoecambridge.mall.constants.Constants;
import com.ivanhoecambridge.mall.activities.DetailActivity;
import com.ivanhoecambridge.mall.activities.InterestedCategoryActivity;
import com.ivanhoecambridge.mall.activities.SocialDetailActivity;
import com.ivanhoecambridge.mall.factory.GlideFactory;
import com.ivanhoecambridge.mall.factory.KcpContentTypeFactory;
import com.ivanhoecambridge.mall.fragments.HomeFragment;
import com.ivanhoecambridge.mall.interfaces.FavouriteInterface;
import com.ivanhoecambridge.mall.managers.FavouriteManager;
import com.ivanhoecambridge.mall.managers.NetworkManager;
import com.ivanhoecambridge.mall.movies.MovieManager;
import com.ivanhoecambridge.mall.movies.models.House;
import com.ivanhoecambridge.mall.movies.models.MovieDetail;
import com.ivanhoecambridge.mall.movies.models.Movies;
import com.ivanhoecambridge.mall.utility.Utility;
import com.ivanhoecambridge.mall.views.ActivityAnimation;
import com.ivanhoecambridge.mall.views.MovieRecyclerItemDecoration;
import com.ivanhoecambridge.mall.views.NewsRecyclerItemDecoration;
import com.ivanhoecambridge.mall.views.RecyclerViewFooter;

import java.util.ArrayList;
import java.util.Iterator;

import constants.MallConstants;

import static com.ivanhoecambridge.mall.adapters.MoviesRecyclerViewAdapter.ITEM_TYPE_THEATER_VIEWER;

/**
 * Created by Kay on 2016-05-05.
 */
public class NewsRecyclerViewAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<KcpContentPage> mKcpContentPagesNews;
    private ArrayList<KcpContentPage> mAnnouncements = new ArrayList<KcpContentPage>();
    private SocialFeedViewPagerAdapter mSocialFeedViewPagerAdapter;
    private FavouriteInterface mFavouriteInterface;
    private boolean showInstagramFeed = false;

    public NewsRecyclerViewAdapter(Context context, ArrayList<KcpContentPage> news) {
        initAdapter(context, news, false);
    }

    public NewsRecyclerViewAdapter(Context context, ArrayList<KcpContentPage> news, boolean emptyHolderExist) {
        initAdapter(context, news, emptyHolderExist);
    }

    public void initAdapter(Context context, ArrayList<KcpContentPage> news, boolean emptyHolderExist){
        mContext = context;

        mKcpContentPagesNews = news == null ? new ArrayList<KcpContentPage>() : news;
        mKcpContentPagesNews = removeInstagram(mKcpContentPagesNews);
        mKcpContentPagesNews = removeInterestIfNeeded(mKcpContentPagesNews);

        //emptyHolderExist = true meaning announcements are grouped together, empty holder exist for events and announcements
        if(emptyHolderExist) {
            mKcpContentPagesNews = removeAnnouncements(mKcpContentPagesNews); //return mKcpContentPagesNews after removing all announcements from it and save the removed announcements in mAnnouncements
            mKcpContentPagesNews = insertAnnouncements(mKcpContentPagesNews); //if Announcement's bigger than 0, insert it as CONTENT_TYPE_ANNOUNCEMENT

            mKcpContentPagesNews = insertEmptyCards(mKcpContentPagesNews, KcpContentTypeFactory.ITEM_TYPE_ANNOUNCEMENT, KcpContentTypeFactory.CONTENT_TYPE_EMPTY_ANNOUNCEMENT);
            mKcpContentPagesNews = insertEmptyCards(mKcpContentPagesNews, KcpContentTypeFactory.ITEM_TYPE_EVENT, KcpContentTypeFactory.CONTENT_TYPE_EMPTY_EVENT);
        }
    }

    public void updateData(ArrayList<KcpContentPage> kcpContentPages) {
        kcpContentPages = removeInterestIfNeeded(kcpContentPages);
        kcpContentPages = removeInstagram(kcpContentPages);
        kcpContentPages = removeAnnouncements(kcpContentPages);
        kcpContentPages = insertAnnouncements(kcpContentPages);

        kcpContentPages = insertEmptyCards(kcpContentPages, KcpContentTypeFactory.ITEM_TYPE_ANNOUNCEMENT, KcpContentTypeFactory.CONTENT_TYPE_EMPTY_ANNOUNCEMENT);
        kcpContentPages = insertEmptyCards(kcpContentPages, KcpContentTypeFactory.ITEM_TYPE_EVENT, KcpContentTypeFactory.CONTENT_TYPE_EMPTY_EVENT);

        if(mKcpContentPagesNews.equals(kcpContentPages)) return; //just return without updating the list because no changes detected
        mKcpContentPagesNews.clear();
        mKcpContentPagesNews.addAll(kcpContentPages);
        notifyDataSetChanged();
    }

    public void setFavouriteListener(FavouriteInterface favouriteInterface){
        mFavouriteInterface = favouriteInterface;
    }

    public void addData(ArrayList<KcpContentPage> kcpContentPages){
        removeLoadingImage();
        mKcpContentPagesNews.addAll(kcpContentPages);
        mKcpContentPagesNews = removeInterestIfNeeded(mKcpContentPagesNews);
        int curSize = getItemCount();
        notifyItemRangeInserted(curSize, kcpContentPages.size() - 1);
    }

    /**
     *
     * @param kcpContentPages
     * @return (kcpContentPages - interestcard) if you have set interest previously
     */
    private ArrayList<KcpContentPage> removeInterestIfNeeded(ArrayList<KcpContentPage> kcpContentPages){

        if(FavouriteManager.getInstance(mContext).getInterestFavSize() > 0 && kcpContentPages != null){
            for(int i = 0; i < kcpContentPages.size(); i++){
                KcpContentPage kcpContentPage = kcpContentPages.get(i);
                if(KcpContentTypeFactory.getContentType(kcpContentPage) == KcpContentTypeFactory.ITEM_TYPE_SET_MY_INTEREST) {
                    kcpContentPages.remove(i);
                    return kcpContentPages;
                }
            }
        }
        return kcpContentPages;
    }

    /**
     *
     * @param kcpContentPages
     * @return (kcpContentPages - instagram)
     */
    private ArrayList<KcpContentPage> removeInstagram(ArrayList<KcpContentPage> kcpContentPages){
        if(!showInstagramFeed){
            for(KcpContentPage kcpContentPage : kcpContentPages){
                int contentType = KcpContentTypeFactory.getContentType(kcpContentPage);
                if(contentType == KcpContentTypeFactory.ITEM_TYPE_INSTAGRAM){
                    kcpContentPages.remove(kcpContentPage);
                    return kcpContentPages;
                }
            }
        }
        return kcpContentPages;
    }

    /**
     *
     * @param kcpContentPages
     * @return (kcpContentPages - announcements)
     */
    private ArrayList<KcpContentPage> removeAnnouncements(ArrayList<KcpContentPage> kcpContentPages){
        mAnnouncements = new ArrayList<KcpContentPage>();
        Iterator<KcpContentPage> i = kcpContentPages.iterator();
        while (i.hasNext()) {
            KcpContentPage kcpContentPage = i.next();
            if(KcpContentTypeFactory.getContentType(kcpContentPage) == KcpContentTypeFactory.ITEM_TYPE_ANNOUNCEMENT) {
                mAnnouncements.add(kcpContentPage);
                i.remove();
            }
        }

        return kcpContentPages;
    }

    /**
     * insert Announcements after Events/Movies and Before Twitter, Interest
     */
    private ArrayList<KcpContentPage> insertAnnouncements(ArrayList<KcpContentPage> kcpContentPages) {
        //if announcement exists, it should go  "Events, Movies(If applicable) -------  <NEWS & ANNOUNCEMENT> ----- Twitter, Interest"
        if(mAnnouncements.size() > 0) {
            for(int i = 0; i < kcpContentPages.size(); i++){
                KcpContentPage kcpContentPage = kcpContentPages.get(i);
                if(KcpContentTypeFactory.getContentType(kcpContentPage) == KcpContentTypeFactory.ITEM_TYPE_MOVIE){
                    KcpContentPage announcement = new KcpContentPage();
                    announcement.setContentType(KcpContentTypeFactory.CONTENT_TYPE_ANNOUNCEMENT);
                    kcpContentPages.add(i + 1, announcement);
                    return kcpContentPages;
                } else if(KcpContentTypeFactory.getContentType(kcpContentPage) == KcpContentTypeFactory.ITEM_TYPE_TWITTER || KcpContentTypeFactory.getContentType(kcpContentPage) == KcpContentTypeFactory.ITEM_TYPE_SET_MY_INTEREST) {
                    KcpContentPage announcement = new KcpContentPage();
                    announcement.setContentType(KcpContentTypeFactory.CONTENT_TYPE_ANNOUNCEMENT);
                    kcpContentPages.add(i, announcement);
                    return kcpContentPages;
                }
            }

            KcpContentPage announcement = new KcpContentPage();
            announcement.setContentType(KcpContentTypeFactory.CONTENT_TYPE_ANNOUNCEMENT);
            kcpContentPages.add(announcement);
        }

        return kcpContentPages;
    }

    private ArrayList<KcpContentPage> insertEmptyCards(ArrayList<KcpContentPage> kcpContentPages, int contentTypeInt, String contentTypeString) {
        for(int i = 0; i < kcpContentPages.size(); i++){
            KcpContentPage kcpContentPage = kcpContentPages.get(i);
            if(KcpContentTypeFactory.getContentType(kcpContentPage) == contentTypeInt) {
                return kcpContentPages;
            }
        }

        //if no announcement exists, empty placeholder should exist in the end
        KcpContentPage announcement = new KcpContentPage();
        announcement.setContentType(contentTypeString);
        kcpContentPages.add(announcement);

        return kcpContentPages;
    }


    private int mFooterLayout;
    private boolean mFooterExist = false;
    private String mFooterText;
    private View.OnClickListener mOnClickListener;

    public void addFooter(String footerText, int footerLayout, View.OnClickListener onClickListener){
        mFooterExist = true;
        mFooterText = footerText;
        mFooterLayout = footerLayout;
        mOnClickListener = onClickListener;
        KcpContentPage fakeKcpContentPageForFooter = new KcpContentPage();
        fakeKcpContentPageForFooter.setContentType("footer");
        mKcpContentPagesNews.add(fakeKcpContentPageForFooter);
        notifyDataSetChanged();
    }

    public void prepareLoadingImage(){
        mKcpContentPagesNews.add(null);
        notifyItemInserted(mKcpContentPagesNews.size() - 1);
    }

    public void removeLoadingImage(){
        mKcpContentPagesNews.remove(mKcpContentPagesNews.size() - 1);
        notifyItemRemoved(mKcpContentPagesNews.size());
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        //for social feed specific
        public ViewPager vpTw;
        public ImageView ivSocialFeedLogo;
        public TextView tvSocialFeedUser;
        public LinearLayout llViewPagerCountDots;

        //for viewpager indicator
        public int dotsCount;
        public ImageView[] dots;


        public MainViewHolder(View v) {
            super(v);
            mView = v;
        }

        public void onSocialFeedCreated(int socialFeedIcon, String socialFeedType) {
            ivSocialFeedLogo.setImageResource(socialFeedIcon);
            tvSocialFeedUser.setText(socialFeedType);
        }
    }

    public class LoadingViewHolder extends MainViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.pbNewsAdapter);
        }
    }



    public class EventViewHolder extends MainViewHolder {
        public RelativeLayout rlAncmt;
        public ImageView ivAnnouncementLogo;
        public TextView  tvAnnouncementTitle;
        public TextView  tvAnnouncementDate;
        public ImageView  ivFav;
        public ImageView  ivSymbol;

        public EventViewHolder(View v) {
            super(v);
            rlAncmt             = (RelativeLayout)  v.findViewById(R.id.rlAncmt);
            ivAnnouncementLogo  = (ImageView) v.findViewById(R.id.ivAncmtLogo);
            tvAnnouncementTitle = (TextView)  v.findViewById(R.id.tvAncmtTitle);
            tvAnnouncementDate  = (TextView)  v.findViewById(R.id.tvAncmtDate);
            ivFav         = (ImageView)  v.findViewById(R.id.ivFav);
            ivSymbol         = (ImageView)  v.findViewById(R.id.ivSymbol);
        }
    }

    public class AncmtViewHolder extends MainViewHolder {
        public CardView cvAncmt;
        public TextView tvViewAll;
        public TextView tvEmptyState;
        public View separator;
        public RecyclerView rvAncmtChild;

        public AncmtViewHolder(View v) {
            super(v);
            cvAncmt                 = (CardView)  v.findViewById(R.id.cvAncmt);
            tvViewAll               = (TextView)  v.findViewById(R.id.tvViewAll);
            tvEmptyState            = (TextView)  v.findViewById(R.id.tvEmptyState);
            separator               = (View)  v.findViewById(R.id.separator);
            rvAncmtChild            = (RecyclerView)  v.findViewById(R.id.rvAncmtChild);

            if(rvAncmtChild.getLayoutManager() == null) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                rvAncmtChild.setLayoutManager(linearLayoutManager);

                NewsRecyclerItemDecoration itemDecoration = new NewsRecyclerItemDecoration(mContext, R.dimen.card_vertical_margin);
                rvAncmtChild.addItemDecoration(itemDecoration);
            }
        }
    }

    public class EmptyViewHolder extends MainViewHolder {
        public TextView tvEmptyTitle;
        public TextView tvEmptyState;

        public EmptyViewHolder(View v, String emptyTitle, String emptyMsg) {
            super(v);
            tvEmptyTitle            = (TextView)  v.findViewById(R.id.tvEmptyTitle);
            tvEmptyState            = (TextView)  v.findViewById(R.id.tvEmptyState);
            tvEmptyTitle.setText(emptyTitle);
            tvEmptyState.setText(emptyMsg);
        }
    }



    public class SetMyInterestViewHolder extends MainViewHolder {
        public SetMyInterestViewHolder (View v){
            super(v);
        }
    }

    public class TwitterFeedViewHolder extends MainViewHolder {
        public TwitterFeedViewHolder (View v, int socialFeedIcon, String socialFeedType) {
            super(v);
            vpTw                 = (ViewPager) v.findViewById(R.id.vpSocialFeed);
            ivSocialFeedLogo     = (ImageView) v.findViewById(R.id.ivSocialFeedLogo);
            tvSocialFeedUser     = (TextView) v.findViewById(R.id.tvSocialFeedUser);
            llViewPagerCountDots = (LinearLayout) v.findViewById(R.id.llViewPagerCircle);

            onSocialFeedCreated(socialFeedIcon, socialFeedType);
        }
    }

    public class MovieViewHolder extends MainViewHolder {
        public LinearLayout  llViewShowTimes;
        public RecyclerView  rvMovies;

        public MovieViewHolder (View v) {
            super(v);
            llViewShowTimes             = (LinearLayout) v.findViewById(R.id.llViewShowTimes);
            rvMovies                    = (RecyclerView) v.findViewById(R.id.rvMovies);

            if(rvMovies.getLayoutManager() == null) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                rvMovies.setLayoutManager(linearLayoutManager);
                MovieRecyclerItemDecoration itemDecoration = new MovieRecyclerItemDecoration(mContext, R.dimen.movie_poster_horizontal_margin);
                rvMovies.addItemDecoration(itemDecoration);
            }
        }
    }

    public class InstagramFeedViewHolder extends MainViewHolder {
        public InstagramFeedViewHolder (View v, int socialFeedIcon, String socialFeedType){
            super(v);

            vpTw                 = (ViewPager) v.findViewById(R.id.vpSocialFeed);
            ivSocialFeedLogo     = (ImageView) v.findViewById(R.id.ivSocialFeedLogo);
            tvSocialFeedUser     = (TextView) v.findViewById(R.id.tvSocialFeedUser);
            llViewPagerCountDots = (LinearLayout) v.findViewById(R.id.llViewPagerCircle);

            onSocialFeedCreated(socialFeedIcon, socialFeedType);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case KcpContentTypeFactory.ITEM_TYPE_LOADING:
                return new LoadingViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_loading_item, parent, false));
            case KcpContentTypeFactory.ITEM_TYPE_ANNOUNCEMENT:
                return new AncmtViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_ancmt, parent, false));
            case KcpContentTypeFactory.ITEM_TYPE_EMPTY_ANNOUNCEMENT:
                return new EmptyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_empty_ancmt_event, parent, false), mContext.getString(R.string.card_title_ancmt), mContext.getString(R.string.empty_placeholder_ancmt));
            case KcpContentTypeFactory.ITEM_TYPE_EMPTY_EVENT:
                return new EmptyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_empty_ancmt_event, parent, false), mContext.getString(R.string.card_title_event), mContext.getString(R.string.empty_placeholder_event));
            case KcpContentTypeFactory.ITEM_TYPE_EVENT:
                return new EventViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_event, parent, false));
            case KcpContentTypeFactory.ITEM_TYPE_MOVIE:
                return new MovieViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_movie_news_fragment, parent, false));
            case KcpContentTypeFactory.ITEM_TYPE_SET_MY_INTEREST:
                return new SetMyInterestViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_interest, parent, false));
            case KcpContentTypeFactory.ITEM_TYPE_TWITTER:
                return new TwitterFeedViewHolder(
                        LayoutInflater.from(mContext).inflate(R.layout.list_item_social_feed_pager, parent, false),
                        R.drawable.icn_twitter,
                        "@" + MallConstants.TWITTER_SCREEN_NAME);
            case KcpContentTypeFactory.ITEM_TYPE_INSTAGRAM:
                return new InstagramFeedViewHolder(
                        LayoutInflater.from(mContext).inflate(R.layout.list_item_social_feed_pager, parent, false),
                        R.drawable.icn_instagram,
                        "@" + MallConstants.INSTAGRAM_USER_NAME);
            case KcpContentTypeFactory.ITEM_TYPE_FOOTER:
                return new RecyclerViewFooter.FooterViewHolder(
                        LayoutInflater.from(mContext).inflate(mFooterLayout, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final KcpContentPage kcpContentPage = mKcpContentPagesNews.get(position);
        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        } else if (getItemViewType(position) == KcpContentTypeFactory.ITEM_TYPE_EVENT) {
            final EventViewHolder eventHolder = (EventViewHolder) holder;
            String imageUrl = kcpContentPage.getHighestResImageUrl();
            new GlideFactory().glideWithDefaultRatio(
                    mContext,
                    imageUrl,
                    eventHolder.ivAnnouncementLogo,
                    R.drawable.placeholder);

            String title = kcpContentPage.getTitle();
            eventHolder.tvAnnouncementTitle.setText(title);

            //if event is for one day, also show its begin/end hours
            String time = "";
            String startingTime = kcpContentPage.getFormattedDate(kcpContentPage.effectiveStartTime, Constants.DATE_FORMAT_EFFECTIVE);
            String endingTime = kcpContentPage.getFormattedDate(kcpContentPage.effectiveEndTime, Constants.DATE_FORMAT_EFFECTIVE);

            if(!startingTime.equals(endingTime)) {
                time = startingTime + " - " + endingTime;
            } else {
                String eventStartHour = kcpContentPage.getFormattedDate(kcpContentPage.effectiveStartTime, Constants.DATE_FORMAT_EVENT_HOUR);
                String eventEndingHour = kcpContentPage.getFormattedDate(kcpContentPage.effectiveEndTime, Constants.DATE_FORMAT_EVENT_HOUR);
                time = startingTime + " @ " + eventStartHour + " to " + eventEndingHour;
            }

            eventHolder.tvAnnouncementDate.setText(time);

            final String likeLink = kcpContentPage.getLikeLink();
            eventHolder.ivFav.setSelected(FavouriteManager.getInstance(mContext).isLiked(likeLink, kcpContentPage));
            eventHolder.ivFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.startSqueezeAnimationForFav(new Utility.SqueezeListener() {
                        @Override
                        public void OnSqueezeAnimationDone() {
                            eventHolder.ivFav.setSelected(!eventHolder.ivFav.isSelected());
                            FavouriteManager.getInstance(mContext).addOrRemoveFavContent(likeLink, kcpContentPage, mFavouriteInterface);
                        }
                    }, (Activity) mContext, eventHolder.ivFav);
                }
            });

            eventHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra(Constants.ARG_CONTENT_PAGE, kcpContentPage);

                    String transitionNameImage = mContext.getResources().getString(R.string.transition_news_image);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity)mContext,
                            Pair.create((View)eventHolder.ivAnnouncementLogo, transitionNameImage));

                    ActivityCompat.startActivityForResult((Activity) mContext, intent, Constants.REQUEST_CODE_VIEW_STORE_ON_MAP, options.toBundle());
                    ActivityAnimation.startActivityAnimation(mContext);
                }
            });

        } else if(getItemViewType(position) == KcpContentTypeFactory.ITEM_TYPE_ANNOUNCEMENT){
            AncmtViewHolder ancmtViewHolder = (AncmtViewHolder) holder;
            AncmtRecyclerViewAdapter ancmtRecyclerViewAdapter = new AncmtRecyclerViewAdapter(mContext, mAnnouncements, true);
            ancmtViewHolder.rvAncmtChild.setAdapter(ancmtRecyclerViewAdapter);

            ancmtViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MyPagesActivity.class);
                    intent.putExtra(Constants.ARG_CAT_NAME, mContext.getResources().getString(R.string.my_page_ancmt));
                    intent.putExtra(Constants.ARG_CONTENT_PAGE, mAnnouncements);
                    ((Activity) mContext).startActivityForResult(intent, Constants.REQUEST_CODE_MY_PAGE_TYPE);
                    ActivityAnimation.startActivityAnimation(mContext);
                }
            });

        } else if (getItemViewType(position) == KcpContentTypeFactory.ITEM_TYPE_MOVIE) {

            final MovieViewHolder movieViewHolder = (MovieViewHolder) holder;

            movieViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ShowtimesActivity.class);
                    ((MainActivity)mContext).startActivityForResult(intent, Constants.REQUEST_CODE_VIEW_STORE_ON_MAP);
                    ActivityAnimation.startActivityAnimation(mContext);
                }
            });

            ArrayList<MovieDetail> movieDetails = (ArrayList) MovieManager.sMovies.getMovies();
            if(movieDetails.size() != 0) {
                Movies.sortMoviesList(movieDetails);
                House house = (House) MovieManager.sTheaters.getHouse();
                MoviesRecyclerViewAdapter moviesRecyclerViewAdapter = new MoviesRecyclerViewAdapter(mContext, house, movieDetails, ITEM_TYPE_THEATER_VIEWER);
                movieViewHolder.rvMovies.setAdapter(moviesRecyclerViewAdapter);
                movieViewHolder.rvMovies.setNestedScrollingEnabled(false);
            } else {

            }
        } else if(getItemViewType(position) == KcpContentTypeFactory.ITEM_TYPE_SET_MY_INTEREST){
            SetMyInterestViewHolder intrstHolder = (SetMyInterestViewHolder) holder;
            intrstHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity)mContext).startActivityForResult(new Intent(mContext, InterestedCategoryActivity.class), Constants.REQUEST_CODE_CHANGE_INTEREST);
                    ActivityAnimation.startActivityAnimation(mContext);

                }
            });

        } else if(getItemViewType(position) == KcpContentTypeFactory.ITEM_TYPE_TWITTER || getItemViewType(position) == KcpContentTypeFactory.ITEM_TYPE_INSTAGRAM){
            mSocialFeedViewPagerAdapter = new SocialFeedViewPagerAdapter();
            MainViewHolder viewHolder = null;
            if(holder.getItemViewType() == KcpContentTypeFactory.ITEM_TYPE_TWITTER) {
                viewHolder = (TwitterFeedViewHolder) holder;
                mSocialFeedViewPagerAdapter.getTwitterViewPagerAdapter(mContext, HomeFragment.sTwitterFeedList, new SocialFeedViewPagerAdapter.OnSocialFeedClickListener() {
                    @Override
                    public void onSocialFeedClicked() {
                        if(!NetworkManager.isConnected(mContext)) return;
                        Intent intent = new Intent(mContext, SocialDetailActivity.class);
                        intent.putExtra(Constants.ARG_ACTIVITY_TYPE, KcpContentTypeFactory.ITEM_TYPE_TWITTER);
                        mContext.startActivity(intent);
                        ActivityAnimation.startActivityAnimation(mContext);
                    }
                });
            } else if(holder.getItemViewType() == KcpContentTypeFactory.ITEM_TYPE_INSTAGRAM){
                viewHolder = (InstagramFeedViewHolder) holder;
                mSocialFeedViewPagerAdapter.getInstaViewPagerAdapter(mContext, HomeFragment.sInstaFeedList, new SocialFeedViewPagerAdapter.OnSocialFeedClickListener() {
                    @Override
                    public void onSocialFeedClicked() {
                        if(!NetworkManager.isConnected(mContext)) return;
                        Intent intent = new Intent(mContext, SocialDetailActivity.class);
                        intent.putExtra(Constants.ARG_ACTIVITY_TYPE, KcpContentTypeFactory.ITEM_TYPE_INSTAGRAM);
                        mContext.startActivity(intent);
                        ActivityAnimation.startActivityAnimation(mContext);
                    }
                });
            }

            ViewGroup.LayoutParams vpTwParam = (ViewGroup.LayoutParams) viewHolder.vpTw.getLayoutParams();
            vpTwParam.height =  (int) (KcpUtility.getScreenWidth(mContext) / KcpUtility.getFloat(mContext, R.dimen.ancmt_image_ratio));
            viewHolder.vpTw.setLayoutParams(vpTwParam);
            initializeSocialFeedViews(viewHolder, mSocialFeedViewPagerAdapter);
        } else if(getItemViewType(position) == KcpContentTypeFactory.ITEM_TYPE_FOOTER){
            RecyclerViewFooter.FooterViewHolder footerViewHolder = (RecyclerViewFooter.FooterViewHolder) holder;
            footerViewHolder.mView.setOnClickListener(mOnClickListener);
            footerViewHolder.tvFooter.setText(mFooterText);
        }
    }

    private void initializeSocialFeedViews(final MainViewHolder holder, PagerAdapter pagerAdapter) {
        holder.vpTw.setAdapter(pagerAdapter);
        holder.vpTw.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < holder.dotsCount; i++) {
                    holder.dots[i].setImageDrawable(mContext.getResources().getDrawable(R.drawable.viewpager_circle_page_incdicator_dot_unselected));
                }
                holder.dots[position % holder.dots.length].setImageDrawable(mContext.getResources().getDrawable(R.drawable.viewpager_circle_page_incdicator_dot_selected));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        Log.d("NewsRecyclerViewAdapter", "setUiPageViewController");
        setUiPageViewController(holder);
    }

    private View getAncmtView(ViewGroup viewGroup, final KcpContentPage kcpContentPage){
        View view = null;
        try {
            view = ((Activity) mContext).getLayoutInflater().inflate(
                    R.layout.layout_item_ancmt,
                    viewGroup,
                    false);

            final ImageView ivAncmtLogo = (ImageView) view.findViewById(R.id.ivAncmtLogo);
            TextView tvAncmtTitle = (TextView) view.findViewById(R.id.tvAncmtTitle);
            TextView tvAncmtDate = (TextView) view.findViewById(R.id.tvAncmtDate);

            String imageUrl = kcpContentPage.getHighestResImageUrl();
            new GlideFactory().glideWithDefaultRatio(
                    mContext,
                    imageUrl,
                    ivAncmtLogo,
                    R.drawable.placeholder_square);

            String title = kcpContentPage.getTitle();
            tvAncmtTitle.setText(title);

            String startingTime = kcpContentPage.getFormattedDate(kcpContentPage.effectiveStartTime, Constants.DATE_FORMAT_ANNOUNCEMENT_GROUPED);
            tvAncmtDate.setText(startingTime);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra(Constants.ARG_CONTENT_PAGE, kcpContentPage);

                    String transitionNameImage = mContext.getResources().getString(R.string.transition_news_image);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity)mContext,
                            Pair.create((View)ivAncmtLogo, transitionNameImage));

                    ActivityCompat.startActivityForResult((Activity) mContext, intent, Constants.REQUEST_CODE_VIEW_STORE_ON_MAP, options.toBundle());
                    ActivityAnimation.startActivityAnimation(mContext);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public int getItemCount() {
        return mKcpContentPagesNews == null ? 0 : mKcpContentPagesNews.size();
    }

    @Override
    public int getItemViewType(int position) {
        KcpContentPage kcpContentPage = mKcpContentPagesNews.get(position);
        return KcpContentTypeFactory.getContentType(kcpContentPage);
    }

    public SocialFeedViewPagerAdapter getSocialFeedViewPagerAdapter(){
        return mSocialFeedViewPagerAdapter;
    }

    /** circle page indicator*/
    private void setUiPageViewController(MainViewHolder holder) {
        holder.dotsCount = 5; //viewpagerAdapter.getCount(); used for actual counting
        holder.dots = new ImageView[holder.dotsCount];

        holder.llViewPagerCountDots.removeAllViews(); //prevent from creating second indicator
        for (int i = 0; i < holder.dotsCount; i++) {
            holder.dots[i] = new ImageView(mContext);
            holder.dots[i].setImageDrawable(mContext.getResources().getDrawable(R.drawable.viewpager_circle_page_incdicator_dot_unselected));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            int viewPagerCircleMargin = (int) mContext.getResources().getDimension(R.dimen.viewpager_circle_dot_margin);
            params.setMargins(viewPagerCircleMargin, 0, viewPagerCircleMargin, 0);
            holder.llViewPagerCountDots.addView(holder.dots[i], params);
        }

        if(holder.dots.length > 0) holder.dots[0].setImageDrawable(mContext.getResources().getDrawable(R.drawable.viewpager_circle_page_incdicator_dot_selected));
    }
}
