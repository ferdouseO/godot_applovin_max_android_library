package org.godotengine.plugin.android.godotapplovinmax;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.ArraySet;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MaxInterstitial extends GodotPlugin implements MaxAdListener {

    /**
     * Base constructor passing a {@link Godot} instance through which the plugin can access Godot's
     * APIs and lifecycle events.
     *
     * @param godot
     */

    private MaxInterstitialAd interstitialAd;
    private String MaxAdUnitIdInt = "";
    private int retryAttempt;

    public MaxInterstitial(Godot godot) {
        super(godot);
    }

    public Set<SignalInfo> getPluginSignals(){
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("interstitial_ad_loaded"));
        signals.add(new SignalInfo("interstitial_ad_load_failed", int.class));
        signals.add(new SignalInfo("interstitial_ad_displayed"));
        signals.add(new SignalInfo("interstitial_ad_display_failed", String.class));
        signals.add(new SignalInfo("interstitial_ad_closed"));
        signals.add(new SignalInfo("interstitial_ad_clicked"));
        signals.add(new SignalInfo("interstitial_ad_not_ready"));



        return signals;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return this.getClass().getSimpleName().toString();
    }


    @UsedByGodot
    public void load_interstitial(String intAdId){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                load_interstitial(intAdId, getGodot().getActivity());
            }
        });

    }

    @UsedByGodot
    public void show_interstitial(){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                show_interstitial(getGodot().getActivity());
            }
        });

    }

    @UsedByGodot
    public boolean get_is_interstitial_ad_ready(){
        return interstitialAd.isReady();
    }





    public void load_interstitial(String intAdId, Context ctx){
        MaxAdUnitIdInt = intAdId;
        createInterstitialAd(intAdId, ctx);
    }

    public void show_interstitial(Activity activity){
        if ( interstitialAd.isReady() )
        {
            // `this` is the activity that will be used to show the ad
            interstitialAd.showAd( activity );
        }
        else {
            emitSignal("interstitial_ad_not_ready");
        }
    }


    void createInterstitialAd(String intAdId, Context ctx)
    {
        interstitialAd = new com.applovin.mediation.ads.MaxInterstitialAd( intAdId, ctx.getApplicationContext() );
        interstitialAd.setListener( this );

        // Load the first ad
        interstitialAd.loadAd();
    }



















    @Override
    public void onAdLoaded(@NonNull MaxAd maxAd) {
        emitSignal("interstitial_ad_loaded");
        retryAttempt = 0;
    }

    @Override
    public void onAdDisplayed(@NonNull MaxAd maxAd) {
        emitSignal("interstitial_ad_displayed");
    }

    @Override
    public void onAdHidden(@NonNull MaxAd maxAd) {
        emitSignal("interstitial_ad_closed");
    }

    @Override
    public void onAdClicked(@NonNull MaxAd maxAd) {
        emitSignal("interstitial_ad_clicked");
    }

    @Override
    public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
        long delayMillis = 100;

        retryAttempt ++;
        delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );
        emitSignal("interstitial_ad_load_failed", retryAttempt);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                interstitialAd.loadAd();
            }
        }, delayMillis );
    }

    @Override
    public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
        emitSignal("interstitial_ad_display_failed", maxError.toString());
        interstitialAd.loadAd();
    }


}
