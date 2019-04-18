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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Tobias Metzke
 */
public class BpmPlatformSubsystemWithExpressionsTest extends AbstractSubsystemBaseTest {

  private static Map<String, String> PROPERTIES = new HashMap<>();
  
  static {
    PROPERTIES.put("org.camunda.bpm.jboss.process-engine.test.isDefault", "true");
    PROPERTIES.put("org.camunda.bpm.jboss.job-executor.core-threads", "5");          
    PROPERTIES.put("org.camunda.bpm.jboss.job-executor.max-threads", "15");          
    PROPERTIES.put("org.camunda.bpm.jboss.job-executor.queue-length", "15");         
    PROPERTIES.put("org.camunda.bpm.jboss.job-executor.keepalive-time", "10");       
    PROPERTIES.put("org.camunda.bpm.jboss.job-executor.allow-core-timeout", "false");
  }
  
  public BpmPlatformSubsystemWithExpressionsTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new BpmPlatformExtension());
  }

  @BeforeClass
  public static void setUp() {
    System.getProperties().putAll(PROPERTIES);
  }
  
  @AfterClass
  public static void tearDown() {
    for (String key : PROPERTIES.keySet()) {
      System.clearProperty(key);
    }
  }
  
  @Override
  protected String getSubsystemXml() throws IOException {
    try {
      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ALL_OPTIONS_WITH_EXPRESSIONS);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

}
