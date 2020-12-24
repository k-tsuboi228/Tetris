package jp.co.abs.tetrisver2_1;

import android.content.Context;
import android.media.MediaPlayer;

public class BGMPlayer {
    private MediaPlayer mMediaPlayer;

    public BGMPlayer(Context context) {
        this.mMediaPlayer = MediaPlayer.create(context, R.raw.tetrisbgm);
        this.mMediaPlayer.setLooping(true);
        this.mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    public void start() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(0);
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
