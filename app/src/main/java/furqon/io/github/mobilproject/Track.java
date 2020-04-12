package furqon.io.github.mobilproject;

public class Track implements Comparable {

    private String number;
    private String name;
    private String uri;
    private String duration;

    //to be used by audio player
    Track(String duration, String name, String url) {
        //this.number = no;
        this.duration = duration;
        this.name = name;
        this.uri = url;

    }

    public String getNumber() {
        return number;
    }

    String getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public int compareTo(Object o) {
        int rv = 0;
        try {
            rv = Integer.parseInt(this.getName()) - Integer.parseInt(((Track) o).getName());
        } catch (NumberFormatException ignored) {

        } catch (NullPointerException ignored) {

        } catch (ClassCastException ignored) {

        }

        return rv;
    }
}
