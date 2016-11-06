package com.example.sam.drawerlayoutprac.Partner;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.icu.text.MessagePattern;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.nhaarman.listviewanimations.appearance.ViewAnimator;
import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;
import com.yalantis.euclid.library.EuclidListAdapter;
import com.yalantis.euclid.library.EuclidState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by cuser on 2016/10/9.
 */
public class PartnerFragment extends Fragment {

    private final static String TAG = "SearchActivity";

    private ProgressDialog progressDialog;
    AsyncTask retrievePartnerTask;
    ListView listview;

    //testing
    private static final int MAX_DELAY_SHOW_DETAILS_ANIMATION = 500;
    private static final int CIRCLE_RADIUS_DP = 50;
    private static final int REVEAL_ANIMATION_DURATION = 1000;
    private static final int ANIMATION_DURATION_SHOW_PROFILE_DETAILS = 500;
    private static final int ANIMATION_DURATION_SHOW_PROFILE_BUTTON = 300;
    protected RelativeLayout mWrapper;
    private View mOverlayListItemView;
    protected TextView mTextViewProfileName;
    protected TextView mTextViewProfileDescription;
    protected FrameLayout mToolbar;
    protected RelativeLayout mToolbarProfile;
    private Animation mProfileButtonShowAnimation;
    protected View mButtonProfile;
    private float mInitialProfileButtonX;
    private AnimatorSet mOpenProfileAnimatorSet;
    protected LinearLayout mProfileDetails;

    // testing
    private EuclidState mState = EuclidState.Closed;
    private AnimatorSet mCloseProfileAnimatorSet;
    private static final int STEP_DELAY_HIDE_DETAILS_ANIMATION = 80;
    private static final int ANIMATION_DURATION_CLOSE_PROFILE_DETAILS = 500;
    private SwingLeftInAnimationAdapter mListViewAnimationAdapter;
    private ViewAnimator mListViewAnimator;

