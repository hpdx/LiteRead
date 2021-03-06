package com.wenen.literead.ui.image;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wenen.literead.R;
import com.wenen.literead.adapter.image.ImageAdapter;
import com.wenen.literead.api.APIUrl;
import com.wenen.literead.http.HttpClient;
import com.wenen.literead.http.HttpSubscriber;
import com.wenen.literead.model.image.ImageModel;
import com.wenen.literead.ui.BaseActivity;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import rx.Subscriber;

public class ThumbleActivity extends BaseActivity {


    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.indeterminate_horizontal_progress_toolbar)
    MaterialProgressBar indeterminateHorizontalProgressToolbar;
    @Bind(R.id.rcl_image_list)
    RecyclerView rclImageList;
    @Bind(R.id.nsv_parent)
    NestedScrollView nsvParent;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    private int id;
    private String title;
    private Subscriber subscribers;
    private ArrayList<ImageModel.ListEntity> listEntities = new ArrayList<>();
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumble);
        ButterKnife.bind(this);
        title = getIntent().getStringExtra("title");
        id = getIntent().getIntExtra("id", 0);
        toolbar.setNavigationIcon(R.mipmap.ic_action_arrow_left);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rclImageList.setLayoutManager(staggeredGridLayoutManager);
        mAdapter = new ImageAdapter(listEntities, title);
        rclImageList.setAdapter(mAdapter);
        mAdapter.getRandomHeight(listEntities);
        SpacesItemDecoration decoration = new SpacesItemDecoration(2, 10, true);
        rclImageList.addItemDecoration(decoration);
        rclImageList.setHasFixedSize(true);
        rclImageList.setItemAnimator(new DefaultItemAnimator());
        getImage(id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle(title);
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public SpacesItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private void getImage(final int id) {
        subscribers = new HttpSubscriber<ImageModel>(indeterminateHorizontalProgressToolbar) {
            @Override
            public void onCompleted() {
                super.onCompleted();
                mAdapter.getRandomHeight(listEntities);
                mAdapter.updateList(listEntities);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                showSnackBar(indeterminateHorizontalProgressToolbar, e.toString(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getImage(id);
                    }
                });
            }

            @Override
            public void onNext(ImageModel imageModel) {
                super.onNext(imageModel);
                listEntities.clear();
                for (ImageModel.ListEntity listEntitie : imageModel.list
                        ) {
                    listEntities.add(listEntitie);
                    Log.e("list", listEntitie.src);
                }
            }
        };
        HttpClient.getSingle(APIUrl.TIANGOU_IMG_URL).getImg(id, subscribers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
