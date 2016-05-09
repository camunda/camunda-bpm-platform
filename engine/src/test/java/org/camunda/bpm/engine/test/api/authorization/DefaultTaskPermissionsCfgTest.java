/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.authorization;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultTaskPermissionsCfgTest {

  @Test
  public void updateIsDefaultTaskPermission() {
    assertEquals("UPDATE", new StandaloneInMemProcessEngineConfiguration().getDefaultTaskPermissionForUser());
  }

  @Test
  public void shouldInitUpdatePermission() {
    TestProcessEngineCfg testProcessEngineCfg = new TestProcessEngineCfg();

    // given
    testProcessEngineCfg.setDefaultTaskPermissionForUser("UPDATE");

    // if
    testProcessEngineCfg.initDefaultTaskPermission();

    // then
    assertEquals(Permissions.UPDATE, testProcessEngineCfg.getDefaultUserPermissionForTask());
  }

  @Test
  public void shouldInitTaskWorkPermission() {
    TestProcessEngineCfg testProcessEngineCfg = new TestProcessEngineCfg();

    // given
    testProcessEngineCfg.setDefaultTaskPermissionForUser("TASK_WORK");

    // if
    testProcessEngineCfg.initDefaultTaskPermission();

    // then
    assertEquals(Permissions.TASK_WORK, testProcessEngineCfg.getDefaultUserPermissionForTask());
  }

  @Test
  public void shouldThrowExceptionOnUnsupportedPermission() {
    TestProcessEngineCfg testProcessEngineCfg = new TestProcessEngineCfg();

    // given
    testProcessEngineCfg.setDefaultTaskPermissionForUser("UNSUPPORTED");

    // if
    try {
      testProcessEngineCfg.initDefaultTaskPermission();
      fail("Exception expected");

    } catch(ProcessEngineException e) {
      String expectedExceptionMessage = String.format("Invalid value '%s' for configuration property 'defaultTaskPermissionForUser'.", "UNSUPPORTED");
      assertThat(e.getMessage(), containsString(expectedExceptionMessage));
    }
  }

  @Test
  public void shouldInitTaskPermission() {
    ProcessEngine engine = null;
    try {
      // if
      final TestProcessEngineCfg testProcessEngineCfg = new TestProcessEngineCfg();

      engine = testProcessEngineCfg.setProcessEngineName("DefaultTaskPermissionsCfgTest-engine")
        .setJdbcUrl(String.format("jdbc:h2:mem:%s", "DefaultTaskPermissionsCfgTest-engine-db"))
        .setMetricsEnabled(false)
        .setJobExecutorActivate(false)
        .buildProcessEngine();

      // then
      assertTrue(testProcessEngineCfg.initMethodCalled);
    } finally {
      if(engine != null) {
        engine.close();
      }
    }
  }

  static class TestProcessEngineCfg extends StandaloneInMemProcessEngineConfiguration {

    boolean initMethodCalled = false;

    @Override
    public void initDefaultTaskPermission() {
      super.initDefaultTaskPermission();
      initMethodCalled = true;
    }
  }


}
