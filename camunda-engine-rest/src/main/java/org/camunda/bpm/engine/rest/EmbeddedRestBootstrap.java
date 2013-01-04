package org.camunda.bpm.engine.rest;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;

public class EmbeddedRestBootstrap {
  
  private String address;
  private int port;
  
  protected static Server server;
  
  public EmbeddedRestBootstrap(String address, int port) {
    this.address = address;
    this.port = port;
  }
  
  public void start() {
    
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setResourceClasses(ProcessDefinitionServiceImpl.class);
    sf.setProvider(JSONProvider.class);
    String serverAddress = address + port;
    sf.setAddress(serverAddress);
    server = sf.create();
  }
  
  public void stop() {
    
    server.stop();
    server.destroy();
  }
}
