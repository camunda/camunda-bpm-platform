package com.camunda.fox.cycle.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.api.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.impl.connector.signavio.SignavioConnector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/test/signavio-connector-xml-config.xml"}
)
public class SignavioConnectorTest {

  @Inject
  private SignavioConnector signavioConnector;
  
  
  @Test
  public void testConfigurableViaXml() throws Exception {
    assertNotNull(this.signavioConnector);
    
    assertEquals("123", this.signavioConnector.getConnectorId());
    assertEquals("My SignavioConnector", this.signavioConnector.getName());
    
    ConnectorConfiguration config = this.signavioConnector.getConfiguration();
    assertNotNull(config);
    
    assertEquals(ConnectorLoginMode.GLOBAL, config.getLoginMode());
    assertEquals("test@camunda.com", config.getGlobalUser());
    assertEquals("testtest", config.getGlobalPassword()); // TODO decrypt password (waiting for encryptionService)

    Map<String, String> prop = config.getProperties();
    assertNotNull(prop);
    
    assertEquals("http://vm2.camunda.com:8080", prop.get(SignavioConnector.CONFIG_KEY_SIGNAVIO_BASE_URL));
  }
  
}
