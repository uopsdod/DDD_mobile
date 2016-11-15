package com.example.sam.drawerlayoutprac.Partner;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
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
import java.util.Iterator;
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
    public static String URL_Partner = Common.URL + "/android/live2/partner.do";

    ListView listview;

    //進入個人詳細頁面動畫屬性
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

    // 處理聊天訊息回來畫面
    private FloatingActionButton mMapFloatingBtn;
    private Map<String, Object> mItemSelected;
    private View mViewSelected;

    // closing動畫屬性
    private EuclidState mState = EuclidState.ProfilePageClosed;
    private AnimatorSet mCloseProfileAnimatorSet;
    private static final int STEP_DELAY_HIDE_DETAILS_ANIMATION = 80;
    private static final int ANIMATION_DURATION_CLOSE_PROFILE_DETAILS = 500;
    private SwingLeftInAnimationAdapter mListViewAnimationAdapter;
    private ViewAnimator mListViewAnimator;

    // 紀錄要將訊息傳給誰的toMemId
    private String toMemId;

    // 處理從訊息視窗切到navigation後，切換到其他頁面時當掉的bug
    public static boolean backBtnPressed = false;

    @Override
    public void onResume() {
        super.onResume();
        backBtnPressed();
        // 處理聊天訊息回來畫面
        if (getState() == EuclidState.ProfilePageOpened && backBtnPressed == true) { // 改
            PartnerFragment.this.backBtnPressed = false;
            try {
                showProfileDetails(mItemSelected, mViewSelected);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // end of // 處理聊天訊息回來畫面
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        View view = inflater.inflate(R.layout.fragment_partner, viewGroup, false);
        findViews(view);
        setFloatingBtnClickListener(viewGroup);
        setButtonProfileClickListener();
        initList();
        return view;
    }// end of onCreateView

    private void setButtonProfileClickListener() {
        this.mButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Util.showToast(getContext(), "進入聊天視窗");
                mMapFloatingBtn.setVisibility(View.INVISIBLE);
                Fragment fragment = new PartnerChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ToMemId", PartnerFragment.this.toMemId);
                fragment.setArguments(bundle);
                Util.switchFragment(PartnerFragment.this, fragment);
            }
        });
    }

    // onCreateView方法01
    private void findViews(View view) {
        listview = (ListView) view.findViewById(com.example.sam.drawerlayoutprac.R.id.list_partner);
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


    }

    // onCreateView方法02
    private void setFloatingBtnClickListener(ViewGroup viewGroup) {
        // set up floatingBtn click Listener
        LinearLayout ll_view = (LinearLayout) viewGroup.getParent();
        CoordinatorLayout cdl_view = (CoordinatorLayout) ll_view.getParent();
        this.mMapFloatingBtn = (FloatingActionButton) cdl_view.findViewById(R.id.floatingBtn);

        mMapFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Util.showToast(getContext(), "ftBtn clicked");
                Fragment fragment = new PartnerMapFragment();
                Util.switchFragment(PartnerFragment.this, fragment);
            }
        });

    }

    // onCreateView方法03
    private void initList() {
        // get data
        // 檢查使用者是否有連線功能
        List<MemVO> memVOList = null;
        if (Common.networkConnected(getActivity())) {
            // send request to server and get the response - 重點在new這個動作
            String url = PartnerFragment.URL_Partner;
            Log.d("url", url);
            try {
                memVOList = (List<MemVO>) new PartnerGetAllTextTask(getContext(), this.listview).execute(url).get();
                // 去掉自己
                SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE,getActivity().MODE_PRIVATE);
                String memid = preferences_r.getString("memId", null);
                Iterator<MemVO> itr = memVOList.iterator();
                while(itr.hasNext()){
                    if (itr.next().getMemId().equals(memid)){
                        itr.remove();
                        break;
                    }
                }// end while
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Util.showToast(getActivity(), "no network");
        }
        // end of get data
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

        // mListViewAnimationAdapter - 是當進入個人詳細頁面後，返回時所用到的animator。而這個animator會放在此mListViewAnimationAdapter裡面。而我們會將真正屬於自己要用的dapter再放入此adpater裡面。
        mListViewAnimationAdapter = new SwingLeftInAnimationAdapter(new PartnerListAdapter(getContext(), R.layout.list_item, profilesList));
        mListViewAnimationAdapter.setAbsListView(PartnerFragment.this.listview);
        mListViewAnimator = mListViewAnimationAdapter.getViewAnimator();
        if (mListViewAnimator != null) {
            mListViewAnimator.setAnimationDurationMillis(getAnimationDurationCloseProfileDetails());
            mListViewAnimator.disableAnimations();
        }

        PartnerFragment.this.listview.setAdapter(mListViewAnimationAdapter);
        PartnerFragment.this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Util.showToast(getContext(), "listview clicked");
                mItemSelected = (Map<String, Object>) parent.getItemAtPosition(position);
                mViewSelected = view; // testing
                //Util.showToast(getContext(), mItem.get(PartnerListAdapter.KEY_NAME).toString() + "***");
                mState = EuclidState.ProfilePageOpening;
                try {
                    showProfileDetails(mItemSelected, view);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 此方法由initList() -> PartnerFragment.this.listview.setOnItemClickListener事件觸發到這邊
    // 進入個人詳細頁面動畫01
    private void showProfileDetails(Map<String, Object> item, final View view) throws ExecutionException, InterruptedException {
        listview.setEnabled(false);
        int sScreenWidth = getResources().getDisplayMetrics().widthPixels;

        int profileDetailsAnimationDelay = PartnerFragment.MAX_DELAY_SHOW_DETAILS_ANIMATION * Math.abs(view.getTop()) / sScreenWidth;
        // 處理聊天訊息回來畫面
        if (getState() == EuclidState.ProfilePageOpened) {
            // 模擬重新開啟一次個人頁面
            mState = EuclidState.ProfilePageOpening;
            // error處理: the child view already has parent view - try to call its parent's removeView() method
            mWrapper.removeView(mOverlayListItemView);
            mOverlayListItemView = null;

        }
        // end of 處理聊天訊息回來畫面

        addOverlayListItem(item, view);
        startRevealAnimation(profileDetailsAnimationDelay);
        animateOpenProfileDetails(profileDetailsAnimationDelay);
    }

    // 進入個人詳細頁面動畫02
    private void addOverlayListItem(Map<String, Object> item, View view) throws ExecutionException, InterruptedException {
        if (mOverlayListItemView == null) {
            // 載入overlay_list_item
            mOverlayListItemView = getActivity().getLayoutInflater().inflate(R.layout.overlay_list_item_partner, mWrapper, false);
        } else {
            // 此狀況是給一般正常狀況用的(非從聊天訊息回來的): 列表與個人頁面間的切換
            mWrapper.removeView(mOverlayListItemView);
            mOverlayListItemView = getActivity().getLayoutInflater().inflate(R.layout.overlay_list_item_partner, mWrapper, false);
        }
        mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.view_avatar_overlay).setBackground(buildAvatarCircleOverlay());
        // 建立新的Thread去DB抓圖片
        ImageView profileImg = (ImageView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.image_view_reveal_avatar);
        ImageView profileOverlay = (ImageView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.image_view_avatar);
            // 將現在頁面的memId放入實體變數，若使用者進入訊息視窗，則將memId也帶過去
        PartnerFragment.this.toMemId = (String) item.get(PartnerListAdapter.KEY_MEMID);
        String url = PartnerFragment.URL_Partner;
        int imageSize = 250;
        new PartnerGetOneImageTask(profileImg).execute(url, toMemId, imageSize);
        new PartnerGetOneImageTask(profileOverlay).execute(url, toMemId, imageSize);
        // end of 建立新的Thread去DB抓圖片

        // 將文字data放上view
        ((TextView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.text_view_name)).setText((String) item.get(EuclidListAdapter.KEY_NAME));
        ((TextView) mOverlayListItemView.findViewById(com.example.sam.drawerlayoutprac.R.id.text_view_description)).setText((String) item.get(EuclidListAdapter.KEY_DESCRIPTION_SHORT));
        setProfileDetailsInfo(item);
        // end of 將文字data放上view

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = view.getTop() + mToolbar.getHeight();
        params.bottomMargin = (listview.getHeight() - view.getBottom());
        mWrapper.addView(mOverlayListItemView, params);
        mToolbar.bringToFront();
    }

    // 進入個人詳細頁面動畫03
    private void startRevealAnimation(final int profileDetailsAnimationDelay) {
        mOverlayListItemView.post(new Runnable() {
            @Override
            public void run() {
                getAvatarRevealAnimator().start();
                getAvatarShowAnimator(profileDetailsAnimationDelay).start();
            }
        });
    }

    // 進入個人詳細頁面動畫03
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
        mRevealAnimator.setDuration(PartnerFragment.REVEAL_ANIMATION_DURATION);
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

    // 進入個人詳細頁面動畫04
    private Animator getAvatarShowAnimator(int profileDetailsAnimationDelay) {
        final Animator mAvatarShowAnimator = ObjectAnimator.ofFloat(mOverlayListItemView, View.Y, mOverlayListItemView.getTop(), mToolbarProfile.getBottom());
        mAvatarShowAnimator.setDuration(profileDetailsAnimationDelay + PartnerFragment.ANIMATION_DURATION_SHOW_PROFILE_DETAILS);
        mAvatarShowAnimator.setInterpolator(new DecelerateInterpolator());
        return mAvatarShowAnimator;
    }

    // 進入個人詳細頁面動畫05
    private void animateOpenProfileDetails(int profileDetailsAnimationDelay) {
        createOpenProfileButtonAnimation();
        getOpenProfileAnimatorSet(profileDetailsAnimationDelay).start();
        // 我無法確定以上兩個方法誰先結束
    }

    // 進入個人詳細頁面動畫06
    private void createOpenProfileButtonAnimation() {
        // 處理聊天訊息回來畫面
        mProfileButtonShowAnimation = null;
        // end of 處理聊天訊息回來畫面
        if (mProfileButtonShowAnimation == null) {
            mProfileButtonShowAnimation = AnimationUtils.loadAnimation(getContext(), com.example.sam.drawerlayoutprac.R.anim.profile_button_scale);
            mProfileButtonShowAnimation.setDuration(PartnerFragment.ANIMATION_DURATION_SHOW_PROFILE_BUTTON);
            mProfileButtonShowAnimation.setInterpolator(new AccelerateInterpolator());
            mProfileButtonShowAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mButtonProfile.setVisibility(View.VISIBLE);
                    //Log.d("mButtonProfile","mButtonProfile - onAnimationStart - set VISIBLE");
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

    // 進入個人詳細頁面動畫07
    private AnimatorSet getOpenProfileAnimatorSet(int profileDetailsAnimationDelay) {
        // 處理聊天訊息回來畫面
        mOpenProfileAnimatorSet = null;
        // end of 處理聊天訊息回來畫面
        if (mOpenProfileAnimatorSet == null) {
            List<Animator> profileAnimators = new ArrayList<>();
            profileAnimators.add(getOpenProfileToolbarAnimator());
            profileAnimators.add(getOpenProfileDetailsAnimator());

            mOpenProfileAnimatorSet = new AnimatorSet();
            mOpenProfileAnimatorSet.playTogether(profileAnimators);
            mOpenProfileAnimatorSet.setDuration(PartnerFragment.ANIMATION_DURATION_SHOW_PROFILE_DETAILS);
        }
        mOpenProfileAnimatorSet.setStartDelay(profileDetailsAnimationDelay);
        mOpenProfileAnimatorSet.setInterpolator(new DecelerateInterpolator());
        return mOpenProfileAnimatorSet;
    }

    // 進入個人詳細頁面動畫08
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
                //Log.d("mButtonProfile","mButtonProfile - bringToFront");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // 關鍵-解決點第一個人->進入聊天室窗->按下返回鍵後訊息小圓圈不見的bug
                mButtonProfile.setX(883.0f);
                // end of 關鍵-解決點第一個人->進入聊天室窗->按下返回鍵後訊息小圓圈不見的bug
                mButtonProfile.startAnimation(mProfileButtonShowAnimation);
//                Log.d("mButtonProfile","mButtonProfile - startAnimation");
//                Log.d("mButtonProfile - X: ", Float.toString(mButtonProfile.getX()));
//                Log.d("mButtonProfile - Y: ", Float.toString(mButtonProfile.getY()));

                mState = EuclidState.ProfilePageOpened;
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

    // 進入個人詳細頁面動畫09
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

    // 設定個人頁面資料
    private void setProfileDetailsInfo(Map<String, Object> item) {
        mTextViewProfileName.setText((String) item.get(EuclidListAdapter.KEY_NAME));
        mTextViewProfileDescription.setText((String) item.get(EuclidListAdapter.KEY_DESCRIPTION_FULL));
    }

    // closing動畫01
    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    //Util.showToast(getContext(),"backBtnPressed");
                    PartnerFragment.this.backBtnPressed = true;

                    if (getState() == EuclidState.ProfilePageOpened) {
                        mProfileButtonShowAnimation = null;
                        mOpenProfileAnimatorSet = null;
                        animateCloseProfileDetails();

                        return true;
                    }
                }
                return false;
            }
        });
    }

    // closing動畫02
    public EuclidState getState() {
        return mState;
    }

    // closing動畫03
    private void animateCloseProfileDetails() {
//        Log.d("method - ", "animateCloseProfileDetails");
        mState = EuclidState.ProfilePageClosing;
        getCloseProfileAnimatorSet().start();
    }

    // closing動畫04
    private AnimatorSet getCloseProfileAnimatorSet() {
        // 處理聊天訊息回來畫面
        mCloseProfileAnimatorSet = null;
        // end of 處理聊天訊息回來畫面
        if (mCloseProfileAnimatorSet == null) {
//            Log.d("method - ", "getCloseProfileAnimatorSet");
            Animator profileToolbarAnimator = ObjectAnimator.ofFloat(mToolbarProfile, View.X,
                    0, mToolbarProfile.getWidth());

            Animator profilePhotoAnimator = ObjectAnimator.ofFloat(mOverlayListItemView, View.X,
                    0, mOverlayListItemView.getWidth());
            profilePhotoAnimator.setStartDelay(getStepDelayHideDetailsAnimation());

            Animator profileButtonAnimator = ObjectAnimator.ofFloat(mButtonProfile, View.X,
                    mInitialProfileButtonX, mOverlayListItemView.getWidth() + mInitialProfileButtonX);
//            Log.d("mButtonProfile","mButtonProfile - ObjectAnimator.ofFloat");
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

                    mState = EuclidState.ProfilePageClosed;
                    // 處理聊天訊息回來畫面
                    mWrapper.removeView(mOverlayListItemView);
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

    // closing動畫05
    protected int getStepDelayHideDetailsAnimation() {
        return STEP_DELAY_HIDE_DETAILS_ANIMATION;
    }    // closing動畫01

    // closing動畫06
    protected int getAnimationDurationCloseProfileDetails() {
        return ANIMATION_DURATION_CLOSE_PROFILE_DETAILS;
    }


}// end
