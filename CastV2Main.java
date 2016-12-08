package com.colortv.cast.testapp;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.Menu;
import android.view.MenuItem;

import com.colortv.android.googlecast.ColorTvCastSDK;
import com.colortv.cast.testapp.connection.CastApiManager;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;

public class MainActivity extends MainActivityCommon {
    private static final String APP_ID = "3C1F0065"; 

    private MediaRouteSelector mediaRouteSelector;
    private CastDevice selectedDevice;
    private MediaRouter mediaRouter;
    private MediaRouter.Callback mediaRouterCallback;
    private TestappChannel testappChannel;
    private final CastApiManager castApiManager;
    private boolean mediaRouteItemInitialized;
    private Menu menu;

    private class TestappChannel implements CastChannel {
        public String getNamespace() {
            return "urn:x-cast:com.colortv.testapp";
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
        }
    }

    public MainActivity() {
        testappChannel = new TestappChannel();
        CastApiManager.init(APP_ID, testappChannel);
        castApiManager = CastApiManager.getInstance();
    }

    @Override
    protected void init() {
        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();

        mediaRouter.addCallback(mediaRouteSelector, prepareMediaRouterCallback(),
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        mediaRouter.updateSelectedRoute(mediaRouteSelector);
        if (!mediaRouteItemInitialized && menu != null) {
            initGoogleCastButton();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        initGoogleCastButton();
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.item_google_cast);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);

        return true;
    }

    private MediaRouter.Callback prepareMediaRouterCallback() {
        return new MediaRouter.Callback() {

            @Override
            public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
                selectedDevice = CastDevice.getFromBundle(info.getExtras());
                castApiManager.prepareConnection(getApplicationContext(), selectedDevice);
            }

            @Override
            public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
                selectedDevice = null;
                castApiManager.getApiClient().disconnect();
            }
        };
    }

    @Override
    protected void onDestroy() {
        if (mediaRouter != null) {
            mediaRouter.removeCallback(mediaRouterCallback);
        }
        super.onDestroy();
    }

}