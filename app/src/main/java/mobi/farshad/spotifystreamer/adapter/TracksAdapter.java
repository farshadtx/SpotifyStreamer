package mobi.farshad.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import mobi.farshad.spotifystreamer.R;
import mobi.farshad.spotifystreamer.objects.TrackObject;

public class TracksAdapter extends ArrayAdapter<TrackObject> {
    Context mainContext;
    private static class ViewHolder {
        TextView name;
        TextView album_name;
        ImageView image;
    }

    public TracksAdapter(Context context, List<TrackObject> tracks) {
        super(context, 0, tracks);
        mainContext = context;
    }

    @Override
    public View getView(int position, View trackRowView, ViewGroup parent) {
        // Get the data item for this position
        TrackObject track = getItem(position);
        ViewHolder viewHolder;
        if (trackRowView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            trackRowView = inflater.inflate(R.layout.listview_row_tracks, parent, false);
            viewHolder.name = (TextView) trackRowView.findViewById(R.id.txtName);
            viewHolder.album_name = (TextView) trackRowView.findViewById(R.id.txtAlbum);
            viewHolder.image = (ImageView) trackRowView.findViewById(R.id.imgThumb);
            trackRowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) trackRowView.getTag();
        }
        viewHolder.name.setText(track.Name);
        viewHolder.album_name.setText(track.AlbumName);
        if (track.AlbumThumbImage.equals("")) {
            viewHolder.image.setImageResource(R.drawable.singer_placeholder);
        } else {
            Picasso.with(mainContext)
                    .load(track.AlbumThumbImage)
                    .placeholder(R.drawable.singer_placeholder)
                    .resize(50,50)
                    .into(viewHolder.image);
        }

        return trackRowView;
    }
}