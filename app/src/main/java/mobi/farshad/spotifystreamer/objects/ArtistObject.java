package mobi.farshad.spotifystreamer.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class ArtistObject implements Parcelable {
    public String Id;
    public String Name;
    public String ThumbImage;

    public ArtistObject(String _id, String _name, String _thumbImage) {
        this.Id = _id;
        this.Name = _name;
        this.ThumbImage = _thumbImage;
    }

    private ArtistObject(Parcel in) {
        Id = in.readString();
        Name = in.readString();
        ThumbImage = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(ThumbImage);
    }

    public static final Parcelable.Creator<ArtistObject> CREATOR = new Parcelable.Creator<ArtistObject>(){

        @Override
        public ArtistObject createFromParcel(Parcel source) {
            return new ArtistObject(source);
        }

        @Override
        public ArtistObject[] newArray(int i) {
            return new ArtistObject[i];
        }
    };
}
