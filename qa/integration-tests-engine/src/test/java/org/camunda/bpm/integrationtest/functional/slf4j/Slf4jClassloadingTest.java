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
package org.camunda.bpm.integrationtest.functional.slf4j;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class Slf4jClassloadingTest extends AbstractFoxPlatformIntegrationTest {

  public static final String JDK14_LOGGER_FACTORY = "org.slf4j.impl.JDK14LoggerFactory";
  public static final String JBOSS_SLF4J_LOGGER_FACTORY = "org.slf4j.impl.Slf4jLoggerFactory";

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestLogger.class);

    TestContainer.addContainerSpecificResourcesWithoutWeld(webArchive);
    TestContainer.addCommonLoggingDependency(webArchive);

    return webArchive;
  }

  @Test
  public void shouldNotUseNopLoggerFactory() {
    ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

    // verify that a SLF4J backend is used which is not the NOP logger
    assertFalse("Should not use NOPLoggerFactory", loggerFactory instanceof NOPLoggerFactory);

    // should either use slf4j-jdk14 or slf4j-jboss-logmanager
    String loggerFactoryClassName = loggerFactory.getClass().getCanonicalName();
    assertTrue("Should use slf4j-jdk14 or slf4j-jboss-logmanager",
        JDK14_LOGGER_FACTORY.equals(loggerFactoryClassName) || JBOSS_SLF4J_LOGGER_FACTORY.equals(loggerFactoryClassName));
  }

  @Test
  public void shouldBeAbleToLogMessageWithFormatParameters() {
    TestLogger logger = TestLogger.INSTANCE;

    // verify that we can use different formatting methods
    logger.testLogWithSingleFormatParameter();
    logger.testLogWithTwoFormatParameters();
    logger.testLogWithArrayFormatter();
  }


}
