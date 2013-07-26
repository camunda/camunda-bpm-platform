package org.camunda.bpm.cycle.connector;

import java.io.File;

import org.camunda.bpm.cycle.connector.svn.SvnConnector;
import org.camunda.bpm.cycle.connector.test.util.RepositoryUtil;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;



public class SvnConnectorTest extends AbstractConnectorTestBase {

  private static SvnConnector connector;

  private static final File SVN_DIRECTORY = new File("target/svn-repository");

  public static void setupConnector() throws Exception {

    String svnUrl = RepositoryUtil.createSVNRepository(SVN_DIRECTORY);

    ConnectorConfiguration config = new ConnectorConfiguration();

    config.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    config.getProperties().put(SvnConnector.CONFIG_KEY_REPOSITORY_PATH, svnUrl);

    // NOT a spring bean!
    connector = new SvnConnector();
    connector.setConfiguration(config);
    connector.init();
  }

  public static void teardownConnector() throws Exception {
    connector.dispose();
    RepositoryUtil.clean(SVN_DIRECTORY);
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    setupConnector();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    teardownConnector();
  }

  @Override
  public Connector getConnector() {
    return connector;
  }
}
