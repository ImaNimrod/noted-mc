package net.nimrod.noted.util;

import com.google.gson.*;
import net.nimrod.noted.Noted;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ApiUtils {

    private static final String apiURL = "URL/IP of your API instance here";

    public static List<JsonElement> getSongs() {
        String res = apiGET("/songs");

        JsonElement jsonElement = JsonParser.parseString(res);
        if (jsonElement.isJsonNull())
            return null;

        JsonArray songData = jsonElement.getAsJsonObject().get("songs").getAsJsonArray();

        List<JsonElement> songs = new ArrayList<>();

        for (JsonElement songElement: songData)
            songs.add(songElement);

        return songs;
    }

    public static String getNextSongId() {
        String res = apiGET("/queue/next");

        JsonElement jsonElement = JsonParser.parseString(res);
        if (jsonElement.isJsonNull())
            return null;

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String nextSongId = jsonObject.get("_id").getAsString();
        if (nextSongId == null)
            return null;

        return nextSongId;
    }

    public static String getSongName(String songId) {
        List<JsonElement> songs = getSongs(); 

        for (JsonElement songElement : songs) {
            if (songElement.getAsJsonObject().get("_id").getAsString().equals(songId))
                return songElement.getAsJsonObject().get("name").getAsString();
        }

        return null;
    }

    public static byte[] getSong(String songId) throws IOException, MalformedURLException {
        URL url = new URL(apiURL + "/songs/" + songId);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        BufferedInputStream downloadStream = new BufferedInputStream(connection.getInputStream());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            int result; 

            while((result = downloadStream.read()) != -1)
                byteArrayOutputStream.write((byte) result);
        } finally {
            downloadStream.close();

            if (connection != null)
                connection.disconnect();

            return byteArrayOutputStream.toByteArray();
        }
    }

    private static String apiGET(String uri) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(apiURL + uri);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append("\r");
            }

            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

}
