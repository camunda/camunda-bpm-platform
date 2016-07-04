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

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MultiTenancyCommandTenantCheckTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected IdentityService identityService;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService = engineRule.getIdentityService();

    identityService.setAuthentication("user", null, null);
  }

  @Test
  public void disableTenantCheckForProcessEngine() {
    // disable tenant check for process engine
    processEngineConfiguration.setTenantCheckEnabled(false);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        // cannot enable tenant check for command when it is disabled for process engine
        commandContext.enableTenantCheck();
        assertThat(commandContext.getTenantManager().isTenantCheckEnabled(), is(false));

        return null;
      }
    });
  }

  @Test
  public void disableTenantCheckForCommand() {

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        // disable tenant check for the current command
        commandContext.disableTenantCheck();
        assertThat(commandContext.isTenantCheckEnabled(), is(false));
        assertThat(commandContext.getTenantManager().isTenantCheckEnabled(), is(false));

        return null;
      }
    });

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        // assert that it is enabled again for further commands
        assertThat(commandContext.isTenantCheckEnabled(), is(true));
        assertThat(commandContext.getTenantManager().isTenantCheckEnabled(), is(true));

        return null;
      }
    });
  }

  @Test
  public void disableAndEnableTenantCheckForCommand() {

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {

        commandContext.disableTenantCheck();
        assertThat(commandContext.getTenantManager().isTenantCheckEnabled(), is(false));

        commandContext.enableTenantCheck();
        assertThat(commandContext.getTenantManager().isTenantCheckEnabled(), is(true));

        return null;
      }
    });
  }

  @Test
  public void disableTenantCheckForCamundaAdmin() {
    identityService.setAuthentication("user", Collections.singletonList(Groups.CAMUNDA_ADMIN), null);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        // camunda-admin should access data from all tenants
        assertThat(commandContext.getTenantManager().isTenantCheckEnabled(), is(false));

        return null;
      }
    });
  }

}
