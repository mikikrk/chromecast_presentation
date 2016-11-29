package com.colortv.styledmediareceiver;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {

//        TODO Extended Notifications
//        ArrayList<String> buttonActions = new ArrayList<>();
//        buttonActions.add(MediaIntentReceiver.ACTION_REWIND);
//        buttonActions.add(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK);
//        buttonActions.add(MediaIntentReceiver.ACTION_FORWARD);
//        buttonActions.add(MediaIntentReceiver.ACTION_STOP_CASTING);
//        int[] compatButtonActionsIndicies = new int[]{ 1, 3 };
//        NotificationOptions notificationOptions = new NotificationOptions.Builder()
//                .setActions(buttonActions, compatButtonActionsIndicies)
//                .setSkipStepMs(30 * DateUtils.SECOND_IN_MILLIS)
//                .setTargetActivityClassName(ExpandedControlsActivity.class.getName())
//                .build();
//
//        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
//                .setNotificationOptions(notificationOptions)
//                .setExpandedControllerActivityClassName(ExpandedControlsActivity.class.getName())
//                .build();

        return new CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.cast_app_id))
//                .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID) //Default Media Receiver

//                .setCastMediaOptions(mediaOptions)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}