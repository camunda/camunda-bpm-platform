package org.camunda.bpm.engine.rest;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.ws.rs.core.MediaType;

import org.activiti.engine.ProcessEngine;
import org.apache.http.entity.ContentType;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class AbstractRestServiceTest {

  protected static ProcessEngine processEngine;
  protected static final String TEST_RESOURCE_ROOT_PATH = "/rest-test";
  
  protected static final String POST_JSON_CONTENT_TYPE = ContentType.create(MediaType.APPLICATION_JSON, "UTF-8").toString();

//  @BeforeClass
//  public static void initialize() {
//
//    loadProcessEngineService();
//  }

  protected static void loadProcessEngineService() {
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader
        .load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();

    if (iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      processEngine = provider.getProcessEngine();
    }
  }

  @Deployment(testable = true)
  public static WebArchive createDeployment() {
    
    JavaArchive jar = ShrinkWrap
        .create(JavaArchive.class, "rest-test.jar")
        .addPackages(true, "org.camunda.bpm.engine.rest")
        .addAsResource(
            "META-INF/services/org.camunda.bpm.engine.rest.spi.ProcessEngineProvider");

    WebArchive war = ShrinkWrap
        .create(WebArchive.class, "rest-test-webapp.war")
        .addAsLibrary(jar)
        .addAsLibraries(
            DependencyResolvers.use(MavenDependencyResolver.class)
            .goOffline()
                .artifact("org.mockito:mockito-core:1.8.2")
                .artifact("com.jayway.restassured:rest-assured:1.7.2")
                .artifact("joda-time:joda-time:2.1")
            .resolveAsFiles());
    
    addDirectoryContentsAsWebInfResources(war, "WEB-INF");

    return war;
  }
  
  private static void addDirectoryContentsAsWebInfResources(WebArchive war, String directoryInClasspath) {
    ClassLoader classLoader = AbstractRestServiceTest.class.getClassLoader();
    URL resource = classLoader.getResource(directoryInClasspath);
    if (resource != null) {
      File file = new File(resource.getFile());
      if (file.exists() && file.isDirectory()) {
        File[] list = file.listFiles();
        for (int i = 0; i < list.length; i++) {
          File child = list[i];
          if (child.isFile()) {
            war.addAsWebInfResource(child);
          }
        }
      }
    }
  }

}
