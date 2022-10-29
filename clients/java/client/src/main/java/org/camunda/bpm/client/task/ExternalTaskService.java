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
package org.camunda.bpm.client.task;

import org.camunda.bpm.client.exception.ConnectionLostException;
import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.EngineException;
import org.camunda.bpm.client.exception.UnknownHttpErrorException;
import org.camunda.bpm.client.exception.ValueMapperException;

import java.util.Map;

/**
 * <p>Service that provides possibilities to interact with fetched and locked tasks.</p>
 *
 * @author Tassilo Weidner
 */
public interface ExternalTaskService {

  /**
   * Locks a task by a given amount of time.
   *
   * Note: This method should be used to lock external tasks
   * that have been obtained without using the fetch & lock API.
   *
   * @param externalTaskId the id of the external task whose lock will be extended
   * @param lockDuration specifies the lock duration in milliseconds
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client
   */
  void lock(String externalTaskId, long lockDuration);

  /**
   * Locks a task by a given amount of time.
   *
   * Note: This method should be used to lock external tasks
   * that have been obtained without using the fetch & lock API.
   *
   * @param externalTask which lock will be extended
   * @param lockDuration specifies the lock duration in milliseconds
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void lock(ExternalTask externalTask, long lockDuration);

  /**
   * Unlocks a task and clears the tasks lock expiration time and worker id.
   *
   * @param externalTask which will be unlocked
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void unlock(ExternalTask externalTask);

  /**
   * Completes a task.
   *
   * @param externalTask which will be completed
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if the tasks most recent lock could not be acquired
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws ValueMapperException
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   * <ul>
   *   <li> if an object cannot be serialized
   *   <li> if no 'objectTypeName' is provided for non-null value
   *   <li> if value is of type abstract
   *   <li> if no suitable serializer could be found
   * </ul>
   */
  void complete(ExternalTask externalTask);

  /**
   * Set variables
   *
   * @param variables     are set in the tasks ancestor execution hierarchy. The key and the value represent
   *                      the variable name and its value. Map can consist of both typed and untyped variables.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws ValueMapperException
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   * <ul>
   *   <li> if an object cannot be serialized
   *   <li> if no 'objectTypeName' is provided for non-null value
   *   <li> if value is of type abstract
   *   <li> if no suitable serializer could be found
   * </ul>
   */
  public void setVariables(String processInstanceId, Map<String, Object> variables);


  /**
   * Set variables
   *
   * @param variables     are set in the tasks ancestor execution hierarchy. The key and the value represent
   *                      the variable name and its value. Map can consist of both typed and untyped variables.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws ValueMapperException
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   * <ul>
   *   <li> if an object cannot be serialized
   *   <li> if no 'objectTypeName' is provided for non-null value
   *   <li> if value is of type abstract
   *   <li> if no suitable serializer could be found
   * </ul>
   */
  public void setVariables(ExternalTask externalTask, Map<String, Object> variables);


  /**
   * Completes a task.
   *
   * @param externalTask  which will be completed
   * @param variables     are set in the tasks ancestor execution hierarchy The key and the value represent
   *                      the variable name and its value. Map can consist of both typed and untyped variables.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws ValueMapperException
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   * <ul>
   *   <li> if an object cannot be serialized
   *   <li> if no 'objectTypeName' is provided for non-null value
   *   <li> if value is of type abstract
   *   <li> if no suitable serializer could be found
   * </ul>
   */
  void complete(ExternalTask externalTask, Map<String, Object> variables);

  /**
   * Completes a task.
   *
   * @param externalTask    which will be completed
   * @param variables       are set in the tasks ancestor execution hierarchy. The key and the value represent
   *                        the variable name and its value. Map can consist of both typed and untyped variables.
   * @param localVariables  are set in the execution of the external task instance. The key and the value represent
   *                        the variable name and its value. Map can consist of both typed and untyped variables.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws ValueMapperException
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   * <ul>
   *   <li> if an object cannot be serialized
   *   <li> if no 'objectTypeName' is provided for non-null value
   *   <li> if value is of type abstract
   *   <li> if no suitable serializer could be found
   * </ul>
   */
  void complete(ExternalTask externalTask, Map<String, Object> variables, Map<String, Object> localVariables);

