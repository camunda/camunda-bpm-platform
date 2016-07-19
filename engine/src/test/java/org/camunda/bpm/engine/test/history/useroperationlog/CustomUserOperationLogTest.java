package org.camunda.bpm.engine.test.history.useroperationlog;


import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntry;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CustomUserOperationLogTest extends AbstractUserOperationLogTest {

    private static final String TASK_ID = UUID.randomUUID().toString();

    private CommandExecutor commandExecutor() {
        return ((ProcessEngineConfigurationImpl)getProcessEngine().getProcessEngineConfiguration()).getCommandExecutorTxRequired();
    }

    public void testDoNotOverwriteUserId() throws Exception {
        commandExecutor().execute(new Command<Void>(){
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
