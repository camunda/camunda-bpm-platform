package org.camunda.bpm.engine.rest.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;
import org.camunda.bpm.engine.rest.mapper.ProcessDefinitionQueryDtoReader;

public class CXFRestBootstrap {

  private static final int PORT = 8080;
  protected static final String SERVER_ADDRESS = "http://localhost:" + PORT;
  
  private Server server;
  
  public CXFRestBootstrap() {
    setupServer();
  }
  
  public void start() {
    server.start();
  }
  
  public void stop() {
    server.stop();
    server.destroy();
  }
  
  private void setupServer() {
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setResourceClasses(ProcessDefinitionServiceImpl.class);
    
    List<Object> providers = new ArrayList<Object>();
    providers.add(new JSONProvider());
    providers.add(new ProcessDefinitionQueryDtoReader());
    sf.setProviders(providers);
    
    sf.setAddress(SERVER_ADDRESS);
    server = sf.create();
    
  }
}
