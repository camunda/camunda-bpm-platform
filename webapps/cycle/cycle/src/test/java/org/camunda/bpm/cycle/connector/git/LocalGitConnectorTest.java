package org.camunda.bpm.cycle.connector.git;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.camunda.bpm.cycle.connector.AbstractConnectorTestBase;
import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.svn.SvnConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joerg Bellmann <joerg.bellmann@zalando.de>
 */
public class LocalGitConnectorTest extends AbstractConnectorTestBase {

  private final Logger log = LoggerFactory.getLogger(LocalGitConnectorTest.class);

  private static GitConnector connector;

  @Rule
  public TemporaryFolder tempFolders = new TemporaryFolder();

  private File remoteRepositoryFolder;

  protected File temporaryFileStoreFolder;

  protected ConnectorConfiguration config;

  @Override
  @Before
  public void before() throws Exception {

    initializeRemoteRepository();

    fillRemoteRepository();
    if (config == null) {

      config = new ConnectorConfiguration();
    }

    // String url = RepositoryUtil.createVFSRepository(VFS_DIRECTORY);

    temporaryFileStoreFolder = tempFolders.newFolder();

    log.info("tempFileStore: " + temporaryFileStoreFolder.getAbsolutePath());
    config.getProperties().put(SvnConnector.CONFIG_KEY_TEMPORARY_FILE_STORE, temporaryFileStoreFolder.getAbsolutePath());

    config.getProperties().put(SvnConnector.CONFIG_KEY_REPOSITORY_PATH, getUri());

    //
    // String username = config.getProperties().get(GitConnector.USERNAME);
    // if (username == null) {
    // config.getProperties().put(GitConnector.USERNAME, "klaus");
    // config.getProperties().put(GitConnector.PASSWORD, "geheim");
    // }

    // NOT a spring bean!
    connector = new GitConnector();
    connector.setConfiguration(config);
    connector.login("klaus", "geheim");
    connector.init();

    //
    super.before();
  }

  protected String getUsername() {
    return "klaus";
  }

  protected String getPassword() {
    return "geheim";
  }

  protected void initializeRemoteRepository() throws IOException, GitAPIException {

    // create RemoteGitRepository
    remoteRepositoryFolder = tempFolders.newFolder();

    Git.init().setBare(true).setDirectory(remoteRepositoryFolder).call();
    log.info("Remote-Repository: " + remoteRepositoryFolder.getAbsolutePath());
  }

  protected void fillRemoteRepository() throws InvalidRemoteException, TransportException, GitAPIException, IOException {

    Git cloned = Git.cloneRepository().setBare(false).setURI(getUri()).setDirectory(new File(tempFolders.newFolder().getAbsolutePath())).call();

    File missingFile = new File(getLocalCloneDirectory(), "missing.txt");
    FileWriter writer = new FileWriter(missingFile);
    writer.append("Hello Git");
    writer.close();
    cloned.commit().setMessage("a message").call();
    cloned.push().call();

  }

  protected String getUri() {
    return remoteRepositoryFolder.getAbsolutePath();
  }

  protected File getLocalCloneDirectory() {
    return null;
  }

  @After
  public void tearDown() {
    connector.dispose();
    connector = null;
    log.info("Delete all created tempDirectories.");
    tempFolders.delete();
    log.info(" ------");
  }

  @Override
  public Connector getConnector() {
    return connector;
  }

  @Test
  public void run() {

    // for dEbug
    Assert.assertTrue(true);
  }
}