    @Override
    public void onResume() {
        super.onResume();
        backBtnPressed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        View view = inflater.inflate(R.layout.fragment_partner, viewGroup, false);
        listview = (ListView) view.findViewById(com.example.sam.drawerlayoutprac.R.id.list_partner);
        // testing
        mWrapper = (RelativeLayout) view.findViewById(com.example.sam.drawerlayoutprac.R.id.wrapper);
        mTextViewProfileName = (TextView) view.findViewById(com.example.sam.drawerlayoutprac.R.id.text_view_profile_name);
        mTextViewProfileDescription = (TextView) view.findViewById(com.example.sam.drawerlayoutprac.R.id.text_view_profile_description);
        mToolbar = (FrameLayout) view.findViewById(com.example.sam.drawerlayoutprac.R.id.toolbar_list);
        mToolbarProfile = (RelativeLayout) view.findViewById(com.example.sam.drawerlayoutprac.R.id.toolbar_profile);
        mButtonProfile = view.findViewById(com.example.sam.drawerlayoutprac.R.id.button_profile);
        mProfileDetails = (LinearLayout) view.findViewById(com.example.sam.drawerlayoutprac.R.id.wrapper_profile_details);
        mButtonProfile.post(new Runnable() {
            @Override
            public void run() {
                mInitialProfileButtonX = mButtonProfile.getX();
            }
        });



        // get data
        // 檢查使用者是否有連線功能
//        if (Common.networkConnected(getActivity())) {
//            // send request to server and get the response - 重點在new這個動作
//            String url = Common.URL + "/live2/Partner";
//            Log.d("url", url);
//            // 注意:listview的setAdapter是在PartnerGetTextTask裡面設定的
//            retrievePartnerTask = new PartnerGetTextTask(getContext(), this.listview).execute(url);
//        } else {
//            Util.showToast(getActivity(), "no network");
//        }
        // end of get data

        // set up floatingBtn click Listener
        LinearLayout ll_view = (LinearLayout) viewGroup.getParent();
        CoordinatorLayout cdl_view = (CoordinatorLayout) ll_view.getParent();
        FloatingActionButton floatingBtn = (FloatingActionButton) cdl_view.findViewById(R.id.floatingBtn);

        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.showToast(getContext(), "ftBtn clicked");
                Fragment fragment = new PartnerMapFragment();
                Util.switchFragment(PartnerFragment.this, fragment);
            }
        });
        try {
            initList();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }// end of onCreateView

    private void initList() throws ExecutionException, InterruptedException {
        String url = Common.URL + "/live2/Partner";
        List<MemVO> memVOList = (List<MemVO>)new PartnerGetTextTask(getContext(), this.listview).execute(url).get();
        Map<String, Object> profileMap;
        List<Map<String, Object>> profilesList = new ArrayList<>();

        for (int i = 0; i < memVOList.size(); i++) {
            MemVO myVO = memVOList.get(i);

            profileMap = new HashMap<>();

            //放入文字資料
            profileMap.put(PartnerListAdapter.KEY_MEMID, myVO.getMemId());
            profileMap.put(PartnerListAdapter.KEY_NAME, myVO.getMemName());
            profileMap.put(PartnerListAdapter.KEY_DESCRIPTION_SHORT, myVO.getMemIntro());
            profileMap.put(PartnerListAdapter.KEY_DESCRIPTION_FULL, myVO.getMemIntro());
            profilesList.add(profileMap);
        }

        mListViewAnimationAdapter = new SwingLeftInAnimationAdapter(new PartnerListAdapter(getContext(), R.layout.list_item, profilesList));
        mListViewAnimationAdapter.setAbsListView(this.listview);
        mListViewAnimator = mListViewAnimationAdapter.getViewAnimator();
        if (mListViewAnimator != null) {
            mListViewAnimator.setAnimationDurationMillis(getAnimationDurationCloseProfileDetails());
            mListViewAnimator.disableAnimations();
        }

        PartnerFragment.this.listview.setAdapter(mListViewAnimationAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Util.showToast(getContext(), "listview clicked");
                Map<String, Object> mItem = (Map<String, Object>) parent.getItemAtPosition(position);
                //Util.showToast(getContext(), mItem.get(PartnerListAdapter.KEY_NAME).toString() + "***");
                mState = EuclidState.Opening;
                try {
                    showProfileDetails(mItem, view);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showProfileDetails(Map<String, Object> item, final View view) throws ExecutionException, InterruptedException {
        listview.setEnabled(false);
        int sScreenWidth = getResources().getDisplayMetrics().widthPixels;

        int profileDetailsAnimationDelay = PartnerFragment.MAX_DELAY_SHOW_DETAILS_ANIMATION * Math.abs(view.getTop()) / sScreenWidth;

        addOverlayListItem(item, view);
        startRevealAnimation(profileDetailsAnimationDelay);
        animateOpenProfileDetails(profileDetailsAnimationDelay);
    }

    private void addOverlayListItem(Map<String, Object> item, View view) throws ExecutionException, InterruptedException {
        if (mOverlayListItemView == null) {
            // 載入overlay_list_item
            mOverlayListItemView = getActivity().getLayoutInflater().inflate(R.layout.overlay_list_item_partner, mWrapper, false);
        } else {
            mWrapper.removeView(mOverlayListItemView);
        }
        mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.view_avatar_overlay).setBackground(buildAvatarCircleOverlay());
        // 建立新的Thread去DB抓圖片
        ImageView profileImg = (ImageView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.image_view_reveal_avatar);
        ImageView profileOverlay = (ImageView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.image_view_avatar);
        String memId = (String) item.get(PartnerListAdapter.KEY_MEMID);
        String url = Common.URL + "/live2/Partner";
        int imageSize = 250;
        new PartnerGetImageTask(profileImg).execute(url, memId, imageSize);
        new PartnerGetImageTask(profileOverlay).execute(url, memId, imageSize);
        // end of 建立新的Thread去DB抓圖片

        // 將文字data放上view
        ((TextView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.text_view_name)).setText((String) item.get(EuclidListAdapter.KEY_NAME));
        ((TextView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.text_view_description)).setText((String) item.get(EuclidListAdapter.KEY_DESCRIPTION_SHORT));
        setProfileDetailsInfo(item);
        // end of 將文字data放上view

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = view.getTop() + mToolbar.getHeight();
        params.bottomMargin = -(view.getBottom() - listview.getHeight());
        mWrapper.addView(mOverlayListItemView, params);
        mToolbar.bringToFront();
    }

    private void startRevealAnimation(final int profileDetailsAnimationDelay) {
        mOverlayListItemView.post(new Runnable() {
            @Override
            public void run() {
                getAvatarRevealAnimator().start();
                getAvatarShowAnimator(profileDetailsAnimationDelay).start();
            }
        });
    }

    private SupportAnimator getAvatarRevealAnimator() {
        final LinearLayout mWrapperListItemReveal = (LinearLayout) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.wrapper_list_item_reveal);

        int finalRadius = Math.max(mOverlayListItemView.getWidth(), mOverlayListItemView.getHeight());
        int sScreenWidth = getResources().getDisplayMetrics().widthPixels;
        int sProfileImageHeight = getContext().getResources().getDimensionPixelSize(R.dimen.height_profile_image);
        final SupportAnimator mRevealAnimator = ViewAnimationUtils.createCircularReveal(
                mWrapperListItemReveal,
                sScreenWidth / 2,
                sProfileImageHeight / 2,
                dpToPx(getCircleRadiusDp() * 2),
                finalRadius);
        mRevealAnimator.setDuration(getRevealAnimationDuration());
        mRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                mWrapperListItemReveal.setVisibility(View.VISIBLE);
                mOverlayListItemView.setX(0);
            }

            @Override
            public void onAnimationEnd() {

            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        return mRevealAnimator;
    }

    protected int getRevealAnimationDuration() {
        return REVEAL_ANIMATION_DURATION;
    }

    private Animator getAvatarShowAnimator(int profileDetailsAnimationDelay) {
        final Animator mAvatarShowAnimator = ObjectAnimator.ofFloat(mOverlayListItemView, View.Y, mOverlayListItemView.getTop(), mToolbarProfile.getBottom());
        mAvatarShowAnimator.setDuration(profileDetailsAnimationDelay + getAnimationDurationShowProfileDetails());
        mAvatarShowAnimator.setInterpolator(new DecelerateInterpolator());
        return mAvatarShowAnimator;
    }

    protected int getAnimationDurationShowProfileDetails() {
        return ANIMATION_DURATION_SHOW_PROFILE_DETAILS;
    }

    private void animateOpenProfileDetails(int profileDetailsAnimationDelay) {
        createOpenProfileButtonAnimation();
        getOpenProfileAnimatorSet(profileDetailsAnimationDelay).start();
    }

    private void createOpenProfileButtonAnimation() {
        if (mProfileButtonShowAnimation == null) {
            mProfileButtonShowAnimation = AnimationUtils.loadAnimation(getContext(), com.example.sam.drawerlayoutprac.R.anim.profile_button_scale);
            mProfileButtonShowAnimation.setDuration(getAnimationDurationShowProfileButton());
            mProfileButtonShowAnimation.setInterpolator(new AccelerateInterpolator());
            mProfileButtonShowAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mButtonProfile.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    protected int getAnimationDurationShowProfileButton() {
        return ANIMATION_DURATION_SHOW_PROFILE_BUTTON;
    }

    private AnimatorSet getOpenProfileAnimatorSet(int profileDetailsAnimationDelay) {
        if (mOpenProfileAnimatorSet == null) {
            List<Animator> profileAnimators = new ArrayList<>();
            profileAnimators.add(getOpenProfileToolbarAnimator());
            profileAnimators.add(getOpenProfileDetailsAnimator());

            mOpenProfileAnimatorSet = new AnimatorSet();
            mOpenProfileAnimatorSet.playTogether(profileAnimators);
            mOpenProfileAnimatorSet.setDuration(getAnimationDurationShowProfileDetails());
        }
        mOpenProfileAnimatorSet.setStartDelay(profileDetailsAnimationDelay);
        mOpenProfileAnimatorSet.setInterpolator(new DecelerateInterpolator());
        return mOpenProfileAnimatorSet;
    }

    private Animator getOpenProfileToolbarAnimator() {
        Animator mOpenProfileToolbarAnimator = ObjectAnimator.ofFloat(mToolbarProfile, View.Y, -mToolbarProfile.getHeight(), 0);
        mOpenProfileToolbarAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mToolbarProfile.setX(0);
                mToolbarProfile.bringToFront();
                mToolbarProfile.setVisibility(View.VISIBLE);
                mProfileDetails.setX(0);
                mProfileDetails.bringToFront();
                mProfileDetails.setVisibility(View.VISIBLE);

                mButtonProfile.setX(mInitialProfileButtonX);
                mButtonProfile.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mButtonProfile.startAnimation(mProfileButtonShowAnimation);

                mState = EuclidState.Opened;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return mOpenProfileToolbarAnimator;
    }

    private Animator getOpenProfileDetailsAnimator() {
        Animator mOpenProfileDetailsAnimator = ObjectAnimator.ofFloat(mProfileDetails, View.Y,
                getResources().getDisplayMetrics().heightPixels,
                getResources().getDimensionPixelSize(com.example.sam.drawerlayoutprac.R.dimen.height_profile_picture_with_toolbar)); // 在這邊設定profile detail要有多高
        return mOpenProfileDetailsAnimator;
    }

    // 把大頭貼變成圓的 - 方法1
    private ShapeDrawable buildAvatarCircleOverlay() {
        int radius = 666;
        int sScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int sProfileImageHeight = getContext().getResources().getDimensionPixelSize(R.dimen.height_profile_image);

        ShapeDrawable overlay = new ShapeDrawable(new RoundRectShape(null,
                new RectF(
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2)),
                new float[]{radius, radius, radius, radius, radius, radius, radius, radius}));
        overlay.getPaint().setColor(getContext().getResources().getColor(com.example.sam.drawerlayoutprac.R.color.gray));

        return overlay;
    }

    // 把大頭貼變成圓的 - 方法2
    public int dpToPx(int dp) {
        return Math.round((float) dp * getContext().getResources().getDisplayMetrics().density);
    }

    // 把大頭貼變成圓的 - 方法3
    protected int getCircleRadiusDp() {
        return CIRCLE_RADIUS_DP;
    }

    private void setProfileDetailsInfo(Map<String, Object> item) {
        mTextViewProfileName.setText("profileNameHere");
        mTextViewProfileDescription.setText("I come from Mars.");
        mTextViewProfileName.setText((String) item.get(EuclidListAdapter.KEY_NAME));
        mTextViewProfileDescription.setText((String) item.get(EuclidListAdapter.KEY_DESCRIPTION_FULL));
    }



    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Util.showToast(getContext(),"backBtnPressed");
//                    mWrapper.removeView(mOverlayListItemView);
//                    mToolbarProfile.setVisibility(View.INVISIBLE);
//                    mProfileDetails.setVisibility(View.INVISIBLE);
//                    PartnerFragment.this.listview.bringToFront();
//                    return true;

                    if (getState() == EuclidState.Opened) {
                        animateCloseProfileDetails();
                    } else if (getState() == EuclidState.Closed) {
                        //this.onKey(v, keyCode, event);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public EuclidState getState() {
        return mState;
    }

    private void animateCloseProfileDetails() {
        mState = EuclidState.Closing;
        getCloseProfileAnimatorSet().start();
    }

    private AnimatorSet getCloseProfileAnimatorSet() {
        if (mCloseProfileAnimatorSet == null) {
            Animator profileToolbarAnimator = ObjectAnimator.ofFloat(mToolbarProfile, View.X,
                    0, mToolbarProfile.getWidth());

            Animator profilePhotoAnimator = ObjectAnimator.ofFloat(mOverlayListItemView, View.X,
                    0, mOverlayListItemView.getWidth());
            profilePhotoAnimator.setStartDelay(getStepDelayHideDetailsAnimation());

            Animator profileButtonAnimator = ObjectAnimator.ofFloat(mButtonProfile, View.X,
                    mInitialProfileButtonX, mOverlayListItemView.getWidth() + mInitialProfileButtonX);
            profileButtonAnimator.setStartDelay(getStepDelayHideDetailsAnimation() * 2);

            Animator profileDetailsAnimator = ObjectAnimator.ofFloat(mProfileDetails, View.X,
                    0, mToolbarProfile.getWidth());
            profileDetailsAnimator.setStartDelay(getStepDelayHideDetailsAnimation() * 2);

            List<Animator> profileAnimators = new ArrayList<>();
            profileAnimators.add(profileToolbarAnimator);
            profileAnimators.add(profilePhotoAnimator);
            profileAnimators.add(profileButtonAnimator);
            profileAnimators.add(profileDetailsAnimator);

            mCloseProfileAnimatorSet = new AnimatorSet();
            mCloseProfileAnimatorSet.playTogether(profileAnimators);
            mCloseProfileAnimatorSet.setDuration(getAnimationDurationCloseProfileDetails());
            mCloseProfileAnimatorSet.setInterpolator(new AccelerateInterpolator());
            mCloseProfileAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mListViewAnimator != null) {
                        mListViewAnimator.reset();
                        mListViewAnimationAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mToolbarProfile.setVisibility(View.INVISIBLE);
                    mButtonProfile.setVisibility(View.INVISIBLE);
                    mProfileDetails.setVisibility(View.INVISIBLE);

                    PartnerFragment.this.listview.setEnabled(true);
                    mListViewAnimator.disableAnimations();

                    mState = EuclidState.Closed;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return mCloseProfileAnimatorSet;
    }

    protected int getStepDelayHideDetailsAnimation() {
        return STEP_DELAY_HIDE_DETAILS_ANIMATION;
    }

    protected int getAnimationDurationCloseProfileDetails() {
        return ANIMATION_DURATION_CLOSE_PROFILE_DETAILS;
    }


}// end
