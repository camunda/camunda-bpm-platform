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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;

/**
 * JobEntity for ever living job, which can be rescheduled and executed again.
 *
 * @author Svetlana Dorokhova
 */
public class EverLivingJobEntity extends JobEntity {

  private static final long serialVersionUID = 1L;

  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public static final String TYPE = "ever-living";

  public String getType() {
    return TYPE;
  }

  @Override
  protected void postExecute(CommandContext commandContext) {
    LOG.debugJobExecuted(this);
    init(commandContext);
    commandContext.getHistoricJobLogManager().fireJobSuccessfulEvent(this);
  }

  @Override
  public void init(CommandContext commandContext) {
    // clean additional data related to this job
    JobHandler jobHandler = getJobHandler();
    if (jobHandler != null) {
      jobHandler.onDelete(getJobHandlerConfiguration(), this);
    }

    //cancel the retries -> will resolve job incident if present
    setRetries(commandContext.getProcessEngineConfiguration().getDefaultNumberOfRetries());

    //delete the job's exception byte array and exception message
    String exceptionByteArrayIdToDelete =null;
    if (exceptionByteArrayId != null) {
      exceptionByteArrayIdToDelete = exceptionByteArrayId;
      this.exceptionByteArrayId = null;
      this.exceptionMessage = null;
    }
    //clean the lock information
    setLockOwner(null);
    setLockExpirationTime(null);

    if (exceptionByteArrayIdToDelete != null) {
      ByteArrayEntity byteArray = commandContext.getDbEntityManager().selectById(ByteArrayEntity.class, exceptionByteArrayIdToDelete);
      commandContext.getDbEntityManager().delete(byteArray);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", revision=" + revision
           + ", duedate=" + duedate
           + ", lockOwner=" + lockOwner
           + ", lockExpirationTime=" + lockExpirationTime
           + ", executionId=" + executionId
           + ", processInstanceId=" + processInstanceId
           + ", isExclusive=" + isExclusive
           + ", retries=" + retries
           + ", jobHandlerType=" + jobHandlerType
           + ", jobHandlerConfiguration=" + jobHandlerConfiguration
           + ", exceptionByteArray=" + exceptionByteArray
           + ", exceptionByteArrayId=" + exceptionByteArrayId
           + ", exceptionMessage=" + exceptionMessage
           + ", deploymentId=" + deploymentId
           + "]";
  }

}
