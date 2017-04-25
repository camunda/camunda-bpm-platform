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

package org.camunda.bpm.engine.impl.batch.deletion;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Askar Akhmerov
 */
public class DeleteProcessInstancesJobHandler extends AbstractBatchJobHandler<DeleteProcessInstanceBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_INSTANCE_DELETION);

  @Override
  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_DELETION;
  }

  protected DeleteProcessInstanceBatchConfigurationJsonConverter getJsonConverterInstance() {
    return DeleteProcessInstanceBatchConfigurationJsonConverter.INSTANCE;
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected DeleteProcessInstanceBatchConfiguration createJobConfiguration(DeleteProcessInstanceBatchConfiguration configuration, List<String> processIdsForJob) {
    return new DeleteProcessInstanceBatchConfiguration(processIdsForJob, configuration.getDeleteReason(), configuration.isSkipCustomListeners());
  }

  @Override
  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    ByteArrayEntity configurationEntity = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());

    DeleteProcessInstanceBatchConfiguration batchConfiguration = readConfiguration(configurationEntity.getBytes());

    boolean initialLegacyRestrictions = commandContext.isRestrictUserOperationLogToAuthenticatedUsers();
    commandContext.disableUserOperationLog();
    commandContext.setRestrictUserOperationLogToAuthenticatedUsers(true);
    try {
      commandContext.getProcessEngineConfiguration()
          .getRuntimeService()
          .deleteProcessInstances(batchConfiguration.getIds(), batchConfiguration.deleteReason, batchConfiguration.isSkipCustomListeners(), true);
    } finally {
      commandContext.enableUserOperationLog();
      commandContext.setRestrictUserOperationLogToAuthenticatedUsers(initialLegacyRestrictions);
    }

    commandContext.getByteArrayManager().delete(configurationEntity);
  }

  @Override
  protected int createJobEntities(BatchEntity batch, ByteArrayManager byteArrayManager, JobManager jobManager,
      DeleteProcessInstanceBatchConfiguration configuration, int invocationsPerBatchJob, List<String> processIds) {
    int createdJobs = 0;
    while (!processIds.isEmpty()) {
      int lastIdIndex = Math.min(invocationsPerBatchJob, processIds.size());
      // view of process instances for this job
      List<String> idsForJob = processIds.subList(0, lastIdIndex);

      Map<String, List<String>> map = getDeploymentIds(configuration, idsForJob);
      if (!map.isEmpty()) {

        for (String deploymentId : map.keySet()) {
          DeleteProcessInstanceBatchConfiguration jobConfiguration = createJobConfiguration(configuration, map.get(deploymentId));
          ByteArrayEntity configurationEntity = saveConfiguration(byteArrayManager, jobConfiguration);

          JobEntity job = createBatchJob(batch, configurationEntity);
          job.setDeploymentId(deploymentId);

          jobManager.insertAndHintJobExecutor(job);
          createdJobs++;
        }
      }
      idsForJob.clear();
    }
    return createdJobs;
  }

  private Map<String, List<String>> getDeploymentIds(DeleteProcessInstanceBatchConfiguration configuration, final List<String> processInstanceIds) {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    final CommandContext commandContext = Context.getCommandContext();

    List<ProcessInstance> processInstances = commandContext.runWithoutAuthorization(new Callable<List<ProcessInstance>>() {
      @Override
      public List<ProcessInstance> call() throws Exception {
        return commandContext.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery()
            .processInstanceIds(new HashSet<String>(processInstanceIds)).list();
      }
    });

    for (final ProcessInstance processInstance : processInstances) {
      ProcessDefinitionEntity pde = commandContext.runWithoutAuthorization(new Callable<ProcessDefinitionEntity>() {
        @Override
        public ProcessDefinitionEntity call() throws Exception {
          return commandContext.getProcessDefinitionManager().findLatestDefinitionById(processInstance.getProcessDefinitionId());
        }
      });

      List<String> set = map.get(pde.getDeploymentId());
      String processInstanceId = processInstance.getId();
      if (set == null) {
        map.put(pde.getDeploymentId(), new LinkedList<String>(Arrays.asList(processInstanceId)));
      } else {
        set.add(processInstanceId);
      }
    }

    if (processInstances.size() != processInstanceIds.size()) {
      for (ProcessInstance processInstance : processInstances) {
        for (String processInstanceId : processInstanceIds) {
          if (processInstance.getId().equals(processInstanceId)) {
            processInstanceIds.remove(processInstanceId);
          }
        }
      }

      if (!processInstanceIds.isEmpty()) {
        map.put(null, (processInstanceIds));
      }
    }
    
    return map;
  }
}
