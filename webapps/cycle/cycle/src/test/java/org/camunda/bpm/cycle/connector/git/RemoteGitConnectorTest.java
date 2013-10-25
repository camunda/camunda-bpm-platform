package org.camunda.bpm.cycle.connector.git;

import java.io.IOException;

import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Joerg Bellmann <joerg.bellmann@zalando.de>
 */
@Ignore
public class RemoteGitConnectorTest extends LocalGitConnectorTest {

  @Override
  @Before
  public void before() throws Exception {
    config = new ConnectorConfiguration();

    // config.getProperties().put(GitConnector.USERNAME, "");
    // config.getProperties().put(GitConnector.PASSWORD, "");
    super.before();
  }

  @Override
  protected void initializeRemoteRepository() throws IOException, GitAPIException {

    // nothing to do here
  }

  @Override
  protected void fillRemoteRepository() throws InvalidRemoteException, TransportException, GitAPIException, IOException {

    // nothing to do here
  }

  @Override
  protected String getUri() {

    return "http://temp-git.zalando:7990/scm/asa/cycle-git-connector-sync-test.git";
  }

  @Test
  public void run() {
    Assert.assertTrue(true);
  }

}
