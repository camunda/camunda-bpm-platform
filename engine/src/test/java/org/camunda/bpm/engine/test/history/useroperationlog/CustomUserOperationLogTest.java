package org.camunda.bpm.engine.test.history.useroperationlog;


import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntry;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        assertThat(historyService.createUserOperationLogQuery().taskId(TASK_ID).singleResult().getUserId(), is("kermit"));
    }
}
