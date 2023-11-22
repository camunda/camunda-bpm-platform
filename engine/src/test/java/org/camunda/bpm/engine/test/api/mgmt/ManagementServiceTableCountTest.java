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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Saeid Mizaei
 * @author Joram Barrez
 */
public class ManagementServiceTableCountTest extends PluggableProcessEngineTest {

  @Test
  public void testTableCount() {
    Map<String, Long> tableCount = managementService.getTableCount();

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    if(managementService.getLicenseKey() != null) {
      assertEquals(Long.valueOf(1), tableCount.get(tablePrefix + "ACT_GE_BYTEARRAY"));
    } else {
      assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_GE_BYTEARRAY"));
    }
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RE_DEPLOYMENT"));
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RU_EXECUTION"));
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_ID_GROUP"));
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_ID_MEMBERSHIP"));
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_ID_USER"));
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RE_PROCDEF"));
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RU_TASK"));
    assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RU_IDENTITYLINK"));
  }

}
