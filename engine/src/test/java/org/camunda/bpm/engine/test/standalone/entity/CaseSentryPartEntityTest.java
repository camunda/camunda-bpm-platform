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
package org.camunda.bpm.engine.test.standalone.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Kristin Polenz
 */
public class CaseSentryPartEntityTest extends PluggableProcessEngineTest {

  @Test
  public void testSentryWithTenantId() {
    CaseSentryPartEntity caseSentryPartEntity = new CaseSentryPartEntity();
    caseSentryPartEntity.setTenantId("tenant1");

    insertCaseSentryPart(caseSentryPartEntity);

    caseSentryPartEntity = readCaseSentryPart();
    assertThat(caseSentryPartEntity.getTenantId()).isEqualTo("tenant1");

    deleteCaseSentryPart(caseSentryPartEntity);
  }

  protected void insertCaseSentryPart(final CaseSentryPartEntity caseSentryPartEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseSentryPartManager().insert(caseSentryPartEntity);
        return null;
      }
    });
  }

  protected CaseSentryPartEntity readCaseSentryPart() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    return new CaseSentryPartQueryImpl(commandExecutor).singleResult();
  }

  protected void deleteCaseSentryPart(final CaseSentryPartEntity caseSentryPartEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseSentryPartManager().delete(caseSentryPartEntity);
        return null;
      }
    });
  }

}
