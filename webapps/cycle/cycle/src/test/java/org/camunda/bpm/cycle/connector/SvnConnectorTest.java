package org.camunda.bpm.cycle.connector;

import java.io.File;

import org.camunda.bpm.cycle.connector.AbstractConnectorTestBase;
import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorLoginMode;
import org.camunda.bpm.cycle.connector.svn.SvnConnector;
import org.camunda.bpm.cycle.connector.test.util.RepositoryUtil;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;



public class SvnConnectorTest extends AbstractConnectorTestBase {

  private static SvnConnector connector;
  
  private static final File SVN_DIRECTORY = new File("target/svn-repository");
  
  @BeforeClass
  public static void beforeClass() throws Exception {

    String svnUrl = RepositoryUtil.createSVNRepository(SVN_DIRECTORY);

    ConnectorConfiguration config = new ConnectorConfiguration();

    config.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    config.getProperties().put(SvnConnector.CONFIG_KEY_REPOSITORY_PATH, svnUrl);

    // NOT a spring bean!
    connector = new SvnConnector();
    connector.setConfiguration(config);
    connector.init();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    connector.dispose();
    RepositoryUtil.clean(SVN_DIRECTORY);
  }

  @Override
  public Connector getConnector() {
    return connector;
  }

}
