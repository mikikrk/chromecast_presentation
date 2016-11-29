package com.colortv.styledmediareceiver.model;

import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

public class SampleVideoInfoFactory {

    public static final String VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/mp4/DesigningForGoogleCast.mp4";

    public static MediaInfo getVideoMediaInfo() {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Design for Google Cast");
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, "For Polish JUG");
        mediaMetadata.addImage(new WebImage(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/DesigningForGoogleCast2-480x270.jpg")));
        return new MediaInfo.Builder(VIDEO_URL)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .setContentType("video/mp4")
                .build();
    }
}
