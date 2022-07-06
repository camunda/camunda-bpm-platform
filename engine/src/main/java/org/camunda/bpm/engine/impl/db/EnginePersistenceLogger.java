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
package org.camunda.bpm.engine.impl.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.executor.BatchExecutorException;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.WrongDbException;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClassNameUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Stefan Hentschel.
 */
public class EnginePersistenceLogger extends ProcessEngineLogger {

  protected static final String HINT_TEXT = "Hint: Set <property name=\"databaseSchemaUpdate\" to value=\"true\" or " +
                                            "value=\"create-drop\" (use create-drop for testing only!) in bean " +
                                            "processEngineConfiguration in camunda.cfg.xml for automatic schema creation";

  protected String buildStringFromList(Collection<?> list) {
    StringBuilder message = new StringBuilder();
    message.append("[");
    message.append("\n");
    for( Object object : list ) {
      message.append("  ");
      message.append(object.toString());
      message.append("\n");
    }
    message.append("]");

    return message.toString();
  }

  private String buildStringFromMap(Map<String, ?> map) {
    StringBuilder message = new StringBuilder();
    message.append("[");
    message.append("\n");
    for( Map.Entry<String, ?> entry : map.entrySet() ) {
      message.append("  ");
      message.append(entry.getKey());
      message.append(": ");
      message.append(entry.getValue().toString());
      message.append("\n");
    }
    message.append("]");
    return message.toString();
  }

