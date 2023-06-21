package net.nimrod.noted.utils;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import net.nimrod.noted.Noted;

public class ApiUtils {

    private static final String apiURL = "http://stardustdiving.xyz:4546";

    public static ArrayList<JsonElement> getSongs() {
        String res = apiGET("/songs");

        JsonElement jsonElement = JsonParser.parseString(res);
        if (jsonElement.isJsonNull())
            return null;

        JsonArray songData = jsonElement.getAsJsonObject().get("songs").getAsJsonArray();

        ArrayList<JsonElement> songs = new ArrayList<JsonElement>();

        for (JsonElement songElement: songData)
            songs.add(songElement);

        return songs;
    }

    public static String getNextSong() {
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

    public static File getSong(String songId) throws IOException, MalformedURLException {
        File songFile = new File(Noted.SONG_DIR, songId);

        // if file has already been downloaded, dont download it again
        if (!songFile.createNewFile())
            return songFile;

        URL url = new URL(apiURL + "/songs/" + songId);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        BufferedInputStream downloadStream = new BufferedInputStream(connection.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(songFile);

        try {
            int result; 

            while((result = downloadStream.read()) != -1)
                fileOutputStream.write((byte) result);
        } finally {
            downloadStream.close();

            fileOutputStream.flush();
            fileOutputStream.close();

            if (connection != null)
                connection.disconnect();

            return songFile; 
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
