package furqon.io.github.mobilproject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioTimer {
    public static String getNumberWithLeadingZero(int _number) {
        if (_number < 10) {
            return "0" + _number;
        } else {
            String min = "";
            if (_number > 59) {
                int h = 1;
                if (_number / 60 > 1) {

                    h = (int) Math.floor(_number / 60);
                    int m = _number % 60;
                    if (m < 10) {
                        min = "0" + m;
                    } else {
                        min = String.valueOf(m);
                    }
                }
                return h + ":" + min;
            } else {
                return String.valueOf(_number);
            }
        }
        }


    public static String getTimeStringFromMs(Integer _ms) {
        int totalSeconds = _ms / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60;

        String hms = String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(_ms),
                TimeUnit.MILLISECONDS.toMinutes(_ms) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(_ms) % TimeUnit.MINUTES.toSeconds(1));
        return hms;
        //return getNumberWithLeadingZero(minutes) + ":" + getNumberWithLeadingZero(seconds);
    }
}
