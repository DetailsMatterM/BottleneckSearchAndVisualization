package group3.creator;

import org.json.*;

import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ThreadLocalRandom;

public class TrafficInfo {
    String urlAPI = "https://api.vasttrafik.se/bin/rest.exe/v2";

    //variable passed are Longitude Minimum/Maximum , Latitude Minimum/Maximum
    public int countTransportationInArea(String lonMin, String lonMax, String latMin, String latMax) {
        String uri = urlAPI + "/livemap?minx=" + lonMin + "&maxx=" + lonMax + "&miny=" + latMin +
            "&maxy=" + latMax + "&onlyRealtime=yes";

        JSONObject responseJson = getHttpResponseObject(uri);

        JSONArray arr = null;
        if (responseJson != null) {
            arr = responseJson.getJSONObject("livemap").getJSONArray("vehicles");
            return arr.length();
        } else
            return 0;
    }

    // get one of the nearby stations
    public JSONObject getFirstNearbyStop(String lat, String lon) {
        JSONObject stopInfo = null;
        String uri = urlAPI + "/location.nearbystops?originCoordLat=" + lat + "&originCoordLong=" +
            lon + "&maxNo=10&format=json";

        try {
            JSONObject responseJson = getHttpResponseObject(uri);
            if (responseJson != null) {
                JSONArray arr = responseJson.getJSONObject("LocationList").getJSONArray("StopLocation");
                int len = arr.length();
                int index = ThreadLocalRandom.current().nextInt(0, len);
                stopInfo = arr.getJSONObject(index);
                return stopInfo;
            } else


                return null;

        } catch (JSONException e) {
            return null;
        }
    }

    public int nearbyStops(String lat, String lon) {
        String uri = urlAPI + "/location.nearbystops?originCoordLat=" + lat + "&originCoordLong=" +
            lon + "&maxNo=10&maxDist=3000&format=json";

        try {
            JSONObject responseJson = getHttpResponseObject(uri);
            if (responseJson != null) {
                JSONArray arr = responseJson.getJSONObject("LocationList").getJSONArray("StopLocation");
                int len = arr.length();

                return len;
            } else
                return 0;
        } catch (JSONException e) {
            return 0;
        }

    }

    // get the station id
    public long getStationID(String lat, String lon) {
        long result = 1L;
        JSONObject res = getFirstNearbyStop(lat, lon);
        if (res != null) { //if it exists
            result = res.getLong("id");
        }
        return result;
    }

    // info on route detail using vasttrafik api and stations ID
    public JSONArray getTripDetail2(long orignId, long destnId, String trDate, String time1, String time2) {

        JSONArray result = new JSONArray();
        System.out.print("");
        //System.out.println("Serviceable request");
        String uri = urlAPI + "/trip?originId=" + orignId + "&destId=" + destnId + "&date=" + trDate
            + "&time=" + time1 + "%3A" + time2 + "&needJourneyDetail=0&numTrips=1&format=json";
        //https://api.vasttrafik.se/bin/rest.exe/v2/trip?originId=9021014007160000&destId=
        // 9021014004490000&date=2019-12-16&time=19%3A01&needJourneyDetail=0&numTrips=1&format=json
        try {
            JSONObject responseJson = getHttpResponseObject(uri);
            if (responseJson != null) {
                JSONArray arr = responseJson.getJSONObject("TripList").getJSONObject("Trip").getJSONArray("Leg");
                int len = arr.length();

                for (int i = 0; i < len; i++) {
                    JSONObject currentArr = new JSONObject(arr.get(i).toString());
                    JSONObject originInfo = currentArr.getJSONObject("Origin");
                    String trType = currentArr.getString("type");
                    if (!trType.equals("WALK")) {
                        JSONObject arrPart = new JSONObject();
                        JSONObject location = getStationLocation(originInfo.getString("name"));
                        String direction = currentArr.getString("direction");
                        String vehicleName = currentArr.getString("name");
                        String date = originInfo.getString("date");
                        String time = originInfo.getString("time");

                        String midTime = getMiddleTime(time);
                        String dateTime = date + " " + midTime;
                        arrPart.put("location", location);
                        arrPart.put("direction", direction);
                        arrPart.put("vehicleName", vehicleName);
                        arrPart.put("dateTime", dateTime);

                        result.put(arrPart);
                    }
                }
            }
            return result;
        } catch (JSONException e) {
            return null;
        }
    }


    //time rounded to 10 minutes interval (hr:mm)
    private String getMiddleTime(String timeInput) { //16:56 2012-12-12
        int len = timeInput.length();
        String hrPart = timeInput.substring(0, 2);
        String minPart;
        if (len > 5)
            minPart = timeInput.substring(3, 5);
        else
            minPart = timeInput.substring(3);

        int hrInt = Integer.parseInt(hrPart);
        int minInt = Integer.parseInt(minPart);
        int beforeRound = (hrInt * 60) + minInt;
        int afterRound = beforeRound / 10;
        int timeLength = afterRound * 10;

        double hrPart1 = timeLength / 60.0;
        int hrPart2 = (int) hrPart1;
        int minPart2 = (int) Math.round((hrPart1 - hrPart2) * 60.0);
        String minPart3;

        if (minPart2 < 10)
            minPart3 = "0" + minPart2;
        else
            minPart3 = minPart2 + "";
        String result = ((int) hrPart2 + ":" + minPart3);

        return result;
    }

    // get lat and lon of a station
    public JSONObject getStationLocation(String stationName) {
        try {
            URLUTF8Encoder encoder = new URLUTF8Encoder(stationName);

            String uri = urlAPI + "/location.name?input=" + encoder.getEncoded() + "&format=json";

            JSONObject responseJson = getHttpResponseObject(uri);

            if (responseJson != null) {
                JSONArray arr = responseJson.getJSONObject("LocationList").getJSONArray("CoordLocation");
                int len = arr.length();

                JSONObject locationObj = new JSONObject(arr.get(0).toString());

                double lat = locationObj.getDouble("lat");
                double lon = locationObj.getDouble("lon");

                JSONObject newData = new JSONObject();
                newData.put("latitude", lat);
                newData.put("longitude", lon);

                return newData;
            } else
                return null;
        } catch (Exception e) {
            return null;
        }
    }


    public JSONObject getHttpResponseObject(String uri) {

        HttpClient client = HttpClient.newHttpClient();

        JSONObject responseJson = null;

        HttpRequest request = HttpRequest.newBuilder()
            .header("Authorization", "Bearer " + PipeManager.token)
            .GET()
            .uri(URI.create(uri))
            .setHeader("User-Agent", "Java 11 HttpClient Bot")
            .build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.
            BodyHandlers.ofString());

        try {
            int statusCode = response.thenApply(HttpResponse::statusCode).get();

            if (statusCode == 401) {
                PipeManager.token = Oauth2Traffic.getClientCredentials();
                getHttpResponseObject(uri);

            } else {
                responseJson = new JSONObject(response.thenApply(HttpResponse::body).
                    get(5, TimeUnit.SECONDS) + "");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if (e.toString().contains("concurrent.ExecutionException"))
                System.out.println("Concurrent Execution");
            //e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            if (ex.toString().contains("org.json.JSONException: A JSONObject text")) {
                System.out.println(" API JSONObject Format Problem");
            }
        }
        return responseJson;
    }

}