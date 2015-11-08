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
package org.camunda.bpm.engine.cdi;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.cdi.annotation.BusinessProcessScoped;
import org.camunda.bpm.engine.cdi.impl.context.ContextAssociationManager;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Bean supporting contextual business process management. This allows us to
 * implement a unit of work, in which a particular CDI scope (Conversation /
 * Request / Thread) is associated with a particular Execution / ProcessInstance
 * or Task.
 * <p />
 * The protocol is that we <em>associate</em> the {@link BusinessProcess} bean
 * with a particular Execution / Task, then perform some changes (retrieve / set process
 * variables) and then end the unit of work. This bean makes sure that our changes are
 * only "flushed" to the process engine when we successfully complete the unit of work.
 * <p />
 * A typical usage scenario might look like this:<br />
 * <strong>1st unit of work ("process instantiation"):</strong>
 * <pre>
 * conversation.begin();
 * ...
 * businessProcess.setVariable("billingId", "1"); // setting variables before starting the process
 * businessProcess.startProcessByKey("billingProcess");
 * conversation.end();
 * </pre>
 * <strong>2nd unit of work ("perform a user task"):</strong>
 * <pre>
 * conversation.begin();
 * businessProcess.startTask(id); // now we have associated a task with the current conversation
 * ...                            // this allows us to retrieve and change process variables
 *                                // and @BusinessProcessScoped beans
 * businessProcess.setVariable("billingDetails", "someValue"); // these changes are cached in the conversation
 * ...
 * businessProcess.completeTask(); // now all changed process variables are flushed
 * conversation.end();
 * </pre>
 * <p />
 * <strong>NOTE:</strong> in the absence of a conversation, (non faces request, i.e. when processing a JAX-RS,
 * JAX-WS, JMS, remote EJB or plain Servlet requests), the {@link BusinessProcess} bean associates with the
 * current Request (see {@link RequestScoped @RequestScoped}).
 * <p />
 * <strong>NOTE:</strong> in the absence of a request, ie. when the JobExecutor accesses
 * {@link BusinessProcessScoped @BusinessProcessScoped} beans, the execution is associated with the
 * current thread.
 *
 * @author Daniel Meyer
 * @author Falko Menge
 */
