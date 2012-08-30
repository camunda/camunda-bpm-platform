package com.camunda.fox.cycle.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.impl.connector.signavio.SignavioConnector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/test/signavio-connector-xml-config.xml"}
)
public class SignavioConnectorTest {

  @Inject
  private SignavioConnector signavioConnector;
  
//  @Test
//  public void shouldBeConfigurableViaXml() throws Exception  {
//    assertNotNull(this.signavioConnector.getConfiguration().getProperties());
//    assertEquals("http://vm2.camunda.com:8080", this.signavioConnector.getConfiguration().getProperties().get("signavioBaseUrl"));
//    
//    assertEquals("My SignavioConnector", this.signavioConnector.getName());
//    
//    ConnectorNode node = new ConnectorNode();
//    node.setId("/9b865d020a9d4714a189caeffec772e3");
//    node.setLabel("9b865d020a9d4714a189caeffec772e3");
//    node.setType(ConnectorNodeType.FILE);
//    
//    this.signavioConnector.getContent(node);
//    
//  }
}
