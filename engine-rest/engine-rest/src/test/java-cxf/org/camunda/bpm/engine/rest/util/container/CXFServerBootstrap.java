package org.camunda.bpm.engine.rest.util.container;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

  private void separateProvidersAndResources(Application application, List<Class<?>> resourceClasses, List<Object> providerInstances) {
    Set<Class<?>> classes = application.getClasses();

    for (Class<?> clazz : classes) {
      Annotation[] annotations = clazz.getAnnotations();
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(Provider.class)) {
          // for providers we have to create an instance
          try {
            providerInstances.add(clazz.newInstance());
            break;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        } else if (annotation.annotationType().equals(Path.class)) {
          resourceClasses.add(clazz);
          break;
        }
      }
    }
  }


  private void setupServer(Application application) {
    jaxrsServerFactoryBean = new JAXRSServerFactoryBean();
    List<Class<?>> resourceClasses = new ArrayList<Class<?>>();
    List<Object> providerInstances = new ArrayList<Object>();

    // separate the providers and resources from the application returned classes
    separateProvidersAndResources(application, resourceClasses, providerInstances);
    jaxrsServerFactoryBean.setResourceClasses(resourceClasses);
    jaxrsServerFactoryBean.setProviders(providerInstances);

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
