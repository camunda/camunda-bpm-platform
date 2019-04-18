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
package org.camunda.bpm.integrationtest.deployment.ear;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.deployment.ear.beans.AnnotatedEjbPa;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tassilo Weidner
 */
@RunWith(Arquillian.class)
public class TestPaAnnotatedEjb extends AbstractFoxPlatformIntegrationTest {

  /**
   *
   * test-application.ear
   *    |-- pa.jar
   *        |-- AbstractFoxPlatformIntegrationTest.class
   *        |-- TestPaAnnotatedEjb.class
   *
   *        |-- AnnotatedEjbPa.class
   *        |-- process.bpmn
   *        |-- deployment-descriptor-with-custom-filename.xml
   *        |-- META-INF/beans.xml
   *
   *    |-- camunda-engine-cdi.jar
   *        |-- META-INF/MANIFEST.MF
   *
   */
  @Deployment
  public static EnterpriseArchive paAsEjbModule() {

    JavaArchive processArchive1Jar = ShrinkWrap.create(JavaArchive.class, "pa.jar")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(TestPaAnnotatedEjb.class)
      .addClass(AnnotatedEjbPa.class)
      .addAsResource("org/camunda/bpm/integrationtest/deployment/ear/process1.bpmn20.xml", "process.bpmn")
      .addAsResource("META-INF/processes.xml", "deployment-descriptor-with-custom-filename.xml")
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    return ShrinkWrap.create(EnterpriseArchive.class, "paAsEjbModule.ear")
      .addAsModule(processArchive1Jar)
      .addAsLibrary(DeploymentHelper.getEngineCdi());
  }

  @Test
  public void testPaAnnotatedEjb() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process1");

    Assert.assertNotNull(processInstance);
  }

}
