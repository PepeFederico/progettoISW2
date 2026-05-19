package it.uniroma2.ISW2.Pepe.Federico;

import it.uniroma2.ISW2.Pepe.Federico.labeling.LabelingDataset;
import it.uniroma2.ISW2.Pepe.Federico.metrics.DatasetUtils;
import it.uniroma2.ISW2.Pepe.Federico.metrics.MetricsCollector;
import it.uniroma2.ISW2.Pepe.Federico.proportion.ProportionCalculator;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        DatasetUtils datasetUtils           = new DatasetUtils();
        AnalyzerVersion analyzer;

        List<MetricsCollector> dataset = null;

        if (executionMode.equalsIgnoreCase("FULL") || executionMode.equalsIgnoreCase("BUILD")) {
            // 2    -->  Recupero e filtraggio release
            LOGGER.info("Inizio recupero informazioni sulle release per il progetto: " + projectName);
            List<ProjectVersion> allVersions = retriever.getReleaseInfo(projectName);
            datasetUtils.generateVersionsDataset(projectName,allVersions);

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
                    datasetUtils.generateDataset(projectName, finalDataset);
                } else {
                    LOGGER.warning("Nessun dato raccolto: il file CSV non verrà generato.");
                }
            }
            LOGGER.info("Processo completato con successo per tutte le release filtrate.");
        } else {
            String path = props.getProperty("datasetPath");
            dataset = datasetUtils.loadDatasetFromFile(path);
        }

        if (executionMode.equalsIgnoreCase("LABEL") || executionMode.equalsIgnoreCase("FULL")){
            LOGGER.info("Dataset caricato in memoria. Inizio fase di Labeling del Dataset");

            //Inizializzazione della fase di Labeling del Dataset
            LOGGER.info("Inizio procedura di recupero di tutti i Ticket da Jira...");
            List<JiraTicket> jiraTickets = retrieveTicketFromJira(projectName);
            LOGGER.info("Ticket recuperati con successo ");

            String pathAllVersionsTable = props.getProperty("versionsPath");
            List<VersionField> allVersions = datasetUtils.loadDatasetVersionsFromFile(pathAllVersionsTable);
            double meanP = computeProportion(allVersions, jiraTickets);

            try(GitHandler gitHandler = new GitHandler(localPath, githubUrl)){
                computeLabeling(meanP, dataset, jiraTickets, allVersions, gitHandler);
            }
            LOGGER.info("Conclusa fase di Labeling del Dataset!");
        }

        if (executionMode.equalsIgnoreCase("FEATURE_SELECTIONS") ||
                executionMode.equalsIgnoreCase("LABEL") ||
                executionMode.equalsIgnoreCase("FULL")) {

            LOGGER.info("Milestone 2 --> Sezione Feature Selections. Preparazione Dataset incrementali.");

            WekaDataProcessorController wekaDataProcessorController = new WekaDataProcessorController();

            try {
                Path pathDir = Paths.get("feature_selections");
                if (Files.notExists(pathDir)) {
                    Files.createDirectories(pathDir);
                }

                List<String> cases = Arrays.asList(
                        "dataset_base",
                        "dataset_ig",
                        "dataset_ig_spearman",
                        "dataset_full"
                );

                String path = props.getProperty("finalDataset");
                dataset = datasetUtils.loadDatasetFromFileFeature(path);

                //      Ciclo per scorrere tutti i casi
                for (String caseName : cases) {
                    Path subDir = pathDir.resolve(caseName);

                    if (Files.notExists(subDir)) {
                        Files.createDirectories(subDir);
                        LOGGER.info("Creazione cartella per caso: " + caseName);
                    }

                    wekaDataProcessorController.processDataset(caseName, subDir, dataset, caseName);
                }

            } catch (IOException e) {
                LOGGER.warning("Errore durante la creazione delle directory per la Feature Selection: " +  e.getMessage());
            }
        }
    }

    private void computeLabeling(Double meanP, List<MetricsCollector> dataset, List<JiraTicket> jiraTickets, List<VersionField> allVersions, GitHandler gitHandler) throws GitAPIException, IOException {
        LabelingDataset labelingDataset = new LabelingDataset();

        LOGGER.info("Inizio procedura di Labeling del dataset...");
        labelingDataset.labelTickets(meanP, dataset, jiraTickets, allVersions, gitHandler);
    }

    private double computeProportion(List<VersionField> allVersions, List<JiraTicket> jiraTickets) {
        ProportionCalculator proportionCalculator = new ProportionCalculator(allVersions, jiraTickets);
        proportionCalculator.runCalculation();
        double meanP = proportionCalculator.getMean();

        LOGGER.info("Intervallo di Confidenza per P (alpha = 0.05) : " + "[ " + meanP + " +/- " + proportionCalculator.getConfidenceIntervalWidth() + " ]");
        LOGGER.info("Calcolo della Proportion Total calcolata con successo!");

        return meanP;
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

    private List<JiraTicket> retrieveTicketFromJira(String projectName) throws IOException {
        JiraTicketRetriever jiraTicketRetriever = new JiraTicketRetriever();

        return jiraTicketRetriever.retrieveTicketFromJira(projectName);
    }
}