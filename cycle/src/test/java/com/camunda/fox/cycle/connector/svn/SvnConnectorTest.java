package com.camunda.fox.cycle.connector.svn;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.camunda.fox.cycle.connector.AbstractConnectorTestBase;
import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;


public class SvnConnectorTest extends AbstractConnectorTestBase {

  private static SvnConnector connector;
  
  @BeforeClass
  public static void beforeClass() throws Exception {
    ConnectorConfiguration config = new ConnectorConfiguration();

    config.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    config.getProperties().put(SvnConnector.CONFIG_KEY_REPOSITORY_PATH, "https://svn.camunda.com/sandbox");
    
    // NOT a spring bean!
    connector = new SvnConnector();
    connector.setConfiguration(config);
    connector.init();
  }
  
  @AfterClass
  public static void afterClass() throws Exception {
    connector.deleteNode(TMP_FOLDER);
  }
  
  @Override
  public Connector getConnector() {
    return connector;
  }

}
