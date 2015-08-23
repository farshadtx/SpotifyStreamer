package mobi.farshad.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import mobi.farshad.spotifystreamer.objects.TrackObject;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {
    private final static String LOG_TAG = PlayerService.class.getSimpleName();

    public static final String PLAYLIST = "playlist";
    public static final String CURRENT_TRACK = "current.track";
    public static final String CURRENT_TIME = "current.time";

    public static final String ACTION_INIT = "action.INIT";
    public static final String ACTION_GETINFO = "action.GETINFO";
    public static final String ACTION_PLAY_PAUSE = "action.PLAY_PAUSE";
    public static final String ACTION_NEXT = "action.NEXT";
    public static final String ACTION_PREVIOUS = "action.PREVIOUS";
    public static final String ACTION_SET_PROGRESS = "action.SET_PROGRESS";

    MediaPlayer mediaPlayer = null;
    private Handler mHandler = new Handler();
    WifiManager.WifiLock wifiLock;
    private ArrayList<TrackObject> arrayPlaylist = new ArrayList<>();
    private Integer currentTrack = -1;

    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_INIT:
                if (intent.getExtras().getInt(PlayerService.CURRENT_TRACK) == currentTrack) {
                    broadcastTrackInfo();
                    break;
                }
                currentTrack = intent.getExtras().getInt(PlayerService.CURRENT_TRACK);
                arrayPlaylist = intent.getExtras().getParcelableArrayList(PlayerService.PLAYLIST);
                playSong(currentTrack);
                break;
            case ACTION_GETINFO:
                broadcastTrackInfo();
                break;
            case ACTION_PLAY_PAUSE:
                if (mediaPlayer != null) {
                    playPauseSong();
                } else {
                    playSong(currentTrack);
                }
                break;
            case ACTION_NEXT:
                playNextSong();
                break;
            case ACTION_PREVIOUS:
                playPreviousSong();
                break;
            case ACTION_SET_PROGRESS:
                int mSec = intent.getExtras().getInt(PlayerService.CURRENT_TIME);
                seekTo(mSec);
                break;
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        wifiLock.release();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void playSong(int songIndex) {
        TrackObject track = arrayPlaylist.get(songIndex);

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        wifiLock = ((WifiManager) getSystemService(WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);

        broadcastTrackInfo();

        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(track.URL);

            mediaPlayer.prepareAsync();

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
            Log.e("Try/Catch", e.toString());
        }
    }

    public void playPreviousSong() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (currentTrack > 0) {
            currentTrack -= 1;
        } else {
            currentTrack = arrayPlaylist.size() - 1;
        }
        playSong(currentTrack);
    }

    public void playNextSong() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (currentTrack < (arrayPlaylist.size() - 1)) {
            currentTrack += 1;
        } else {
            currentTrack = 0;
        }
        playSong(currentTrack);
    }

    public void playPauseSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mHandler.removeCallbacks(mUpdateTimeTask);
            broadcastTrackStatus();
        } else {
            mediaPlayer.start();
            broadcastTrackStatus();
            updateProgressBar();
        }
    }


    public void seekTo(int msec) {
        if (mediaPlayer != null) {
            mHandler.removeCallbacks(mUpdateTimeTask);
            mediaPlayer.seekTo(msec);
        } else {
            Log.e(LOG_TAG, "Attempted to seek on a null media player");
        }
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
        broadcastTrackStatus();
        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        wifiLock.release();
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer = null;
        mHandler.removeCallbacks(mUpdateTimeTask);

        broadcastTrackPlaybackCompleted();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        updateProgressBar();
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 500);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying()) {
                broadcastTrackPlayingProgress();
            }
            mHandler.postDelayed(this, 500);
        }
    };

    /**
     * Player broadcasts
     */

    private void broadcastTrackInfo() {
        TrackInfoEvent event = new TrackInfoEvent(arrayPlaylist.get(currentTrack));
        EventBus.getDefault().post(event);
    }


    private void broadcastTrackStatus() {
        TrackStatusEvent event = new TrackStatusEvent(!mediaPlayer.isPlaying());
        EventBus.getDefault().post(event);
    }

    private void broadcastTrackPlayingProgress() {
        TrackPlayingProgressEvent event = TrackPlayingProgressEvent.newInstance(
                arrayPlaylist.get(currentTrack),
                mediaPlayer.getCurrentPosition(),
                mediaPlayer.getDuration()
        );
        EventBus.getDefault().post(event);
    }

    private void broadcastTrackPlaybackCompleted() {
        TrackPlaybackCompletedEvent event = new TrackPlaybackCompletedEvent(arrayPlaylist.get(currentTrack));
        EventBus.getDefault().post(event);
    }
}