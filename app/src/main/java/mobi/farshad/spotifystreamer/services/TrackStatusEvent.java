package mobi.farshad.spotifystreamer.services;

public class TrackStatusEvent {

    Boolean isPause;

    public TrackStatusEvent(Boolean is_pause) {
        isPause = is_pause;
    }

    public static TrackStatusEvent newInstance(Boolean is_pause) {
        return new TrackStatusEvent(is_pause);
    }

    public Boolean isPause() {
        return isPause;
    }
}
