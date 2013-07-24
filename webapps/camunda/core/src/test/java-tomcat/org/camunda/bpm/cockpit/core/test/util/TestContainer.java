package org.camunda.bpm.cockpit.core.test.util;

import org.camunda.bpm.cockpit.test.util.DeploymentHelper;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Tomcat test container
 *
 * @author nico.rehwaldt
 */
public class TestContainer {

  public static void addContainerSpecificResources(WebArchive archive) {

    archive
      .addAsManifestResource("context.xml")
      .addAsLibraries(DeploymentHelper.getResteasyJaxRs())
      .addAsWebInfResource("web.xml");
  }
}
