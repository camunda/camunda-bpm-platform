package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * Job handler for history cleanup job.
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobHandler implements JobHandler<HistoryCleanupJobHandlerConfiguration> {

  public static final String TYPE = "history-cleanup";

  public void execute(HistoryCleanupJobHandlerConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {

    HistoryCleanupHandler cleanupHandler = initCleanupHandler(configuration, commandContext);

    if (configuration.isImmediatelyDue() || isWithinBatchWindow(commandContext) ) {
      cleanupHandler.performCleanup();
    }

    commandContext.getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, cleanupHandler);

  }

  protected HistoryCleanupHandler initCleanupHandler(HistoryCleanupJobHandlerConfiguration configuration, CommandContext commandContext) {
    HistoryCleanupHandler cleanupHandler = null;

    if (isHistoryCleanupByRemovalTimeEnabled(commandContext)) {
      cleanupHandler = new HistoryCleanupRemovalTime();
    } else {
      cleanupHandler = new HistoryCleanupBatch();
    }

    CommandExecutor commandExecutor = commandContext.getProcessEngineConfiguration()
      .getCommandExecutorTxRequiresNew();

    String jobId = commandContext.getCurrentJob().getId();

    return cleanupHandler
      .setConfiguration(configuration)
      .setCommandExecutor(commandExecutor)
      .setJobId(jobId);
  }

  protected boolean isHistoryCleanupByRemovalTimeEnabled(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration()
      .isHistoryCleanupByRemovalTime();
  }

  protected boolean isWithinBatchWindow(CommandContext commandContext) {
    return HistoryCleanupHelper.isWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration());
  }

  public HistoryCleanupJobHandlerConfiguration newConfiguration(String canonicalString) {
    JSONObject jsonObject = new JSONObject(canonicalString);
    return HistoryCleanupJobHandlerConfiguration.fromJson(jsonObject);
  }

  public String getType() {
    return TYPE;
  }

  public void onDelete(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
  }

}
