package com.wenen.literead.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wenen.literead.R;
import com.wenen.literead.adapter.image.ImageListAdapter;
import com.wenen.literead.api.APIUrl;
import com.wenen.literead.http.HttpClient;
import com.wenen.literead.model.image.ImageListModel;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;

/**
 * Created by Wen_en on 16/8/12.
 */
public class ImageListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @Bind(R.id.rcl_image_list)
    RecyclerView rclImageList;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    private Subscriber subscriber;
    private boolean isRefreshed = false;
    /**
     * URL
     */
    private int id = 1;
    private int page = 1;
    private ArrayList<ImageListModel.TngouEntity> list = new ArrayList<>();
    private ImageListAdapter mAdapter;
    private boolean hasLoad;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            id = savedInstanceState.getInt("id");
            page = savedInstanceState.getInt("page");
        } else
            id = getArguments().getInt("id");
        page = 1;
        Log.e("id", id + "");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", id);
        outState.putInt("page", page);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_list, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rclImageList.setLayoutManager(linearLayoutManager);
        mAdapter = new ImageListAdapter(list);
        rclImageList.setAdapter(mAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("id_page", "id=" + id + "page=" + page);
        if (!hasLoad)
            getImgThumbleList(id, page);
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void refreshIf(boolean needRefresh) {
        if (needRefresh) {
            doRefresh();
        }
    }

    private void doRefresh() {
        getImgThumbleList(id, page);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private boolean shouldRefreshOnVisibilityChange(boolean isVisibleToUser) {
        Log.e("id_page", "id=" + id + "page=" + page);
        return isVisibleToUser && !isRefreshed;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        refreshIf(shouldRefreshOnVisibilityChange(isVisibleToUser));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void getImgThumbleList(int id, int page) {
        subscriber = new Subscriber<ImageListModel>() {
            @Override
            public void onCompleted() {
                isRefreshed = true;
                hasLoad = true;
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
                mAdapter.updateList(list);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("error", e.getMessage().toString());
            }

            @Override
            public void onNext(ImageListModel imageListModel) {
                list.clear();
                for (ImageListModel.TngouEntity tnEntity : imageListModel.tngou
                        ) {
                    list.add(tnEntity);
                }
            }
        };
        HttpClient.getSingle(APIUrl.TIANGOU_IMG_URL).getIMGThumbleList(id, page, subscriber);
    }
}
