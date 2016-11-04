package com.example.sam.drawerlayoutprac;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.yalantis.euclid.library.EuclidActivity;
import com.yalantis.euclid.library.EuclidListAdapter;
import com.yalantis.euclid.library.EuclidState;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private EuclidState mState = EuclidState.Closed;
    private static final int MAX_DELAY_SHOW_DETAILS_ANIMATION = 500;
    private static final int CIRCLE_RADIUS_DP = 50;
    protected RelativeLayout mWrapper;
    private View mOverlayListItemView;
    protected TextView mTextViewProfileName;
    protected TextView mTextViewProfileDescription;
    protected FrameLayout mToolbar;
    private static final int REVEAL_ANIMATION_DURATION = 1000;
    private static final int ANIMATION_DURATION_SHOW_PROFILE_DETAILS = 500;
    protected RelativeLayout mToolbarProfile;
    private Animation mProfileButtonShowAnimation;
    private static final int ANIMATION_DURATION_SHOW_PROFILE_BUTTON = 300;
    protected View mButtonProfile;
    private float mInitialProfileButtonX;
    private AnimatorSet mOpenProfileAnimatorSet;
    protected LinearLayout mProfileDetails;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        View view = inflater.inflate(R.layout.fragment_partner, viewGroup, false);
        listview = (ListView) view.findViewById(R.id.list_partner);
        // testing
        mWrapper = (RelativeLayout) view.findViewById(com.yalantis.euclid.library.R.id.wrapper);
        mTextViewProfileName = (TextView) view.findViewById(com.yalantis.euclid.library.R.id.text_view_profile_name);
        mTextViewProfileDescription = (TextView) view.findViewById(com.yalantis.euclid.library.R.id.text_view_profile_description);
        mToolbar = (FrameLayout) view.findViewById(com.yalantis.euclid.library.R.id.toolbar_list);
        mToolbarProfile = (RelativeLayout) view.findViewById(com.yalantis.euclid.library.R.id.toolbar_profile);
        mButtonProfile = view.findViewById(com.yalantis.euclid.library.R.id.button_profile);
        mProfileDetails = (LinearLayout) view.findViewById(com.yalantis.euclid.library.R.id.wrapper_profile_details);
        mButtonProfile.post(new Runnable() {
            @Override
            public void run() {
                mInitialProfileButtonX = mButtonProfile.getX();
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Util.showToast(getContext(),"listview clicked");
                Map<String, Object> mItem = (Map<String, Object>) parent.getItemAtPosition(position);
                Util.showToast(getContext(),mItem.get(PartnerListAdapter.KEY_NAME).toString()+"***");
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


        // get data
            // 檢查使用者是否有連線功能
        if (Common.networkConnected(getActivity())) {
            // send request to server and get the response - 重點在new這個動作
            String url = Common.URL + "/live2/Partner";
            Log.d("url", url);
            retrievePartnerTask = new RetrievePartnerTask().execute(url);
        } else {
            Util.showToast(getActivity(), "no network");
        }
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

        return view;
    }

    class RetrievePartnerTask extends AsyncTask<String, Void, List<MemVO>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PartnerFragment.this.getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected List<MemVO> doInBackground(String... params) {
            String url = params[0]; // 傳入的Common.URL字串
            String jsonIn;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll"); // 在這邊控制請求參數
            try {
                jsonIn = getRemoteData(url, jsonObject.toString());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                return null;
            }

            // 處理Oracle Date型態與gson之間的格式問題
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
            Type listType = new TypeToken<List<MemVO>>() {
            }.getType();
            // end of // 處理Oracle Date型態與gson之間的格式問題

            // 回傳至onPostExecute(List<MemVO> items) - 備註:此為 UI main thread在呼叫的
            return gson.fromJson(jsonIn, listType);
        }

        @Override
        protected void onPostExecute(List<MemVO> items) {
            Map<String, Object> profileMap;
            List<Map<String, Object>> profilesList = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                MemVO myVO = items.get(i);

                profileMap = new HashMap<>();

                //放入文字資料
                profileMap.put(PartnerListAdapter.KEY_MEMID, myVO.getMemId());
                profileMap.put(PartnerListAdapter.KEY_NAME, myVO.getMemName());
                profileMap.put(PartnerListAdapter.KEY_DESCRIPTION_SHORT, myVO.getMemIntro());
                profileMap.put(PartnerListAdapter.KEY_DESCRIPTION_FULL, myVO.getMemIntro());
                profilesList.add(profileMap);
            }
            // 放入ListView的Adapter
            listview.setAdapter(new PartnerListAdapter(getContext(), R.layout.list_item, profilesList));

            progressDialog.cancel();
        }

        private String getRemoteData(String url, String jsonOut) throws IOException {
            StringBuilder jsonIn = new StringBuilder();
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true); // allow inputs
            connection.setDoOutput(true); // allow outputs
            connection.setUseCaches(false); // do not use a cached copy
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "UTF-8");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write(jsonOut); // 塞入請求參數
            Log.d(TAG, "jsonOut: " + jsonOut);
            bw.close(); // 送出request

            int responseCode = connection.getResponseCode();
            // 確認能與server建立Socket連線
            if (responseCode == 200) {
                // connection.getInputStream() - 等待server回應，如果還沒有回應就hold在這邊
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    jsonIn.append(line); // 重點:把server的資料拿到手
                }
            } else {
                Log.d(TAG, "response code: " + responseCode);
            }
            connection.disconnect();
            Log.d(TAG, "jsonIn: " + jsonIn);
            return jsonIn.toString();
        }
    }


    private void showProfileDetails(Map<String, Object> item, final View view) throws ExecutionException, InterruptedException {
        listview.setEnabled(false);
        int sScreenWidth = getResources().getDisplayMetrics().widthPixels;

        int profileDetailsAnimationDelay = getMaxDelayShowDetailsAnimation() * Math.abs(view.getTop())
                / sScreenWidth;

        addOverlayListItem(item, view);
        startRevealAnimation(profileDetailsAnimationDelay);
        animateOpenProfileDetails(profileDetailsAnimationDelay);
    }

    protected int getMaxDelayShowDetailsAnimation() {
        return MAX_DELAY_SHOW_DETAILS_ANIMATION;
    }

    private void addOverlayListItem(Map<String, Object> item, View view) throws ExecutionException, InterruptedException {
        if (mOverlayListItemView == null) {
            mOverlayListItemView = getActivity().getLayoutInflater().inflate(com.yalantis.euclid.library.R.layout.overlay_list_item, mWrapper, false);
        } else {
            mWrapper.removeView(mOverlayListItemView);
        }

        mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.view_avatar_overlay).setBackground(buildAvatarCircleOverlay());

        ImageView profileImg = (ImageView) mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.image_view_reveal_avatar);
        ImageView profileOverlay = (ImageView) mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.image_view_avatar);
        String memId = (String)item.get(PartnerListAdapter.KEY_MEMID);
        String url = Common.URL + "/live2/Partner";
        int imageSize = 250;
        new PartnerGetImageTask(profileImg).execute(url, memId, imageSize);
        new PartnerGetImageTask(profileOverlay).execute(url, memId, imageSize);
        //profileOverlay.setImageResource(R.drawable.mem10000001);



