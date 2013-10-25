package org.camunda.bpm.cycle.connector.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.connector.Secured;
import org.camunda.bpm.cycle.connector.Threadsafe;
import org.camunda.bpm.cycle.connector.vfs.VfsConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * This connector just delegates to a {@link VfsConnector} and pulls from remote
 * before and push to remote after any read or write action.
 * 
 * @author Joerg Bellmann <joerg.bellmann@zalando.de>
 */
@Component
public class GitConnector extends Connector {

  private final Logger log = LoggerFactory.getLogger(GitConnector.class);

  // taken von SVN-Connector
  public static final String CONFIG_KEY_REPOSITORY_PATH = "repositoryPath";
  public static final String CONFIG_KEY_BRANCH_NAME = "branchname";

  public static final String DEFAULT_BRANCH_NAME = "cycle";

  protected VfsConnector delegate = null;

  private Git localGit = null;

  private String baseTemporaryFileStore;

  private final Object mutex = new Object();

  protected String username;
  protected String password;

  protected String branch;

  @Override
  public void init(final ConnectorConfiguration config) {
    if (isConnectorDelegateInitialized()) {
      return;
    } else {
      synchronized (mutex) {
        if (isConnectorDelegateInitialized()) {
          return;
        } else {

          log.debug("Initialize Git-Connector ...");

          try {

            final File baseTemporaryFileStoreDirectory = Files.createTempDirectory("CAMUNDA_GIT_CON_TEMP_", new FileAttribute[0]).toFile();

            baseTemporaryFileStore = baseTemporaryFileStoreDirectory.getAbsolutePath();
          } catch (IOException e) {
            throw new RuntimeException("Unable to create TempFolder for GitConnector!", e);
          }

          String branchNameProperty = config.getProperties().get(CONFIG_KEY_BRANCH_NAME);
          if (branchNameProperty != null) {
            this.branch = branchNameProperty.trim();
          }

          if (this.branch == null || this.branch.trim().isEmpty()) {
            this.branch = DEFAULT_BRANCH_NAME;
          }

          log.debug("branchname was set to default : {}", this.branch);

          //
          config.getProperties().put(VfsConnector.BASE_PATH_KEY, baseTemporaryFileStore);

          // check configuration is available
          if (getConfiguration() == null) {
            setConfiguration(config);
          }

          // set config and init
          this.delegate = new VfsConnector();
          delegate.setConfiguration(config);
          delegate.init();

          log.debug("GitConnector initialized, Repository will be initialized on first access.");
        }
      }
    }
  }

  @Threadsafe
  @Secured
  @Override
  public List<ConnectorNode> getChildren(final ConnectorNode node) {

    pullFromRemote();
    return delegate.getChildren(node);
  }

  @Threadsafe
  @Secured
  @Override
  public InputStream getContent(final ConnectorNode node) {
    pullFromRemote();
    return delegate.getContent(node);
  }

  protected void initRepository() {
    if (isRepositoryInitialized()) {
      return;
    } else {
      synchronized (mutex) {
        if (isRepositoryInitialized()) {
          return;
        } else {
          log.debug("Initialize Repository ...");
          Assert.hasText(this.username, "The username should never be null or empty! Does the Aspect work?");
          Assert.hasText(this.password, "The password should never be null or empty! Does the Aspect work?");

          final String uri = getConfiguration().getProperties().get(CONFIG_KEY_REPOSITORY_PATH);
          Assert.hasText(uri, "URI should never be null or empty!");
          log.debug("clone from Remote uri: {}", uri);
          try {
            this.localGit = Git.cloneRepository().setBare(false).setCloneAllBranches(true).setBranch("master")
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setURI(uri).setDirectory(new File(baseTemporaryFileStore))
                .call();

            if (log.isDebugEnabled()) {

              List<Ref> branches = this.localGit.branchList().call();

              for (Ref ref : branches) {
                log.debug("Found branch : {}", ref.getName());
              }
            }

            boolean branchExist = this.localGit.getRepository().getRef(branch) != null;

            if (!branchExist) {
              log.debug("Branch does not exist");
              this.localGit.checkout().setName(branch).setCreateBranch(true).setUpstreamMode(SetupUpstreamMode.TRACK).call();

              log.debug("Branch created and checked out");
            } else {
              log.debug("Branch does exist");
              this.localGit.checkout().setName(branch).call();
              log.debug("Branch checked out");
            }

            // has to be configured, otherwise we could not pull
            StoredConfig config = localGit.getRepository().getConfig();
            config.setString("branch", branch, "merge", "refs/heads/" + branch);
            config.save();

            // Just to make sure it is working, if not better on initialization
            this.localGit.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
            this.localGit.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();

            log.debug("Repository initialized with temporary file store {}", baseTemporaryFileStore);
          } catch (GitAPIException e) {

            throw new RuntimeException("Exception occurred when initialize Repository", e);
          } catch (IOException e) {

            throw new RuntimeException("Exception occurred when initialize Repository", e);
          }
        }
      }
    }
  }

  /**
   * Returns true if the repository is not null.
   * 
   * @return
   */
  protected boolean isRepositoryInitialized() {
    return this.localGit != null;
  }

