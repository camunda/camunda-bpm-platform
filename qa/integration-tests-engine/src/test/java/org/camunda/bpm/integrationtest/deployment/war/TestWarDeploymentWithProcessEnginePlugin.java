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
package org.camunda.bpm.integrationtest.deployment.war;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.deployment.war.beans.GroovyProcessEnginePlugin;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


/**
 * Assert that we can deploy a WAR with a process engine plugin
 * which ships and requires groovy as a dependency for scripting purposes.
 *
 * Does not work on JBoss, see https://app.camunda.com/jira/browse/CAM-1778
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithProcessEnginePlugin extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment("test.war", "singleEngineWithProcessEnginePlugin.xml")
        .addClass(GroovyProcessEnginePlugin.class)
        .addAsResource("org/camunda/bpm/integrationtest/deployment/war/groovy.bpmn20.xml")
        .addAsResource("org/camunda/bpm/integrationtest/deployment/war/groovyAsync.bpmn20.xml")
        .addAsLibraries(Maven.resolver()
            .offline()
            .loadPomFromFile("pom.xml")
            .resolve("org.apache.groovy:groovy-jsr223")
            .withoutTransitivity()
            .as(JavaArchive.class));
  }

  @Test
  public void testPAGroovyProcessEnginePlugin() {
    ProcessEngine groovyEngine = processEngineService.getProcessEngine("groovy");
    Assert.assertNotNull(groovyEngine);

    ProcessInstance pi = groovyEngine.getRuntimeService().startProcessInstanceByKey("groovy");
    HistoricProcessInstance hpi = groovyEngine.getHistoryService()
        .createHistoricProcessInstanceQuery().processDefinitionKey("groovy").finished().singleResult();
    assertEquals(pi.getId(), hpi.getId());
  }

  @Test
  public void testPAGroovyAsyncProcessEnginePlugin() {
    ProcessEngine groovyEngine = processEngineService.getProcessEngine("groovy");
    Assert.assertNotNull(groovyEngine);

    ProcessInstance pi = groovyEngine.getRuntimeService().startProcessInstanceByKey("groovyAsync");

    waitForJobExecutorToProcessAllJobs();

    HistoricProcessInstance hpi = groovyEngine.getHistoryService()
        .createHistoricProcessInstanceQuery().processDefinitionKey("groovyAsync").finished().singleResult();
    assertEquals(pi.getId(), hpi.getId());
  }

}
