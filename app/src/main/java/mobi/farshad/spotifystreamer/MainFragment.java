package mobi.farshad.spotifystreamer;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


import kaaes.spotify.webapi.android.models.Track;
import mobi.farshad.spotifystreamer.objects.ArtistObject;
import mobi.farshad.spotifystreamer.objects.TrackObject;
import mobi.farshad.spotifystreamer.adapter.ArtistsAdapter;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Tracks;


public class MainFragment extends Fragment {

    ListView lstArtists;
    EditText txtSearch;
    SearchSpotifyTask spotifyTask;
    ArrayList<ArtistObject> arrayOfArtists = new ArrayList<>();
    String strSearchBar = "";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("arrayOfArtists", arrayOfArtists);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
            getActivity().getActionBar().setTitle(R.string.app_name);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (strSearchBar.length() > 0) {
                    spotifyTask = new SearchSpotifyTask();
                    spotifyTask.execute(strSearchBar);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            arrayOfArtists = savedInstanceState.getParcelableArrayList("arrayOfArtists");
        }

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
            getActivity().getActionBar().setTitle(R.string.app_name);
        }

        View view = inflater.inflate(R.layout.fragment_main,
                container, false);

        lstArtists = (ListView) view.findViewById(R.id.lst_artists);
        lstArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                SearchSpotifyTask2 spotifyTask2 = new SearchSpotifyTask2();
                spotifyTask2.execute(arrayOfArtists.get(position));
            }
        });

        ArtistsAdapter adapter = new ArtistsAdapter(getActivity().getApplicationContext(), arrayOfArtists);
        lstArtists.setAdapter(adapter);

        txtSearch = (EditText) view.findViewById(R.id.search_bar);
        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (spotifyTask != null) {
                        spotifyTask.cancel(false);
                    }
                    if (v.getText().length() > 0) {
                        spotifyTask = new SearchSpotifyTask();
                        strSearchBar = v.getText().toString();
                        spotifyTask.execute(strSearchBar);
                    } else {
                        if (arrayOfArtists != null)
                            arrayOfArtists.clear();
                        lstArtists.setAdapter(null);
                        strSearchBar = "";
                        Toast.makeText(getActivity().getApplicationContext(), R.string.search_error, Toast.LENGTH_SHORT).show();
                    }
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
                return false;
            }
        });

        return view;
    }

    public class SearchSpotifyTask extends AsyncTask<String, Void, ArtistsPager> {
        @Override
        protected ArtistsPager doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            HashMap<String, Object> queryMap = new HashMap<>();
            queryMap.put("limit", "10");

            return spotify.searchArtists(strings[0], queryMap);
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            if (arrayOfArtists != null) {
                arrayOfArtists.clear();
            } else {
                arrayOfArtists = new ArrayList<>();
            }
            if (artistsPager.artists.items.size() > 0) {
                for (int i = 0; i < artistsPager.artists.items.size(); i++) {
                    Artist artist = artistsPager.artists.items.get(i);

                    ArtistObject objArtist = new ArtistObject(artist.id, artist.name, (artist.images.size() == 0) ? "" : artist.images.get(artist.images.size() - 1).url);

                    arrayOfArtists.add(objArtist);
                }

                ArtistsAdapter adapter = new ArtistsAdapter(getActivity().getApplicationContext(), arrayOfArtists);
                lstArtists.setAdapter(adapter);
            } else {

                lstArtists.setAdapter(null);
                Toast.makeText(getActivity().getApplicationContext(), R.string.search_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class SearchSpotifyTask2 extends AsyncTask<ArtistObject, Void, Tracks> {

        ArtistObject objArtist;

        @Override
        protected Tracks doInBackground(ArtistObject... params) {

            objArtist = params[0];

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            return spotify.getArtistTopTrack(objArtist.Id, "US");
        }

        @Override
        protected void onPostExecute(Tracks topTracks) {

            if (topTracks.tracks.size() > 0) {
                ArrayList<TrackObject> arrayOfTracks = new ArrayList<>();
                for (int i = 0; i < topTracks.tracks.size(); i++) {
                    Track track = topTracks.tracks.get(i);
                    TrackObject objTrack = new TrackObject(track.id, track.name, objArtist.Name, track.album.name, (long)30000,  (track.album.images.size() == 0) ? "" : track.album.images.get(track.album.images.size() - 1).url, track.album.images.get(0).url, track.preview_url);
                    arrayOfTracks.add(objTrack);
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.content_fragment, ArtistFragment.newInstance(objArtist, arrayOfTracks))
                        .addToBackStack(null)
                        .commit();

            } else {
                Toast.makeText(getActivity().getApplicationContext(), R.string.search_error_2, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
