package mobi.farshad.spotifystreamer.services;

import mobi.farshad.spotifystreamer.objects.TrackObject;

public class TrackPlayingProgressEvent {

    TrackObject pTrack;
    int pProgress;
    int pMaxProgress;

    public TrackPlayingProgressEvent(TrackObject track, int progress, int maxProgress) {
        pTrack = track;
        pProgress = progress;
        pMaxProgress = maxProgress;
    }

    public TrackObject getTrack() {
        return pTrack;
    }
    public int getProgress() {
        return pProgress;
    }
    public int getMaxProgress() {
        return pMaxProgress;
    }

    public static TrackPlayingProgressEvent newInstance(TrackObject track, int progress, int maxProgress) {
        return new TrackPlayingProgressEvent(track, progress, maxProgress);
    }
}
