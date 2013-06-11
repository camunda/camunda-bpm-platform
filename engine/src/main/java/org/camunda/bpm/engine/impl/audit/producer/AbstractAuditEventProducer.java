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
package org.camunda.bpm.engine.impl.audit.producer;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.audit.AuditEvent;
import org.camunda.bpm.engine.impl.audit.handler.AuditEventHandler;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * Base class for implementing Audit Event Handlers.
 * 
 * @author Daniel Meyer
 *
 */
public abstract class AbstractAuditEventProducer implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {
    
    final AuditEventHandler auditEventHandler = getAuditEventHandler();
    
    // create the audit event
    AuditEvent auditEvent = createAuditEvent(execution);
    
    // fire the audit event using the audit event handler
    auditEventHandler.handleAuditEvent(auditEvent);

  }
  
  /** to be used by subclasses for initializing an audit event */
  protected void initAuditEvent(AuditEvent evt, DelegateExecution execution) {
    
    final IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
    
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    String processDefinitionId = executionEntity.getProcessDefinitionId();
    String processInstanceId = executionEntity.getProcessInstanceId();
    String executionId = execution.getId();
    
    evt.setTimestamp(ClockUtil.getCurrentTime().getTime());
    evt.setId(idGenerator.getNextId());
    evt.setProcessDefinitionId(processDefinitionId);
    evt.setProcessInstanceId(processInstanceId);
    evt.setExecutionId(executionId);
    
  }

  /**
   * @return the audit event handler
   */
  protected AuditEventHandler getAuditEventHandler() {
    return Context.getProcessEngineConfiguration().getAuditEventHandler();
  }
  
  /**
   * <p>creates the audit event</p>
   * 
   * @param execution
   * 
   * @return the audit event
   */
  protected abstract AuditEvent createAuditEvent(DelegateExecution execution);

}
