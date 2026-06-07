package lol.catgirl.utils.client;

import com.google.gson.JsonArray;
import lol.catgirl.Catgirl;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    public static String requestLine(String url, String requestMethod) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(requestMethod);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String request(String url) {
        try {
            final HttpsURLConnection connection =
                    (HttpsURLConnection) new URL(url)
                            .openConnection();

            final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String current = "";
            StringBuilder response = new StringBuilder();
            while ((current = in.readLine()) != null) response.append(current);

            return response.toString();
        } catch (final Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static JsonArray requestAsGsonArray(String url) {
        String response = NetworkUtils.request(url);
        assert response != null;
        return Catgirl.INSTANCE.getGSON().fromJson(response, JsonArray.class);
    }
}