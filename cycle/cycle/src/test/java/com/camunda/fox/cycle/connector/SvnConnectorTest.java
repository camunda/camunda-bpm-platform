package com.camunda.fox.cycle.connector;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.camunda.fox.cycle.connector.AbstractConnectorTestBase;
import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.connector.svn.SvnConnector;
import com.camunda.fox.cycle.connector.test.util.RepositoryUtil;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;


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
