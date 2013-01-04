package org.camunda.bpm.engine.rest;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.ws.rs.core.Application;

import org.activiti.engine.ProcessEngine;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;

//@RunWith(Arquillian.class)
public abstract class AbstractRestServiceTest {

  private static final int PORT = 8080;
  protected static final String SERVER_ADDRESS = "http://localhost:" + PORT;
  protected static ProcessEngine processEngine;
  protected static Server server;
  
  @BeforeClass
  public static void initialize() {
    
    loadProcessEngineService();
    setUpServer();
  }
  
  private static void setUpServer() {
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
//    Application testApp = new TestApplication();
    sf.setResourceClasses(ProcessDefinitionServiceImpl.class);
//    sf.setApplication(testApp);
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
  
  @AfterClass
  public static void tearDown() {
    server.stop();
    server.destroy();
  }

//  @Deployment
//  public static JavaArchive createDeployment() {
//    return ShrinkWrap.create(JavaArchive.class, "test.jar")
//        .addPackages(true, "org.camunda.bpm.engine.rest")
//        .addAsResource("META-INF/services/org.camunda.bpm.engine.rest.spi.ProcessEngineProvider");
//  }
}
