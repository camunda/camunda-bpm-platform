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
package org.camunda.bpm.engine.impl.batch;

import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler.BatchSeedJobConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

/**
 * Job declaration for batch seed jobs. The batch seed job
 * creates all batch jobs required to complete the batch.
 */
public class BatchSeedJobDeclaration extends JobDeclaration<BatchEntity, MessageEntity> {

  private static final long serialVersionUID = 1L;

  public BatchSeedJobDeclaration() {
    super(BatchSeedJobHandler.TYPE);
  }

  protected ExecutionEntity resolveExecution(BatchEntity batch) {
    return null;
  }

  protected MessageEntity newJobInstance(BatchEntity batch) {
    return new MessageEntity();
  }

  @Override
  protected JobHandlerConfiguration resolveJobHandlerConfiguration(BatchEntity batch) {
    return new BatchSeedJobConfiguration(batch.getId());
  }

  @Override
  protected String resolveJobDefinitionId(BatchEntity batch) {
    return batch.getSeedJobDefinitionId();
  }

}
