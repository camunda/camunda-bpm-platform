package org.camunda.bpm.engine.rest.util.container;

import org.camunda.bpm.engine.rest.util.container.TomcatServerBootstrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;


public class ResteasyTomcatServerBootstrap extends TomcatServerBootstrap {

  public ResteasyTomcatServerBootstrap(String webXmlPath) {
    super(webXmlPath);
  }

  @Override
  protected void addRuntimeSpecificLibraries(WebArchive wa, PomEquippedResolveStage resolver) {
    // inject rest easy version to differentiate between resteasy and wildfly-compatibility profile
    String restEasyVersion = System.getProperty("restEasyVersion");

    wa.addAsLibraries(resolver.addDependencies(
      MavenDependencies.createDependency("org.jboss.resteasy:resteasy-jaxrs:" + restEasyVersion, ScopeType.TEST, false,
        MavenDependencies.createExclusion("org.apache.httpcomponents:httpclient"))).resolve()
      .withTransitivity().asFile());
  }

}
