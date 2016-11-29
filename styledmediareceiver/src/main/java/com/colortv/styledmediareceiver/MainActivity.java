package com.colortv.styledmediareceiver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import com.colortv.styledmediareceiver.model.SampleVideoInfoFactory;
import com.colortv.styledmediareceiver.widgets.ExpandedControlsActivity;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

public class MainActivity extends AppCompatActivity {

    private static final int PROGRESS_UPDATE_PERIOD = 200;

    private static final String TAG = MainActivity.class.getSimpleName();

    private CastContext castContext;
    private CastSession castSession;

    private SessionManagerListener<CastSession> sessionManagerListener
            = new SessionManagerListener<CastSession>() {
        @Override
        public void onSessionEnded(CastSession session, int error) {
            onApplicationDisconnected();
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            onApplicationConnected(session);
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
            onApplicationDisconnected();
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            onApplicationConnected(session);
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
            onApplicationDisconnected();
        }

        @Override
        public void onSessionStarting(CastSession session) {
        }

        @Override
        public void onSessionEnding(CastSession session) {
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
        }

        private void onApplicationConnected(CastSession castSession) {
            MainActivity.this.castSession = castSession;
            loadSampleVideoIfOtherIsPlayed();
            playVideo(videoView.isPlaying(), videoView.getCurrentPosition());
            if (videoView.isPlaying()) {
                videoView.pause();
            }
            invalidateOptionsMenu();
        }

        private void loadSampleVideoIfOtherIsPlayed() {
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            if (remoteMediaClient != null) {
                MediaInfo mediaInfo = remoteMediaClient.getMediaInfo();
                if (mediaInfo != null) {
                    String receiversUrl = mediaInfo.getContentId();
                    if (!receiversUrl.equals(SampleVideoInfoFactory.VIDEO_URL)) {
                        remoteMediaClient.load(SampleVideoInfoFactory.getVideoMediaInfo(), videoView.isPlaying(), videoView.getCurrentPosition());
                    }
                }
            }
        }

        private void onApplicationDisconnected() {
            castSession = null;
            videoView.seekTo(progress);
            if (isPlaying) {
                videoView.start();
            }
            invalidateOptionsMenu();
        }
    };

    private boolean isPlaying = false;
    private int progress;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = (VideoView) findViewById(R.id.videoView);

        Uri uri = Uri.parse(SampleVideoInfoFactory.VIDEO_URL);
        videoView.setVideoURI(uri);

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isPlaying) {
                        pauseVideo();
                    } else {
                        playVideo(true, videoView.getCurrentPosition());
                    }
                }
                return false;
            }

        });

        castContext = CastContext.getSharedInstance(this);
        castContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
    }

    private void playVideo(boolean autoPlay, int position) {
        isPlaying = true;
        if (castSession != null) {
            playVideoRemotely(autoPlay, position);
        } else {
            if (progress > videoView.getCurrentPosition()) {
                videoView.seekTo(progress);
            }
            videoView.start();
        }
    }

    private void playVideoRemotely(boolean autoPlay, int position) {
        RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        if (remoteMediaClient != null) {
            //TODO Expanded Controller
            playInExpandedController(remoteMediaClient, autoPlay, position);
//            if (remoteMediaClient.isPaused()) {
//                remoteMediaClient.play();
//            } else {
//                remoteMediaClient.load(SampleVideoInfoFactory.getVideoMediaInfo(), autoPlay, position);
//                remoteMediaClient.addProgressListener(new RemoteMediaClient.ProgressListener() {
//                    @Override
//                    public void onProgressUpdated(long progressMs, long durationMs) {
//                        progress = (int) progressMs;
//                    }
//                }, PROGRESS_UPDATE_PERIOD);
//            }
        }
    }

    private void pauseVideo() {
        isPlaying = false;
        if (castSession == null) {
            videoView.pause();
            progress = videoView.getCurrentPosition();
        } else {
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            if (remoteMediaClient != null) {
                remoteMediaClient.pause();
                MediaStatus mediaStatus = remoteMediaClient.getMediaStatus();
                if (mediaStatus != null) {
                    videoView.seekTo((int)mediaStatus.getStreamPosition());
                }
            }
        }
    }

    private void playInExpandedController(final RemoteMediaClient remoteMediaClient, boolean autoPlay, int position) {
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(MainActivity.this, ExpandedControlsActivity.class);
                startActivity(intent);
                remoteMediaClient.removeListener(this);
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }
        });
        remoteMediaClient.load(SampleVideoInfoFactory.getVideoMediaInfo(), autoPlay, position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        castContext.getSessionManager().addSessionManagerListener(sessionManagerListener,
                CastSession.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        castContext.getSessionManager().removeSessionManagerListener(sessionManagerListener,
                CastSession.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        castSession = null;
    }
}
