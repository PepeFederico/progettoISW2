package it.uniroma2.ISW2.Pepe.Federico;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JiraTicketRetriever {

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = URI.create(url).toURL().openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    public List<JiraTicket> retrieveTicketFromJira(String projectName) throws IOException {
        DateTimeFormatter jiraFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        List<JiraTicket> tickets = new ArrayList<>();
        int i            = 0;
        int total;
        int maxResults   = 1000;

        do {
            String jql = String.format("project=\"%s\" AND issueType=\"Bug\" AND (status=\"closed\" OR status=\"resolved\") AND resolution=\"fixed\"", projectName);

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql="
                    + URLEncoder.encode(jql, StandardCharsets.UTF_8)
                    + "&fields=key,resolutiondate,versions,created,fixVersions"
                    + "&startAt=" + i
                    + "&maxResults=" + maxResults;

            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (int k = 0; k < issues.length(); k++) {
                JSONObject issue = issues.getJSONObject(k);
                JSONObject fields = issue.getJSONObject("fields");

                String key = issue.getString("key");
                LocalDateTime created = ZonedDateTime.parse(fields.getString("created"), jiraFormatter).toLocalDateTime();

                LocalDateTime resDate = fields.isNull("resolutiondate") ? null :
                        ZonedDateTime.parse(fields.getString("resolutiondate"), jiraFormatter).toLocalDateTime();

                List<String> affected = getVersionNames(fields.getJSONArray("versions"));
                List<String> fix = getVersionNames(fields.getJSONArray("fixVersions"));

                tickets.add(new JiraTicket(key, created, resDate, affected, fix));
            }
            i += maxResults;
        } while (i < total);

        tickets.sort(Comparator.comparing(JiraTicket::resolutionDate, Comparator.nullsLast(Comparator.naturalOrder())));

        return tickets;
    }

    private List<String> getVersionNames(JSONArray versionsArray) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < versionsArray.length(); i++) {
            names.add(versionsArray.getJSONObject(i).getString("name"));
        }
        return names;
    }
}
