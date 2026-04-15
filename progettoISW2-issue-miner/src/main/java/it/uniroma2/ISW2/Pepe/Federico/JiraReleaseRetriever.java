package it.uniroma2.ISW2.Pepe.Federico;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class JiraReleaseRetriever {

    public List<ProjectVersion> getReleaseInfo(String projName) throws IOException {
        Map<LocalDateTime, ProjectVersion> versionMap = new HashMap<>();
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;

        JSONObject json = readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            JSONObject v = versions.getJSONObject(i);
            if (v.has("releaseDate")) {
                LocalDateTime date = LocalDate.parse(v.getString("releaseDate")).atStartOfDay();
                String id = v.getString("id");
                String name = v.getString("name");

                versionMap.put(date, new ProjectVersion(0, id, name, date));
            }
        }

        List<ProjectVersion> allVersions = new ArrayList<>(versionMap.values());
        allVersions.sort(Comparator.comparing(ProjectVersion::date));

        List<ProjectVersion> IndexedVersions = new ArrayList<>();

        int counter = 1;
        for (ProjectVersion temp : allVersions) {
            IndexedVersions.add(new ProjectVersion(
                    counter++,              //  index Release
                    temp.versionID(),       //  versionID release
                    temp.versionName(),     //  versionName release
                    temp.date()             //  date release
            ));
        }
        return IndexedVersions;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = URI.create(url).toURL().openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}


