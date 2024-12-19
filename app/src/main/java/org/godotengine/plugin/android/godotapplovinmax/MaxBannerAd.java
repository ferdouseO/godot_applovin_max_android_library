package org.godotengine.plugin.android.godotapplovinmax;

import android.app.Activity;
import android.content.Context;
import android.util.ArraySet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
//import com.applovin.mediation.MaxAd

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

public class MaxBannerAd extends GodotPlugin implements MaxAdViewAdListener {
    /**
     * Base constructor passing a {@link Godot} instance through which the plugin can access Godot's
     * APIs and lifecycle events.
     *
     * @param godot
     */

    private MaxAdView adView;
    private String MaxAdUnitIdBanner = "";
    private boolean is_banner_auto_refresh;

    public MaxBannerAd(Godot godot) {
        super(godot);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return this.getClass().getSimpleName().toString();
    }


    public Set<SignalInfo> getPluginSignals(){
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("banner_ad_loaded"));
        signals.add(new SignalInfo("banner_ad_displayed"));
        signals.add(new SignalInfo("banner_ad_load_failed"));
        signals.add(new SignalInfo("banner_ad_display_failed"));
        signals.add(new SignalInfo("banner_ad_expanded"));
        signals.add(new SignalInfo("banner_ad_clicked"));
        signals.add(new SignalInfo("banner_ad_collapsed"));
        signals.add(new SignalInfo("banner_ad_hidden"));



        return signals;
    }


    @UsedByGodot
    public void load_banner(String bannerAdId, int position){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                load_n_show(bannerAdId, position);
            }
        });
    }


    private void load_n_show(String bannerAdId, int position){

        MaxAdUnitIdBanner = bannerAdId;
        adView = new MaxAdView( bannerAdId, MaxAdFormat.BANNER, getGodot().getActivity() );
        adView.setListener( this );
        if(position == 1){
            adView.setGravity(Gravity.TOP);
        } else if (position == 2) {
            adView.setGravity(Gravity.BOTTOM);
        }

//        Toast.makeText(getGodot().getActivity(), Integer.toString(position), Toast.LENGTH_SHORT).show();

//        switch_banner_position(position);

        // Stretch to the width of the screen for banners to be fully functional
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;

        // Get the adaptive banner height.
//        int heightDp = MaxAdFormat.BANNER.getAdaptiveSize( ctx ).getHeight();
//        int heightDp = 256;
//        int heightPx = AppLovinSdkUtils.dpToPx( ctx, heightDp );


        FrameLayout.LayoutParams banner_layout_params = new FrameLayout.LayoutParams(width, height);
//        banner_layout_params.gravity = Gravity.BOTTOM;


        adView.setLayoutParams(banner_layout_params);

        adView.setExtraParameter( "adaptive_banner", "false" );
        adView.setLocalExtraParameter( "adaptive_banner_width", 400 );
        adView.getAdFormat().getAdaptiveSize( 400, getGodot().getActivity() ).getHeight(); // Set your ad height to this value

        // Set background or background color for banners to be fully functional
        adView.setBackgroundColor( 0 );

        ViewGroup rootView = ((Activity) getGodot().getActivity()).findViewById( android.R.id.content );
//        View v = new View(ctx);
        rootView.addView( adView );

        // Load the ad
        adView.loadAd();
    }

    @UsedByGodot
    public void destroy_banner(){
        adView.destroy();
    }

    @UsedByGodot
    public void switch_banner_position(int position){
        switch(position){
            case 1:
                adView.setGravity(Gravity.TOP);
            case 2:
                adView.setGravity(Gravity.BOTTOM);
            case 3:
                adView.setGravity(Gravity.START);
            case 4:
                adView.setGravity(Gravity.END);

        }
    }

    @UsedByGodot
    public void stop_banner_auto_refresh(){
        adView.stopAutoRefresh();
        is_banner_auto_refresh = false;
    }

    @UsedByGodot
    public void start_banner_auto_refresh(){
        adView.startAutoRefresh();
        is_banner_auto_refresh = true;
    }

    @UsedByGodot
    public boolean get_banner_auto_refresh_status(){
        return is_banner_auto_refresh;
    }






    @Override
    public void onAdExpanded(@NonNull MaxAd maxAd) {
        emitSignal("banner_ad_expanded");
    }

    @Override
    public void onAdCollapsed(@NonNull MaxAd maxAd) {
        emitSignal("banner_ad_collapsed");
    }

    @Override
    public void onAdLoaded(@NonNull MaxAd maxAd) {
        emitSignal("banner_ad_loaded");
        is_banner_auto_refresh = true;
    }

    @Override
    public void onAdDisplayed(@NonNull MaxAd maxAd) {
        emitSignal("banner_ad_displayed");
    }

    @Override
    public void onAdHidden(@NonNull MaxAd maxAd) {
        emitSignal("banner_ad_hidden");
    }

    @Override
    public void onAdClicked(@NonNull MaxAd maxAd) {
        emitSignal("banner_ad_clicked", maxAd.getAdUnitId());
    }

    @Override
    public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
        emitSignal("banner_ad_load_failed", maxError.toString());
    }

    @Override
    public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
        emitSignal("banner_ad_display_failed", maxError.toString());
    }
}
