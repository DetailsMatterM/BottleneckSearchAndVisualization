package group3.creator;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Oauth2Traffic {
    private static final Pattern pat = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
    private static final String clientId = Keys.getClientId(); // clientId
    private static final String clientSecret = Keys.getClientSecret(); // client secret
    private static final String tokenUrl = "https://api.vasttrafik.se:443/token";
    private static final String auth = clientId + ":" + clientSecret;
    private static final String authentication = Base64.getEncoder().encodeToString(auth.getBytes());

    public static String getClientCredentials() {
        String content = "grant_type=client_credentials";
        BufferedReader reader = null;
        HttpsURLConnection connection = null;
        String returnValue = "";

        try {
            URL url = new URL(tokenUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + authentication);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            PrintStream os = new PrintStream(connection.getOutputStream());
            os.print(content);
            os.close();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.
                getContentLength() : 2048);

            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            String response = out.toString();
            Matcher matcher = pat.matcher(response);

            if (matcher.matches() && matcher.groupCount() > 0) {
                returnValue = matcher.group(1);
            }
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    connection.disconnect();
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        return returnValue;
    }
}