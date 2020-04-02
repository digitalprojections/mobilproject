package furqon.io.github.mobilproject;

public class Track {

    private String number;
    private String name;
    private String uri;
    private String duration;

    //to be used by audio player
    public Track(String duration, String name, String url) {
        //this.number = no;
        this.duration = duration;
        this.name = name;
        this.uri = url;

    }

    public String getNumber() {
        return number;
    }

    public String getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }
}
