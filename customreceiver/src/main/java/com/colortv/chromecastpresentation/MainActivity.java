package com.colortv.chromecastpresentation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CastContext castContext;
    private CastSession castSession;
    private HelloWorldChannel helloWorldChannel;

    private void setUpMediaRouteButton() {
        MediaRouteButton btnMediaRoute = (MediaRouteButton) findViewById(R.id.btnMediaRoute);
        CastButtonFactory.setUpMediaRouteButton(this, btnMediaRoute);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    private SessionManagerListener<CastSession> sessionManagerListener
            = new SessionManagerListener<CastSession>() {
        @Override
        public void onSessionStarting(CastSession castSession) {
            Log.d(TAG, "Session starting");
        }

        @Override
        public void onSessionStarted(CastSession castSession, String sessionId) {
            Log.d(TAG, "Session started");
            MainActivity.this.castSession = castSession;
            startCustomMessageChannel();
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean wasSuspended) {
            Log.d(TAG, "Session resumed");
            MainActivity.this.castSession = castSession;
            invalidateOptionsMenu();
        }

        private void startCustomMessageChannel() {
            if (castSession != null && helloWorldChannel == null) {
                helloWorldChannel = new HelloWorldChannel(getString(R.string.cast_namespace));
                try {
                    castSession.setMessageReceivedCallbacks(helloWorldChannel.getNamespace(),
                            helloWorldChannel);
                    Log.d(TAG, "Message channel started");
                } catch (IOException e) {
                    Log.d(TAG, "Error starting message channel", e);
                    helloWorldChannel = null;
                }
            }
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int error) {
            Log.e(TAG, "Session ended with error " + error);
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
        }

        @Override
        public void onSessionEnded(CastSession castSession, int error) {
            Log.d(TAG, "Session ended");
            if (MainActivity.this.castSession == castSession) {
                cleanupSession();
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int reason) {
            Log.d(TAG, "Session suspended");
        }

        @Override
        public void onSessionResuming(CastSession castSession, String sessionId) {
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int error) {
        }
    };

    private class HelloWorldChannel implements MessageReceivedCallback {

        private final String namespace;

        HelloWorldChannel(String namespace) {
            this.namespace = namespace;
        }

        public String getNamespace() {
            return namespace;
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            tvReceivedMessage.setText(message);
        }

    }

    private void sendMessage(String message) {
        if (helloWorldChannel != null && castSession != null && castSession.isConnected()) {
            castSession.sendMessage(helloWorldChannel.getNamespace(), message);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private EditText etMessage;
    private Button btnSubmit;
    private TextView tvReceivedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        tvReceivedMessage = (TextView) findViewById(R.id.tvReceiverMessage);
        setUpMediaRouteButton();

        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(etMessage.getText().toString());
            }
        });

        castContext = CastContext.getSharedInstance(this);
        castContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        castContext.getSessionManager().addSessionManagerListener(sessionManagerListener,
                CastSession.class);
        if (castSession == null) {
            castSession = castContext.getSessionManager().getCurrentCastSession();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        castContext.getSessionManager().removeSessionManagerListener(sessionManagerListener,
                CastSession.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanupSession();
    }

    private void cleanupSession() {
        closeCustomMessageChannel();
        castSession = null;
    }

    private void closeCustomMessageChannel() {
        if (castSession != null && helloWorldChannel != null) {
            try {
                castSession.removeMessageReceivedCallbacks(helloWorldChannel.getNamespace());
                Log.d(TAG, "Message channel closed");
            } catch (IOException e) {
                Log.d(TAG, "Error closing message channel", e);
            } finally {
                helloWorldChannel = null;
            }
        }
    }

}
