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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.codelab.R;

public class FailActivity extends AppCompatActivity implements RewardedVideoAdListener {
    private static final String HINT_EXTRA_KEY = "hint";
    private static final String HINT_WEIGHT_EXTRA_KEY = "hint_weight";

    private RewardedVideoAd mAd;
    private RewardItem mRewardItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail);

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


        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        // If ad is already loaded, show the hint button.
        if (mAd.isLoaded()) {
            showHintButton();
        }
    }

    private void showHintButton() {
        Button hint_button = (Button) findViewById(R.id.hint_button);
        hint_button.setVisibility(View.VISIBLE);
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
        if (mRewardItem != null) {
            Intent intent = new Intent(FailActivity.this, GameActivity.class);
            intent.putExtra(HINT_EXTRA_KEY, true);
            float hint_weight = 100.0f / (float) mRewardItem.getAmount() - 1.0f;
            intent.putExtra(HINT_WEIGHT_EXTRA_KEY, hint_weight);
            startActivity(intent);
        } else {
            Intent intent = new Intent(FailActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        showHintButton();
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
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
}
