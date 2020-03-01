package furqon.io.github.mobilproject;

public class Track {

    private String number;
    private String name;
    private String uri;

    //to be used by audio player
    public Track(String no, String name){
        this.number = no;
        this.name = name;

    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }
}
