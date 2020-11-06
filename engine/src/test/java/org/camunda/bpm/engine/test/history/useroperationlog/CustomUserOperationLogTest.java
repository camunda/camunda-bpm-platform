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
package org.camunda.bpm.engine.test.history.useroperationlog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntry;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class CustomUserOperationLogTest  {

    public static final String USER_ID = "demo";

    @ClassRule
    public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
            "org/camunda/bpm/engine/test/history/useroperationlog/enable.legacy.user.operation.log.camunda.cfg.xml");


    private static final String TASK_ID = UUID.randomUUID().toString();

    private CommandExecutor commandExecutor;
    private HistoryService historyService;

    @Before
    public void setUp() throws Exception {
        commandExecutor = ((ProcessEngineConfigurationImpl)bootstrapRule.getProcessEngine().getProcessEngineConfiguration()).getCommandExecutorTxRequired();
        historyService = bootstrapRule.getProcessEngine().getHistoryService();
    }

    @Test
    public void testDoNotOverwriteUserId() throws Exception {
        commandExecutor.execute(new Command<Void>(){
            @Override
            public Void execute(final CommandContext commandContext) {
                final UserOperationLogContext userOperationLogContext = new UserOperationLogContext();
                userOperationLogContext.setUserId("kermit");

                final UserOperationLogContextEntry entry = new UserOperationLogContextEntry("foo", "bar");
                entry.setPropertyChanges(Arrays.asList(new PropertyChange(null, null, null)));
                entry.setTaskId(TASK_ID);
                userOperationLogContext.addEntry(entry);

                commandContext.getOperationLogManager().logUserOperations(userOperationLogContext);
                return null;
            }
        });

        // and check its there
        assertThat(historyService.createUserOperationLogQuery().taskId(TASK_ID).singleResult().getUserId()).isEqualTo("kermit");
    }
}