  /**
   * Returns true if the delegate is not null.
   * 
   * @see #delegate
   * @return
   */
  protected boolean isConnectorDelegateInitialized() {
    return this.delegate != null;
  }

  @Threadsafe
  @Secured
  @Override
  public ConnectorNode getRoot() {

    pullFromRemote();

    return delegate.getRoot();
  }

  @Threadsafe
  @Secured
  @Override
  public ContentInformation updateContent(final ConnectorNode node, final InputStream newContent, final String message) throws Exception {

    pullFromRemote();

    ContentInformation temp = delegate.updateContent(node, newContent, message);

    pushToRemote(node, "Update Content for " + node.getId());

    return temp;
  }

  protected void pullFromRemote() {
    initRepository();
    log.debug("enter 'pullFromRemote'");

    try {

      this.localGit.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setRebase(false).call();
    } catch (GitAPIException e) {

      throw new RuntimeException(e);
    } finally {
      clearCredentials();
    }
  }

  protected void pushToRemote(final ConnectorNode node, String message) {
    initRepository();
    log.debug("enter 'pushToRemote', for node with Label '{}' and message '{}'", node.getId(), message);

    // Folders does not exist in git
    if (node.getType().equals(ConnectorNodeType.FOLDER)) {
      log.debug("Is an Folder. Skip push.");
      return;
    }

    if (message == null) {
      message = "No message provided";
    }

    try {
      String path = node.getId();
      if (path.startsWith("//")) {
        path = path.substring(2);
      }

      log.debug("Filepattern: {}", path);

      this.localGit.add().addFilepattern(path).call();
      this.localGit.commit().setMessage(message).call();

      Iterable<PushResult> pushResults = this.localGit.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setPushAll()
          .call();

      for (PushResult pResult : pushResults) {

        Iterator<RemoteRefUpdate> update = pResult.getRemoteUpdates().iterator();

        while (update.hasNext()) {

          RemoteRefUpdate u = update.next();
          log.debug("RemoteRefUpdate-Status: {}", u.getStatus().toString());
        }
      }
    } catch (InvalidRemoteException e) {

      throw new RuntimeException(e);
    } catch (TransportException e) {

      throw new RuntimeException(e);
    } catch (GitAPIException e) {

      throw new RuntimeException(e);
    } finally {
      clearCredentials();
    }
  }

  private void clearCredentials() {
    log.debug("Clear Credentials");
    this.username = "";
    this.password = "";
  }

  @Threadsafe
  @Secured
  @Override
  public ConnectorNode getNode(final String id) {

    pullFromRemote();
    return delegate.getNode(id);
  }

  @Threadsafe
  @Secured
  @Override
  public ConnectorNode createNode(final String parentId, final String label, final ConnectorNodeType type, final String message) {

    pullFromRemote();

    ConnectorNode temp = delegate.createNode(parentId, label, type, message);

    pushToRemote(temp, "Create Node " + parentId + "/" + label);

    return temp;
  }

  @Threadsafe
  @Secured
  @Override
  public void deleteNode(final ConnectorNode node, final String message) {

    pullFromRemote();

    delegate.deleteNode(node, message);

    pushToRemote(node, "Delete Node " + node.getId());
  }

  @Override
  public ContentInformation getContentInformation(final ConnectorNode node) {

    pullFromRemote();

    return delegate.getContentInformation(node);
  }

  @Threadsafe
  @Override
  public void login(final String userName, final String password) {
    log.debug("Login user with username: '{}'", userName);
    this.username = userName;
    this.password = password;
  }

  @Override
  public boolean needsLogin() {

    return true;
  }

  @Override
  public boolean isSupportsCommitMessage() {

    return false;
  }

  @Override
  public void setConfiguration(final ConnectorConfiguration configuration) {

    super.setConfiguration(configuration);
  }

  @Override
  public void init() {

    super.init();
  }

  @Override
  public void dispose() {
    log.debug("enter 'dispose'");
    delegate.dispose();
    disposeRepository();
    localGit = null;
  }

  /**
   * Last Push when Connector will be disposed happens here.
   */
  protected void disposeRepository() {
    log.debug("enter 'disposeRepository'");

    try {
      this.localGit.commit().setMessage("Dispose Connector").call();

      Iterable<PushResult> pushResults = this.localGit.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setPushAll()
          .call();

      for (PushResult pResult : pushResults) {

        Iterator<RemoteRefUpdate> update = pResult.getRemoteUpdates().iterator();

        while (update.hasNext()) {

          RemoteRefUpdate u = update.next();
          log.debug("RemoteRefUpdate-Status: {}", u.getStatus().toString());
        }
      }

      try {
        FileUtils.deleteDirectory(new File(this.baseTemporaryFileStore));
      } catch (IOException e) {
        log.error("Unable to delete 'baseTemporaryFileStore'", e);
      }
    } catch (InvalidRemoteException e) {

      log.error(e.getMessage(), e);
    } catch (TransportException e) {

      log.error(e.getMessage(), e);
    } catch (GitAPIException e) {

      log.error(e.getMessage(), e);
    } finally {
      this.localGit = null;
    }
  }
}
