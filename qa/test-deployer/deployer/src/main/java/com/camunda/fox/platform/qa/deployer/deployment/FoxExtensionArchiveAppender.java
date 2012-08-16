package com.camunda.fox.platform.qa.deployer.deployment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import com.camunda.fox.platform.qa.deployer.client.FoxExtension;
import com.camunda.fox.platform.qa.deployer.configuration.ConfigurationExporter;
import com.camunda.fox.platform.qa.deployer.configuration.FoxConfiguration;
import com.camunda.fox.platform.qa.deployer.container.RemoteFoxExtension;
import com.camunda.fox.platform.qa.deployer.war.impl.ApplicationArchiveContextImpl;

/**
 * Creates <code>arquillian-fox-extension.jar</code> archive to run the fox extension. 
 * Includes all dependencies required by the extension.
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxExtensionArchiveAppender implements AuxiliaryArchiveAppender {
  
  public static final String ARCHIVE_NAME = "arquillian-fox-extension.jar";
  
  @Inject
  private Instance<FoxConfiguration> configuration;
  
  @Override
  public Archive<?> createAuxiliaryArchive() {
    return ShrinkWrap
      .create(JavaArchive.class, ARCHIVE_NAME)
        .addPackages(true,
          packagesFilter(),
          "com.camunda.fox.platform.qa.deployer")
        .addPackages(true, requiredLibraries())
        .addAsServiceProvider(RemoteLoadableExtension.class, RemoteFoxExtension.class)
        .addAsResource(new ByteArrayAsset(exportConfigurationAsProperties().toByteArray()), FoxConfiguration.PROPERTIES_FILE);
  }

  // Private helper methods
  private String[] requiredLibraries() {
    List<String> libraries = new ArrayList<String>();
    return libraries.toArray(new String[libraries.size()]);
  }
  
  private Filter<ArchivePath> packagesFilter() {
    return Filters.exclude(
      FoxExtension.class.getPackage(), 
      FoxDynamicDependencyAppender.class.getPackage(), 
      ApplicationArchiveContextImpl.class.getPackage());
  }
  
  private ByteArrayOutputStream exportConfigurationAsProperties() {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final ConfigurationExporter exporter = new ConfigurationExporter(configuration.get(), FoxConfiguration.PROPERTY_PREFIX);
    exporter.toProperties(output);
    return output;
  }
}
