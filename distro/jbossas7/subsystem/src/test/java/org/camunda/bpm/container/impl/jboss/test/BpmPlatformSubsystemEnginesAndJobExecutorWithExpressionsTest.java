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
package org.camunda.bpm.container.impl.jboss.test;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 *
 * @author christian.lipphardt@camunda.com
 */
public class BpmPlatformSubsystemEnginesAndJobExecutorWithExpressionsTest extends AbstractSubsystemBaseTest {

  private static String PROPERTY_KEY = "org.camunda.bpm.jboss.process-engine.test.isDefault";
  
  public BpmPlatformSubsystemEnginesAndJobExecutorWithExpressionsTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new BpmPlatformExtension());
  }

  @BeforeClass
  public static void setUp() {
    System.setProperty(PROPERTY_KEY, "true");
  }
  
  @AfterClass
  public static void tearDown() {
    System.clearProperty(PROPERTY_KEY);
  }
  
  @Override
  protected String getSubsystemXml() throws IOException {
    try {
      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS_AND_JOB_EXECUTOR_WITH_EXPRESSIONS);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

}
