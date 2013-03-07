package org.camunda.bpm.cycle.repository;

import javax.inject.Inject;

import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.cycle.repository.ConnectorConfigurationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/context.xml", "classpath:/spring/test-*.xml"}
)
public class ConnectorConfigurationRepositoryTest {

  @Inject
  private ConnectorConfigurationRepository connectorRepository;
  
  private ConnectorConfiguration config1;
  private ConnectorConfiguration config2;

  @Before
  @Transactional
  public void before() throws Exception {
    
    config1 = new ConnectorConfiguration();
    config1.setName("aaa");
    config1.setConnectorClass("my.bar");
    
    config2 = new ConnectorConfiguration();
    config2.setName("ddd");
    config2.setConnectorClass("my.foo");
    
    connectorRepository.saveAndFlush(config1);
    connectorRepository.saveAndFlush(config2);
  }
  
  @After
  public void after() {
    connectorRepository.deleteAll();
  }
  
  @Test
  public void shouldDeleteAllConnectorConfigurations() throws Exception {
    connectorRepository.deleteAll();
  }
}
