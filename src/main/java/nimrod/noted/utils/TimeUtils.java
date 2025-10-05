package nimrod.noted.utils;

public class TimeUtils {
    public static String formatTime(long millis) {
        long temp = Math.abs(millis) / 1000;

        long seconds = temp % 60;
        temp /= 60;
        long minutes = temp % 60;
        temp /= 60;
        long hours = temp;

        StringBuilder timeString = new StringBuilder();
        if (millis < 0) {
            timeString.append("-");
        }

        if (hours > 0) {
            timeString.append(String.format("%d:", hours));
            timeString.append(String.format("%02d:", minutes));
        } else {
            timeString.append(String.format("%d:", minutes));
        }

        timeString.append(String.format("%02d", seconds));
        return timeString.toString();
    }
}
