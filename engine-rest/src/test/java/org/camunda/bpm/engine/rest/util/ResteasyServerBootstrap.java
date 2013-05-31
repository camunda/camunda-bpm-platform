package org.camunda.bpm.engine.rest.util;

import java.util.Properties;

import org.camunda.bpm.engine.rest.exception.ProcessEngineExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.impl.ExecutionRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.IdentityRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.MessageRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;

public class ResteasyServerBootstrap extends EmbeddedServerBootstrap {

  private NettyJaxrsServer server;
  
  public ResteasyServerBootstrap() {
    setupServer();
  }
  
  public void start() {
    server.start();
  }
  
  public void stop() {
    server.stop();
  }
  
  private void setupServer() {
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    
    server = new NettyJaxrsServer();
    server.setRootResourcePath(ROOT_RESOURCE_PATH);
    server.setPort(port);
    
    server.getDeployment().getActualResourceClasses().add(ProcessDefinitionRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessInstanceRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(TaskRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessEngineRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(IdentityRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(MessageRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ExecutionRestServiceImpl.class);
    
    server.getDeployment().getActualProviderClasses().add(JacksonConfigurator.class);
    
    server.getDeployment().getActualProviderClasses().add(JacksonJsonProvider.class);
    
    server.getDeployment().getActualProviderClasses().add(ProcessEngineExceptionHandler.class);
    server.getDeployment().getActualProviderClasses().add(RestExceptionHandler.class);
  }

}