  public <T extends DbEntity> ProcessEngineException entityCacheLookupException(Class<T> type, String id,
      Class<? extends DbEntity> entity, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "001",
      "Could not lookup entity of type '{}' and id '{}': found entity of type '{}'.",
      type,
      id,
      entity
    ), cause);
  }

  public ProcessEngineException entityCacheDuplicateEntryException(String currentState, String id,
      Class<? extends DbEntity> entityClass, DbEntityState foundState) {
    return new ProcessEngineException(exceptionMessage(
      "002",
      "Cannot add {} entity with id '{}' and type '{}' into cache. An entity with the same id and type is already in state '{}'",
      currentState,
      id,
      entityClass,
      foundState
    ));
  }

  public ProcessEngineException alreadyMarkedEntityInEntityCacheException(String id,
      Class<? extends DbEntity> entityClass, DbEntityState state) {

    return new ProcessEngineException(exceptionMessage(
      "003",
      "Inserting an entity with Id '{}' and type '{}' which is already marked with state '{}'",
      id,
      entityClass,
      state
    ));
  }

  public ProcessEngineException flushDbOperationException(List<DbOperation> operationsToFlush,
                                                          DbOperation failedOperation,
                                                          Throwable e) {

    String message = collectExceptionMessages(e);

    String exceptionMessage = exceptionMessage(
        "004",
        "Exception while executing Database Operation '{}' with message '{}'. Flush summary: \n {}",
        failedOperation,
        message,
        buildStringFromList(operationsToFlush)
    );

    ProcessEngineException subException = new ProcessEngineException(exceptionMessage, e);
    return ExceptionUtil.wrapPersistenceException(subException);
  }

  public OptimisticLockingException concurrentUpdateDbEntityException(DbOperation operation) {
    return new OptimisticLockingException(exceptionMessage(
      "005",
      "Execution of '{}' failed. Entity was updated by another transaction concurrently.",
      operation
    ));
  }

  public void flushedCacheState(List<CachedDbEntity> cachedEntities) {
    if(isDebugEnabled()) {
      logDebug("006", "Cache state after flush: {}", buildStringFromList(cachedEntities));
    }

  }

  public ProcessEngineException mergeDbEntityException(DbEntity entity) {
    return new ProcessEngineException(exceptionMessage("007", "Cannot merge DbEntity '{}' without id", entity));
  }

  public void databaseFlushSummary(Collection<DbOperation> operations) {
   if(isDebugEnabled()) {
     logDebug("008", "Flush Summary: {}", buildStringFromList(operations));
   }
  }

  public void executeDatabaseOperation(String operationType, Object parameter) {
    if(isDebugEnabled()) {

      String message;
      if(parameter != null) {
        message = parameter.toString();
      } else {
        message = "null";
      }

      if(parameter instanceof DbEntity) {
        DbEntity dbEntity = (DbEntity) parameter;
        message = ClassNameUtil.getClassNameWithoutPackage(dbEntity) + "[id=" + dbEntity.getId() + "]";
      }

      logDebug("009", "SQL operation: '{}'; Entity: '{}'", operationType, message);
    }
  }

  public void executeDatabaseBulkOperation(String operationType, String statement, Object parameter) {
    logDebug("010", "SQL bulk operation: '{}'; Statement: '{}'; Parameter: '{}'", operationType, statement, parameter);
  }

  public void fetchDatabaseTables(String source, List<String> tableNames) {
    if(isDebugEnabled()) {
      logDebug(
        "011",
        "Retrieving process engine tables from: '{}'. Retrieved tables: {}",
        source,
        buildStringFromList(tableNames)
      );
    }
  }

  public void missingSchemaResource(String resourceName, String operation) {
    logDebug("012", "There is no schema resource '{}' for operation '{}'.", resourceName, operation);
  }

  public ProcessEngineException missingSchemaResourceException(String resourceName, String operation) {
    return new ProcessEngineException(
      exceptionMessage("013", "There is no schema resource '{}' for operation '{}'.", resourceName, operation));
  }

  public ProcessEngineException missingSchemaResourceFileException(String fileName, Throwable cause) {
    return new ProcessEngineException(
      exceptionMessage("014", "Cannot find schema resource file with name '{}'",fileName), cause);
  }

  public void failedDatabaseOperation(String operation, String statement, Throwable cause) {
    logError(
      "015",
      "Problem during schema operation '{}' with statement '{}'. Cause: '{}'",
      operation,
      statement,
      cause.getMessage()
    );
  }

  public void performingDatabaseOperation(String operation, String component, String resourceName) {
    logInfo(
      "016",
      "Performing database operation '{}' on component '{}' with resource '{}'",
      operation,
      component,
      resourceName);
  }

  public void successfulDatabaseOperation(String operation, String component) {
    logDebug("Database schema operation '{}' for component '{}' was successful.", operation, component);
  }

  public ProcessEngineException performDatabaseOperationException(String operation, String sql, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "017",
      "Could not perform operation '{}' on database schema for SQL Statement: '{}'.",
      operation,
      sql
    ), cause);
  }

  public ProcessEngineException checkDatabaseTableException(Throwable cause) {
    return new ProcessEngineException(
      exceptionMessage("018", "Could not check if tables are already present using metadata."), cause);
  }

  public ProcessEngineException getDatabaseTableNameException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage("019", "Unable to fetch process engine table names."), cause);
  }

  public ProcessEngineException missingRelationMappingException(String relation) {
    return new ProcessEngineException(
      exceptionMessage("020", "There is no mapping for the relation '{}' registered.", relation));
  }

  public ProcessEngineException databaseHistoryLevelException(String level) {
    return new ProcessEngineException(
      exceptionMessage("021", "historyLevel '{}' is higher then 'none' and dbHistoryUsed is set to false.", level));
  }

  public ProcessEngineException invokeSchemaResourceToolException(int length) {
    return new ProcessEngineException(exceptionMessage(
      "022",
      "Schema resource tool was invoked with '{}' parameters." +
      "Schema resource tool must be invoked with exactly 2 parameters:" +
      "\n - 1st parameter is the process engine configuration file," +
      "\n - 2nd parameter is the schema resource file name",
      length
    ));
  }

  public ProcessEngineException loadModelException(String type, String modelName, String id, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "023",
      "Could not load {} Model for {} definition with id '{}'.",
      type,
      modelName,
      id
    ), cause);
  }

  public void removeEntryFromDeploymentCacheFailure(String modelName, String id, Throwable cause) {
    logWarn(
      "024",
      "Could not remove {} definition with id '{}' from the cache. Reason: '{}'",
      modelName,
      id,
      cause.getMessage(),
      cause
    );
  }


  public ProcessEngineException engineAuthorizationTypeException(int usedType, int global, int grant, int revoke) {
    return new ProcessEngineException(exceptionMessage(
      "025",
      "Unrecognized authorization type '{}'. Must be one of ['{}', '{}', '{}']",
      usedType,
      global,
      grant,
      revoke
    ));
  }

  public IllegalStateException permissionStateException(String methodName, String type) {
    return new IllegalStateException(
      exceptionMessage("026", "Method '{}' cannot be used for authorization with type '{}'.", methodName, type));
  }

  public ProcessEngineException notUsableGroupIdForGlobalAuthorizationException() {
    return new ProcessEngineException(exceptionMessage("027", "Cannot use 'groupId' for GLOBAL authorization"));
  }

  public ProcessEngineException illegalValueForUserIdException(String id, String expected) {
    return new ProcessEngineException(
      exceptionMessage("028", "Illegal value '{}' for userId for GLOBAL authorization. Must be '{}'", id, expected));
  }

  public AuthorizationException requiredCamundaAdmin() {
    return requiredCamundaAdminOrPermissionException(null);
  }

  public AuthorizationException requiredCamundaAdminOrPermissionException(List<MissingAuthorization> missingAuthorizations) {
    String exceptionCode = "029";
    StringBuilder sb = new StringBuilder();
    sb.append("Required admin authenticated group or user");
    if(missingAuthorizations != null && !missingAuthorizations.isEmpty()) {
      sb.append(" or any of the following permissions: ");
      sb.append(AuthorizationException.generateMissingAuthorizationsList(missingAuthorizations));
      exceptionCode = "110";
    }
    sb.append(".");
    return new AuthorizationException(exceptionMessage(exceptionCode, sb.toString()));
  }

  public void createChildExecution(ExecutionEntity child, ExecutionEntity parent) {
    if(isDebugEnabled()) {
      logDebug("030", "Child execution '{}' created with parent '{}'.", child.toString(), parent.toString());
    }
  }

  public void initializeExecution(ExecutionEntity entity) {
    logDebug("031", "Initializing execution '{}'", entity.toString());
  }

  public void initializeTimerDeclaration(ExecutionEntity entity) {
    logDebug("032", "Initializing timer declaration '{}'", entity.toString());
  }

  public ProcessEngineException requiredAsyncContinuationException(String id) {
    return new ProcessEngineException(exceptionMessage(
      "033",
      "Asynchronous Continuation for activity with id '{}' requires a message job declaration",
      id
    ));
  }

  public ProcessEngineException restoreProcessInstanceException(ExecutionEntity entity) {
    return new ProcessEngineException(exceptionMessage(
      "034",
      "Can only restore process instances. This method must be called on a process instance execution but was called on '{}'",
      entity.toString()
    ));

  }

  public ProcessEngineException executionNotFoundException(String id) {
    return new ProcessEngineException(exceptionMessage("035", "Unable to find execution for id '{}'", id));
  }

  public ProcessEngineException castModelInstanceException(ModelElementInstance instance, String toElement, String type,
      String namespace, Throwable cause) {

    return new ProcessEngineException(exceptionMessage(
      "036",
      "Cannot cast '{}' to '{}'. Element is of type '{}' with namespace '{}'.",
      instance,
      toElement,
      type,
      namespace
    ), cause);
  }

  public BadUserRequestException requestedProcessInstanceNotFoundException(String id) {
    return new BadUserRequestException(exceptionMessage("037", "No process instance found for id '{}'", id));
  }

  public NotValidException queryExtensionException(String extendedClassName, String extendingClassName) {
    return new NotValidException(exceptionMessage(
      "038",
      "Unable to extend a query of class '{}' by a query of class '{}'.",
      extendedClassName,
      extendingClassName
    ));
  }

  public ProcessEngineException unsupportedResourceTypeException(String type) {
    return new ProcessEngineException(exceptionMessage("039", "Unsupported resource type '{}'", type));
  }

  public ProcessEngineException serializerNotDefinedException(Object entity) {
    return new ProcessEngineException(exceptionMessage("040", "No serializer defined for variable instance '{}'", entity));
  }

  public ProcessEngineException serializerOutOfContextException() {
    return new ProcessEngineException(exceptionMessage("041", "Cannot work with serializers outside of command context."));
  }

  public ProcessEngineException taskIsAlreadyAssignedException(String usedId, String foundId) {
    return new ProcessEngineException(
      exceptionMessage("042", "Cannot assign '{}' to a task assignment that has already '{}' set.", usedId, foundId));
  }

  public SuspendedEntityInteractionException suspendedEntityException(String type, String id) {
    return new SuspendedEntityInteractionException(exceptionMessage("043", "{} with id '{}' is suspended.", type, id));
  }

  public void logUpdateUnrelatedProcessDefinitionEntity(String thisKey, String thatKey, String thisDeploymentId, String thatDeploymentId) {
    logDebug(
        "044",
        "Cannot update entity from an unrelated process definition: this key '{}', that key '{}', this deploymentId '{}', that deploymentId '{}'",
        thisKey,
        thatKey,
        thisDeploymentId,
        thatDeploymentId);
  }

  public ProcessEngineException toManyProcessDefinitionsException(int count, String key, String versionAttribute, String versionValue, String tenantId) {
    return new ProcessEngineException(exceptionMessage(
      "045",
      "There are '{}' results for a process definition with key '{}', {} '{}' and tenant-id '{}'.",
      count,
      key,
      versionAttribute,
      versionValue
    ));
  }

  public ProcessEngineException notAllowedIdException(String id) {
    return new ProcessEngineException(
      exceptionMessage("046", "Cannot set id '{}'. Only the provided id generation is allowed for properties.", id));
  }

  public void countRowsPerProcessEngineTable(Map<String, Long> map) {
    if(isDebugEnabled()) {
      logDebug("047", "Number of rows per process engine table: {}", buildStringFromMap(map));
    }
  }

  public ProcessEngineException countTableRowsException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage("048", "Could not fetch table counts."), cause);
  }

  public void selectTableCountForTable(String name) {
    logDebug("049", "Selecting table count for table with name '{}'", name);
  }

  public ProcessEngineException retrieveMetadataException(Throwable cause) {
    String exceptionMessage = exceptionMessage("050", "Could not retrieve database metadata. Reason: '{}'", cause.getMessage());
    ProcessEngineException exception = new ProcessEngineException(exceptionMessage, cause);
    return ExceptionUtil.wrapPersistenceException(exception);
  }

  public ProcessEngineException invokeTaskListenerException(Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "051",
      "There was an exception while invoking the TaskListener. Message: '{}'",
      cause.getMessage()
    ), cause);
  }

  public BadUserRequestException uninitializedFormKeyException() {
    return new BadUserRequestException(exceptionMessage(
      "052",
      "The form key / form reference is not initialized. You must call initializeFormKeys() on the task query before you can " +
      "retrieve the form key or the form reference."
    ));
  }

  public ProcessEngineException disabledHistoryException() {
    return new ProcessEngineException(exceptionMessage("053", "History is not enabled."));
  }

  public ProcessEngineException instantiateSessionException(String name, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "054",
      "Could not instantiate class '{}'. Message: '{}'",
      name,
      cause.getMessage()
    ), cause);
  }

  public WrongDbException wrongDbVersionException(String version, String dbVersion) {
    return new WrongDbException(exceptionMessage(
      "055",
      "Version mismatch: Camunda library version is '{}' and db version is '{}'. " +
      HINT_TEXT,
      version,
      dbVersion
    ), version, dbVersion);
  }

  public ProcessEngineException missingTableException(List<String> components) {
    return new ProcessEngineException(exceptionMessage(
      "056",
      "Tables are missing for the following components: {}",
      buildStringFromList(components)
    ));
  }

  public ProcessEngineException missingActivitiTablesException() {
    return new ProcessEngineException(exceptionMessage(
      "057",
      "There are no Camunda tables in the database. " +
        HINT_TEXT
    ));
  }

  public ProcessEngineException unableToFetchDbSchemaVersion(Throwable cause) {
    return new ProcessEngineException(exceptionMessage("058", "Could not fetch the database schema version."), cause);
  }

  public void failedTofetchVariableValue(Throwable cause) {
    logDebug("059", "Could not fetch value for variable.", cause);
  }

  public ProcessEngineException historicDecisionInputInstancesNotFetchedException() {
    return new ProcessEngineException(exceptionMessage(
        "060",
        "The input instances for the historic decision instance are not fetched. You must call 'includeInputs()' on the query to enable fetching."
        ));
  }

  public ProcessEngineException historicDecisionOutputInstancesNotFetchedException() {
    return new ProcessEngineException(exceptionMessage(
        "061",
        "The output instances for the historic decision instance are not fetched. You must call 'includeOutputs()' on the query to enable fetching."
        ));
  }

  public void executingDDL(List<String> logLines) {
    if(isDebugEnabled()) {
      logDebug(
          "062",
          "Executing Schmema DDL {}",
          buildStringFromList(logLines));
    }
  }

  public ProcessEngineException collectResultValueOfUnsupportedTypeException(TypedValue collectResultValue) {
    return new ProcessEngineException(exceptionMessage(
        "063",
        "The collect result value '{}' of the decision table result is not of type integer, long or double.",
        collectResultValue
        ));
  }

  public void creatingHistoryLevelPropertyInDatabase(HistoryLevel historyLevel) {
    logInfo(
        "065",
        "Creating historyLevel property in database for level: {}", historyLevel);
  }

  public void couldNotSelectHistoryLevel(String message) {
    logWarn(
        "066", "Could not select history level property: {}", message);
  }

  public void noHistoryLevelPropertyFound() {
    logInfo(
        "067", "No history level property found in database");
  }

  public void noDeploymentLockPropertyFound() {
    logError(
        "068", "No deployment lock property found in databse");
  }

  public void debugJobExecuted(JobEntity jobEntity) {
    logDebug(
        "069", "Job executed, deleting it", jobEntity);
  }

  public ProcessEngineException multipleTenantsForProcessDefinitionKeyException(String processDefinitionKey) {
    return new ProcessEngineException(exceptionMessage(
        "070",
        "Cannot resolve a unique process definition for key '{}' because it exists for multiple tenants.",
        processDefinitionKey
        ));
  }

  public ProcessEngineException cannotDeterminePaDataformats(ProcessApplicationUnavailableException e) {
    return new ProcessEngineException(exceptionMessage(
        "071","Cannot determine process application variable serializers. Context Process Application is unavailable."), e);
  }

  public ProcessEngineException cannotChangeTenantIdOfTask(String taskId, String currentTenantId, String tenantIdToSet) {
    return new ProcessEngineException(exceptionMessage(
        "072", "Cannot change tenantId of Task '{}'. Current tenant id '{}', Tenant id to set '{}'", taskId, currentTenantId, tenantIdToSet));
  }

  public ProcessEngineException cannotSetDifferentTenantIdOnSubtask(String parentTaskId, String tenantId, String tenantIdToSet) {
    return new ProcessEngineException(exceptionMessage(
        "073", "Cannot set different tenantId on subtask than on parent Task. Parent taskId: '{}', tenantId: '{}', tenant id to set '{}'", parentTaskId, tenantId, tenantIdToSet));
  }

  public ProcessEngineException multipleTenantsForDecisionDefinitionKeyException(String decisionDefinitionKey) {
    return new ProcessEngineException(exceptionMessage(
        "074",
        "Cannot resolve a unique decision definition for key '{}' because it exists for multiple tenants.",
        decisionDefinitionKey
        ));
  }

  public ProcessEngineException multipleTenantsForCaseDefinitionKeyException(String caseDefinitionKey) {
    return new ProcessEngineException(exceptionMessage(
        "075",
        "Cannot resolve a unique case definition for key '{}' because it exists for multiple tenants.",
        caseDefinitionKey
        ));
  }

  public ProcessEngineException deleteProcessDefinitionWithProcessInstancesException(String processDefinitionId, Long processInstanceCount) {
    return new ProcessEngineException(exceptionMessage(
        "076",
        "Deletion of process definition without cascading failed. Process definition with id: {} can't be deleted, since there exists {} dependening process instances.",
        processDefinitionId, processInstanceCount
        ));
  }

  public ProcessEngineException resolveParentOfExecutionFailedException(String parentId, String executionId) {
    return new ProcessEngineException(exceptionMessage(
        "077",
        "Cannot resolve parent with id '{}' of execution '{}', perhaps it was deleted in the meantime",
        parentId,
        executionId
        ));
  }

  public void noHistoryCleanupLockPropertyFound() {
    logError(
        "078", "No history cleanup lock property found in databse");
  }

  public void logUpdateUnrelatedCaseDefinitionEntity(String thisKey, String thatKey, String thisDeploymentId, String thatDeploymentId) {
    logDebug(
      "079",
      "Cannot update entity from an unrelated case definition: this key '{}', that key '{}', this deploymentId '{}', that deploymentId '{}'",
      thisKey,
      thatKey,
      thisDeploymentId,
      thatDeploymentId
    );
  }

  public void logUpdateUnrelatedDecisionDefinitionEntity(String thisKey, String thatKey, String thisDeploymentId, String thatDeploymentId) {
    logDebug(
        "080",
        "Cannot update entity from an unrelated decision definition: this key '{}', that key '{}', this deploymentId '{}', that deploymentId '{}'",
        thisKey,
        thatKey,
        thisDeploymentId,
        thatDeploymentId);
  }

  public void noStartupLockPropertyFound() {
    logError(
        "081", "No startup lock property found in database");
  }

  public ProcessEngineException flushDbOperationUnexpectedException(List<DbOperation> operationsToFlush,
                                                                    Throwable cause) {
    String exceptionMessage = exceptionMessage(
      "083",
      "Unexpected exception while executing database operations with message '{}'. Flush summary: \n {}",
        collectExceptionMessages(cause),
        buildStringFromList(operationsToFlush)
    );

    ProcessEngineException subException = new ProcessEngineException(exceptionMessage, cause);
    return ExceptionUtil.wrapPersistenceException(subException);
  }

  public ProcessEngineException wrongBatchResultsSizeException(List<DbOperation> operationsToFlush) {
    return new ProcessEngineException(exceptionMessage(
      "084",
      "Exception while executing Batch Database Operations: the size of Batch Result does not correspond to the number of flushed operations. Flush summary: \n {}",
      buildStringFromList(operationsToFlush)
    ));
  }

  public ProcessEngineException multipleDefinitionsForVersionTagException(String decisionDefinitionKey, String decisionDefinitionVersionTag) {
    return new ProcessEngineException(exceptionMessage(
        "085",
        "Found more than one decision definition for key '{}' and versionTag '{}'",
        decisionDefinitionKey, decisionDefinitionVersionTag
        ));
  }

  public BadUserRequestException invalidResourceForPermission(String resourceType, String permission) {
    return new BadUserRequestException(exceptionMessage(
        "086",
        "The resource type '{}' is not valid for '{}' permission.",
        resourceType, permission
        ));
  }

  public BadUserRequestException invalidResourceForAuthorization(int resourceType, String permission) {
    return new BadUserRequestException(exceptionMessage(
        "087",
        "The resource type with id:'{}' is not valid for '{}' permission.",
        resourceType, permission
        ));
  }


  public BadUserRequestException disabledPermissionException(String permission) {
    return new BadUserRequestException(exceptionMessage(
        "088",
        "The '{}' permission is disabled, please check your process engine configuration.",
        permission
        ));
  }

  public ProcessEngineException batchingNotSupported(DbOperation operation) {
    throw new ProcessEngineException(exceptionMessage(
        "089",
        "Batching not supported: The jdbc driver in use does not return the number of "
        + "affected rows when executing statement batches. "
        + "Consider setting the engine configuration property 'jdbcBatchProcessing' to false."
        + "Failed operation: {}",
        operation));
  }

  public ProcessEngineException disabledHistoricInstancePermissionsException() {
    return  new BadUserRequestException(exceptionMessage(
        "090",
        "Historic instance permissions are disabled, " +
            "please check your process engine configuration."
    ));
  }

  public void noTelemetryLockPropertyFound() {
    logDebug(
        "091", "No telemetry lock property found in the database");
  }

  public void noTelemetryPropertyFound() {
    logDebug(
        "092", "No telemetry property found in the database");
  }

  public void creatingTelemetryPropertyInDatabase(Boolean telemetryEnabled) {
    logDebug(
        "093",
        "Creating the telemetry property in database with the value: {}", telemetryEnabled);
  }

  public void errorFetchingTelemetryPropertyInDatabase(Exception exception) {
    logDebug(
        "094",
        "Error while fetching the telemetry property from the database: {}", exception.getMessage());
  }

  public void errorConfiguringTelemetryProperty(Exception exception) {
    logDebug(
        "095",
        "Error while configuring the telemetry property: {}", exception.getMessage());
  }

  public void noInstallationIdPropertyFound() {
    logDebug(
        "096", "No installation id property found in database");
  }

  public void creatingInstallationPropertyInDatabase(String value) {
    logDebug(
        "097",
        "Creating the installation id property in database with the value: {}", value);
  }

  public void couldNotSelectInstallationId(String message) {
    logDebug(
        "098",
        "Could not select installation id property: {}", message);
  }

  public void noInstallationIdLockPropertyFound() {
    logDebug(
        "099", "No installation id lock property found in the database");
  }

  public void installationIdPropertyFound(String value) {
    logDebug(
        "100", "Installation id property found in the database: {}", value);
  }

  public void ignoreFailureDuePreconditionNotMet(DbOperation ignoredOperation, String preconditionMessage, DbOperation failedOperation) {
    logDebug(
        "101",
        "Ignoring '{}' database operation failure due to an unmet precondition. {}: '{}'",
        ignoredOperation.toString(),
        preconditionMessage,
        failedOperation.toString());
  }

  public CrdbTransactionRetryException crdbTransactionRetryException(DbOperation operation) {
    return new CrdbTransactionRetryException(exceptionMessage(
        "102",
        "Execution of '{}' failed. Entity was updated by another transaction concurrently, " +
            "and the transaction needs to be retried",
        operation),
        operation.getFailure());
  }

  public CrdbTransactionRetryException crdbTransactionRetryExceptionOnCommit(Throwable cause) {
    return new CrdbTransactionRetryException(exceptionMessage(
        "104",
        "Could not commit transaction. The transaction needs to be retried."),
        cause
    );
  }

  public void crdbFailureIgnored(DbOperation operation) {
    logDebug(
      "105",
      "An OptimisticLockingListener attempted to ignore a failure of: {}. "
      + "Since CockroachDB aborted the transaction, ignoring the failure "
      + "is not possible and an exception is thrown instead.",
      operation
    );
  }

  public void debugDisabledPessimisticLocks() {
    logDebug(
      "106", "No exclusive lock is acquired on CockroachDB or H2, " +
            "as pessimistic locks are disabled on these databases.");
  }

  public void errorFetchingTelemetryInitialMessagePropertyInDatabase(Exception exception) {
    logDebug(
        "107",
        "Error while fetching the telemetry initial message status property from the database: {}", exception.getMessage());
  }


  public void logTaskWithoutExecution(String taskId) {
    logDebug("108",
      "Execution of external task {} is null. This indicates that the task was concurrently completed or deleted. "
      + "It is not returned by the current fetch and lock command.",
      taskId);
  }

  public ProcessEngineException multipleTenantsForCamundaFormDefinitionKeyException(String camundaFormDefinitionKey) {
    return new ProcessEngineException(exceptionMessage(
        "109",
        "Cannot resolve a unique Camunda Form definition for key '{}' because it exists for multiple tenants.",
        camundaFormDefinitionKey
        ));
  }

  // exception code 110 is already taken. See requiredCamundaAdminOrPermissionException() for details.

  public static List<SQLException> findRelatedSqlExceptions(Throwable exception) {
    List<SQLException> sqlExceptionList = new ArrayList<>();
    Throwable cause = exception;
    do {
      if (cause instanceof SQLException) {
        SQLException sqlEx = (SQLException) cause;
        sqlExceptionList.add(sqlEx);
        while (sqlEx.getNextException() != null) {
          sqlExceptionList.add(sqlEx.getNextException());
          sqlEx = sqlEx.getNextException();
        }
      }
      cause = cause.getCause();
    } while (cause != null);
    return sqlExceptionList;
  }

  public static String collectExceptionMessages(Throwable cause) {
    StringBuilder message = new StringBuilder(cause.getMessage());

    //collect real SQL exception messages in case of batch processing
    Throwable exCause = cause;
    do {
      if (exCause instanceof BatchExecutorException) {
        final List<SQLException> relatedSqlExceptions = findRelatedSqlExceptions(exCause);
        StringBuilder sb = new StringBuilder();
        for (SQLException sqlException : relatedSqlExceptions) {
          sb.append(sqlException).append("\n");
        }
        message.append("\n").append(sb);
      }
      exCause = exCause.getCause();
    } while (exCause != null);

    return message.toString();
  }

}
