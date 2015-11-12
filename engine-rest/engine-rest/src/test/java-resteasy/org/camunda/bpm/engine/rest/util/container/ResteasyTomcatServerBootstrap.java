package org.camunda.bpm.engine.rest.util.container;

import org.camunda.bpm.engine.rest.util.container.TomcatServerBootstrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public class ResteasyTomcatServerBootstrap extends TomcatServerBootstrap {

  public ResteasyTomcatServerBootstrap(String webXmlPath) {
    super(webXmlPath);
  }

  @Override
  protected void addRuntimeSpecificLibraries(WebArchive wa,
      PomEquippedResolveStage resolver) {
    wa.addAsLibraries(resolver.resolve("org.jboss.resteasy:resteasy-jaxrs").withTransitivity().asFile());
  }

}
