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
package org.camunda.bpm.engine.impl.migration.batch;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

/**
 * Job declaration for batch migration jobs. The batch migration job
 * migrates a list of process instances.
 */
public class MigrationBatchJobDeclaration extends JobDeclaration<ByteArrayEntity, MessageEntity> {

  private static final long serialVersionUID = 1L;

  public MigrationBatchJobDeclaration() {
    super(Batch.TYPE_PROCESS_INSTANCE_MIGRATION);
  }

  protected ExecutionEntity resolveExecution(ByteArrayEntity configuration) {
    return null;
  }

  protected MessageEntity newJobInstance(ByteArrayEntity configuration) {
    return new MessageEntity();
  }

  @Override
  protected JobHandlerConfiguration resolveJobHandlerConfiguration(ByteArrayEntity configuration) {
    return new BatchJobConfiguration(configuration.getId());
  }

}
