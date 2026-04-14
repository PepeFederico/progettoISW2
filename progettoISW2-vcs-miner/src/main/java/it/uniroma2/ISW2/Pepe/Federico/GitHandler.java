package it.uniroma2.ISW2.Pepe.Federico;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;

public class GitHandler {
    private final Git git;
    private final File localPath;

    public GitHandler(String localPath, String githubUrl) throws GitAPIException, IOException {
        this.localPath = new File(localPath);

        if(!this.localPath.exists()){
            System.out.println("    >> Repository non trovato. Clonazione da: " + githubUrl);
            this.git = Git.cloneRepository().setURI(githubUrl).setDirectory(this.localPath).call();
        } else {
            Repository repository = new FileRepositoryBuilder().setGitDir(new File(this.localPath, ".git")).build();
            this.git = new Git(repository);
            System.out.println("    >> Repository in locale aperto correttamente !");
        }
    }

    public void checkoutToRelease(ProjectVersion version) throws IOException, GitAPIException {
        long releaseTimestamp = version.Date().toEpochSecond(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        Iterable<RevCommit> commits = git.log().all().call();
        RevCommit targetCommit = StreamSupport.stream(commits.spliterator(), false)
                .filter(commit ->
                        commit.getCommitTime() <= releaseTimestamp
                )
                .findFirst()
                .orElseThrow(() -> new IOException("Nessun commit trovato per la data: " + version.Date()));

        System.out.println("    >> Release: " + version.VersionName() + " | Data: " + version.Date().format(formatter));
        System.out.println("    >> Commit individuato: " + targetCommit.getName() + " del " + targetCommit.getCommitTime());

        git.checkout()
                .setName(targetCommit.getName())
                .setForced(true)
                .call();
    }

    public String getLocalPath(){
        return localPath.getAbsolutePath();
    }
}