  /**
   * Completes a task.
   *
   * @param externalTaskId  the id of the external task which will be completed
   * @param variables       are set in the tasks ancestor execution hierarchy. The key and the value represent
   *                        the variable name and its value. Map can consist of both typed and untyped variables.
   * @param localVariables  are set in the execution of the external task instance. The key and the value represent
   *                        the variable name and its value. Map can consist of both typed and untyped variables.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws ValueMapperException
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void complete(String externalTaskId, Map<String, Object> variables, Map<String, Object> localVariables);

  /**
   * Reports a failure to execute a task. A number of retries and a timeout until
   * the task can be specified. If the retries are set to 0, an incident for this
   * task is created.
   *
   * @param externalTask external task for which a failure will be reported
   * @param errorMessage indicates the reason of the failure.
   * @param errorDetails provides a detailed error description.
   * @param retries      specifies how often the task should be retried. Must be &gt;= 0.
   *                     If 0, an incident is created and the task cannot be fetched anymore
   *                     unless the retries are increased again. The incident's message is set
   *                     to the errorMessage parameter.
   * @param retryTimeout specifies a timeout in milliseconds before the external task
   *                     becomes available again for fetching. Must be &gt;= 0.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void handleFailure(ExternalTask externalTask, String errorMessage, String errorDetails, int retries, long retryTimeout);

  /**
   * Reports a failure to execute a task. A number of retries and a timeout until
   * the task can be specified. If the retries are set to 0, an incident for this
   * task is created.
   *
   * @param externalTaskId the id of the external task for which a failure will be reported
   * @param errorMessage   indicates the reason of the failure.
   * @param errorDetails   provides a detailed error description.
   * @param retries        specifies how often the task should be retried. Must be &gt;= 0.
   *                       If 0, an incident is created and the task cannot be fetched anymore
   *                       unless the retries are increased again. The incident's message is set
   *                       to the errorMessage parameter.
   * @param retryTimeout   specifies a timeout in milliseconds before the external task
   *                       becomes available again for fetching. Must be &gt;= 0.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void handleFailure(String externalTaskId, String errorMessage, String errorDetails, int retries, long retryTimeout);

  /**
   * Reports a failure to execute a task. A number of retries and a timeout until
   * the task can be specified. If the retries are set to 0, an incident for this
   * task is created.
   *
   * @param externalTaskId the id of the external task for which a failure will be reported
   * @param errorMessage   indicates the reason of the failure.
   * @param errorDetails   provides a detailed error description.
   * @param retries        specifies how often the task should be retried. Must be &gt;= 0.
   *                       If 0, an incident is created and the task cannot be fetched anymore
   *                       unless the retries are increased again. The incident's message is set
   *                       to the errorMessage parameter.
   * @param retryTimeout   specifies a timeout in milliseconds before the external task
   *                       becomes available again for fetching. Must be &gt;= 0.
   * @param variables      a map of variables to set on the execution the external task is assigned to
   * @param localVariables a map of variables to set on the execution locally
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void handleFailure(String externalTaskId, String errorMessage, String errorDetails, int retries, long retryTimeout, Map<String, Object> variables, Map<String, Object> localVariables);

  /**
   * Reports a business error in the context of a running task.
   * The error code must be specified to identify the BPMN error handler.
   *
   * @param externalTask external task for which a BPMN error will be reported
   * @param errorCode    that indicates the predefined error. The error code
   *                     is used to identify the BPMN error handler.
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void handleBpmnError(ExternalTask externalTask, String errorCode);

  /**
   * Reports a business error in the context of a running task.
   * The error code must be specified to identify the BPMN error handler.
   *
   * @param externalTask external task for which a BPMN error will be reported
   * @param errorCode    that indicates the predefined error. The error code
   *                     is used to identify the BPMN error handler.
   * @param errorMessage which will be passed when the BPMN error is caught
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void handleBpmnError(ExternalTask externalTask, String errorCode, String errorMessage);

  /**
   * Reports a business error in the context of a running task.
   * The error code must be specified to identify the BPMN error handler.
   *
   * @param externalTask external task for which a BPMN error will be reported
   * @param errorCode    that indicates the predefined error. The error code
   *                     is used to identify the BPMN error handler.
   * @param errorMessage which will be passed when the BPMN error is caught
   * @param variables    which will be passed to the execution when the BPMN error is caught
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void handleBpmnError(ExternalTask externalTask, String errorCode, String errorMessage, Map<String, Object> variables);

  /**
   * Reports a business error in the context of a running task.
   * The error code must be specified to identify the BPMN error handler.
   *
   * @param externalTaskId the id of the external task for which a BPMN error will be reported
   * @param errorCode      that indicates the predefined error. The error code
   *                       is used to identify the BPMN error handler.
   * @param errorMessage   which will be passed when the BPMN error is caught
   * @param variables      which will be passed to the execution when the BPMN error is caught
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void handleBpmnError(String externalTaskId, String errorCode, String errorMessage, Map<String, Object> variables);

  /**
   * Extends the timeout of the lock by a given amount of time.
   *
   * @param externalTask which lock will be extended
   * @param newDuration  specifies the new lock duration in milliseconds
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void extendLock(ExternalTask externalTask, long newDuration);

  /**
   * Extends the timeout of the lock by a given amount of time.
   *
   * @param externalTaskId the id of the external task which lock will be extended
   * @param newDuration    specifies the new lock duration in milliseconds
   *
   * @throws NotFoundException if the task doesn't exist or has already been canceled or completed
   * @throws BadRequestException if an illegal operation was performed or the given data is invalid.
   * @throws EngineException if something went wrong during the engine execution (e.g., a persistence exception occurred)
   * @throws ConnectionLostException if the connection could not be established
   * @throws UnknownHttpErrorException if the HTTP status code is not known by the client.
   */
  void extendLock(String externalTaskId, long newDuration);

}
