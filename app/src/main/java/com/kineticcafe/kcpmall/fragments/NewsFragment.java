package com.kineticcafe.kcpmall.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kineticcafe.kcpandroidsdk.models.KcpNavigationRoot;
import com.kineticcafe.kcpmall.constants.Constants;
import com.kineticcafe.kcpmall.R;
import com.kineticcafe.kcpmall.activities.MainActivity;
import com.kineticcafe.kcpmall.adapters.NewsRecyclerViewAdapter;
import com.kineticcafe.kcpmall.views.NewsRecyclerItemDecoration;
import com.kineticcafe.kcpmall.widget.EndlessRecyclerViewScrollListener;

/**
 * Created by Kay on 2016-05-04.
 */
public class NewsFragment extends BaseFragment {
    public NewsRecyclerViewAdapter mNewsRecyclerViewAdapter;
    public EndlessRecyclerViewScrollListener mEndlessRecyclerViewScrollListener;
    private TextView tvEmptyState;

    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        RecyclerView rvNews = (RecyclerView) view.findViewById(R.id.rv);
        rvNews.setNestedScrollingEnabled(true);
        setupRecyclerView(rvNews);
        tvEmptyState = (TextView) view.findViewById(R.id.tvEmptyState);
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.themeColor);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mMainActivity.setOnRefreshListener(new MainActivity.RefreshListener() {
                    @Override
                    public void onRefresh(int msg) {
                        srl.setRefreshing(false);
                        mMainActivity.showSnackBar(msg, 0, null);
                    }
                });
                HomeFragment.getInstance().initializeHomeData();
                mEndlessRecyclerViewScrollListener.onLoadDone();
            }
        });

        return view;
    }

    public void setEmptyState(@Nullable String warningMsg){
        if(mMainActivity != null) mMainActivity.setEmptyState(tvEmptyState, warningMsg);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        mNewsRecyclerViewAdapter = new NewsRecyclerViewAdapter(
                getActivity(),
                KcpNavigationRoot.getInstance().getNavigationpage(Constants.EXTERNAL_CODE_FEED).getKcpContentPageList(true));
        recyclerView.setAdapter(mNewsRecyclerViewAdapter);

        mEndlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore() {
                HomeFragment.getInstance().downloadMoreFeeds(Constants.EXTERNAL_CODE_FEED);
            }
        };
        recyclerView.addOnScrollListener(mEndlessRecyclerViewScrollListener);

        NewsRecyclerItemDecoration itemDecoration = new NewsRecyclerItemDecoration(getActivity(), R.dimen.card_vertical_margin);
        recyclerView.addItemDecoration(itemDecoration);

    }
}
