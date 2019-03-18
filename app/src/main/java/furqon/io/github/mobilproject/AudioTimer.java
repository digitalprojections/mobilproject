package furqon.io.github.mobilproject;

public class AudioTimer {
    public static String getNumberWithLeadingZero(int _number) {
        if (_number < 10) {
            return "0" + String.valueOf(_number);
        } else {
            return String.valueOf(_number);
        }
    }

    public static String getTimeStringFromMs(Integer _ms) {
        int totalSeconds = _ms / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60;

        return getNumberWithLeadingZero(minutes) + ":" + getNumberWithLeadingZero(seconds);
    }
}
