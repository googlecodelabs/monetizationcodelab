// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.recipe.firebase.firebaserecipecodelab;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class FailActivity extends AppCompatActivity implements RewardedVideoAdListener {
    private static final String DEFAULT_AD_UNIT_ID = "ca-app-pub-0977440612291676/4209413500";
    private static final String AD_UNIT_KEY = "task_completion_ad_unit_id";
    private static final String HINT_EXTRA_KEY = "hint";
    private static final String HINT_WEIGHT_EXTRA_KEY = "hint_weight";
    private static final String USER_PROPERTY_KEY = "RewardAmount";

    private RewardedVideoAd mAd;
    private RewardItem mRewardItem;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail);

        // Initialize Firebase Analytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Firebase RemoteConfig instance and set default config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put(AD_UNIT_KEY, DEFAULT_AD_UNIT_ID);
        mFirebaseRemoteConfig.setDefaults(defaults);

        // Fetch AdUnitID from Firebase RemoteConfig server.
        fetchAdUnitID();

        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        mAd.loadAd(
                mFirebaseRemoteConfig.getString(AD_UNIT_KEY),
                new AdRequest.Builder().build()
        );

        // Move to GameActivity after showing rewarded video ads if player click
        // "PLAY AGAIN WITH HINT BY WATCHING ADS!" button.
        Button hint_button = (Button) findViewById(R.id.hint_button);
        hint_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mAd.isLoaded()) {
                    mAd.show();
                }
            }
        });

        // Move to GameActivity directory if player click "PLAY AGAIN!" button.
        Button again_button = (Button) findViewById(R.id.again_button);
        again_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(FailActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        // Move to MainActivity if player click "HOME" button.
        Button home_button = (Button) findViewById(R.id.home_button);
        home_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(FailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRewarded(RewardItem reward) {
        mRewardItem = reward;
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Intent intent = new Intent(FailActivity.this, GameActivity.class);
        intent.putExtra(HINT_EXTRA_KEY, true);
        intent.putExtra(HINT_WEIGHT_EXTRA_KEY, (float)mRewardItem.getAmount());
        mFirebaseAnalytics.setUserProperty(
                USER_PROPERTY_KEY,
                Integer.valueOf(mRewardItem.getAmount()).toString()
        );
        startActivity(intent);
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Button hint_button = (Button) findViewById(R.id.hint_button);
        hint_button.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    private void fetchAdUnitID () {
        OnCompleteListener<Void> onCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) mFirebaseRemoteConfig.activateFetched();
            }
        };

        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            // This forces Remote Config to fetch from server every time.
            mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(this, onCompleteListener);
        } else {
            mFirebaseRemoteConfig.fetch().addOnCompleteListener(this, onCompleteListener);
        }
    }
}
