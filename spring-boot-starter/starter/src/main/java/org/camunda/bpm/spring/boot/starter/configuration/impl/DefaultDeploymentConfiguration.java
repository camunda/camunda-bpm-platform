package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaDeploymentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_SET;

public class DefaultDeploymentConfiguration extends AbstractCamundaConfiguration implements CamundaDeploymentConfiguration {
  private final Logger logger = LoggerFactory.getLogger(DefaultDeploymentConfiguration.class);

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    if (camundaBpmProperties.isAutoDeploymentEnabled()) {
      final Set<Resource> resources = getDeploymentResources();
      configuration.setDeploymentResources(resources.toArray(new Resource[resources.size()]));
      LOG.autoDeployResources(resources);
    }
  }

  @Override
  public Set<Resource> getDeploymentResources() {

    final ResourceArrayPropertyEditor resolver = new ResourceArrayPropertyEditor();

    try {
      final String[] resourcePattern = camundaBpmProperties.getDeploymentResourcePattern();
      logger.debug("resolving deployment resources for pattern {}", (Object[]) resourcePattern);
      resolver.setValue(resourcePattern);

      return Arrays.stream((Resource[])resolver.getValue())
        .peek(resource -> logger.debug("processing deployment resource {}", resource))
        .filter(this::isFile)
        .peek(resource -> logger.debug("added deployment resource {}", resource))
        .collect(Collectors.toSet());

    } catch (final RuntimeException e) {
      logger.error("unable to resolve resources", e);
    }
    return EMPTY_SET;
  }



  private boolean isFile(Resource resource) {

    if (resource.isReadable()) {
      if (resource instanceof UrlResource || resource instanceof ClassPathResource) {
        try {
          URL url = resource.getURL();
          return !url.toString().endsWith("/");
        } catch (IOException e) {
          logger.debug("unable to handle " + resource + " as URL", e);
        }
      } else {
        try {
          return !resource.getFile().isDirectory();
        } catch (IOException e) {
          logger.debug("unable to handle " + resource + " as file", e);
        }
      }
    }
    logger.warn("unable to determine if resource {} is a deployable resource", resource);
    return false;
  }


}
