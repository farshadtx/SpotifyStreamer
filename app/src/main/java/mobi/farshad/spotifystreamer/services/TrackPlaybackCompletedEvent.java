package mobi.farshad.spotifystreamer.services;

import mobi.farshad.spotifystreamer.objects.TrackObject;

public class TrackPlaybackCompletedEvent {

    TrackObject pTrack;

    public TrackPlaybackCompletedEvent(TrackObject track) {
        pTrack = track;
    }

    public static TrackPlaybackCompletedEvent newInstance(TrackObject track) {
        return new TrackPlaybackCompletedEvent(track);
    }

    public TrackObject getTrack() {
        return pTrack;
    }

}
