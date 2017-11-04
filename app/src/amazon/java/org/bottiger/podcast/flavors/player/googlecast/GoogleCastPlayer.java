//package org.bottiger.podcast.flavors.player.googlecast;
//
//import android.content.Context;
//import android.net.Uri;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.media.session.PlaybackStateCompat;
//import android.util.Log;
//
//import com.google.android.exoplayer2.ExoPlayer;
//import com.google.android.gms.cast.MediaInfo;
//import com.google.android.gms.cast.MediaMetadata;
//import com.google.android.gms.cast.framework.media.RemoteMediaClient;
//import com.google.android.gms.common.api.PendingResult;
//import com.google.android.gms.common.api.ResolvingResultCallbacks;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.common.images.WebImage;
//
//import org.bottiger.podcast.SoundWaves;
//import org.bottiger.podcast.flavors.MediaCast.IMediaCast;
//import org.bottiger.podcast.flavors.MediaCast.IMediaRouteStateListener;
//import org.bottiger.podcast.listeners.PlayerStatusObservable;
//import org.bottiger.podcast.player.Player;
//import org.bottiger.podcast.player.SoundWavesPlayerBase;
//import org.bottiger.podcast.player.exoplayer.ExoPlayerWrapper;
//import org.bottiger.podcast.provider.FeedItem;
//import org.bottiger.podcast.provider.IEpisode;
//import org.bottiger.podcast.service.PlayerService;
//import org.bottiger.podcast.utils.PlaybackSpeed;
//
//import java.io.IOException;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING;
//import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_IDLE;
//import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED;
//import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;
//import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_UNKNOWN;
//
///**
// * Created by aplb on 28-06-2016.
// */
//
//public abstract class GoogleCastPlayer extends SoundWavesPlayerBase {
//
//    private static final String TAG = GoogleCastPlayer.class.getSimpleName();
//
//    @NonNull
//    private RemoteMediaClient.Listener mClientListener;
//
//    private final CopyOnWriteArrayList<ExoPlayer.EventListener> listeners;
//
//    public GoogleCastPlayer(@NonNull Context argContext) {
//        super(argContext);
//        mClientListener = getRemoteClientListener();
//        listeners = new CopyOnWriteArrayList<>();
//    }
//
//    @NonNull
//    protected RemoteMediaClient.Listener getRemoteMediaClientListener() {
//        return mClientListener;
//    }
//
//    @Override
//    public void addListener(ExoPlayer.EventListener listener) {
//        listeners.add(listener);
//    }
//
//    @Override
//    public void removeListener(ExoPlayer.EventListener listener) {
//        listeners.remove(listener);
//    }
//
//    @Nullable
//    public abstract RemoteMediaClient getRemoteMediaClient();
//
//    @Override
//    public void pause() {
//        if (!isCasting())
//            return;
//
//        // Handle pending result
//        getRemoteMediaClient().pause();
//    }
//
//    @Override
//    public long getCurrentPosition() {
//        if (!isCasting())
//            return 0;
//
//        return getRemoteMediaClient().getApproximateStreamPosition();
//    }
//
//    @Override
//    public void setVolume(float vol) {
//        if (!isCasting())
//            return;
//
//        getRemoteMediaClient().setStreamVolume(vol);
//    }
//
//    public float getVolume() {
//        return getVolume();
//    }
//
//    @Override
//    public boolean isCasting() {
//        return getRemoteMediaClient() != null && getRemoteMediaClient().hasMediaSession();
//    }
//
//    @Override
//    public boolean canSetPitch() {
//        return false;
//    }
//
//    @Override
//    public boolean canSetSpeed() {
//        return false;
//    }
//
//    @Override
//    public float getCurrentPitchStepsAdjustment() {
//        return 0;
//    }
//
//    @Override
//    public float getCurrentSpeedMultiplier() {
//        return PlaybackSpeed.DEFAULT;
//    }
//
//    @Override
//    public long getDuration() {
//        if (!isCasting())
//            return -1;
//
//        return getRemoteMediaClient().getStreamDuration();
//    }
//
//    @Override
//    public float getMaxSpeedMultiplier() {
//        return getCurrentSpeedMultiplier();
//    }
//
//    @Override
//    public float getMinSpeedMultiplier() {
//        return getCurrentSpeedMultiplier();
//    }
//
//    @Override
//    public void prepare() throws IllegalStateException, IOException {
//
//    }
//
//    @Override
//    public void reset() {
//
//    }
//
//    @Override
//    public long seekTo(long msec, boolean argFastSeeking) throws IllegalStateException {
//        if (!isCasting())
//            return getCurrentPosition();
//
//        // handle pending result
//        getRemoteMediaClient().seek(msec);
//
//        return msec;
//    }
//
//    @Override
//    public long seekTo(long msec) throws IllegalStateException {
//        return seekTo(msec, false);
//    }
//
//    @Override
//    public void setAudioStreamType(int streamtype) {
//
//    }
//
//    @Override
//    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, IllegalStateException, IOException {
//
//    }
//
//    @Override
//    public void setPlaybackSpeed(float f) {
//        return;
//    }
//
//
//    @Override
//    public void setDataSourceAsync(@NonNull IEpisode argEpisode) {
//        super.setDataSourceAsync(argEpisode);
//
//        String url = argEpisode.getURL();
//        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
//
//        mediaMetadata.putString(MediaMetadata.KEY_TITLE, argEpisode.getTitle());
//        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, argEpisode.getSubscription(mContext).getTitle());
//        mediaMetadata.addImage(new WebImage(Uri.parse(argEpisode.getArtwork(mContext))));
//
//        Uri uri = Uri.parse(url);
//        String type = mContext.getContentResolver().getType(uri);
//        if (type == null) {
//            type = "audio/mpeg";
//        }
//
//        // Load media
//        MediaInfo mediaInfo = new MediaInfo.Builder(argEpisode.getURL())
//                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//                .setContentType(type)
//                .setMetadata(mediaMetadata)
//                .setStreamDuration(argEpisode.getDuration() * 1000)
//                .build();
//
//
//        RemoteMediaClient remoteMediaClient = getRemoteMediaClient();
//        remoteMediaClient.load(mediaInfo, true, startPos);
//    }
//
//    @Override
//    public int getStatus() {
//        if (!isCasting())
//            return PlayerStatusObservable.STOPPED;
//
//        return isPlaying() ? PlayerStatusObservable.PLAYING : PlayerStatusObservable.PAUSED;
//    }
//
//    @Override
//    public boolean isSteaming() {
//        return isCasting() && getRemoteMediaClient().isLiveStream();
//    }
//
//    @Override
//    public boolean isInitialized() {
//        return isCasting();
//    }
//
//    @Override
//    public void toggle() {
//        if (!isCasting())
//            return;
//
//        getRemoteMediaClient().togglePlayback();
//    }
//
//    @Override
//    public void start() {
//        if (!isCasting())
//            return;
//
//        PendingResult<RemoteMediaClient.MediaChannelResult> result = getRemoteMediaClient().play();
//
//        result.setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {
//            @Override
//            public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
//                Log.w(TAG, "" + mediaChannelResult);
//                notifyListeners(STATE_READY);
//            }
//        });
//    }
//
//    @Override
//    public void stop() {
//        pause();
//    }
//
//    @Override
//    public void release() {
//        if (!isCasting())
//            return;
//
//        getRemoteMediaClient().stop();
//    }
//
//    @Override
//    public boolean isPlaying() {
//        if (!isCasting())
//            return false;
//
//        return getRemoteMediaClient().isPlaying();
//    }
//
//    private void notifyListeners(@SoundWavesPlayerBase.PlayerState int playbackState) {
//        for (int i = 0; i < listeners.size(); i++) {
//            listeners.get(i).onPlayerStateChanged(true, playbackState);
//        }
//    }
//
//    private RemoteMediaClient.Listener getRemoteClientListener() {
//        return new RemoteMediaClient.Listener() {
//            @Override
//            public void onStatusUpdated() {
//                RemoteMediaClient client = getRemoteMediaClient();
//                int state = client.getPlayerState();
//
//                @SoundWavesPlayerBase.PlayerState int playbackState = PLAYER_STATE_UNKNOWN;
//
//                switch (state) {
//                    case PLAYER_STATE_UNKNOWN: {
//                        setState(PlayerStatusObservable.STOPPED);
//                        break;
//                    }
//                    case PLAYER_STATE_IDLE: {
//                        setState(PlayerStatusObservable.PAUSED);
//                        playbackState = STATE_IDLE;
//                        break;
//                    }
//                    case PLAYER_STATE_BUFFERING: {
//                        setState(PlayerStatusObservable.PREPARING);
//                        playbackState = STATE_BUFFERING;
//                        break;
//                    }
//                    case PLAYER_STATE_PAUSED: {
//                        setState(PlayerStatusObservable.PAUSED);
//                        playbackState = STATE_ENDED;
//                        break;
//                    }
//                    case PLAYER_STATE_PLAYING: {
//                        setState(PlayerStatusObservable.PLAYING);
//                        playbackState = STATE_READY;
//                        break;
//                    }
//                }
//
//                notifyListeners(playbackState);
//            }
//
//            @Override
//            public void onMetadataUpdated() {
//
//            }
//
//            @Override
//            public void onQueueStatusUpdated() {
//
//            }
//
//            @Override
//            public void onPreloadStatusUpdated() {
//
//            }
//
//            @Override
//            public void onSendingRemoteMediaRequest() {
//
//            }
//
//            @Override
//            public void onAdBreakStatusUpdated() {
//
//            }
//        };
//    }
//
//}
