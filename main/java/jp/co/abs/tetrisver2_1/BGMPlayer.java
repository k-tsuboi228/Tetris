package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.media.MediaPlayer;

public class BGMPlayer {
    private MediaPlayer mMediaPlayer;

    private static final float LEFT_VOLUME = 1.0f;
    private static final float RIGHT_VOLUME = 1.0f;
    private static final int SEEK_TO_MEDIA_PLAYER = 0;

    public BGMPlayer(Context context) {
        this.mMediaPlayer = MediaPlayer.create(context, R.raw.tetrisbgm);
        this.mMediaPlayer.setLooping(true);
        this.mMediaPlayer.setVolume(LEFT_VOLUME, RIGHT_VOLUME);
    }

    public void start() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(SEEK_TO_MEDIA_PLAYER);
            mMediaPlayer.start();
        }
    }

    public void stop() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.prepareAsync();
        }
    }
}