@Named
public class BusinessProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject private ProcessEngine processEngine;

  @Inject private ContextAssociationManager associationManager;

  @Inject private Instance<Conversation> conversationInstance;

  public ProcessInstance startProcessById(String processDefinitionId) {
    assertCommandContextNotActive();

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, getAndClearCachedVariableMap());
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessById(String processDefinitionId, String businessKey) {
    assertCommandContextNotActive();

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, businessKey, getAndClearCachedVariableMap());
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessById(String processDefinitionId, Map<String, Object> variables) {
    assertCommandContextNotActive();

    VariableMap cachedVariables = getAndClearCachedVariableMap();
    cachedVariables.putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, cachedVariables);
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
    assertCommandContextNotActive();

    VariableMap cachedVariables = getAndClearCachedVariableMap();
    cachedVariables.putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitionId, businessKey, cachedVariables);
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessByKey(String key) {
    assertCommandContextNotActive();

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, getAndClearCachedVariableMap());
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessByKey(String key, String businessKey) {
    assertCommandContextNotActive();

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, businessKey, getAndClearCachedVariableMap());
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessByKey(String key, Map<String, Object> variables) {
    assertCommandContextNotActive();

    VariableMap cachedVariables = getAndClearCachedVariableMap();
    cachedVariables.putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, cachedVariables);
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessByKey(String key, String businessKey, Map<String, Object> variables) {
    assertCommandContextNotActive();

    VariableMap cachedVariables = getAndClearCachedVariableMap();
    cachedVariables.putAll(variables);
    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key, businessKey, cachedVariables);
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessByMessage(String messageName) {
    assertCommandContextNotActive();

    VariableMap cachedVariables = getAndClearCachedVariableMap();
    ProcessInstance instance =  processEngine.getRuntimeService().startProcessInstanceByMessage(messageName, cachedVariables);
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessByMessage(String messageName, Map<String, Object> processVariables) {
    assertCommandContextNotActive();

    VariableMap cachedVariables = getAndClearCachedVariableMap();
    cachedVariables.putAll(processVariables);
    ProcessInstance instance =  processEngine.getRuntimeService().startProcessInstanceByMessage(messageName, cachedVariables);
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }

  public ProcessInstance startProcessByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    assertCommandContextNotActive();

    VariableMap cachedVariables = getAndClearCachedVariableMap();
    cachedVariables.putAll(processVariables);
    ProcessInstance instance =  processEngine.getRuntimeService().startProcessInstanceByMessage(messageName, businessKey, cachedVariables);
    if (!instance.isEnded()) {
      setExecution(instance);
    }
    return instance;
  }


  /**
   * Associate with the provided execution. This starts a unit of work.
   *
   * @param executionId
   *          the id of the execution to associate with.
   * @throw ProcessEngineCdiException
   *          if no such execution exists
   */
  public void associateExecutionById(String executionId) {
    Execution execution = processEngine.getRuntimeService()
      .createExecutionQuery()
      .executionId(executionId)
      .singleResult();
    if(execution == null) {
      throw new ProcessEngineCdiException("Cannot associate execution by id: no execution with id '"+executionId+"' found.");
    }
    associationManager.setExecution(execution);
  }

  /**
   * returns true if an {@link Execution} is associated.
   *
   * @see #associateExecutionById(String)
   */
  public boolean isAssociated() {
    return associationManager.getExecutionId() != null;
  }

  /**
   * Signals the current execution, see {@link RuntimeService#signal(String)}
   * <p/>
   * Ends the current unit of work (flushes changes to process variables set
   * using {@link #setVariable(String, Object)} or made on
   * {@link BusinessProcessScoped @BusinessProcessScoped} beans).
   *
   * @throws ProcessEngineCdiException
   *           if no execution is currently associated
   * @throws ProcessEngineException
   *           if the activiti command fails
   */
  public void signalExecution() {
    assertExecutionAssociated();
    processEngine.getRuntimeService().setVariablesLocal(associationManager.getExecutionId(), getAndClearCachedLocalVariableMap());
    processEngine.getRuntimeService().signal(associationManager.getExecutionId(), getAndClearCachedVariableMap());
    associationManager.disAssociate();
  }

  /**
   * @see #signalExecution()
   *
   * In addition, this method allows to end the current conversation
   */
  public void signalExecution(boolean endConversation) {
    signalExecution();
    if(endConversation) {
      conversationInstance.get().end();
    }
  }

  // -------------------------------------

  /**
   * Associates the task with the provided taskId with the current conversation.
   * <p/>
   *
   * @param taskId
   *          the id of the task
   *
   * @return the resumed task
   *
   * @throws ProcessEngineCdiException
   *           if no such task is found
   */
  public Task startTask(String taskId) {
    Task currentTask = associationManager.getTask();
    if(currentTask != null && currentTask.getId().equals(taskId)) {
      return currentTask;
    }
    Task task = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    if(task == null) {
      throw new ProcessEngineCdiException("Cannot resume task with id '"+taskId+"', no such task.");
    }
    associationManager.setTask(task);
    associateExecutionById(task.getExecutionId());
    return task;
  }

  /**
   * @see #startTask(String)
   *
   * this method allows to start a conversation if no conversation is active
   */
  public Task startTask(String taskId, boolean beginConversation) {
    if(beginConversation) {
      Conversation conversation = conversationInstance.get();
      if(conversation.isTransient()) {
       conversation.begin();
      }
    }
    return startTask(taskId);
  }

  /**
   * Completes the current UserTask, see {@link TaskService#complete(String)}
   * <p/>
   * Ends the current unit of work (flushes changes to process variables set
   * using {@link #setVariable(String, Object)} or made on
   * {@link BusinessProcessScoped @BusinessProcessScoped} beans).
   *
   * @throws ProcessEngineCdiException
   *           if no task is currently associated
   * @throws ProcessEngineException
   *           if the activiti command fails
   */
  public void completeTask() {
    assertTaskAssociated();
    processEngine.getTaskService().setVariablesLocal(getTask().getId(), getAndClearCachedLocalVariableMap());
    processEngine.getTaskService().setVariables(getTask().getId(), getAndClearCachedVariableMap());
    processEngine.getTaskService().complete(getTask().getId());
    associationManager.disAssociate();
  }

  /**
   * @see BusinessProcess#completeTask()
   *
   * In addition this allows to end the current conversation.
   *
   */
  public void completeTask(boolean endConversation) {
    completeTask();
    if(endConversation) {
      conversationInstance.get().end();
    }
  }

  public boolean isTaskAssociated() {
    return associationManager.getTask() != null;
  }

  /**
   * Save the currently associated task.
   *
   * @throws ProcessEngineCdiException if called from a process engine command or if no Task is currently associated.
   *
   */
  public void saveTask() {
    assertCommandContextNotActive();
    assertTaskAssociated();

    final Task task = getTask();
    // save the task
    processEngine.getTaskService().saveTask(task);
  }

  /**
   * <p>Stop working on a task. Clears the current association.</p>
   *
   * <p>NOTE: this method does not flush any changes.</p>
   * <ul>
   *  <li>If you want to flush changes to process variables, call {@link #flushVariableCache()} prior to calling this method,</li>
   *  <li>If you need to flush changes to the task object, use {@link #saveTask()} prior to calling this method.</li>
   * </ul>
   *
   * @throws ProcessEngineCdiException if called from a process engine command or if no Task is currently associated.
   */
  public void stopTask() {
    assertCommandContextNotActive();
    assertTaskAssociated();
    associationManager.disAssociate();
  }

  /**
   * <p>Stop working on a task. Clears the current association.</p>
   *
   * <p>NOTE: this method does not flush any changes.</p>
   * <ul>
   *  <li>If you want to flush changes to process variables, call {@link #flushVariableCache()} prior to calling this method,</li>
   *  <li>If you need to flush changes to the task object, use {@link #saveTask()} prior to calling this method.</li>
   * </ul>
   *
   * <p>This method allows you to optionally end the current conversation</p>
   *
   * @param endConversation if true, end current conversation.
   * @throws ProcessEngineCdiException if called from a process engine command or if no Task is currently associated.
   */
  public void stopTask(boolean endConversation) {
    stopTask();
    if(endConversation) {
      conversationInstance.get().end();
    }
  }

  // -------------------------------------------------

  /**
   * @param variableName
   *          the name of the process variable for which the value is to be
   *          retrieved
   * @return the value of the provided process variable or 'null' if no such
   *         variable is set
   */
  @SuppressWarnings("unchecked")
  public <T> T getVariable(String variableName) {
    TypedValue variable = getVariableTyped(variableName);
    if (variable != null) {
      Object value = variable.getValue();
      if (value != null) {
        return (T) value;
      }
    }
    return null;
  }

  /**
   * @param variableName
   *          the name of the process variable for which the value is to be
   *          retrieved
   * @return the typed value of the provided process variable or 'null' if no
   *         such variable is set
   *
   * @since 7.3
   */
  @SuppressWarnings("unchecked")
  public <T extends TypedValue> T getVariableTyped(String variableName) {
    TypedValue variable = associationManager.getVariable(variableName);
    return variable != null ? (T) (variable) : null;
  }

  /**
   * Set a value for a process variable.
   * <p />
   *
   * <strong>NOTE:</strong> If no execution is currently associated,
   * the value is temporarily cached and flushed to the process instance
   * at the end of the unit of work
   *
   * @param variableName
   *          the name of the process variable for which a value is to be set
   * @param value
   *          the value to be set
   *
   */
  public void setVariable(String variableName, Object value) {
    associationManager.setVariable(variableName, value);
  }

  /**
   * Get the {@link VariableMap} of cached variables and clear the internal variable cache.
   *
   * @return the {@link VariableMap} of cached variables
   *
   * @since 7.3
   */
  public VariableMap getAndClearCachedVariableMap() {
    VariableMap cachedVariables = associationManager.getCachedVariables();
    VariableMap copy = new VariableMapImpl(cachedVariables);
    cachedVariables.clear();
    return copy;
  }

  /**
   * Get the map of cached variables and clear the internal variable cache.
   *
   * @return the map of cached variables
   * @deprecated use {@link #getAndClearCachedVariableMap()} instead
   */
  @Deprecated
  public Map<String, Object> getAndClearVariableCache() {
    return getAndClearCachedVariableMap();
  }

  /**
   * Get a copy of the {@link VariableMap} of cached variables.
   *
   * @return a copy of the {@link VariableMap} of cached variables.
   *
   * @since 7.3
   */
  public VariableMap getCachedVariableMap() {
    return new VariableMapImpl(associationManager.getCachedVariables());
  }

  /**
   * Get a copy of the map of cached variables.
   *
   * @return a copy of the map of cached variables.
   * @deprecated use {@link #getCachedVariableMap()} instead
   */
  @Deprecated
  public Map<String, Object> getVariableCache() {
    return getCachedVariableMap();
  }

  /**
   * @param variableName
   *          the name of the local process variable for which the value is to be
   *          retrieved
   * @return the value of the provided local process variable or 'null' if no such
   *         variable is set
   */
  @SuppressWarnings("unchecked")
  public <T> T getVariableLocal(String variableName) {
    TypedValue variable = getVariableLocalTyped(variableName);
    if (variable != null) {
      Object value = variable.getValue();
      if (value != null) {
        return (T) value;
      }
    }
    return null;
  }

  /**
   * @param variableName
   *          the name of the local process variable for which the value is to
   *          be retrieved
   * @return the typed value of the provided local process variable or 'null' if
   *         no such variable is set
   *
   * @since 7.3
   */
  @SuppressWarnings("unchecked")
  public <T extends TypedValue> T getVariableLocalTyped(String variableName) {
    TypedValue variable = associationManager.getVariableLocal(variableName);
    return variable != null ? (T) variable : null;
  }

  /**
   * Set a value for a local process variable.
   * <p />
   *
   * <strong>NOTE:</strong> If a task or execution is currently associated,
   * the value is temporarily cached and flushed to the process instance
   * at the end of the unit of work - otherwise an Exception will be thrown
   *
   * @param variableName
   *          the name of the local process variable for which a value is to be set
   * @param value
   *          the value to be set
   *
   */
  public void setVariableLocal(String variableName, Object value) {
    associationManager.setVariableLocal(variableName, value);
  }

  /**
   * Get the {@link VariableMap} of local cached variables and clear the internal variable cache.
   *
   * @return the {@link VariableMap} of cached variables
   *
   * @since 7.3
   */
  public VariableMap getAndClearCachedLocalVariableMap() {
    VariableMap cachedVariablesLocal = associationManager.getCachedLocalVariables();
    VariableMap copy = new VariableMapImpl(cachedVariablesLocal);
    cachedVariablesLocal.clear();
    return copy;
  }

  /**
   * Get the map of local cached variables and clear the internal variable cache.
   *
   * @return the map of cached variables
   * @deprecated use {@link #getAndClearCachedLocalVariableMap()} instead
   */
  @Deprecated
  public Map<String, Object> getAndClearVariableLocalCache() {
    return getAndClearCachedLocalVariableMap();
  }

  /**
   * Get a copy of the {@link VariableMap} of local cached variables.
   *
   * @return a copy of the {@link VariableMap} of local cached variables.
   *
   * @since 7.3
   */
  public VariableMap getCachedLocalVariableMap() {
    return new VariableMapImpl(associationManager.getCachedLocalVariables());
  }

  /**
   * Get a copy of the map of local cached variables.
   *
   * @return a copy of the map of local cached variables.
   * @deprecated use {@link #getCachedLocalVariableMap()} instead
   */
  @Deprecated
  public Map<String, Object> getVariableLocalCache() {
    return getCachedLocalVariableMap();
  }

  /**
   * <p>This method allows to flush the cached variables to the Task or Execution.<p>
   *
   * <ul>
   *   <li>If a Task instance is currently associated,
   *       the variables will be flushed using {@link TaskService#setVariables(String, Map)}</li>
   *   <li>If an Execution instance is currently associated,
   *       the variables will be flushed using {@link RuntimeService#setVariables(String, Map)}</li>
   *   <li>If neither a Task nor an Execution is currently associated,
   *       ProcessEngineCdiException is thrown.</li>
   * </ul>
   *
   * <p>A successful invocation of this method will empty the variable cache.</p>
   *
   * <p>If this method is called from an active command (ie. from inside a Java Delegate).
   * {@link ProcessEngineCdiException} is thrown.</p>
   *
   * @throws ProcessEngineCdiException if called from a process engine command or if neither a Task nor an Execution is associated.
   */
  public void flushVariableCache() {
    associationManager.flushVariableCache();
  }

  // ----------------------------------- Getters / Setters

  /*
   * Note that Producers should go into {@link CurrentProcessInstance} in
   * order to allow for specializing {@link BusinessProcess}.
   */

  /**
   * @see #startTask(String)
   */
  public void setTask(Task task) {
    startTask(task.getId());
  }

  /**
   * @see #startTask(String)
   */
  public void setTaskId(String taskId) {
    startTask(taskId);
  }

  /**
   * @see #associateExecutionById(String)
   */
  public void setExecution(Execution execution) {
    associateExecutionById(execution.getId());
  }

  /**
   * @see #associateExecutionById(String)
   */
  protected void setExecutionId(String executionId) {
    associateExecutionById(executionId);
  }

  /**
   * Returns the id of the currently associated process instance or 'null'
   */
  public String getProcessInstanceId() {
    Execution execution = associationManager.getExecution();
    return execution != null ? execution.getProcessInstanceId() : null;
  }

  /**
   * Returns the id of the task associated with the current conversation or 'null'.
   */
  public String getTaskId() {
    Task task = getTask();
    return task != null ? task.getId() : null;
  }

  /**
   * Returns the currently associated {@link Task}  or 'null'
   *
   * @throws ProcessEngineCdiException
   *           if no {@link Task} is associated. Use {@link #isTaskAssociated()}
   *           to check whether an association exists.
   *
   */
  public Task getTask() {
    return associationManager.getTask();
  }

  /**
   * Returns the currently associated execution  or 'null'
   */
  public Execution getExecution() {
    return associationManager.getExecution();
  }

  /**
   * @see #getExecution()
   */
  public String getExecutionId() {
    Execution e = getExecution();
    return e != null ? e.getId() : null;
  }

  /**
   * Returns the {@link ProcessInstance} currently associated or 'null'
   *
   * @throws ProcessEngineCdiException
   *           if no {@link Execution} is associated. Use
   *           {@link #isAssociated()} to check whether an association exists.
   */
  public ProcessInstance getProcessInstance() {
    Execution execution = getExecution();
    if(execution != null && !(execution.getProcessInstanceId().equals(execution.getId()))){
      return processEngine
            .getRuntimeService()
            .createProcessInstanceQuery()
            .processInstanceId(execution.getProcessInstanceId())
            .singleResult();
    }
    return (ProcessInstance) execution;
  }

  // internal implementation //////////////////////////////////////////////////////////

  protected void assertExecutionAssociated() {
    if (associationManager.getExecution() == null) {
      throw new ProcessEngineCdiException("No execution associated. Call busniessProcess.associateExecutionById() or businessProcess.startTask() first.");
    }
  }

  protected void assertTaskAssociated() {
    if (associationManager.getTask() == null) {
      throw new ProcessEngineCdiException("No task associated. Call businessProcess.startTask() first.");
    }
  }

  protected void assertCommandContextNotActive() {
    if(Context.getCommandContext() != null) {
      throw new ProcessEngineCdiException("Cannot use this method of the BusinessProcess bean from an active command context.");
    }
  }

}
