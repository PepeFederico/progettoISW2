package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.metrics.DatasetUtils;
import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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
        Properties props        = loadConfiguration();
        String projectName      = props.getProperty("projectName");
        String localPath        = props.getProperty("localPath");
        String githubUrl        = props.getProperty("githubURL");
        String root             = props.getProperty("root");
        String executionMode    = props.getProperty("executionMode");

        JiraReleaseRetriever retriever      = new JiraReleaseRetriever();
        AnalyzerVersion analyzer;

        List<MetricsCollector> dataset = null;

        if (executionMode.equalsIgnoreCase("FULL") || executionMode.equalsIgnoreCase("BUILD")) {
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

                analyzer = new AnalyzerVersion(gitHandler);
                List<MetricsCollector> finalDataset = new ArrayList<>();

                for (ProjectVersion release : filteredReleases) {

                    Map<String, MetricsCollector> currentReleaseMap = processRelease(release, gitHandler, analyzer, localPath, root);
                    finalDataset.addAll(currentReleaseMap.values());
                    LOGGER.info("Release " + release.versionName() + " analizzata: " + currentReleaseMap.size() + " classi.");
                }

                // Esportazione Dataset
                if (!finalDataset.isEmpty()) {
                    LOGGER.info("Inizio esportazione dataaset sul file CSV...");
                    DatasetUtils datasetUtils = new DatasetUtils();
                    datasetUtils.generateDataset(projectName, finalDataset);
                } else {
                    LOGGER.warning("Nessun dato raccolto: il file CSV non verrà generato.");
                }
            }
            LOGGER.info("Processo completato con successo per tutte le release filtrate.");
        } else {
            String path = props.getProperty("datasetPath");
            DatasetUtils datasetUtils = new DatasetUtils();
            dataset = datasetUtils.loadDatasetFromFile(path);
        }

        if (executionMode.equalsIgnoreCase("LABEL") || executionMode.equalsIgnoreCase("FULL")){
            LOGGER.info("Dataset caricato in memoria. Inizio fase di Labeling del Dataset");
            //Inizializzazione della fase di Labeling del Dataset
            applyLabeling(dataset, projectName);
        }
    }

    private Map<String, MetricsCollector> processRelease(ProjectVersion release, GitHandler git, AnalyzerVersion analyzer, String path, String root) throws GitAPIException, IOException {
        String commitId;
        Map<String, MetricsCollector> metrics;

        LOGGER.log(Level.INFO, ">> Elaborazione Release: ", release.versionName());

        commitId = git.checkoutToRelease(release);
        metrics = analyzer.analyzeVersion(path, release, commitId, root);

        LOGGER.log(Level.INFO, ">> Analisi completata per la versione: ", release.versionName());

        return metrics;
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

    private void applyLabeling(List<MetricsCollector> dataset, String projectName){
        JiraTicketRetriever jiraTicketRetriever = new JiraTicketRetriever();


    }
}