/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
