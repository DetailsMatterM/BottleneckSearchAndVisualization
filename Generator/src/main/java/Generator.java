import org.decimal4j.util.DoubleRounder;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class generates data with different scenario's which
 * is then published by the Publisher.
 */
public class Generator {
    private String[] purposes;
    private long requestId;
    private long deviceId;

    public Generator() {
        deviceId = deviceIdGenerator();
        requestId = -1;
        purposes = new String[]{"work", "leisure", "tourist", "study", "adventure"};
    }

    /**
     * Returns device id.
     *
     * @return deviceId
     */
    public long getDeviceId() {
        return deviceId;
    }

    /**
     * Creates a random request.
     *
     * @param pattern pattern for the random request
     * @return request as JSONObject
     */
    public JSONObject randomRequest(int pattern) {
        JSONObject object = new JSONObject();
        object.put("deviceId", deviceIdGenerator());
        // object.put("requestId", requestIdGenerator());
        object.put("origin", coordinatesGenerator());
        object.put("destination", coordinatesGenerator());
        object.put("issuance", issuanceGenerator());
        object.put("timeOfDeparture", getTime(pattern));
        object.put("purpose", getPurpose());

        return object;
    }

    /**
     * Generates random data for latitude and longitude and returns is as JSONObject
     * given a pattern.
     *
     * @param patternSelector patter for generating random data.
     * @return data as JSONObject
     */
    public JSONObject generateData(int patternSelector) {
        JSONObject object = null;

        switch (patternSelector) {
            case 1:
                object = randomRequest(patternSelector);
                double lat = ThreadLocalRandom.current().nextDouble(57.702393, 57.709600);
                double lon = ThreadLocalRandom.current().nextDouble(11.978672, 11.996000);

                lat = DoubleRounder.round(lat, 6);
                lon = DoubleRounder.round(lon, 6);

                JSONObject obj = new JSONObject();
                obj.put("latitude", lat);
                obj.put("longitude", lon);

                object.put("origin", obj);
                break;
            case 2:
            case 3:
                object = randomRequest(patternSelector);
                break;
        }

        return object;
    }

    private long issuanceGenerator() {
        return System.currentTimeMillis();
    }

    /**
     * Returns a random purpose for which the request was made.
     *
     * @return purpose as a String
     */
    private String getPurpose() {
        int size = purposes.length;
        int random = ThreadLocalRandom.current().nextInt(0, size);

        return purposes[random];
    }

    /**
     * Returns a random device id which is associated with individual
     * requests sent to the server.
     *
     * @return random device id as Long
     */
    public Long deviceIdGenerator() {
        return ThreadLocalRandom.current().nextLong(100000000, 100009000);
    }

    public Long requestIdGenerator() {
        requestId++;
        return requestId;
    }

    /**
     * Generates random co-ordinates and returns them as JSONObject.
     *
     * @return random co-ordinates
     */
    public JSONObject coordinatesGenerator() {
        double lat = ThreadLocalRandom.current().nextDouble(57.569000, 57.860000);
        double lon = ThreadLocalRandom.current().nextDouble(11.852000, 12.290000);

        lat = DoubleRounder.round(lat, 6);
        lon = DoubleRounder.round(lon, 6);

        JSONObject obj = new JSONObject();
        obj.put("latitude", lat);
        obj.put("longitude", lon);

        return obj;
    }

    /**
     * Returns a time for a given pattern.
     *
     * @param patternSelector pattern for time
     * @return formatted Time
     */
    public String getTime(int patternSelector) {
        LocalDateTime date_now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String stringedDate = date_now.format(dateTimeFormatter);

        long millis = System.currentTimeMillis();
        long additional = 0;

        if (patternSelector == 1) {
            // within 15 minutes
            additional = ThreadLocalRandom.current().nextLong(1000, 900000);
        } else if (patternSelector == 2) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(String.valueOf(millis)));

            int hour = c.get(Calendar.HOUR_OF_DAY);

            System.out.println("HOUR " + hour);

            if (hour <= 9) {
                long minTimeToRush = (7 - hour) * 1000 * 60;
                long maxTimeToRush = (9 - hour) * 1000 * 60;
                // next morning rush
                additional = ThreadLocalRandom.current().nextLong(minTimeToRush, maxTimeToRush);
            } else if (hour >= 10 || hour <= 18) { // todo this is always true
                long minTimeToRush = (16 - hour) * 1000 * 60;
                long maxTimeToRush = (19 - hour) * 1000 * 60;
                // next evening rush
                additional = ThreadLocalRandom.current().nextLong(minTimeToRush, maxTimeToRush);
            } else if (hour >= 19) {
                long minTimeToRush = ((24 - hour) * 1000 * 60) + (7 * 1000 * 60);
                long maxTimeToRush = ((19 - hour) * 1000 * 60) + (9 * 1000 * 60);
                // next evening rush
                additional = ThreadLocalRandom.current().nextLong(minTimeToRush, maxTimeToRush);
            }
        } else {
            // within 24 hrs
            additional = ThreadLocalRandom.current().nextLong(1000, 86399999);
        }

        millis = millis + additional;

        String dateFormat = "yyyy-MM-dd HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(String.valueOf(millis)));

        return simpleDateFormat.format(calendar.getTime());
    }
}