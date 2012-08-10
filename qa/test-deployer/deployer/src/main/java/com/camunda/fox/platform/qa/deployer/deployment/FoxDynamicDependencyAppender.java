package com.camunda.fox.platform.qa.deployer.deployment;

import org.activiti.engine.test.Deployment;
import com.camunda.fox.platform.qa.deployer.configuration.ConfigurationExporter;
import com.camunda.fox.platform.qa.deployer.configuration.FoxDeploymentConfiguration;
import com.camunda.fox.platform.qa.deployer.war.impl.ApplicationArchiveContextImpl;
import com.camunda.fox.platform.qa.deployer.metadata.AnnotationInspector;
import com.camunda.fox.platform.qa.deployer.metadata.FoxExtensionEnabler;
import com.camunda.fox.platform.qa.deployer.metadata.MetadataExtractor;
import com.camunda.fox.platform.qa.deployer.war.ContextExecutionException;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxDynamicDependencyAppender implements ApplicationArchiveProcessor {

  private static final Logger log = Logger.getLogger(FoxDynamicDependencyAppender.class.getName());
  
  static final JavaArchive APPLICATION_EXTENSION_ARCHIVE;
  // static final JavaArchive ACTIVITI_CDI_ARCHIVE;
  
  static final String APPLICATION_EXTENSION_ARCHIVE_NAME = "fox-arquillian-application-extension-archive.jar";
  static final String ACTIVITI_CDI_ARCHIVE_NAME = "fox-arquillian-activiti-cdi.jar";
  
  @Inject
  private Instance<FoxDeploymentConfiguration> deploymentConfiguration;
  
  static {
    APPLICATION_EXTENSION_ARCHIVE = ShrinkWrap.create(JavaArchive.class, APPLICATION_EXTENSION_ARCHIVE_NAME)
      .addClass(ApplicationArchiveContextImpl.class)
      .addClass(ApplicationArchiveContext.class)
      .addClass(ContextExecutionException.class);
    
//    ACTIVITI_CDI_ARCHIVE = ShrinkWrap.create(JavaArchive.class, ACTIVITI_CDI_ARCHIVE_NAME)
//      .addPackages(true, "org.activiti.cdi")
//      .addClass(PlatformProcessEngineLookup.class)
//      .addAsResource("META-INF/cdi/services/org.activiti.cdi.spi.ProcessEngineLookup", "META-INF/services/org.activiti.cdi.spi.ProcessEngineLookup")
//      .addAsResource("META-INF/cdi/services/javax.enterprise.inject.spi.Extension", "META-INF/services/javax.enterprise.inject.spi.Extension")
//      .addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml");
  }
  
  @Override
  public void process(Archive<?> applicationArchive, TestClass testClass) {
    final FoxExtensionEnabler foxExtensionEnabler = new FoxExtensionEnabler(testClass);
    if (!foxExtensionEnabler.isExtensionRequired()) {
      return;
    }
    
    String applicationExtensionJndiPrefix = extractApplicationExtensionJndiPrefix(applicationArchive, APPLICATION_EXTENSION_ARCHIVE);
    deploymentConfiguration.get().setExtensionArchiveJndiPrefix(applicationExtensionJndiPrefix);
    
    final Set<String> allProcessDeployments = fetchAllProcessDeployments(testClass);
    if (!allProcessDeployments.isEmpty()) {
      JavaArchive archive = toJavaArchive(allProcessDeployments);
      
      // Add archive specific properties to process archive
      archive.addAsResource(new ByteArrayAsset(exportConfigurationAsProperties().toByteArray()), FoxDeploymentConfiguration.PROPERTIES_FILE);
      addResources(applicationArchive, archive);
    }
    
    addResources(applicationArchive, APPLICATION_EXTENSION_ARCHIVE);
    // addResources(applicationArchive, ACTIVITI_CDI_ARCHIVE);
  }
  
  String extractApplicationExtensionJndiPrefix(Archive<?> applicationArchive, JavaArchive extensionArchive) {
    String name = suffixRemoved(applicationArchive.getName());
    
    if (applicationArchive instanceof EnterpriseArchive) {
      // Application extension archive name is the name of the archive itself + the extension archive name
      name = name + "/" + suffixRemoved(extensionArchive.getName());
    }
    
    return name;
  }
  
  // Private helper methods
  Set<String> fetchAllProcessDeployments(TestClass testClass) {
    final Set<String> processDeployments = new HashSet<String>();

    AnnotationInspector<Deployment> processDeployment = new MetadataExtractor(testClass).deployment();
    Collection<Deployment> deploymentAnnotations = processDeployment.getAll();
    
    for (Deployment annotation: deploymentAnnotations) {
      addProcessDeployments(annotation, processDeployments);
    }
    
    return processDeployments;
  }
  
  private void addProcessDeployments(Deployment annotation, Set<String> deployments) {
    if (annotation != null) {
      deployments.addAll(Arrays.asList(annotation.resources()));
    }
  }

  private void addResources(Archive<?> applicationArchive, final JavaArchive dataArchive) {
    if (JavaArchive.class.isInstance(applicationArchive)) {
      addAsResource(applicationArchive, dataArchive);
    } else {
      addAsLibrary(applicationArchive, dataArchive);
    }
  }

  private void addAsResource(Archive<?> applicationArchive, JavaArchive dataArchive) {
    applicationArchive.merge(dataArchive);
  }

  private void addAsLibrary(Archive<?> applicationArchive, JavaArchive dataArchive) {
    final LibraryContainer<?> libraryContainer = (LibraryContainer<?>) applicationArchive;
    libraryContainer.addAsLibrary(dataArchive);
  }

  JavaArchive toJavaArchive(final Collection<String> deployments) {
    final List<String> paths = new ArrayList<String>(deployments.size());

    for (String deployment : deployments) {
      paths.add(deployment);
    }

    return createArchiveWithResources(paths.toArray(new String[deployments.size()]));
  }

  private JavaArchive createArchiveWithResources(String... resourcePaths) {
    final JavaArchive dataSetsArchive = ShrinkWrap.create(JavaArchive.class);

    for (String path : resourcePaths) {
      dataSetsArchive.addAsResource(path);
    }

    return dataSetsArchive;
  }

  private String suffixRemoved(String name) {
    int indexOfDot = name.lastIndexOf(".");
    if (indexOfDot != -1) {
      return name.substring(0, indexOfDot);
    } else {
      return name;
    }
  }
  
  private ByteArrayOutputStream exportConfigurationAsProperties() {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final ConfigurationExporter exporter = new ConfigurationExporter(deploymentConfiguration.get(), FoxDeploymentConfiguration.PROPERTY_PREFIX);
    exporter.toProperties(output);
    return output;
  }
}
