package org.godotengine.plugin.android.godotapplovinmax;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.ArraySet;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MaxRewarded extends GodotPlugin implements MaxRewardedAdListener {

    private MaxRewardedAd rewardedAd;
    private String MaxAdUnitIdRew = "";
    private int retryAttempt;



    /**
     * Base constructor passing a {@link Godot} instance through which the plugin can access Godot's
     * APIs and lifecycle events.
     *
     * @param godot
     */
    public MaxRewarded(Godot godot) {
        super(godot);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return this.getClass().getSimpleName().toString();
    }


    public Set<SignalInfo> getPluginSignals(){
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("rewarded_ad_loaded"));
        signals.add(new SignalInfo("rewarded_ad_load_failed", String.class, int.class));
        signals.add(new SignalInfo("rewarded_ad_displayed"));
        signals.add(new SignalInfo("rewarded_ad_display_failed", String.class));
        signals.add(new SignalInfo("user_earned_reward", String.class, int.class));
        signals.add(new SignalInfo("rewarded_ad_closed"));
        signals.add(new SignalInfo("rewarded_ad_clicked"));
        signals.add(new SignalInfo("rewarded_ad_not_ready"));



        return signals;
    }




    @UsedByGodot
    public void load_rewarded(String rewAdId){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                load_rewarded(rewAdId, getGodot().getActivity());
            }
        });

    }

    @UsedByGodot
    public void show_rewarded(){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                show_rewarded(getGodot().getActivity());
            }
        });

    }

    @UsedByGodot
    public boolean get_is_rewarded_ad_ready(){
        return rewardedAd.isReady();
    }


    //The plugin runs load_ad in loop until the rewarded ad is finally loaded with a maximum delay of 64 seconds.
    //Loading multiple ads with different ad ids at a time is not supported.
    //If one rewarded ad is loaded and another load ad is requested with a different ad id,
    //the new rewarded ad overrides the previously loaded ad.
    //It is required to manually load new rewarded ad after one ad is displayed and closed.

    public void load_rewarded(String rewAdId, Context ctx){
        MaxAdUnitIdRew = rewAdId;
        createRewardedAd(rewAdId, ctx);
    }


    public void show_rewarded(Activity activity){
        if ( rewardedAd.isReady() )
        {
            // `this` is the activity that will be used to show the ad
            rewardedAd.showAd( activity );
        }
        else {
            emitSignal("rewarded_ad_not_ready");
        }
    }


    void createRewardedAd(String rewAdId, Context ctx)
    {
        rewardedAd = com.applovin.mediation.ads.MaxRewardedAd.getInstance( rewAdId, ctx.getApplicationContext() );
        rewardedAd.setListener( this );

        rewardedAd.loadAd();
    }













    //Callbacks

    @Override
    public void onUserRewarded(@NonNull MaxAd maxAd, @NonNull MaxReward maxReward) {
        emitSignal("user_earned_reward", maxReward.getLabel(), maxReward.getAmount());
    }

    @Override
    public void onAdLoaded(@NonNull MaxAd maxAd) {
        emitSignal("rewarded_ad_loaded");
        retryAttempt = 0;
    }

    @Override
    public void onAdDisplayed(@NonNull MaxAd maxAd) {
        emitSignal("rewarded_ad_displayed");
    }

    @Override
    public void onAdHidden(@NonNull MaxAd maxAd) {
        emitSignal("rewarded_ad_closed");
    }

    @Override
    public void onAdClicked(@NonNull MaxAd maxAd) {
        emitSignal("rewarded_ad_clicked");
    }

    @Override
    public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
        long delayMillis = 100;
        retryAttempt++;
        delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );
        emitSignal("rewarded_ad_load_failed", maxError.toString(), retryAttempt);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                rewardedAd.loadAd();
            }
        }, delayMillis );
    }

    @Override
    public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
        emitSignal("rewarded_ad_display_failed", maxError.toString());
        rewardedAd.loadAd();
    }
}
