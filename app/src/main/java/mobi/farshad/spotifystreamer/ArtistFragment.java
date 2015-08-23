package mobi.farshad.spotifystreamer;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import mobi.farshad.spotifystreamer.objects.ArtistObject;
import mobi.farshad.spotifystreamer.objects.TrackObject;
import mobi.farshad.spotifystreamer.adapter.TracksAdapter;
import java.util.ArrayList;

public class ArtistFragment extends Fragment {
    private static final String ARTIST = "artist";
    private static final String ARTIST_TRACKS = "artist.tracks";

    ArtistObject objArtist;
    ArrayList<TrackObject> arrayOfTracks = new ArrayList<>();
    ListView lstTracks;

    public static ArtistFragment newInstance(ArtistObject _artist, ArrayList<TrackObject> _arrayTracks) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARTIST, _artist);
        args.putParcelableArrayList(ARTIST_TRACKS, _arrayTracks);
        fragment.setArguments(args);
        return fragment;
    }

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARTIST, objArtist);
        outState.putParcelableArrayList(ARTIST_TRACKS, arrayOfTracks);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            objArtist = getArguments().getParcelable(ARTIST);
            arrayOfTracks = getArguments().getParcelableArrayList(ARTIST_TRACKS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //noinspection ConstantConditions
        if (savedInstanceState != null) {
            objArtist = savedInstanceState.getParcelable(ARTIST);
            arrayOfTracks = savedInstanceState.getParcelableArrayList(ARTIST_TRACKS);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        Boolean isSplitView = prefs.getBoolean("is_splitview", false);
        if (!isSplitView) {
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
                getActivity().getActionBar().setTitle(objArtist.Name + " :Top " + arrayOfTracks.size() + " Tracks");
            }
        }
        View view = inflater.inflate(R.layout.fragment_artist,
                container, false);
        lstTracks = (ListView) view.findViewById(R.id.lst_tracks);
        lstTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                Fragment prev = getFragmentManager().findFragmentByTag("player");
                if (prev != null) {
                    getFragmentManager().beginTransaction().remove(prev).addToBackStack(null).commit();
                }

                DialogFragment newFragment = PlayerFragment.newInstance(arrayOfTracks,position);
                newFragment.show(getFragmentManager().beginTransaction(), "player");
            }
        });

        TracksAdapter adapter = new TracksAdapter(getActivity().getApplicationContext(), arrayOfTracks);
        lstTracks.setAdapter(adapter);

        return view;
    }


}
