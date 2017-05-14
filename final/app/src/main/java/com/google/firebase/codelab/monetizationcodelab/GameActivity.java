/*　Copyright 2017 Google Inc.

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.　*/

package com.google.firebase.codelab.monetizationcodelab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.codelab.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class GameActivity extends AppCompatActivity {
    private static final String DEFAULT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6807086514";
    private static final String HINT_EXTRA_KEY = "hint";
    private static final String HINT_WEIGHT_EXTRA_KEY = "hint_weight";

    private static final float DEFAULT_HINT_WEIGHT = 1.0f;
    private static final int READY_GO_DURATION = 2000;
    private static final int START_TIME = 2000;
    private static final int IMAGE_CHANGE_DURATION = 7000;
    private static final int TOTAL_GAME_DURATION = 13000;

    private RewardedVideoAd mAd;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private void fetchSettings() {
        OnCompleteListener<Void> onCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) mFirebaseRemoteConfig.activateFetched();
            }
        };

        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(this, onCompleteListener);
        } else {
            mFirebaseRemoteConfig.fetch().addOnCompleteListener(this, onCompleteListener);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("game_over_rewarded_video_adunit_id", DEFAULT_AD_UNIT_ID);
        mFirebaseRemoteConfig.setDefaults(defaults);

        fetchSettings();

        setContentView(R.layout.activity_game);

        // Run the game.
        game();

        // Move to SuccessActivity if player selects correct area.
        final Button clear_button = (Button) findViewById(R.id.clear_button);
        clear_button.setBackgroundColor(Color.TRANSPARENT);
        clear_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                clearStage();
            }
        });

        // Move to FailActivity if player selects not correct area.
        final Button fail_button = (Button) findViewById(R.id.fail_button);
        fail_button.setBackgroundColor(Color.TRANSPARENT);
        fail_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                failStage();
            }
        });

        // Load rewarded video ads.
        String adUnitId = mFirebaseRemoteConfig.getString("game_over_rewarded_video_adunit_id");

        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.loadAd(
                adUnitId,
                new AdRequest.Builder().build()
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAd.pause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAd.resume(this);
    }

    private void clearStage () {
        mFirebaseAnalytics.logEvent("stage_clear", null);
        Intent intent = new Intent(GameActivity.this, SuccessActivity.class);
        startActivity(intent);
    }

    private void failStage () {
        mFirebaseAnalytics.logEvent("stage_failed", null);
        Intent intent = new Intent(GameActivity.this, FailActivity.class);
        startActivity(intent);
    }

    private void game () {
        final TextView countdown_text = (TextView) findViewById(R.id.countdown_text);
        final RelativeLayout background_before_start =
                (RelativeLayout) findViewById(R.id.background_before_start);
        countdown_text.setText(R.string.ready);
        ScaleAnimation scale_countdown = new ScaleAnimation(
                1.0f, 0.3f, 1.0f, 0.3f,
                Animation.RELATIVE_TO_PARENT, 0.5f,
                Animation.RELATIVE_TO_PARENT, 0.5f);
        scale_countdown.setDuration(READY_GO_DURATION);
        scale_countdown.setFillAfter(true);
        countdown_text.startAnimation(scale_countdown);
        scale_countdown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                countdown_text.setText(R.string.go);
                countdown_text.setTextSize(240.0f);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countdown_text.setText("");
                        background_before_start.setVisibility(View.INVISIBLE);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ImageView before_image =
                                        (ImageView) findViewById(R.id.before_image);
                                AlphaAnimation alpha = new AlphaAnimation(1, 0);
                                alpha.setFillAfter(true);
                                alpha.setDuration(IMAGE_CHANGE_DURATION);
                                before_image.startAnimation(alpha);
                            }
                        }, START_TIME);
                        ImageView red_bar = (ImageView) findViewById(R.id.red_bar);
                        ScaleAnimation scale = new ScaleAnimation(1,0,1,1);
                        scale.setDuration(TOTAL_GAME_DURATION);
                        scale.setFillAfter(true);
                        red_bar.startAnimation(scale);
                        scale.setAnimationListener(new Animation.AnimationListener(){
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                failStage();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                }, READY_GO_DURATION);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        Intent intent = getIntent();
        boolean hint = intent.getBooleanExtra(HINT_EXTRA_KEY, false);
        float hint_weight = intent.getFloatExtra(HINT_WEIGHT_EXTRA_KEY, DEFAULT_HINT_WEIGHT);
        RelativeLayout hint_mask_view = (RelativeLayout) findViewById(R.id.hint_mask);
        hint_mask_view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                hint_weight));
        if (!hint) {
            hint_mask_view.setVisibility(View.INVISIBLE);
        }
    }
}
