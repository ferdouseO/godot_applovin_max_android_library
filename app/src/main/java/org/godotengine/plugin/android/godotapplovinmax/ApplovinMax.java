package org.godotengine.plugin.android.godotapplovinmax;

import android.content.Context;
import android.util.ArraySet;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;


public class ApplovinMax extends GodotPlugin{
    /**
     * Base constructor passing a {@link Godot} instance through which the plugin can access Godot's
     * APIs and lifecycle events.
     *
     * @param godot
     */
    public ApplovinMax(Godot godot) {
        super(godot);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "ApplovinMax";
    }

    private String MaxSdkKey = "";
    private boolean isMaxInitialized = false;


    @UsedByGodot
    public void display_mediation_debugger(){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppLovinSdk.getInstance( getGodot().getActivity() ).showMediationDebugger();
            }
        });
    }

    @UsedByGodot
    public void toast(String txt){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getGodot().getActivity(), txt, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @UsedByGodot
    public void initialize_applovin(String sdk_key){
        getGodot().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initialize_plugin(sdk_key, getGodot().getActivity());
            }
        });
    }


    public void initialize_plugin(String sdk_key, Context ctx){
        this.MaxSdkKey = sdk_key;

        // Create the initialization configuration
        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder( MaxSdkKey, ctx )
                .setMediationProvider( AppLovinMediationProvider.MAX )
                // Perform any additional configuration/setting changes

                .build();

        // Initialize the SDK with the configuration
        AppLovinSdk.getInstance( ctx ).initialize( initConfig, new AppLovinSdk.SdkInitializationListener()
        {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration sdkConfig)
            {
                // Start loading ads
                isMaxInitialized = true;
                emitSignal("applovin_initialized");
            }
        } );
    }



    @UsedByGodot
    public boolean get_is_applovin_initialized(){
        return isMaxInitialized;
    }



    public Set<SignalInfo>  getPluginSignals(){
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("applovin_initialized"));

        return signals;
    }



}

