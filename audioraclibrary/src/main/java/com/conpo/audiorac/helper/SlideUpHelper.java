package com.conpo.audiorac.helper;

import android.content.Context;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.conpo.audiorac.util.Utils;
import com.google.android.material.navigation.NavigationView;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import com.conpo.audiorac.library.R;

/**
 * 슬라이드업 메뉴 제어 헬퍼
 */
public class SlideUpHelper {
    private static Context mContext;
    private static View mParentView;
    private static ViewGroup mPopupMask;
    private static NavigationView mNavigationView;
    private static SlideUp mSlideUpMenu;       // 슬라이드 메뉴
    private static TextView mTvTitle;

    private static boolean mIsGlobal = false;

    private static SlideUpHelper instance;

    public void initialize(Context context, View parent) {
        instance = this;

        mContext = context;
        mParentView = parent;
        mPopupMask = mParentView.findViewById(R.id.popup_mask);
        mPopupMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGlobal())
                    hide();
            }
        });

        View slideUpView = mParentView.findViewById(R.id.slideup_view);

        mTvTitle = mParentView.findViewById(R.id.tv_slideup_title);
        mNavigationView = mParentView.findViewById(R.id.navView);

        mSlideUpMenu = new SlideUpBuilder(slideUpView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        mPopupMask.setVisibility(visibility);
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(true)
                .withGesturesEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .withSlideFromOtherView(mParentView)
                .build();
    }

    public static boolean isGlobal() {
        return mIsGlobal;
    }

    public static SlideUpHelper show(boolean isGlobal) {
        // parent view 및 그 이하 view 에서 열린 SlideupView 를 모두 닫기
        View slideUpView = mParentView.findViewById(R.id.slideup_view);
        slideUpView.setVisibility(View.GONE);

        mIsGlobal = isGlobal;

//        ViewGroup view = mParentView.findViewById(R.id.content_slide_up_view);
//        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//        if (isGlobal) {
//            p.setMargins(0, 0, 0, 0);
//        } else {
//            p.setMargins(0, 0, 0, (int)Utils.convertDpToPixel(56, mContext));
//        }
//        view.requestLayout();
//        view.bringToFront();

        mSlideUpMenu.show();
        return instance;
    }

    public static boolean isVisible() {
        return mSlideUpMenu.isVisible();
    }

    public static SlideUpHelper hide() {
        mPopupMask.setVisibility(View.GONE);
        mSlideUpMenu.hide();
        return instance;
    }

    public static SlideUpHelper setTitle(String title) {
        mTvTitle.setText(title);

        return instance;
    }

    public static SlideUpHelper setMenu(int menuResId) {
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(menuResId);

        return instance;
    }

    public static Menu getMenu() {
        return mNavigationView.getMenu();
    }

    public static SlideUpHelper setMenuItemSelectedListener(NavigationView.OnNavigationItemSelectedListener listener) {
        mNavigationView.setNavigationItemSelectedListener(listener);

        return instance;
    }

    public static void showMenu(String title, int menuResId, boolean isGlobal, NavigationView.OnNavigationItemSelectedListener listener) {
        setTitle(title);
        setMenu(menuResId);
        setMenuItemSelectedListener(listener);
        show(isGlobal);
    }

}
