package mobi.farshad.spotifystreamer.services;


import mobi.farshad.spotifystreamer.objects.TrackObject;

public class TrackInfoEvent {

    TrackObject objTrack;

    public TrackInfoEvent(TrackObject track) {
        objTrack = track;
    }

    public static TrackInfoEvent newInstance(TrackObject track) {
        return new TrackInfoEvent(track);
    }

    public TrackObject getTrack() {
        return objTrack;
    }
}
