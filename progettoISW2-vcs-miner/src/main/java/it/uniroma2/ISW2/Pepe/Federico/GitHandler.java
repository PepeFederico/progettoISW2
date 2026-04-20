package it.uniroma2.ISW2.Pepe.Federico;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.TagOpt;
import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class GitHandler implements AutoCloseable {

    private final Git git;
    private static final String[] TAG_PATTERNS = {"v%s", "%s", "storm-%s"};

    public GitHandler(String localPath, String githubUrl) throws GitAPIException, IOException {
        File localDir = new File(localPath);

        if (!localDir.exists()) {
            log("Repository non trovato. Clonazione da: " + githubUrl);
            this.git = Git.cloneRepository()
                    .setURI(githubUrl)
                    .setDirectory(localDir)
                    .call();
        } else {
            this.git = Git.open(localDir);
            log("Repository in locale aperto correttamente!");
        }
    }

    public void checkoutToRelease(ProjectVersion version) throws IOException, GitAPIException {
        String versionName = version.versionName().trim();

        fetchAllTags();

        // Cerchiamo il commit: prima via Tag, poi via Data
        String commitHash = findHashByTag(versionName).orElseGet(() -> findHashByDate(version));
        executeCheckout(commitHash);
    }

    private void fetchAllTags() throws GitAPIException {
        log("Aggiornamento Tag dal repository remoto...");
        git.fetch().setTagOpt(TagOpt.FETCH_TAGS).call();
    }

    private Optional<String> findHashByTag(String versionName) throws IOException {
        Repository repo = git.getRepository();

        for (String pattern : TAG_PATTERNS) {
            String formattedPattern = String.format(pattern, versionName);
            ObjectId tagId = repo.resolve(formattedPattern);

            // Fallback per refs/tags/ se il resolve diretto fallisce
            if (tagId == null) {
                tagId = repo.resolve("refs/tags/" + formattedPattern);
            }

            if (tagId != null) {
                try (RevWalk walk = new RevWalk(repo)) {
                    RevObject revObject = walk.parseAny(tagId);
                    String hash = (revObject instanceof RevTag revTag)
                            ? revTag.getObject().getName()
                            : revObject.getName();

                    log("Tag Ufficiale trovato: " + formattedPattern + " [Commit: " + formatHash(hash) + "]");
                    return Optional.of(hash);
                }
            }
        }
        return Optional.empty();
    }

    private String findHashByDate(ProjectVersion version) {
        log("Nessun Tag trovato per " + version.versionName() + ". Ricerca per data...");

        try {
            Repository repo = git.getRepository();
            ObjectId rootId = resolveMainBranch(repo)
                    .orElseThrow(() -> new IOException("Riferimento principale (master/main/HEAD) non trovato"));

            long releaseTimestamp = version.date().toEpochSecond(ZoneOffset.UTC);

            Iterable<RevCommit> commits = git.log().add(rootId).call();
            RevCommit targetCommit = StreamSupport.stream(commits.spliterator(), false)
                    .filter(c -> c.getCommitTime() <= releaseTimestamp)
                    .findFirst()
                    .orElseThrow(() -> new IOException("Nessun commit trovato per la data specificata"));

            log("Commit individuato per data: " + formatHash(targetCommit.getName()));
            return targetCommit.getName();

        } catch (Exception e) {
            throw new RuntimeException("Errore critico durante la ricerca per data", e);
        }
    }

    private Optional<ObjectId> resolveMainBranch(Repository repo) throws IOException {
        String[] refs = {"refs/remotes/origin/master", "refs/remotes/origin/main", "HEAD"};
        for (String ref : refs) {
            ObjectId id = repo.resolve(ref);
            if (id != null) return Optional.of(id);
        }
        return Optional.empty();
    }

    private void executeCheckout(String hash) throws GitAPIException {
        git.checkout()
                .setName(hash)
                .setForced(true)
                .call();
        log("Checkout completato su: " + formatHash(hash));
    }

    private void log(String message) {
        System.out.println("    >> " + message);
    }

    private String formatHash(String hash) {
        return hash != null && hash.length() > 10 ? hash.substring(0, 10) : hash;
    }

    @Override
    public void close() {
        if (git != null) {
            git.getRepository().close();
            git.close();
        }
    }
}
