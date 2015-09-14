package org.bottiger.podcast.service.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by aplb on 14-09-2015.
 */
public class CloudSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "CloudSyncAdapter";

    public CloudSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public CloudSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync");
    }
}
