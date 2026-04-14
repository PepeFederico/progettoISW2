package it.uniroma2.ISW2.Pepe.Federico;

import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        String projectName  = "STORM";
        String githubURL    = "https://github.com/PepeFederico/storm.git";
        String localPath    = "repo-storm";

        JiraReleaseRetriever retriever = new JiraReleaseRetriever();

        System.out.println("Recupero informazioni sulle release per: " + projectName);
        List<ProjectVersion> allVersions = retriever.getReleaseInfo(projectName);

        int totalVersions = allVersions.size();
        int versionsToKeep = (int) Math.ceil(totalVersions * 0.34);

        List<ProjectVersion> filteredReleases = allVersions.subList(0, versionsToKeep);
        System.out.println("Versioni trovate: " + allVersions.size());
        System.out.println("Release da analizzare: " + filteredReleases.size());

        GitHandler gitHandler = new GitHandler(localPath, githubURL);

        for (ProjectVersion release : filteredReleases){
            System.out.println("    >> Elaborazione Release " + release.VersionName() + " --- ");
            gitHandler.checkoutToRelease(release);

            // [PROSSIMO STEP DA IMPLEMENTARE]
            // L'analyzer leggerà i file nella cartella 'localPath'
            // analyzeVersion(localPath, release);

            System.out.println("    >> Checkout completato !");
        }


    }
}