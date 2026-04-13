package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.utils.CSVExporter;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        String projectName = "STORM";

        JiraReleaseRetriever retriever = new JiraReleaseRetriever();

        System.out.println("Recupero informazioni sulle release per: " + projectName);
        List<ProjectVersion> allVersions = retriever.getReleaseInfo(projectName);

        int totalVersions = allVersions.size();
        int versionsToKeep = (int) Math.ceil(totalVersions * 0.35);

        System.out.println("Versioni trovate: " + allVersions.size());
        System.out.println("Release da tenere: " + versionsToKeep);

        String header = "Index, VersionID, VersionName, Date";
        String fileName = projectName + "VersionInfo.csv";

        CSVExporter.writeToCSV(
                fileName,
                header,
                allVersions,
                v -> {
                    int currentIndex = allVersions.indexOf(v) + 1;
                    String name = v.VersionName();
                    if (name.contains(",")) {
                        name = "\"" + name + "\"";
                    }

                    return String.format("%d,%s,%s,%s",
                            currentIndex,     // Va in Index
                            v.VersionID(),    // Va in Version ID
                            name,             // Va in Version Name
                            v.Date().toString() // Va in Date (produce YYYY-MM-DDT00:00)
                    );
                }
        );
    }
}