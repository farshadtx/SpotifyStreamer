package mobi.farshad.spotifystreamer;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.annotation.NonNull;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import mobi.farshad.spotifystreamer.objects.TrackObject;
import mobi.farshad.spotifystreamer.services.PlayerService;
import mobi.farshad.spotifystreamer.services.TrackInfoEvent;
import mobi.farshad.spotifystreamer.services.TrackPlaybackCompletedEvent;
import mobi.farshad.spotifystreamer.services.TrackPlayingProgressEvent;
import mobi.farshad.spotifystreamer.services.TrackStatusEvent;

public class PlayerFragment extends DialogFragment {
    private final static String LOG_TAG = PlayerFragment.class.getSimpleName();

    private ArrayList<TrackObject> arrayPlaylist = new ArrayList<>();
    private Integer currentTrack;

    private TextView lblArtist, lblAlbum, lblTrack, lblCurrentTime, lblDuration;
    private ImageView imgCover;
    private SeekBar seekBarTrack;
    private ImageButton btnPlay;

    private boolean isUserChangingSeekbar = false;

    public static PlayerFragment newInstance(ArrayList<TrackObject> _arrayPlaylist, Integer _currentTrack) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(PlayerService.CURRENT_TRACK, _currentTrack);
        args.putParcelableArrayList(PlayerService.PLAYLIST, _arrayPlaylist);
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerFragment() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PlayerService.CURRENT_TRACK, currentTrack);
        outState.putParcelableArrayList(PlayerService.PLAYLIST, arrayPlaylist);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentTrack = getArguments().getInt(PlayerService.CURRENT_TRACK);
            arrayPlaylist = getArguments().getParcelableArrayList(PlayerService.PLAYLIST);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentTrack = getArguments().getInt(PlayerService.CURRENT_TRACK);
            arrayPlaylist = getArguments().getParcelableArrayList(PlayerService.PLAYLIST);
        }

        View view = inflater.inflate(R.layout.fragment_player, container);
        lblArtist = (TextView) view.findViewById(R.id.lblArtist);
        lblAlbum = (TextView) view.findViewById(R.id.lblAlbum);
        lblTrack = (TextView) view.findViewById(R.id.lblTrack);
        lblCurrentTime = (TextView) view.findViewById(R.id.lblTime);
        lblDuration = (TextView) view.findViewById(R.id.lblDuration);
        imgCover = (ImageView) view.findViewById(R.id.imgCover);
        seekBarTrack = (SeekBar) view.findViewById(R.id.seekTrack);
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        final ImageButton btnPrevious = (ImageButton) view.findViewById(R.id.btnPrevious);
        ImageButton btnNext = (ImageButton) view.findViewById(R.id.btnNext);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerService.class).setAction(PlayerService.ACTION_PLAY_PAUSE);
                getActivity().getApplicationContext().startService(intent);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v(LOG_TAG, "Play Next Track");
                lblCurrentTime.setText("00:00");
                seekBarTrack.setProgress(0);
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerService.class).setAction(PlayerService.ACTION_NEXT);
                getActivity().getApplicationContext().startService(intent);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v(LOG_TAG, "Play Previous Track");
                lblCurrentTime.setText("00:00");
                seekBarTrack.setProgress(0);
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerService.class).setAction(PlayerService.ACTION_PREVIOUS);
                getActivity().getApplicationContext().startService(intent);
            }
        });

        seekBarTrack.setMax(100);
        seekBarTrack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserChangingSeekbar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserChangingSeekbar = false;
                int currentPosition = progressToTimer(seekBar.getProgress(), arrayPlaylist.get(currentTrack).Duration.intValue());

                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerService.class).setAction(PlayerService.ACTION_SET_PROGRESS);
                intent.putExtra(PlayerService.CURRENT_TIME, currentPosition);

                getActivity().getApplicationContext().startService(intent);
            }
        });

        if (savedInstanceState == null) {
            playSong(currentTrack);
        } else {
            Intent intent = new Intent(getActivity().getApplicationContext(), PlayerService.class).setAction(PlayerService.ACTION_GETINFO);
            getActivity().getApplicationContext().startService(intent);
        }

        getDialog().setTitle("Now Playing");

//        setRetainInstance(true);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Activity a = getActivity();
            if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void playSong(int songIndex) {
        lblCurrentTime.setText("00:00");
        seekBarTrack.setProgress(0);

        Intent intent = new Intent(getActivity().getApplicationContext(), PlayerService.class).setAction(PlayerService.ACTION_INIT);
        intent.putExtra(PlayerService.CURRENT_TRACK, songIndex);
        intent.putExtra(PlayerService.PLAYLIST, arrayPlaylist);

        getActivity().getApplicationContext().startService(intent);
    }

    public void onEventMainThread(TrackInfoEvent event) {
        lblArtist.setText(event.getTrack().Artist);
        lblAlbum.setText(event.getTrack().AlbumName);
        lblTrack.setText(event.getTrack().Name);
        Picasso.with(getActivity().getApplicationContext())
                .load(event.getTrack().AlbumImage).resize(250, 250)
                .into(imgCover);

    }

    public void onEventMainThread(TrackStatusEvent event) {
        if (event.isPause()) {
            btnPlay.setImageResource(android.R.drawable.ic_media_play);
        } else {
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    public void onEventMainThread(TrackPlayingProgressEvent event) {
        if (isUserChangingSeekbar) {
            return;
        }

        long totalDuration = event.getMaxProgress();
        long currentDuration = event.getProgress();


        lblDuration.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(totalDuration),
                TimeUnit.MILLISECONDS.toSeconds(totalDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration))
        ));

        lblCurrentTime.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentDuration),
                TimeUnit.MILLISECONDS.toSeconds(currentDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration))
        ));

        int progress = (getProgressPercentage(currentDuration, totalDuration));
        seekBarTrack.setProgress(progress);

    }

    public void onEventMainThread(TrackPlaybackCompletedEvent event) {

        btnPlay.setImageResource(android.R.drawable.ic_media_play);

        long totalDuration = arrayPlaylist.get(currentTrack).Duration;
        long currentDuration = 0;

        lblDuration.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(totalDuration),
                TimeUnit.MILLISECONDS.toSeconds(totalDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration))
        ));

        lblCurrentTime.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentDuration),
                TimeUnit.MILLISECONDS.toSeconds(currentDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration))
        ));

        seekBarTrack.setProgress(0);
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        return percentage.intValue();
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration;
        totalDuration = (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        return currentDuration * 1000;
    }
}
