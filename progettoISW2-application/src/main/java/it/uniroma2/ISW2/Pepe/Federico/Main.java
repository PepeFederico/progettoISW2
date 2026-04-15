package it.uniroma2.ISW2.Pepe.Federico;

import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        Properties properties           = new Properties();
        JiraReleaseRetriever retriever  = new JiraReleaseRetriever();
        AnalyzerVersion analyzerVersion = new AnalyzerVersion();

        try(InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(input);
        }

        System.out.println("Recupero informazioni sulle release per: " + properties.getProperty("projectName"));
        List<ProjectVersion> allVersions = retriever.getReleaseInfo(properties.getProperty("projectName"));

        int totalVersions = allVersions.size();
        int versionsToKeep = (int) Math.ceil(totalVersions * 0.34);

        List<ProjectVersion> filteredReleases = allVersions.subList(0, versionsToKeep);
        System.out.println("Versioni trovate: " + allVersions.size());
        System.out.println("Release da analizzare: " + filteredReleases.size());

        GitHandler gitHandler = new GitHandler(properties.getProperty("localPath"), properties.getProperty("githubURL"));

        for (ProjectVersion release : filteredReleases){
            System.out.println("    >> Elaborazione Release " + release.versionName() + " --- ");

            gitHandler.checkoutToRelease(release);
            System.out.println("    >> Checkout completato !");
            analyzerVersion.analyzeVersion(properties.getProperty("localPath"), release);

            break;
        }


    }
}