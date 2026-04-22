package it.uniroma2.ISW2.Pepe.Federico;

import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fallimento critico durante l'esecuzione dell'applicazione", e);
            System.exit(1);
        }
    }

    public void run() throws IOException, GitAPIException {
        // 1    --> Caricamento e validazione configurazione
        Properties props    = loadConfiguration();
        String projectName  = props.getProperty("projectName");
        String localPath    = props.getProperty("localPath");
        String githubUrl    = props.getProperty("githubURL");
        String root         = props.getProperty("root");

        JiraReleaseRetriever retriever = new JiraReleaseRetriever();
        AnalyzerVersion analyzer = new AnalyzerVersion();

        // 2    -->  Recupero e filtraggio release
        LOGGER.info("Inizio recupero informazioni sulle release per il progetto: " + projectName);
        List<ProjectVersion> allVersions = retriever.getReleaseInfo(projectName);

        if (allVersions.isEmpty()) {
            LOGGER.warning("Nessuna versione trovata per il progetto " + projectName);
            return;
        }

        List<ProjectVersion> filteredReleases = filterReleases(allVersions);
        System.out.println("Versioni totali: " + allVersions.size());
        System.out.println("Release da analizzare (34%): " + filteredReleases.size());

        // 3    --> Elaborazione release
        try (GitHandler gitHandler = new GitHandler(localPath, githubUrl)) {
            for (ProjectVersion release : filteredReleases) {
                processRelease(release, gitHandler, analyzer, localPath, root);
            }
        }
        LOGGER.info("Processo completato con successo per tutte le release filtrate.");
    }

    private void processRelease(ProjectVersion release, GitHandler git, AnalyzerVersion analyzer, String path, String root) throws GitAPIException, IOException {
        String commitId;

        LOGGER.log(Level.INFO, ">> Elaborazione Release: ", release.versionName());

        commitId = git.checkoutToRelease(release);
        analyzer.analyzeVersion(path, release, commitId, root);

        LOGGER.log(Level.INFO, ">> Analisi completata per la versione: ", release.versionName());
    }

    private List<ProjectVersion> filterReleases(List<ProjectVersion> versions) {
        int versionsToKeep = (int) Math.ceil(versions.size() * 0.34);
        return versions.subList(0, Math.min(versionsToKeep, versions.size()));
    }

    private Properties loadConfiguration() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Impossibile trovare il file di configurazione: config.properties");
            }
            props.load(input);
        }
        return props;
    }
}