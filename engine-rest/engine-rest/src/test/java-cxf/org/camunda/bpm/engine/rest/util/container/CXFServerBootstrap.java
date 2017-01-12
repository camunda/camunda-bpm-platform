package org.camunda.bpm.engine.rest.util.container;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.camunda.bpm.engine.rest.application.TestCustomResourceApplication;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;

import javax.ws.rs.core.Application;
import java.util.*;

/**
 * @author Christopher Zell
 */
public class CXFServerBootstrap extends EmbeddedServerBootstrap {

  private Server server;
  private String propertyPort;
  private JAXRSServerFactoryBean jaxrsServerFactoryBean;

  public CXFServerBootstrap(Application application) {
    setupServer(application);
  }

  @Override
  public void start() {
    try {
      server.start();
    } catch (Exception e) {
      throw new ServerBootstrapException(e);
    }
  }

  private void setupServer(Application application) {
    jaxrsServerFactoryBean = new JAXRSServerFactoryBean();
    // add resources
    List<Class<?>> resourceClasses = new ArrayList<Class<?>>(CamundaRestResources.getResourceClasses());
    resourceClasses.addAll(TestCustomResourceApplication.getResourceClasses());
    jaxrsServerFactoryBean.setResourceClasses(resourceClasses);

    // get providers classes
    Set<Class<?>> providerClasses = new HashSet<Class<?>>();
    providerClasses.addAll(CamundaRestResources.getConfigurationClasses());
    providerClasses.addAll(TestCustomResourceApplication.getProviderClasses());

    // create instances of providers to add them
    List<Object> instances = new ArrayList<Object>();
    for (Class<?> clazz : providerClasses) {
      try {
        instances.add(clazz.newInstance());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    jaxrsServerFactoryBean.setProviders(instances);

    // set up address
    Properties serverProperties = readProperties();
    propertyPort = serverProperties.getProperty(PORT_PROPERTY);
    jaxrsServerFactoryBean.setAddress("http://localhost:" + propertyPort + ROOT_RESOURCE_PATH);

    // set start to false so create call does not start server
    jaxrsServerFactoryBean.setStart(false);
    server = jaxrsServerFactoryBean.create();
  }

  @Override
  public void stop() {
    try {
      server.stop();
      server.destroy();
      // DO NOT DELETE LINE BELOW
      // KILL'S all jetty threads, otherwise it will block the port and tomcat can't be bind to the address
      jaxrsServerFactoryBean.getBus().shutdown(true);
    } catch (Exception e) {
      throw new ServerBootstrapException(e);
    }
  }
}
