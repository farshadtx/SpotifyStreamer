package mobi.farshad.spotifystreamer.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Farshad on 7/12/15.
 */
public class TrackObject implements Parcelable {
    public String Id;
    public String Name;
    public String Artist;
    public String AlbumName;
    public Long Duration;
    public String AlbumThumbImage;
    public String AlbumImage;
    public String URL;

    public TrackObject(String _id, String _name, String _artist, String _albumName, Long _duration, String _thumbImage, String _image, String _url) {
        this.Id = _id;
        this.Name = _name;
        this.Artist = _artist;
        this.AlbumName = _albumName;
        this.Duration = _duration;
        this.AlbumThumbImage = _thumbImage;
        this.AlbumImage = _image;
        this.URL = _url;
    }

    private TrackObject(Parcel in) {
        Id = in.readString();
        Name = in.readString();
        Artist = in.readString();
        AlbumName = in.readString();
        Duration = in.readLong();
        AlbumThumbImage = in.readString();
        AlbumImage = in.readString();
        URL = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(Artist);
        dest.writeString(AlbumName);
        dest.writeLong  (Duration);
        dest.writeString(AlbumThumbImage);
        dest.writeString(AlbumImage);
        dest.writeString(URL);
    }

    public static final Creator<TrackObject> CREATOR = new Creator<TrackObject>(){

        @Override
        public TrackObject createFromParcel(Parcel source) {
            return new TrackObject(source);
        }

        @Override
        public TrackObject[] newArray(int i) {
            return new TrackObject[i];
        }
    };
}