//        Picasso.with(getContext()).load((Integer) item.get(EuclidListAdapter.KEY_AVATAR))
//                .resize(sScreenWidth, sProfileImageHeight).centerCrop()
//                .placeholder(com.yalantis.euclid.library.R.color.blue)
//                .into((ImageView) mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.image_view_reveal_avatar));
//        Picasso.with(getContext()).load((Integer) item.get(EuclidListAdapter.KEY_AVATAR))
//                .resize(sScreenWidth, sProfileImageHeight).centerCrop()
//                .placeholder(com.yalantis.euclid.library.R.color.blue)
//                .into((ImageView) mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.image_view_avatar));

        ((TextView) mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.text_view_name)).setText((String) item.get(EuclidListAdapter.KEY_NAME));
        ((TextView) mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.text_view_description)).setText((String) item.get(EuclidListAdapter.KEY_DESCRIPTION_SHORT));
        setProfileDetailsInfo(item);

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
        final LinearLayout mWrapperListItemReveal = (LinearLayout) mOverlayListItemView.findViewById(com.yalantis.euclid.library.R.id.wrapper_list_item_reveal);

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
            mProfileButtonShowAnimation = AnimationUtils.loadAnimation(getContext(), com.yalantis.euclid.library.R.anim.profile_button_scale);
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
                getResources().getDimensionPixelSize(com.yalantis.euclid.library.R.dimen.height_profile_picture_with_toolbar));
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
        overlay.getPaint().setColor(getContext().getResources().getColor(com.yalantis.euclid.library.R.color.gray));

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
}
