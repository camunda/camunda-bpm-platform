package org.camunda.bpm.engine.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.activiti.engine.ProcessEngine;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;
import org.camunda.bpm.engine.rest.mapper.ProcessDefinitionQueryDtoReader;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractRestServiceTest {
  
  private static final int PORT = 8080;
  protected static final String SERVER_ADDRESS = "http://localhost:" + PORT;
  
  protected static ProcessEngine processEngine;
  protected static Server server;
  
  @BeforeClass
  public static void initialize() {
    
    loadProcessEngineService();
    setupServer();
  }
  
  @AfterClass
  public static void tearDown() {
    server.stop();
    server.destroy();
  }

  private static void setupServer() {
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setResourceClasses(ProcessDefinitionServiceImpl.class);
    List<Object> providers = new ArrayList<Object>();
//    providers.add(JSONProvider.class);
//    providers.add(InvalidRequestExceptionMapper.class);
    providers.add(new JSONProvider());
    providers.add(new ProcessDefinitionQueryDtoReader());
//    sf.setProvider(JSONProvider.class);
    sf.setProviders(providers);
    sf.setAddress(SERVER_ADDRESS);
    server = sf.create();
    
  }

  private static void loadProcessEngineService() {
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      processEngine = provider.getProcessEngine();      
    }
  }
  
}
