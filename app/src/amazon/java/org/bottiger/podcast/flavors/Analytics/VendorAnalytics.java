package org.bottiger.podcast.flavors.Analytics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import org.bottiger.podcast.ApplicationConfiguration;
import org.bottiger.podcast.SoundWaves;

/**
 * Created by apl on 19-02-2015.
 *
 * Only used on the Amazon app store
 */
public class VendorAnalytics implements IAnalytics {

    private Context mContext;

    public VendorAnalytics(@NonNull Context argContext) {
        mContext = argContext;
    }

    public void startTracking() {


    }

    @Override
    public boolean doShare() {
        return true;
    }

    @Override
    public void stopTracking() {
        return;
    }

    @Override
    public void activityPause() {

    }

    @Override
    public void activityResume() {

    }

    public void trackEvent(EVENT_TYPE argEvent) {
        trackEvent(argEvent, null);
    }

    public void trackEvent(EVENT_TYPE argEvent, @Nullable Integer argValue) {
        return;
    }

    @Override
    public void logFeed(@NonNull String url, boolean argDidSubscribe) {}

}
