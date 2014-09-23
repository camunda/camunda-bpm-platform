package org.camunda.bpm.cycle.connector.git;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example Roundtrip.
 * 
 * @author Joerg Bellmann <joerg.bellmann@zalando.de>
 */
@Ignore
public class SimpleGitRoundtrip {
  private Logger log = LoggerFactory.getLogger(SimpleGitRoundtrip.class);

  private Git repository;

  @Rule
  public TemporaryFolder tempFolders = new TemporaryFolder();

  private File baseFileStore;

  @Test
  public void test() throws InvalidRemoteException, TransportException, GitAPIException, IOException {

    this.baseFileStore = getBaseFileStore();

    log.info("BaseFileStore: {}", baseFileStore.getAbsolutePath());

    // init git clone
    this.repository = Git.cloneRepository().setBare(false).setCredentialsProvider(new UsernamePasswordCredentialsProvider(getUsername(), getPassword()))
        .setURI(getUri()).setDirectory(new File(this.baseFileStore.getAbsolutePath())).call();

    // write files to baseStore

    File one = new File(this.baseFileStore, "one.txt");
    one.createNewFile();

    File two = new File(this.baseFileStore, "two.txt");
    two.createNewFile();

    File three = new File(this.baseFileStore, "three.txt");
    three.createNewFile();

    // add Files written

    try {
      this.repository.add().addFilepattern("one.txt").call();
      this.repository.add().addFilepattern("two.txt").call();
      this.repository.add().addFilepattern("three.txt").call();
      //
      this.repository.commit().setMessage("SimpleGit-Test").call();

      Iterable<PushResult> pushResults = this.repository.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(getUsername(), getPassword()))
          .setPushAll().call();

      for (PushResult pResult : pushResults) {

        Iterator<RemoteRefUpdate> update = pResult.getRemoteUpdates().iterator();

        while (update.hasNext()) {

          RemoteRefUpdate u = update.next();
          log.debug("RemoteRefUpdate-Status: {}", u.getStatus().toString());
        }
      }
    } catch (InvalidRemoteException e) {

      log.error(e.getMessage(), e);
    } catch (TransportException e) {

      log.error(e.getMessage(), e);
    } catch (GitAPIException e) {

      log.error(e.getMessage(), e);
    } finally {
      //
    }
  }

  private File getBaseFileStore() throws IOException {

    return tempFolders.newFolder();
  }

  private String getUri() {

    return "http://temp-git.zalando:7990/scm/asa/cycle-git-connector-sync-test.git";
  }

  private String getUsername() {
    return "";
  }

  private String getPassword() {
    return "";
  }

}
