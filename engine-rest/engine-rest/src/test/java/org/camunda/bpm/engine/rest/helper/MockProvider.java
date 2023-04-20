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
package org.camunda.bpm.engine.rest.helper;

import static org.camunda.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityStatistics;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatistics;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricFormField;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.form.CamundaFormRefImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.MetricIntervalEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServerImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.DatabaseImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.JdkImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.MetricImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.ProductImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.SetJobRetriesBuilder;
import org.camunda.bpm.engine.management.SetJobRetriesByJobsAsyncBuilder;
import org.camunda.bpm.engine.management.SetJobRetriesByProcessAsyncBuilder;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Provides mocks for the basic engine entities, such as
 * {@link ProcessDefinition}, {@link User}, etc., that are reused across the
 * various kinds of tests.
 *
 * @author Thorben Lindhauer
 *
 */
public abstract class MockProvider {

  public static final String FORMAT_APPLICATION_JSON = "application/json";

  // general non existing Id
  public static final String NON_EXISTING_ID = "nonExistingId";

  // tenant ids
  public static final String EXAMPLE_TENANT_ID = "aTenantId";
  public static final String ANOTHER_EXAMPLE_TENANT_ID = "anotherTenantId";
  public static final String EXAMPLE_TENANT_ID_LIST = EXAMPLE_TENANT_ID + "," + ANOTHER_EXAMPLE_TENANT_ID;

  public static final String EXAMPLE_TENANT_NAME = "aTenantName";

  // case activity ids
  public static final String EXAMPLE_CASE_ACTIVITY_ID = "aCaseActivityId";
  public static final String ANOTHER_EXAMPLE_CASE_ACTIVITY_ID = "anotherCaseActivityId";
  public static final String EXAMPLE_CASE_ACTIVITY_ID_LIST = EXAMPLE_CASE_ACTIVITY_ID + "," + ANOTHER_EXAMPLE_CASE_ACTIVITY_ID;

  // version tag
  public static final String EXAMPLE_VERSION_TAG = "aVersionTag";
  public static final String ANOTHER_EXAMPLE_VERSION_TAG = "anotherVersionTag";

  // engine
  public static final String EXAMPLE_PROCESS_ENGINE_NAME = "default";
  public static final String ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME = "anotherEngineName";
  public static final String NON_EXISTING_PROCESS_ENGINE_NAME = "aNonExistingEngineName";

  // task properties
  public static final String EXAMPLE_TASK_ID = "anId";
  public static final String EXAMPLE_TASK_NAME = "aName";
  public static final String EXAMPLE_TASK_ASSIGNEE_NAME = "anAssignee";
  public static final String EXAMPLE_TASK_CREATE_TIME = withTimezone("2013-01-23T13:42:42");
  public static final String EXAMPLE_TASK_LAST_UPDATED = withTimezone("2013-01-23T13:42:45");
  public static final String EXAMPLE_TASK_DUE_DATE = withTimezone("2013-01-23T13:42:43");
  public static final String EXAMPLE_FOLLOW_UP_DATE = withTimezone("2013-01-23T13:42:44");
  public static final DelegationState EXAMPLE_TASK_DELEGATION_STATE = DelegationState.RESOLVED;
  public static final String EXAMPLE_TASK_DESCRIPTION = "aDescription";
  public static final String EXAMPLE_TASK_EXECUTION_ID = "anExecution";
  public static final String EXAMPLE_TASK_OWNER = "anOwner";
  public static final String EXAMPLE_TASK_PARENT_TASK_ID = "aParentId";
  public static final int EXAMPLE_TASK_PRIORITY = 42;
  public static final String EXAMPLE_TASK_DEFINITION_KEY = "aTaskDefinitionKey";
  public static final boolean EXAMPLE_TASK_SUSPENSION_STATE = false;

  // task comment
  public static final String EXAMPLE_TASK_COMMENT_ID = "aTaskCommentId";
  public static final String EXAMPLE_TASK_COMMENT_FULL_MESSAGE = "aTaskCommentFullMessage";
  public static final String EXAMPLE_TASK_COMMENT_TIME = withTimezone("2014-04-24T14:10:44");
  public static final String EXAMPLE_TASK_COMMENT_ROOT_PROCESS_INSTANCE_ID = "aRootProcInstId";

  // task attachment
  public static final String EXAMPLE_TASK_ATTACHMENT_ID = "aTaskAttachmentId";
  public static final String EXAMPLE_TASK_ATTACHMENT_NAME = "aTaskAttachmentName";
  public static final String EXAMPLE_TASK_ATTACHMENT_DESCRIPTION = "aTaskAttachmentDescription";
  public static final String EXAMPLE_TASK_ATTACHMENT_TYPE = "aTaskAttachmentType";
  public static final String EXAMPLE_TASK_ATTACHMENT_URL = "aTaskAttachmentUrl";
  public static final String EXAMPLE_TASK_ATTACHMENT_CREATE_DATE = withTimezone("2018-07-19T15:02:36");
  public static final String EXAMPLE_TASK_ATTACHMENT_REMOVAL_DATE = withTimezone("2018-10-17T13:35:07");
  public static final String EXAMPLE_TASK_ATTACHMENT_ROOT_PROCESS_INSTANCE_ID = "aRootProcInstId";

  // task count by candidate group

  public static final int EXAMPLE_TASK_COUNT_BY_CANDIDATE_GROUP = 2;

  // form data
  public static final String EXAMPLE_FORM_KEY = "aFormKey";
  public static final String EXAMPLE_DEPLOYMENT_ID = "aDeploymentId";
  public static final String EXAMPLE_RE_DEPLOYMENT_ID = "aReDeploymentId";
  public static final String EXAMPLE_FORM_REF_BINDING = "version";
  public static final int EXAMPLE_FORM_REF_VERSION = 3;

  // form property data
  public static final String EXAMPLE_FORM_PROPERTY_ID = "aFormPropertyId";
  public static final String EXAMPLE_FORM_PROPERTY_NAME = "aFormName";
  public static final String EXAMPLE_FORM_PROPERTY_TYPE_NAME = "aFormPropertyTypeName";
  public static final String EXAMPLE_FORM_PROPERTY_VALUE = "aValue";
  public static final boolean EXAMPLE_FORM_PROPERTY_READABLE = true;
  public static final boolean EXAMPLE_FORM_PROPERTY_WRITABLE = true;
  public static final boolean EXAMPLE_FORM_PROPERTY_REQUIRED = true;

  // process instance
  public static final String EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY = "aKey";
  public static final String EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY_LIKE = "aKeyLike";
  public static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcInstId";
  public static final String EXAMPLE_ROOT_HISTORIC_PROCESS_INSTANCE_ID = "aRootProcInstId";
  public static final String ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID = "anotherId";
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED = false;
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_ENDED = false;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_DUP = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID = "aNonExistentProcInstId";
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_NONEXISTENT_ID = EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID;

  // variable instance
  public static final String EXAMPLE_VARIABLE_INSTANCE_ID = "aVariableInstanceId";

  public static final String SERIALIZABLE_VARIABLE_INSTANCE_ID = "serializableVariableInstanceId";
  public static final String SPIN_VARIABLE_INSTANCE_ID = "spinVariableInstanceId";

  public static final String EXAMPLE_VARIABLE_INSTANCE_NAME = "aVariableInstanceName";
  public static final String EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME = "aDeserializedVariableInstanceName";

  public static final StringValue EXAMPLE_PRIMITIVE_VARIABLE_VALUE = Variables.stringValue("aVariableInstanceValue");
  public static final String EXAMPLE_VARIABLE_INSTANCE_PROC_DEF_KEY = "aVariableInstanceProcDefKey";
  public static final String EXAMPLE_VARIABLE_INSTANCE_PROC_DEF_ID = "aVariableInstanceProcDefId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID = "aVariableInstanceProcInstId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID = "aVariableInstanceExecutionId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_CASE_INST_ID = "aVariableInstanceCaseInstId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_CASE_EXECUTION_ID = "aVariableInstanceCaseExecutionId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_TASK_ID = "aVariableInstanceTaskId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_BATCH_ID = "aBatchId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID = "aVariableInstanceVariableInstanceId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_ERROR_MESSAGE = "aVariableInstanceErrorMessage";
  public static final String EXAMPLE_VARIABLE_INSTANCE_CASE_DEF_KEY = "aVariableInstanceCaseDefKey";
  public static final String EXAMPLE_VARIABLE_INSTANCE_CASE_DEF_ID = "aVariableInstanceCaseDefId";
  public static final String EXAMPLE_HISTORIC_VARIABLE_INSTANCE_CREATE_TIME = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_VARIABLE_INSTANCE_REMOVAL_TIME = withTimezone("2018-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_VARIABLE_INSTANCE_ROOT_PROC_INST_ID = "aRootProcInstId";

  public static final String EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE = "aSerializedValue";
  public static final byte[] EXAMPLE_VARIABLE_INSTANCE_BYTE = "aSerializedValue".getBytes();
  public static final String EXAMPLE_VARIABLE_INSTANCE_DESERIALIZED_VALUE = "aDeserializedValue";

  public static final String EXAMPLE_SPIN_DATA_FORMAT = "aDataFormatId";
  public static final String EXAMPLE_SPIN_ROOT_TYPE = "path.to.a.RootType";


  // execution
  public static final String EXAMPLE_EXECUTION_ID = "anExecutionId";
  public static final boolean EXAMPLE_EXECUTION_IS_ENDED = false;

  // event subscription
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_ID = "anEventSubscriptionId";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_TYPE = "message";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_NAME = "anEvent";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE = withTimezone("2013-01-23T13:59:43");

  // process definition
  public static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  public static final String NON_EXISTING_PROCESS_DEFINITION_ID = "aNonExistingProcDefId";
  public static final String EXAMPLE_PROCESS_DEFINITION_NAME = "aName";
  public static final String EXAMPLE_PROCESS_DEFINITION_NAME_LIKE = "aNameLike";
  public static final String EXAMPLE_PROCESS_DEFINITION_KEY = "aKey";
  public static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_KEY="anotherProcessDefinitionKey";
  public static final String EXAMPLE_KEY_LIST = EXAMPLE_PROCESS_DEFINITION_KEY + "," + ANOTHER_EXAMPLE_PROCESS_DEFINITION_KEY;

  public static final String NON_EXISTING_PROCESS_DEFINITION_KEY = "aNonExistingKey";
  public static final String EXAMPLE_PROCESS_DEFINITION_CATEGORY = "aCategory";
  public static final String EXAMPLE_PROCESS_DEFINITION_DESCRIPTION = "aDescription";
  public static final int EXAMPLE_PROCESS_DEFINITION_VERSION = 42;
  public static final String EXAMPLE_PROCESS_DEFINITION_VERSION_TAG = "42";
  public static final String EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME = "aResourceName";
  public static final String EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME = "aResourceName.png";
  public static final boolean EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED = true;
  public static final boolean EXAMPLE_PROCESS_DEFINITION_IS_STARTABLE = true;

  public static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId:2";
  public static final String EXAMPLE_PROCESS_DEFINTION_ID_LIST = EXAMPLE_PROCESS_DEFINITION_ID + "," + ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID;

  public static final String EXAMPLE_ACTIVITY_ID = "anActivity";
  public static final String ANOTHER_EXAMPLE_ACTIVITY_ID = "anotherActivity";
  public static final String EXAMPLE_ACTIVITY_ID_LIST = EXAMPLE_ACTIVITY_ID + "," + ANOTHER_EXAMPLE_ACTIVITY_ID;
  public static final String NON_EXISTING_ACTIVITY_ID = "aNonExistingActivityId";
  public static final String EXAMPLE_ACTIVITY_INSTANCE_ID = "anActivityInstanceId";
  public static final String EXAMPLE_ACTIVITY_NAME = "anActivityName";
  public static final String EXAMPLE_ACTIVITY_TYPE = "anActivityType";
  public static final String EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION = withTimezone("2013-04-23T13:42:43");

  // deployment
  public static final String NON_EXISTING_DEPLOYMENT_ID = "aNonExistingDeploymentId";
  public static final String EXAMPLE_DEPLOYMENT_NAME = "aName";
  public static final String EXAMPLE_DEPLOYMENT_NAME_LIKE = "aNameLike";
  public static final String EXAMPLE_DEPLOYMENT_SOURCE = "aDeploymentSource";
  public static final String EXAMPLE_DEPLOYMENT_TIME = withTimezone("2013-01-23T13:59:43");
  public static final String EXAMPLE_DEPLOYMENT_TIME_BEFORE = withTimezone("2013-01-03T13:59:43");
  public static final String EXAMPLE_DEPLOYMENT_TIME_AFTER = withTimezone("2013-03-23T13:59:43");
  public static final String NON_EXISTING_DEPLOYMENT_TIME = withTimezone("2013-04-23T13:42:43");

  // deployment resources
  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_ID = "aDeploymentResourceId";
  public static final String NON_EXISTING_DEPLOYMENT_RESOURCE_ID = "aNonExistingDeploymentResourceId";
  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_NAME = "aDeploymentResourceName";

  public static final String EXAMPLE_DEPLOYMENT_SVG_RESOURCE_ID = "aDeploymentSvgResourceId";
  public static final String EXAMPLE_DEPLOYMENT_SVG_RESOURCE_NAME = "a-svg-resource.svg";

  public static final String EXAMPLE_DEPLOYMENT_PNG_RESOURCE_ID = "aDeploymentPngResourceId";
  public static final String EXAMPLE_DEPLOYMENT_PNG_RESOURCE_NAME = "an-image-resource.png";

  public static final String EXAMPLE_DEPLOYMENT_JPG_RESOURCE_ID = "aDeploymentJpgResourceId";
  public static final String EXAMPLE_DEPLOYMENT_JPG_RESOURCE_NAME = "an-image-resource.jpg";

  public static final String EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_ID = "aDeploymentJpegResourceId";
  public static final String EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_NAME = "an-image-resource.jpeg";

  public static final String EXAMPLE_DEPLOYMENT_JPE_RESOURCE_ID = "aDeploymentJpeResourceId";
  public static final String EXAMPLE_DEPLOYMENT_JPE_RESOURCE_NAME = "an-image-resource.jpe";

  public static final String EXAMPLE_DEPLOYMENT_GIF_RESOURCE_ID = "aDeploymentGifResourceId";
  public static final String EXAMPLE_DEPLOYMENT_GIF_RESOURCE_NAME = "an-image-resource.gif";

  public static final String EXAMPLE_DEPLOYMENT_TIF_RESOURCE_ID = "aDeploymentTifResourceId";
  public static final String EXAMPLE_DEPLOYMENT_TIF_RESOURCE_NAME = "an-image-resource.tif";

  public static final String EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_ID = "aDeploymentTiffResourceId";
  public static final String EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_NAME = "an-image-resource.tiff";

  public static final String EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_ID = "aDeploymentBpmnResourceId";
  public static final String EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_NAME = "a-bpmn-resource.bpmn";

  public static final String EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_ID = "aDeploymentBpmnXmlResourceId";
  public static final String EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_NAME = "a-bpmn-resource.bpmn20.xml";

  public static final String EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_ID = "aDeploymentCmmnResourceId";
  public static final String EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_NAME = "a-cmmn-resource.cmmn";

  public static final String EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_ID = "aDeploymentCmmnXmlResourceId";
  public static final String EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_NAME = "a-cmmn-resource.cmmn10.xml";

  public static final String EXAMPLE_DEPLOYMENT_DMN_RESOURCE_ID = "aDeploymentDmnResourceId";
  public static final String EXAMPLE_DEPLOYMENT_DMN_RESOURCE_NAME = "a-dmn-resource.dmn";

  public static final String EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_ID = "aDeploymentDmnXmlResourceId";
  public static final String EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_NAME = "a-dmn-resource.dmn11.xml";

  public static final String EXAMPLE_DEPLOYMENT_XML_RESOURCE_ID = "aDeploymentXmlResourceId";
  public static final String EXAMPLE_DEPLOYMENT_XML_RESOURCE_NAME = "a-xml-resource.xml";

  public static final String EXAMPLE_DEPLOYMENT_JSON_RESOURCE_ID = "aDeploymentJsonResourceId";
  public static final String EXAMPLE_DEPLOYMENT_JSON_RESOURCE_NAME = "a-json-resource.json";

  public static final String EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_ID = "aDeploymentGroovyResourceId";
  public static final String EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_NAME = "a-groovy-resource.groovy";

  public static final String EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_ID = "aDeploymentGroovyResourceId";
  public static final String EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_NAME = "a-java-resource.java";

  public static final String EXAMPLE_DEPLOYMENT_JS_RESOURCE_ID = "aDeploymentJsResourceId";
  public static final String EXAMPLE_DEPLOYMENT_JS_RESOURCE_NAME = "a-js-resource.js";

  public static final String EXAMPLE_DEPLOYMENT_PHP_RESOURCE_ID = "aDeploymentPhpResourceId";
  public static final String EXAMPLE_DEPLOYMENT_PHP_RESOURCE_NAME = "a-php-resource.php";

  public static final String EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_ID = "aDeploymentPythonResourceId";
  public static final String EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_NAME = "a-python-resource.py";

  public static final String EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_ID = "aDeploymentRubyResourceId";
  public static final String EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_NAME = "a-ruby-resource.rb";

  public static final String EXAMPLE_DEPLOYMENT_HTML_RESOURCE_ID = "aDeploymentHtmlResourceId";
  public static final String EXAMPLE_DEPLOYMENT_HTML_RESOURCE_NAME = "a-html-resource.html";

  public static final String EXAMPLE_DEPLOYMENT_TXT_RESOURCE_ID = "aDeploymentTxtResourceId";
  public static final String EXAMPLE_DEPLOYMENT_TXT_RESOURCE_NAME = "a-txt-resource.txt";

  public static final String EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_ID = "aDeploymentCamundaFormResourceId";
  public static final String EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_NAME = "a-camunda-form-resource.form";

  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID = "aDeploymentResourceFilenameId";
  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_PATH = "my/path/to/my/bpmn/";
  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_PATH_BACKSLASH = "my\\path\\to\\my\\bpmn\\";
  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME = "process.bpmn";

  // statistics
  public static final int EXAMPLE_FAILED_JOBS = 42;
  public static final int EXAMPLE_INSTANCES = 123;

  public static final long EXAMPLE_INSTANCES_LONG = 123;
  public static final long EXAMPLE_FINISHED_LONG = 124;
  public static final long EXAMPLE_CANCELED_LONG = 125;
  public static final long EXAMPLE_COMPLETE_SCOPE_LONG = 126;
  public static final long EXAMPLE_OPEN_INCIDENTS_LONG = 0;
  public static final long EXAMPLE_RESOLVED_INCIDENTS_LONG = 0;
  public static final long EXAMPLE_DELETED_INCIDENTS_LONG = 0;

  public static final long ANOTHER_EXAMPLE_INSTANCES_LONG = 127;
  public static final long ANOTHER_EXAMPLE_FINISHED_LONG = 128;
  public static final long ANOTHER_EXAMPLE_CANCELED_LONG = 129;
  public static final long ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG = 130;
  public static final long ANOTHER_EXAMPLE_OPEN_INCIDENTS_LONG = 1;
  public static final long ANOTHER_EXAMPLE_RESOLVED_INCIDENTS_LONG = 2;
  public static final long ANOTHER_EXAMPLE_DELETED_INCIDENTS_LONG = 3;

  public static final long EXAMPLE_AVAILABLE_LONG = 123;
  public static final long EXAMPLE_ACTIVE_LONG = 124;
  public static final long EXAMPLE_COMPLETED_LONG = 125;
  public static final long EXAMPLE_DISABLED_LONG = 126;
  public static final long EXAMPLE_ENABLED_LONG = 127;
  public static final long EXAMPLE_TERMINATED_LONG = 128;

  public static final long ANOTHER_EXAMPLE_AVAILABLE_LONG = 129;
  public static final long ANOTHER_EXAMPLE_ACTIVE_LONG = 130;
  public static final long ANOTHER_EXAMPLE_COMPLETED_LONG = 131;
  public static final long ANOTHER_EXAMPLE_DISABLED_LONG = 132;
  public static final long ANOTHER_EXAMPLE_ENABLED_LONG = 133;
  public static final long ANOTHER_EXAMPLE_TERMINATED_LONG = 134;

  public static final int ANOTHER_EXAMPLE_FAILED_JOBS = 43;
  public static final int ANOTHER_EXAMPLE_INSTANCES = 124;

  public static final String ANOTHER_EXAMPLE_INCIDENT_TYPE = "anotherIncidentType";
  public static final int ANOTHER_EXAMPLE_INCIDENT_COUNT = 2;

  // user & groups
  public static final String EXAMPLE_GROUP_ID = "groupId1";
  public static final String EXAMPLE_GROUP_ID2 = "groupId2";
  public static final String EXAMPLE_GROUP_NAME = "group1";
  public static final String EXAMPLE_GROUP_TYPE = "organizational-unit";
  public static final String EXAMPLE_GROUP_NAME_UPDATE = "group1Update";

  public static final String EXAMPLE_USER_ID = "userId";
  public static final String EXAMPLE_USER_ID2 = "userId2";
  public static final String EXAMPLE_USER_FIRST_NAME = "firstName";
  public static final String EXAMPLE_USER_LAST_NAME = "lastName";
  public static final String EXAMPLE_USER_EMAIL = "test@example.org";
  public static final String EXAMPLE_USER_PASSWORD = "s3cret";

  public static final String EXAMPLE_USER_FIRST_NAME_UPDATE = "firstNameUpdate";
  public static final String EXAMPLE_USER_LAST_NAME_UPDATE = "lastNameUpdate";
  public static final String EXAMPLE_USER_EMAIL_UPDATE = "testUpdate@example.org";

  // Job Definitions
  public static final String EXAMPLE_JOB_DEFINITION_ID = "aJobDefId";
  public static final String NON_EXISTING_JOB_DEFINITION_ID = "aNonExistingJobDefId";
  public static final String EXAMPLE_JOB_TYPE = "aJobType";
  public static final String EXAMPLE_JOB_CONFIG = "aJobConfig";
  public static final boolean EXAMPLE_JOB_DEFINITION_IS_SUSPENDED = true;
  public static final String EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION = withTimezone("2013-04-23T13:42:43");
  public static final long EXAMPLE_JOB_DEFINITION_PRIORITY = Integer.MAX_VALUE + 52l;
  public static final String EXAMPLE_JOB_DEFINITION_DEPLOYMENT_ID = "aDeploymentId";

  // Jobs
  public static final String EXAMPLE_JOB_ACTIVITY_ID = "aJobActivityId";
  public static final String EXAMPLE_JOB_ID = "aJobId";
  public static final String ANOTHER_EXAMPLE_JOB_ID = "anotherJobId";
  public static final String EXAMPLE_JOB_ID_LIST = EXAMPLE_JOB_ID + "," + ANOTHER_EXAMPLE_JOB_ID;
  public static final String NON_EXISTING_JOB_ID = "aNonExistingJobId";
  public static final int EXAMPLE_NEGATIVE_JOB_RETRIES = -3;
  public static final int EXAMPLE_JOB_RETRIES = 3;
  public static final String EXAMPLE_JOB_NO_EXCEPTION_MESSAGE = "";
  public static final String EXAMPLE_EXCEPTION_MESSAGE = "aExceptionMessage";
  public static final String EXAMPLE_JOB_FAILED_ACTIVITY_ID = "aFailedJobActivityId";
  public static final String EXAMPLE_EMPTY_JOB_ID = "";
  public static final String EXAMPLE_DUE_DATE =  withTimezone("2013-04-23T13:42:43");
  public static final Boolean EXAMPLE_WITH_RETRIES_LEFT = true;
  public static final Boolean EXAMPLE_EXECUTABLE = true;
  public static final Boolean EXAMPLE_TIMERS = true;
  public static final Boolean EXAMPLE_MESSAGES = true;
  public static final Boolean EXAMPLE_WITH_EXCEPTION = true;
  public static final Boolean EXAMPLE_NO_RETRIES_LEFT = true;
  public static final Boolean EXAMPLE_JOB_IS_SUSPENDED = true;
  public static final long EXAMPLE_JOB_PRIORITY = Integer.MAX_VALUE + 42l;

  public static final String EXAMPLE_RESOURCE_TYPE_NAME = "exampleResource";
  public static final int EXAMPLE_RESOURCE_TYPE_ID = 12345678;
  public static final String EXAMPLE_RESOURCE_TYPE_ID_STRING = "12345678";
  public static final String EXAMPLE_RESOURCE_ID = "exampleResourceId";
  public static final String EXAMPLE_PERMISSION_NAME = "READ";
  public static final Permission[] EXAMPLE_GRANT_PERMISSION_VALUES = new Permission[] { Permissions.NONE, Permissions.READ, Permissions.UPDATE };
  public static final Permission[] EXAMPLE_REVOKE_PERMISSION_VALUES = new Permission[] { Permissions.ALL, Permissions.READ, Permissions.UPDATE };
  public static final String[] EXAMPLE_PERMISSION_VALUES_STRING = new String[] { "READ", "UPDATE" };
  public static final String EXAMPLE_AUTH_REMOVAL_TIME = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_AUTH_ROOT_PROCESS_INSTANCE_ID = "aRootProcessInstanceId";

  public static final String EXAMPLE_AUTHORIZATION_ID = "someAuthorizationId";
  public static final int EXAMPLE_AUTHORIZATION_TYPE = 0;
  public static final String EXAMPLE_AUTHORIZATION_TYPE_STRING = "0";

  // process applications
  public static final String EXAMPLE_PROCESS_APPLICATION_NAME = "aProcessApplication";
  public static final String EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH = "http://camunda.org/someContext";

  // Historic Process Instance
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON = "aDeleteReason";
  public static final long EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS = 2000l;
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_REMOVAL_TIME = withTimezone("2013-04-26T13:42:43");
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID = "aStartUserId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID = "aStartActivityId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_ROOT_PROCESS_INSTANCE_ID = "aRootProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID = "aSuperProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_CASE_INSTANCE_ID = "aSuperCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUB_PROCESS_INSTANCE_ID = "aSubProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID = "aCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUB_CASE_INSTANCE_ID = "aSubCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_STATE = "aState";

  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE = withTimezone("2013-01-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_AFTER = withTimezone("2013-01-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_BEFORE = withTimezone("2013-04-23T13:42:43");

  // historic process instance duration report
  public static final long EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG = 10;
  public static final long EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN = 5;
  public static final long EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX = 15;
  public static final int EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_PERIOD = 1;

  // Historic Case Instance
  public static final long EXAMPLE_HISTORIC_CASE_INSTANCE_DURATION_MILLIS = 2000l;
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_TIME = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSE_TIME = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_USER_ID = "aCreateUserId";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID = "aSuperCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_SUB_CASE_INSTANCE_ID = "aSubCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_PROCESS_INSTANCE_ID = "aSuperProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_SUB_PROCESS_INSTANCE_ID = "aSuperProcessInstanceId";

  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE = withTimezone("2013-01-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER = withTimezone("2013-01-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE = withTimezone("2013-04-23T13:42:43");

  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_ACTIVE = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_COMPLETED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_TERMINATED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_CLOSED = true;

  // Historic Activity Instance
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID = "aHistoricActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID = "aHistoricParentActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID = "aHistoricCalledProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID = "aHistoricCalledCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME = withTimezone("2013-04-23T18:42:43");
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_REMOVAL_TIME = withTimezone("2013-04-23T13:42:43");
  public static final long EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_DURATION = 2000l;
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_AFTER = withTimezone("2013-04-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_BEFORE = withTimezone("2013-01-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_AFTER = withTimezone("2013-01-23T13:42:43");
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_BEFORE = withTimezone("2013-04-23T13:42:43");
  public static final boolean EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED = true;
  public static final boolean EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE = true;
  public static final String EXAMPLE_HISTORIC_ACTIVITY_ROOT_PROCESS_INSTANCE_ID = "aRootProcInstId";
  public static final long EXAMPLE_HISTORIC_ACTIVITY_SEQUENCE_COUNTER = 10L;

  // Historic Case Activity Instance
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID = "aCaseActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_ANOTHER_CASE_ACTIVITY_INSTANCE_ID = "anotherCaseActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_PARENT_CASE_ACTIVITY_INSTANCE_ID = "aParentCaseActivityId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_ID = "aCaseActivityId";
  public static final String EXAMPLE_HISTORIC_ANOTHER_CASE_ACTIVITY_ID = "anotherCaseActivityId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_NAME = "aCaseActivityName";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_TYPE = "aCaseActivityType";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID = "aCalledProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID = "aCalledCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME = withTimezone("2014-04-23T18:42:42");
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME = withTimezone("2014-04-23T18:42:43");
  public static final long EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION = 2000l;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_REQUIRED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_AVAILABLE = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ENABLED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_DISABLED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ACTIVE = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_FAILED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_SUSPENDED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_COMPLETED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_TERMINATED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_UNFINISHED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_FINISHED = true;

  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_AFTER = withTimezone("2014-04-23T18:41:42");
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_BEFORE = withTimezone("2014-04-23T18:43:42");
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_AFTER = withTimezone("2014-04-23T18:41:43");
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_BEFORE = withTimezone("2014-04-23T18:43:43");

  // user operation log
  public static final String EXAMPLE_USER_OPERATION_LOG_ID = "userOpLogId";
  public static final String EXAMPLE_USER_OPERATION_ID = "opId";
  public static final String EXAMPLE_USER_OPERATION_TYPE = UserOperationLogEntry.OPERATION_TYPE_CLAIM;
  public static final String EXAMPLE_USER_OPERATION_ENTITY = EntityTypes.TASK;
  public static final String EXAMPLE_USER_OPERATION_PROPERTY = "opProperty";
  public static final String EXAMPLE_USER_OPERATION_ORG_VALUE = "orgValue";
  public static final String EXAMPLE_USER_OPERATION_NEW_VALUE = "newValue";
  public static final String EXAMPLE_USER_OPERATION_TIMESTAMP = withTimezone("2014-02-20T16:53:37");
  public static final String EXAMPLE_USER_OPERATION_ANNOTATION = "anAnnotation";

  // historic detail
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ID = "aHistoricVariableUpdateId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_PROC_DEF_KEY = "aProcDefKey";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID = "aProcInst";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID = "anActInst";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID = "anExecutionId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_OPERATION_ID = "anOperationId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID = "aTaskId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TIME = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_NAME = "aVariableName";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TYPE_NAME = "String";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_VALUE_TYPE_NAME = "String";
  public static final int EXAMPLE_HISTORIC_VAR_UPDATE_REVISION = 1;
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ERROR = "anErrorMessage";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_VAR_INST_ID = "aVariableInstanceId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_CASE_DEF_KEY = "aCaseDefKey";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_CASE_DEF_ID = "aCaseDefId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_CASE_INST_ID = "aCaseInstId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_CASE_EXEC_ID = "aCaseExecId";

  public static final String EXAMPLE_HISTORIC_FORM_FIELD_ID = "anId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_PROC_DEF_KEY = "aProcDefKey";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID = "aProcInst";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID = "anActInst";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID = "anExecutionId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_OPERATION_ID = "anOperationId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID = "aTaskId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_TIME = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID = "aFormFieldId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_VALUE = "aFormFieldValue";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_CASE_DEF_KEY = "aCaseDefKey";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_CASE_DEF_ID = "aCaseDefId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_CASE_INST_ID = "aCaseInstId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_CASE_EXEC_ID = "aCaseExecId";
  public static final String EXAMPLE_HISTORIC_FORM_ROOT_PROCESS_INSTANCE_ID = "aRootProcInstId";

  // historic task instance
  public static final String EXAMPLE_HISTORIC_TASK_INST_ID = "aHistoricTaskInstanceId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_KEY = "aProcDefKey";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_INST_BUSINESS_KEY = "aBusinessKey";
  public static final String EXAMPLE_HISTORIC_TASK_INST_EXEC_ID = "anExecId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID = "anActInstId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_NAME = "aName";
  public static final String EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION = "aDescription";
  public static final String EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON = "aDeleteReason";
  public static final String EXAMPLE_HISTORIC_TASK_INST_OWNER = "anOwner";
  public static final String EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE = "anAssignee";
  public static final String EXAMPLE_HISTORIC_TASK_INST_START_TIME = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_TASK_INST_END_TIME = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_TASK_INST_REMOVAL_TIME = withTimezone("2018-01-01T00:00:00");
  public static final Long EXAMPLE_HISTORIC_TASK_INST_DURATION = 5000L;
  public static final String EXAMPLE_HISTORIC_TASK_INST_DEF_KEY = "aTaskDefinitionKey";
  public static final int EXAMPLE_HISTORIC_TASK_INST_PRIORITY = 60;
  public static final String EXAMPLE_HISTORIC_TASK_INST_DUE_DATE = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID = "aParentTaskId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_KEY = "aCaseDefinitionKey";
  public static final String EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_ID = "aCaseDefinitionId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_CASE_INST_ID = "aCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_CASE_EXEC_ID = "aCaseExecutionId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_TASK_INVOLVED_USER = "aUserId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_TASK_INVOLVED_GROUP = "aGroupId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_TASK_HAD_CANDIDATE_USER = "cUserId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_TASK_HAD_CANDIDATE_GROUP = "cGroupId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_ROOT_PROC_INST_ID = "aRootProcInstId";

  // Incident
  public static final String EXAMPLE_INCIDENT_ID = "anIncidentId";
  public static final String EXAMPLE_INCIDENT_TIMESTAMP = withTimezone("2014-01-02T00:00:00");
  public static final String EXAMPLE_INCIDENT_TIMESTAMP_AFTER = withTimezone("2014-01-03T00:00:00");
  public static final String EXAMPLE_INCIDENT_TIMESTAMP_BEFORE = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_INCIDENT_TYPE = "anIncidentType";
  public static final String EXAMPLE_INCIDENT_EXECUTION_ID = "anExecutionId";
  public static final String EXAMPLE_INCIDENT_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_INCIDENT_FAILED_ACTIVITY_ID = "aFailedActivityId";
  public static final String EXAMPLE_INCIDENT_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_INCIDENT_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_INCIDENT_CAUSE_INCIDENT_ID = "aCauseIncidentId";
  public static final String EXAMPLE_INCIDENT_ROOT_CAUSE_INCIDENT_ID = "aRootCauseIncidentId";
  public static final String EXAMPLE_INCIDENT_CONFIGURATION = "aConfiguration";
  public static final String EXAMPLE_INCIDENT_MESSAGE = "anIncidentMessage";
  public static final String EXAMPLE_INCIDENT_MESSAGE_LIKE = "%anIncidentMessageLike%";
  public static final int EXAMPLE_INCIDENT_COUNT = 1;

  // Historic Incident
  public static final String EXAMPLE_HIST_INCIDENT_ID = "anIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_CREATE_TIME = withTimezone("2014-01-02T00:00:00");
  public static final String EXAMPLE_HIST_INCIDENT_CREATE_TIME_AFTER = withTimezone("2014-01-03T00:00:00");
  public static final String EXAMPLE_HIST_INCIDENT_CREATE_TIME_BEFORE = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HIST_INCIDENT_END_TIME = withTimezone("2014-01-02T00:00:00");
  public static final String EXAMPLE_HIST_INCIDENT_END_TIME_AFTER = withTimezone("2014-01-03T00:00:00");
  public static final String EXAMPLE_HIST_INCIDENT_END_TIME_BEFORE = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HIST_INCIDENT_REMOVAL_TIME = withTimezone("2018-01-01T00:00:00");
  public static final String EXAMPLE_HIST_INCIDENT_TYPE = "anIncidentType";
  public static final String EXAMPLE_HIST_INCIDENT_EXECUTION_ID = "anExecutionId";
  public static final String EXAMPLE_HIST_INCIDENT_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_HIST_INCIDENT_FAILED_ACTIVITY_ID = "aFailedActivityId";
  public static final String EXAMPLE_HIST_INCIDENT_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HIST_INCIDENT_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HIST_INCIDENT_PROC_DEF_KEY = "aProcDefKey";
  public static final String EXAMPLE_HIST_INCIDENT_ROOT_PROC_INST_ID = "aRootProcInstId";
  public static final String EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID = "aCauseIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID = "aRootCauseIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_CONFIGURATION = "aConfiguration";
  public static final String EXAMPLE_HIST_INCIDENT_HISTORY_CONFIGURATION = "aHistoryConfiguration";
  public static final String EXAMPLE_HIST_INCIDENT_MESSAGE = "anIncidentMessage";
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_OPEN = false;
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_DELETED = false;
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_RESOLVED = true;

  // Historic Identity Link
  public static final String EXAMPLE_HIST_IDENTITY_LINK_TYPE = "assignee";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_OPERATION_TYPE = "add";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_TIME = withTimezone("2014-01-05T00:00:00");
  public static final String EXAMPLE_HIST_IDENTITY_LINK_REMOVAL_TIME = withTimezone("2018-01-05T00:00:00");
  public static final String EXAMPLE_HIST_IDENTITY_LINK_DATE_BEFORE = withTimezone("2014-01-01T00:00:00");
  public static final String EXAMPLE_HIST_IDENTITY_LINK_DATE_AFTER = withTimezone("2014-01-06T00:00:00");
  public static final String EXAMPLE_HIST_IDENTITY_LINK_ASSIGNER_ID = "aAssignerId";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_TASK_ID = "aTaskId";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_USER_ID = "aUserId";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_GROUP_ID = "aGroupId";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_ID = "aProcDefId";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_KEY = "aProcDefKey";
  public static final String EXAMPLE_HIST_IDENTITY_LINK_ROOT_PROC_INST_ID = "aRootProcInstId";

  // case definition
  public static final String EXAMPLE_CASE_DEFINITION_ID = "aCaseDefnitionId";
  public static final String ANOTHER_EXAMPLE_CASE_DEFINITION_ID = "anotherCaseDefnitionId";
  public static final String EXAMPLE_CASE_DEFINITION_ID_LIST = EXAMPLE_CASE_DEFINITION_ID + "," + ANOTHER_EXAMPLE_CASE_DEFINITION_ID;
  public static final String EXAMPLE_CASE_DEFINITION_KEY = "aCaseDefinitionKey";
  public static final int EXAMPLE_CASE_DEFINITION_VERSION = 1;
  public static final String EXAMPLE_CASE_DEFINITION_CATEGORY = "aCaseDefinitionCategory";
  public static final String EXAMPLE_CASE_DEFINITION_NAME = "aCaseDefinitionName";
  public static final String EXAMPLE_CASE_DEFINITION_NAME_LIKE = "aCaseDefinitionNameLike";
  public static final String EXAMPLE_CASE_DEFINITION_RESOURCE_NAME = "aCaseDefinitionResourceName";
  public static final String EXAMPLE_CASE_DEFINITION_DIAGRAM_RESOURCE_NAME = "aResourceName.png";

  // case instance
  public static final String EXAMPLE_CASE_INSTANCE_ID = "aCaseInstId";
  public static final String EXAMPLE_CASE_INSTANCE_BUSINESS_KEY = "aBusinessKey";
  public static final String EXAMPLE_CASE_INSTANCE_BUSINESS_KEY_LIKE = "aBusinessKeyLike";
  public static final String EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID = "aCaseDefinitionId";
  public static final boolean EXAMPLE_CASE_INSTANCE_IS_ACTIVE = true;
  public static final boolean EXAMPLE_CASE_INSTANCE_IS_COMPLETED = true;
  public static final boolean EXAMPLE_CASE_INSTANCE_IS_TERMINATED = true;

  // case execution
  public static final String EXAMPLE_CASE_EXECUTION_ID = "aCaseExecutionId";
  public static final String ANOTHER_EXAMPLE_CASE_EXECUTION_ID = "anotherCaseExecutionId";
  public static final String EXAMPLE_CASE_EXECUTION_CASE_INSTANCE_ID = "aCaseInstanceId";
  public static final String EXAMPLE_CASE_EXECUTION_PARENT_ID = "aParentId";
  public static final String EXAMPLE_CASE_EXECUTION_CASE_DEFINITION_ID = "aCaseDefinitionId";
  public static final String EXAMPLE_CASE_EXECUTION_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_CASE_EXECUTION_ACTIVITY_NAME = "anActivityName";
  public static final String EXAMPLE_CASE_EXECUTION_ACTIVITY_TYPE = "anActivityType";
  public static final String EXAMPLE_CASE_EXECUTION_ACTIVITY_DESCRIPTION = "anActivityDescription";
  public static final boolean EXAMPLE_CASE_EXECUTION_IS_REQUIRED = true;
  public static final boolean EXAMPLE_CASE_EXECUTION_IS_ENABLED = true;
  public static final boolean EXAMPLE_CASE_EXECUTION_IS_ACTIVE = true;
  public static final boolean EXAMPLE_CASE_EXECUTION_IS_DISABLED = true;

  // filter
  public static final String EXAMPLE_FILTER_ID = "aFilterId";
  public static final String ANOTHER_EXAMPLE_FILTER_ID = "anotherFilterId";
  public static final String EXAMPLE_FILTER_RESOURCE_TYPE = EntityTypes.TASK;
  public static final String EXAMPLE_FILTER_NAME = "aFilterName";
  public static final String EXAMPLE_FILTER_OWNER = "aFilterOwner";
  public static final Query EXAMPLE_FILTER_QUERY = new TaskQueryImpl().taskName("test").processVariableValueEquals("foo", "bar").caseInstanceVariableValueEquals("foo", "bar").taskVariableValueEquals("foo", "bar");
  public static final TaskQueryDto EXAMPLE_FILTER_QUERY_DTO = TaskQueryDto.fromQuery(EXAMPLE_FILTER_QUERY);
  public static final Map<String, Object> EXAMPLE_FILTER_PROPERTIES = Collections.singletonMap("color", (Object) "#112233");

  // decision definition
  public static final String EXAMPLE_DECISION_DEFINITION_ID_IN = "aDecisionDefinitionId,anotherDecisionDefinitionId";
  public static final String EXAMPLE_DECISION_DEFINITION_ID = "aDecisionDefinitionId";
  public static final String ANOTHER_EXAMPLE_DECISION_DEFINITION_ID = "anotherDecisionDefinitionId";
  public static final String EXAMPLE_DECISION_DEFINITION_ID_LIST = EXAMPLE_DECISION_DEFINITION_ID + "," + ANOTHER_EXAMPLE_DECISION_DEFINITION_ID;
  public static final String EXAMPLE_DECISION_DEFINITION_KEY = "aDecisionDefinitionKey";
  public static final String ANOTHER_DECISION_DEFINITION_KEY = "anotherDecisionDefinitionKey";
  public static final String EXAMPLE_DECISION_DEFINITION_KEY_IN = "aDecisionDefinitionKey,anotherDecisionDefinitionKey";
  public static final int EXAMPLE_DECISION_DEFINITION_VERSION = 1;
  public static final String EXAMPLE_DECISION_DEFINITION_CATEGORY = "aDecisionDefinitionCategory";
  public static final String EXAMPLE_DECISION_DEFINITION_NAME = "aDecisionDefinitionName";
  public static final String EXAMPLE_DECISION_DEFINITION_NAME_LIKE = "aDecisionDefinitionNameLike";
  public static final String EXAMPLE_DECISION_DEFINITION_RESOURCE_NAME = "aDecisionDefinitionResourceName";
  public static final String EXAMPLE_DECISION_DEFINITION_DIAGRAM_RESOURCE_NAME = "aResourceName.png";

  public static final String EXAMPLE_DECISION_OUTPUT_KEY = "aDecisionOutput";
  public static final StringValue EXAMPLE_DECISION_OUTPUT_VALUE = Variables.stringValue("aDecisionOutputValue");

  // decision requirement definition
  public static final String EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID = "aDecisionRequirementsDefinitionId";
  public static final String EXAMPLE_DECISION_INSTANCE_ID = "aDecisionInstanceId";
  public static final String ANOTHER_EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID = "anotherDecisionRequirementsDefinitionId";
  public static final String EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID_LIST = EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID + "," + ANOTHER_EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID;
  public static final String EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY = "aDecisionRequirementsDefinitionKey";
  public static final int EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_VERSION = 1;
  public static final String EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_CATEGORY = "aDecisionRequirementsDefinitionCategory";
  public static final String EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_NAME = "aDecisionRequirementsDefinitionName";
  public static final String EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_RESOURCE_NAME = "aDecisionRequirementsDefinitionResourceName";
  public static final String EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_DIAGRAM_RESOURCE_NAME = "aResourceName.png";

  // historic job log

  public static final String EXAMPLE_HISTORIC_JOB_LOG_ID = "aHistoricJobLogId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_TIMESTAMP = withTimezone(withTimezone("2015-01-01T00:00:00"));
  public static final String EXAMPLE_HISTORIC_JOB_LOG_REMOVAL_TIME = withTimezone(withTimezone("2018-01-01T00:00:00"));

  public static final String EXAMPLE_HISTORIC_JOB_LOG_JOB_ID = "aJobId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_JOB_DUE_DATE = withTimezone("2015-10-01T00:00:00");
  public static final int EXAMPLE_HISTORIC_JOB_LOG_JOB_RETRIES = 5;
  public static final long EXAMPLE_HISTORIC_JOB_LOG_JOB_PRIORITY = Integer.MAX_VALUE + 42l;
  public static final String EXAMPLE_HISTORIC_JOB_LOG_JOB_EXCEPTION_MSG = "aJobExceptionMsg";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_ID = "aJobDefId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_TYPE = "aJobDefType";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_CONFIG = "aJobDefConfig";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_ACTIVITY_ID = "anActId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_FAILED_ACTIVITY_ID = "aFailedActId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_EXECUTION_ID = "anExecId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_PROC_DEF_KEY = "aProcDefKey";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_DEPLOYMENT_ID = "aDeploymentId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_ROOT_PROC_INST_ID = "aRootProcInstId";
  public static final String EXAMPLE_HISTORIC_JOB_LOG_HOSTNAME = "aHostname";
  public static final boolean EXAMPLE_HISTORIC_JOB_LOG_IS_CREATION_LOG= true;
  public static final boolean EXAMPLE_HISTORIC_JOB_LOG_IS_FAILURE_LOG = true;
  public static final boolean EXAMPLE_HISTORIC_JOB_LOG_IS_SUCCESS_LOG = true;
  public static final boolean EXAMPLE_HISTORIC_JOB_LOG_IS_DELETION_LOG = true;

  // historic decision instance
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_ID = "aHistoricDecisionInstanceId";
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_ID_IN = "aHistoricDecisionInstanceId,anotherHistoricDecisionInstanceId";
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID = "aHistoricDecisionInstanceActivityId";
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID_IN = "aHistoricDecisionInstanceActivityId,anotherHistoricDecisionInstanceActivityId";
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID = "aHistoricDecisionInstanceActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID_IN = "aHistoricDecisionInstanceActivityInstanceId,anotherHistoricDecisionInstanceActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATION_TIME = withTimezone("2015-09-07T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_REMOVAL_TIME = withTimezone("2015-09-10T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATED_BEFORE = withTimezone("2015-09-08T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATED_AFTER = withTimezone("2015-09-06T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_INSTANCE_USER_ID = "aUserId";
  public static final Double EXAMPLE_HISTORIC_DECISION_INSTANCE_COLLECT_RESULT_VALUE = 42.0;
  public static final String EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_ID = "aDecisionInputInstanceId";
  public static final String EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CLAUSE_ID = "aDecisionInputClauseId";
  public static final String EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CLAUSE_NAME = "aDecisionInputClauseName";
  public static final String EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CREATE_TIME = withTimezone("2015-09-06T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_REMOVAL_TIME = withTimezone("2015-10-18T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_INPUT_ROOT_PROCESS_INSTANCE_ID = "aRootProcInstId";
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_ID = "aDecisionInputInstanceId";
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_VARIABLE_NAME = "aDecisionInputInstanceName";
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CLAUSE_ID = "aDecisionInputClauseId";
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CLAUSE_NAME = "aDecisionInputClauseName";
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_RULE_ID = "aDecisionInputRuleId";
  public static final Integer EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_RULE_ORDER = 12;
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CREATE_TIME = withTimezone("2015-09-06T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_REMOVAL_TIME = withTimezone("2015-10-18T11:00:00");
  public static final String EXAMPLE_HISTORIC_DECISION_OUTPUT_ROOT_PROCESS_INSTANCE_ID = "aRootProcInstId";
  public static final ObjectValue EXAMPLE_HISTORIC_DECISION_SERIALIZED_VALUE = MockObjectValue.fromObjectValue(Variables.objectValue("test").serializationDataFormat("aDataFormat").create()).objectTypeName("aTypeName");
  public static final BytesValue EXAMPLE_HISTORIC_DECISION_BYTE_ARRAY_VALUE = Variables.byteArrayValue("test".getBytes());
  public static final StringValue EXAMPLE_HISTORIC_DECISION_STRING_VALUE = Variables.stringValue("test");

  // metrics
  public static final String EXAMPLE_METRICS_START_DATE = withTimezone("2015-01-01T00:00:00");
  public static final String EXAMPLE_METRICS_END_DATE = withTimezone("2015-02-01T00:00:00");
  public static final String EXAMPLE_METRICS_REPORTER = "REPORTER";
  public static final String EXAMPLE_METRICS_NAME = "metricName";

  // external task
  public static final String EXTERNAL_TASK_ID = "anExternalTaskId";
  public static final String EXTERNAL_TASK_ANOTHER_ID = "anotherExternalTaskId";
  public static final String EXTERNAL_TASK_ID_LIST = EXTERNAL_TASK_ID + "," + EXTERNAL_TASK_ANOTHER_ID;
  public static final String EXTERNAL_TASK_ERROR_MESSAGE = "some error";
  public static final String EXTERNAL_TASK_LOCK_EXPIRATION_TIME = withTimezone("2015-10-05T13:25:00");
  public static final Integer EXTERNAL_TASK_RETRIES = new Integer(5);
  public static final boolean EXTERNAL_TASK_SUSPENDED = true;
  public static final String EXTERNAL_TASK_TOPIC_NAME = "aTopic";
  public static final String EXTERNAL_TASK_WORKER_ID = "aWorkerId";
  public static final long EXTERNAL_TASK_PRIORITY = Integer.MAX_VALUE + 466L;

  // batch
  public static final String EXAMPLE_BATCH_ID = "aBatchId";
  public static final String EXAMPLE_BATCH_TYPE = "aBatchType";
  public static final int EXAMPLE_BATCH_TOTAL_JOBS = 10;
  public static final int EXAMPLE_BATCH_JOBS_CREATED = 9;
  public static final int EXAMPLE_BATCH_JOBS_PER_SEED = 11;
  public static final int EXAMPLE_INVOCATIONS_PER_BATCH_JOB = 12;
  public static final String EXAMPLE_SEED_JOB_DEFINITION_ID = "aSeedJobDefinitionId";
  public static final String EXAMPLE_MONITOR_JOB_DEFINITION_ID = "aMonitorJobDefinitionId";
  public static final String EXAMPLE_BATCH_JOB_DEFINITION_ID = "aBatchJobDefinitionId";
  public static final String EXAMPLE_HISTORIC_BATCH_START_TIME = withTimezone("2016-04-12T15:29:33");
  public static final String EXAMPLE_HISTORIC_BATCH_END_TIME = withTimezone("2016-04-12T16:23:34");
  public static final String EXAMPLE_HISTORIC_BATCH_REMOVAL_TIME = withTimezone("2016-04-12T16:23:34");
  public static final int EXAMPLE_BATCH_REMAINING_JOBS = 21;
  public static final int EXAMPLE_BATCH_COMPLETED_JOBS = 22;
  public static final int EXAMPLE_BATCH_FAILED_JOBS = 23;

  // tasks
  public static final Long EXAMPLE_HISTORIC_TASK_REPORT_COUNT = 12L;
  public static final String EXAMPLE_HISTORIC_TASK_REPORT_DEFINITION = "aTaskDefinition";
  public static final String EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEFINITION = "aProcessDefinition";
  public static final String EXAMPLE_HISTORIC_TASK_START_TIME = withTimezone("2016-04-12T15:29:33");
  public static final String EXAMPLE_HISTORIC_TASK_END_TIME = withTimezone("2016-04-12T16:23:34");
  public static final String EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEF_ID = "aProcessDefinitionId:1:1";
  public static final String EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEF_NAME = "aProcessDefinitionName";
  public static final String EXAMPLE_HISTORIC_TASK_REPORT_TASK_NAME = "aTaskName";

  // historic task instance duration report
  public static final long EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_AVG = 10;
  public static final long EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MIN = 5;
  public static final long EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MAX = 15;
  public static final int EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_PERIOD = 1;

  // historic external task log
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID = "aHistoricExternalTaskLogId";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TIMESTAMP = withTimezone("2015-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_REMOVAL_TIME = withTimezone("2018-01-01T00:00:00");
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXTERNAL_TASK_ID = "anExternalTaskId";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TOPIC_NAME = "aTopicName";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_WORKER_ID = "aWorkerId";
  public static final int EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_RETRIES = 5;
  public static final long EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PRIORITY = Integer.MAX_VALUE + 42l;
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ERROR_MSG = "aEXTERNAL_TASKExceptionMsg";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_ID = "anActId";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_INSTANCE_ID = "anActInstanceId";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXECUTION_ID = "anExecId";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_KEY = "aProcDefKey";
  public static final String EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ROOT_PROC_INST_ID = "aRootProcInstId";
  public static final boolean EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_CREATION_LOG= true;
  public static final boolean EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_FAILURE_LOG = true;
  public static final boolean EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_SUCCESS_LOG = true;
  public static final boolean EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_DELETION_LOG = true;
  public static final String EXAMPLE_JOB_CREATE_TIME = withTimezone("2015-01-01T00:00:00");

  // problems
  public static final int EXAMPLE_PROBLEM_LINE = 77;
  public static final int EXAMPLE_PROBLEM_COLUMN = 78;
  public static final String EXAMPLE_PROBLEM_ELEMENT_ID = "element_79";
  public static final int EXAMPLE_PROBLEM_LINE_2 = 87;
  public static final int EXAMPLE_PROBLEM_COLUMN_2 = 88;
  public static final String EXAMPLE_PROBLEM_ELEMENT_ID_2 = "element_89";
  public static final String EXAMPLE_RESOURCE_NAME = "abc";
  public static final List<String> EXAMPLE_ELEMENT_IDS = Arrays.asList(EXAMPLE_PROBLEM_ELEMENT_ID, EXAMPLE_PROBLEM_ELEMENT_ID_2);

  // Telemetry
  public static final String EXAMPLE_TELEMETRY_INSTALLATION_ID = "8343cc7a-8ad1-42d4-97d2-43452c0bdfa3";
  public static final String EXAMPLE_TELEMETRY_PRODUCT_NAME = "Camunda BPM Runtime";
  public static final String EXAMPLE_TELEMETRY_PRODUCT_VERSION = "7.14.0";
  public static final String EXAMPLE_TELEMETRY_PRODUCT_EDITION = "enterprise";
  public static final String EXAMPLE_TELEMETRY_DB_VENDOR = "h2";
  public static final String EXAMPLE_TELEMETRY_DB_VERSION = "1.4.190 (2015-10-11)";
  public static final String EXAMPLE_TELEMETRY_APP_SERVER_VENDOR = "Wildfly";
  public static final String EXAMPLE_TELEMETRY_APP_SERVER_VERSION = "WildFly Full 19.0.0.Final (WildFly Core 11.0.0.Final) - 2.0.30.Final";
  public static final String EXAMPLE_TELEMETRY_TELEMETRY_CONFIGURE_CMD = "TelemetryConfigureCmd";
  public static final String EXAMPLE_TELEMETRY_IS_TELEMETRY_ENABLED_CMD = "IsTelemetryEnabledCmd";
  public static final String EXAMPLE_TELEMETRY_GET_TELEMETRY_DATA_CMD = "GetTelemetryDataCmd";
  public static final String EXAMPLE_TELEMETRY_LICENSE_CUSTOMER_NAME = "customer name";
  public static final String EXAMPLE_TELEMETRY_LICENSE_TYPE = "UNIFIED";
  public static final String EXAMPLE_TELEMETRY_LICENSE_VALID_UNTIL = "2022-09-30";
  public static final boolean EXAMPLE_TELEMETRY_LICENSE_UNLIMITED = false;
  public static final Map<String, String> EXAMPLE_TELEMETRY_LICENSE_FEATURES = Collections.singletonMap("camundaBPM", "true");
  public static final String EXAMPLE_TELEMETRY_LICENSE_RAW = "customer=customer name;expiryDate=2022-09-30;camundaBPM=true;optimize=false;cawemo=false";
  public static final String EXAMPLE_TELEMETRY_JDK_VERSION = "14.0.2";
  public static final String EXAMPLE_TELEMETRY_JDK_VENDOR = "Oracle Corporation";
  public static final String EXAMPLE_TELEMETRY_DATA_COLLECTION_START_DATE = withTimezone("2022-01-01T00:00:00");

  public static final DatabaseImpl EXAMPLE_TELEMETRY_DATABASE = new DatabaseImpl(EXAMPLE_TELEMETRY_DB_VENDOR,
      EXAMPLE_TELEMETRY_DB_VERSION);
  public static final ApplicationServerImpl EXAMPLE_TELEMETRY_SERVER = new ApplicationServerImpl(
      EXAMPLE_TELEMETRY_APP_SERVER_VENDOR, EXAMPLE_TELEMETRY_APP_SERVER_VERSION);
  public static final LicenseKeyDataImpl EXAMPLE_TELEMETRY_LICENSE = new LicenseKeyDataImpl(
      EXAMPLE_TELEMETRY_LICENSE_CUSTOMER_NAME, EXAMPLE_TELEMETRY_LICENSE_TYPE, EXAMPLE_TELEMETRY_LICENSE_VALID_UNTIL,
      EXAMPLE_TELEMETRY_LICENSE_UNLIMITED, EXAMPLE_TELEMETRY_LICENSE_FEATURES, EXAMPLE_TELEMETRY_LICENSE_RAW);
  public static final JdkImpl EXAMPLE_TELEMETRY_JDK = new JdkImpl(EXAMPLE_TELEMETRY_JDK_VERSION, EXAMPLE_TELEMETRY_JDK_VENDOR);
  public static final InternalsImpl EXAMPLE_TELEMETRY_INTERNALS = new InternalsImpl(EXAMPLE_TELEMETRY_DATABASE,
      EXAMPLE_TELEMETRY_SERVER, EXAMPLE_TELEMETRY_LICENSE, EXAMPLE_TELEMETRY_JDK);
  static {
    EXAMPLE_TELEMETRY_INTERNALS.setTelemetryEnabled(false);
    EXAMPLE_TELEMETRY_INTERNALS.setCamundaIntegration(
        Stream.of("spring-boot-starter", "camunda-bpm-run").collect(Collectors.toCollection(HashSet::new)));
    EXAMPLE_TELEMETRY_INTERNALS
        .setWebapps(Stream.of("cockpit", "admin").collect(Collectors.toCollection(HashSet::new)));
    EXAMPLE_TELEMETRY_INTERNALS
        .setCommands(Stream.of(new Object[][] { { "StartProcessInstanceCmd", 40 }, { "FetchExternalTasksCmd", 100 } })
            .collect(Collectors.toMap(data -> (String) data[0], data -> new CommandImpl((Integer) data[1]))));
    EXAMPLE_TELEMETRY_INTERNALS.setMetrics(Stream
        .of(new Object[][] { { "process-instances", 936L }, { "flow-node-instances", 6125L },
            { "decision-instances", 140L }, { "executed-decision-elements", 732L } })
        .collect(Collectors.toMap(data -> (String) data[0], data -> new MetricImpl((Long) data[1]))));
    EXAMPLE_TELEMETRY_INTERNALS.setCamundaIntegration(Collections.singleton("spring-boot"));
    EXAMPLE_TELEMETRY_INTERNALS.setWebapps(Collections.singleton("cockpit"));
    EXAMPLE_TELEMETRY_INTERNALS.setDataCollectionStartDate(DateTimeUtil.parseDate(EXAMPLE_TELEMETRY_DATA_COLLECTION_START_DATE));
  }
  public static final ProductImpl EXAMPLE_TELEMETRY_PRODUCT = new ProductImpl(EXAMPLE_TELEMETRY_PRODUCT_NAME,
      EXAMPLE_TELEMETRY_PRODUCT_VERSION, EXAMPLE_TELEMETRY_PRODUCT_EDITION, EXAMPLE_TELEMETRY_INTERNALS);
  public static final TelemetryDataImpl EXAMPLE_TELEMETRY_DATA = new TelemetryDataImpl(EXAMPLE_TELEMETRY_INSTALLATION_ID, EXAMPLE_TELEMETRY_PRODUCT);

  public static Task createMockTask() {
    return mockTask().build();
  }

  public static MockTaskBuilder mockTask() {
    return new MockTaskBuilder()
      .id(EXAMPLE_TASK_ID).name(EXAMPLE_TASK_NAME)
      .assignee(EXAMPLE_TASK_ASSIGNEE_NAME)
      .createTime(DateTimeUtil.parseDate(EXAMPLE_TASK_CREATE_TIME))
      .lastUpdated(DateTimeUtil.parseDate(EXAMPLE_TASK_LAST_UPDATED))
      .dueDate(DateTimeUtil.parseDate(EXAMPLE_TASK_DUE_DATE))
      .followUpDate(DateTimeUtil.parseDate(EXAMPLE_FOLLOW_UP_DATE))
      .delegationState(EXAMPLE_TASK_DELEGATION_STATE).description(EXAMPLE_TASK_DESCRIPTION)
      .executionId(EXAMPLE_TASK_EXECUTION_ID).owner(EXAMPLE_TASK_OWNER)
      .parentTaskId(EXAMPLE_TASK_PARENT_TASK_ID)
      .priority(EXAMPLE_TASK_PRIORITY)
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
      .taskDefinitionKey(EXAMPLE_TASK_DEFINITION_KEY)
      .caseDefinitionId(EXAMPLE_CASE_DEFINITION_ID)
      .caseInstanceId(EXAMPLE_CASE_INSTANCE_ID)
      .caseExecutionId(EXAMPLE_CASE_EXECUTION_ID)
      .formKey(EXAMPLE_FORM_KEY)
      .camundaFormRef(EXAMPLE_FORM_KEY, EXAMPLE_FORM_REF_BINDING, EXAMPLE_FORM_REF_VERSION)
      .tenantId(EXAMPLE_TENANT_ID);
  }

  public static List<Task> createMockTasks() {
    List<Task> mocks = new ArrayList<>();
    mocks.add(createMockTask());
    return mocks;
  }

  public static TaskFormData createMockTaskFormData() {
    FormProperty mockFormProperty = mock(FormProperty.class);
    when(mockFormProperty.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormProperty.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormProperty.getValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);
    when(mockFormProperty.isReadable()).thenReturn(EXAMPLE_FORM_PROPERTY_READABLE);
    when(mockFormProperty.isWritable()).thenReturn(EXAMPLE_FORM_PROPERTY_WRITABLE);
    when(mockFormProperty.isRequired()).thenReturn(EXAMPLE_FORM_PROPERTY_REQUIRED);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormProperty.getType()).thenReturn(mockFormType);

    TaskFormData mockFormData = mock(TaskFormData.class);
    when(mockFormData.getFormKey()).thenReturn(EXAMPLE_FORM_KEY);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    List<FormProperty> mockFormProperties = new ArrayList<>();
    mockFormProperties.add(mockFormProperty);
    when(mockFormData.getFormProperties()).thenReturn(mockFormProperties);
    return mockFormData;
  }

  public static TaskFormData createMockTaskFormDataUsingFormFieldsWithoutFormKey() {
    FormField mockFormField = mock(FormField.class);
    when(mockFormField.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormField.getLabel()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormField.getDefaultValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormField.getType()).thenReturn(mockFormType);

    TaskFormData mockFormData = mock(TaskFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    List<FormField> mockFormFields = new ArrayList<>();
    mockFormFields.add(mockFormField);
    when(mockFormData.getFormFields()).thenReturn(mockFormFields);
    return mockFormData;
  }

  public static TaskFormData createMockTaskFormDataUsingFormRef() {
    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);

    TaskFormData mockFormData = mock(TaskFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    CamundaFormRefImpl formRef = new CamundaFormRefImpl(EXAMPLE_FORM_KEY, EXAMPLE_FORM_REF_BINDING);
    formRef.setVersion(EXAMPLE_FORM_REF_VERSION);
    when(mockFormData.getCamundaFormRef()).thenReturn(formRef);

    return mockFormData;
  }

  // task comment
  public static Comment createMockTaskComment() {
    Comment mockComment = mock(Comment.class);
    when(mockComment.getId()).thenReturn(EXAMPLE_TASK_COMMENT_ID);
    when(mockComment.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mockComment.getUserId()).thenReturn(EXAMPLE_USER_ID);
    when(mockComment.getTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_TASK_COMMENT_TIME));
    when(mockComment.getFullMessage()).thenReturn(EXAMPLE_TASK_COMMENT_FULL_MESSAGE);
    when(mockComment.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_TASK_COMMENT_TIME));
    when(mockComment.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mockComment.getRootProcessInstanceId()).thenReturn(EXAMPLE_TASK_COMMENT_ROOT_PROCESS_INSTANCE_ID);
    return mockComment;
  }

  public static List<Comment> createMockTaskComments() {
    List<Comment> mocks = new ArrayList<>();
    mocks.add(createMockTaskComment());
    return mocks;
  }

  // task attachment
  public static Attachment createMockTaskAttachment() {
    Attachment mockAttachment = mock(Attachment.class);
    when(mockAttachment.getId()).thenReturn(EXAMPLE_TASK_ATTACHMENT_ID);
    when(mockAttachment.getName()).thenReturn(EXAMPLE_TASK_ATTACHMENT_NAME);
    when(mockAttachment.getDescription()).thenReturn(EXAMPLE_TASK_ATTACHMENT_DESCRIPTION);
    when(mockAttachment.getType()).thenReturn(EXAMPLE_TASK_ATTACHMENT_TYPE);
    when(mockAttachment.getUrl()).thenReturn(EXAMPLE_TASK_ATTACHMENT_URL);
    when(mockAttachment.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mockAttachment.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mockAttachment.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_TASK_ATTACHMENT_CREATE_DATE));
    when(mockAttachment.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_TASK_ATTACHMENT_REMOVAL_DATE));
    when(mockAttachment.getRootProcessInstanceId()).thenReturn(EXAMPLE_TASK_ATTACHMENT_ROOT_PROCESS_INSTANCE_ID);

    return mockAttachment;
  }

  public static List<Attachment> createMockTaskAttachments() {
    List<Attachment> mocks = new ArrayList<>();
    mocks.add(createMockTaskAttachment());
    return mocks;
  }

  public static List<TaskCountByCandidateGroupResult> createMockTaskCountByCandidateGroupReport() {
    TaskCountByCandidateGroupResult mock = mock(TaskCountByCandidateGroupResult.class);
    when(mock.getGroupName()).thenReturn(EXAMPLE_GROUP_ID);
    when(mock.getTaskCount()).thenReturn(EXAMPLE_TASK_COUNT_BY_CANDIDATE_GROUP);

    List<TaskCountByCandidateGroupResult> mockList = new ArrayList<>();
    mockList.add(mock);
    return mockList;
  }

  public static List<HistoricTaskInstanceReportResult> createMockHistoricTaskInstanceReport() {
    HistoricTaskInstanceReportResult mock = mock(HistoricTaskInstanceReportResult.class);
    when(mock.getCount()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_COUNT);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEF_ID);
    when(mock.getProcessDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEFINITION);
    when(mock.getProcessDefinitionName()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEF_NAME);
    when(mock.getTenantId()).thenReturn(EXAMPLE_TENANT_ID);
    when(mock.getTaskName()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_TASK_NAME);

    return Collections.singletonList(mock);
  }

  public static List<HistoricTaskInstanceReportResult> createMockHistoricTaskInstanceReportWithProcDef() {
    HistoricTaskInstanceReportResult mock = mock(HistoricTaskInstanceReportResult.class);
    when(mock.getCount()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_COUNT);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEF_ID);
    when(mock.getProcessDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEFINITION);
    when(mock.getProcessDefinitionName()).thenReturn(EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEF_NAME);
    when(mock.getTenantId()).thenReturn(EXAMPLE_TENANT_ID);
    when(mock.getTaskName()).thenReturn(null);

    return Collections.singletonList(mock);
  }

  public static List<DurationReportResult> createMockHistoricTaskInstanceDurationReport(PeriodUnit periodUnit) {
    DurationReportResult mock = mock(DurationReportResult.class);
    when(mock.getAverage()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_AVG);
    when(mock.getMinimum()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MIN);
    when(mock.getMaximum()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MAX);
    when(mock.getPeriod()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_PERIOD);
    when(mock.getPeriodUnit()).thenReturn(periodUnit);

    List<DurationReportResult> mockList = new ArrayList<>();
    mockList.add(mock);
    return mockList;
  }


  // form data
  public static StartFormData createMockStartFormData(ProcessDefinition definition) {
    FormProperty mockFormProperty = mock(FormProperty.class);
    when(mockFormProperty.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormProperty.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormProperty.getValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);
    when(mockFormProperty.isReadable()).thenReturn(EXAMPLE_FORM_PROPERTY_READABLE);
    when(mockFormProperty.isWritable()).thenReturn(EXAMPLE_FORM_PROPERTY_WRITABLE);
    when(mockFormProperty.isRequired()).thenReturn(EXAMPLE_FORM_PROPERTY_REQUIRED);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormProperty.getType()).thenReturn(mockFormType);

    StartFormData mockFormData = mock(StartFormData.class);
    when(mockFormData.getFormKey()).thenReturn(EXAMPLE_FORM_KEY);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockFormData.getProcessDefinition()).thenReturn(definition);

    List<FormProperty> mockFormProperties = new ArrayList<>();
    mockFormProperties.add(mockFormProperty);
    when(mockFormData.getFormProperties()).thenReturn(mockFormProperties);
    return mockFormData;
  }

  public static StartFormData createMockStartFormDataUsingFormFieldsWithoutFormKey(ProcessDefinition definition) {
    FormField mockFormField = mock(FormField.class);
    when(mockFormField.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormField.getLabel()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormField.getDefaultValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormField.getType()).thenReturn(mockFormType);

    StartFormData mockFormData = mock(StartFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockFormData.getProcessDefinition()).thenReturn(definition);

    List<FormField> mockFormFields = new ArrayList<>();
    mockFormFields.add(mockFormField);
    when(mockFormData.getFormFields()).thenReturn(mockFormFields);

    return mockFormData;
  }

  public static StartFormData createMockStartFormDataUsingFormRef() {
    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);

    StartFormData mockFormData = mock(StartFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    CamundaFormRefImpl formRef = new CamundaFormRefImpl(EXAMPLE_FORM_KEY, EXAMPLE_FORM_REF_BINDING);
    formRef.setVersion(EXAMPLE_FORM_REF_VERSION);
    when(mockFormData.getCamundaFormRef()).thenReturn(formRef);

    return mockFormData;
  }

  public static ProcessInstanceWithVariables createMockInstanceWithVariables() {
    return createMockInstanceWithVariables(EXAMPLE_TENANT_ID);
  }

  public static ProcessInstanceWithVariables createMockInstanceWithVariables(String tenantId) {
    ProcessInstanceWithVariables mock = mock(ProcessInstanceWithVariables.class);

    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isSuspended()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED);
    when(mock.isEnded()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_ENDED);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.getVariables()).thenReturn(createMockSerializedVariables());
    return mock;
  }


  public static ProcessInstance createMockInstance() {
    return createMockInstance(EXAMPLE_TENANT_ID);
  }

  public static ProcessInstance createMockInstance(String tenantId) {
    ProcessInstance mock = mock(ProcessInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isSuspended()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED);
    when(mock.isEnded()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_ENDED);
    when(mock.getTenantId()).thenReturn(tenantId);

    return mock;
  }

  public static VariableInstance createMockVariableInstance() {
    return mockVariableInstance().build();
  }

  public static MockVariableInstanceBuilder mockVariableInstance() {
    return new MockVariableInstanceBuilder()
      .id(EXAMPLE_VARIABLE_INSTANCE_ID)
      .name(EXAMPLE_VARIABLE_INSTANCE_NAME)
      .typedValue(EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
      .processInstanceId(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID)
      .executionId(EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID)
      .caseInstanceId(EXAMPLE_VARIABLE_INSTANCE_CASE_INST_ID)
      .caseExecutionId(EXAMPLE_VARIABLE_INSTANCE_CASE_EXECUTION_ID)
      .taskId(EXAMPLE_VARIABLE_INSTANCE_TASK_ID)
      .activityInstanceId(EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID)
      .tenantId(EXAMPLE_TENANT_ID)
      .batchId(EXAMPLE_VARIABLE_INSTANCE_BATCH_ID)
      .errorMessage(null);
  }

  public static VariableInstance createMockVariableInstance(TypedValue value) {
    return mockVariableInstance().typedValue(value).build();
  }

  public static VariableMap createMockSerializedVariables() {
    VariableMap variables = Variables.createVariables();
    ObjectValue serializedVar = Variables.serializedObjectValue(EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE)
            .serializationDataFormat(FORMAT_APPLICATION_JSON)
            .objectTypeName(ArrayList.class.getName())
            .create();

    ObjectValue deserializedVar = new ObjectValueImpl(EXAMPLE_VARIABLE_INSTANCE_DESERIALIZED_VALUE,
                                                      EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE,
                                                      FORMAT_APPLICATION_JSON, Object.class.getName(), true);
    variables.putValueTyped(EXAMPLE_VARIABLE_INSTANCE_NAME, serializedVar);
    variables.putValueTyped(EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME, deserializedVar);
    return variables;
  }

  public static Execution createMockExecution() {
    return createMockExecution(EXAMPLE_TENANT_ID);
  }

  public static Execution createMockExecution(String tenantId) {
    Execution mock = mock(Execution.class);

    when(mock.getId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isEnded()).thenReturn(EXAMPLE_EXECUTION_IS_ENDED);
    when(mock.getTenantId()).thenReturn(tenantId);

    return mock;
  }

  public static EventSubscription createMockEventSubscription() {
    return createMockEventSubscription(EXAMPLE_TENANT_ID);
  }

  public static EventSubscription createMockEventSubscription(String tenantId) {
    EventSubscription mock = mock(EventSubscription.class);

    when(mock.getId()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_ID);
    when(mock.getEventType()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_TYPE);
    when(mock.getEventName()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_NAME);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getCreated()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE));
    when(mock.getTenantId()).thenReturn(tenantId);

    return mock;
  }

  // statistics
  public static List<ProcessDefinitionStatistics> createMockProcessDefinitionStatistics() {
    ProcessDefinitionStatistics statistics = mock(ProcessDefinitionStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(statistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(statistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(statistics.getTenantId()).thenReturn(EXAMPLE_TENANT_ID);
    when(statistics.getVersionTag()).thenReturn(EXAMPLE_VERSION_TAG);
    when(statistics.getCategory()).thenReturn(EXAMPLE_PROCESS_DEFINITION_CATEGORY);
    when(statistics.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(statistics.getDiagramResourceName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME);
    when(statistics.getResourceName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME);
    when(statistics.getVersion()).thenReturn(EXAMPLE_PROCESS_DEFINITION_VERSION);
    when(statistics.getDescription()).thenReturn(EXAMPLE_PROCESS_DEFINITION_DESCRIPTION);

    IncidentStatistics incidentStaticits = mock(IncidentStatistics.class);
    when(incidentStaticits.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incidentStaticits.getIncidentCount()).thenReturn(EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> exampleIncidentList = new ArrayList<>();
    exampleIncidentList.add(incidentStaticits);
    when(statistics.getIncidentStatistics()).thenReturn(exampleIncidentList);

    ProcessDefinitionStatistics anotherStatistics = mock(ProcessDefinitionStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    when(anotherStatistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(anotherStatistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(anotherStatistics.getTenantId()).thenReturn(ANOTHER_EXAMPLE_TENANT_ID);
    when(anotherStatistics.getVersionTag()).thenReturn(ANOTHER_EXAMPLE_VERSION_TAG);

    IncidentStatistics anotherIncidentStaticits = mock(IncidentStatistics.class);
    when(anotherIncidentStaticits.getIncidentType()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_TYPE);
    when(anotherIncidentStaticits.getIncidentCount()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> anotherExampleIncidentList = new ArrayList<>();
    anotherExampleIncidentList.add(anotherIncidentStaticits);
    when(anotherStatistics.getIncidentStatistics()).thenReturn(anotherExampleIncidentList);

    List<ProcessDefinitionStatistics> processDefinitionResults = new ArrayList<>();
    processDefinitionResults.add(statistics);
    processDefinitionResults.add(anotherStatistics);

    return processDefinitionResults;
  }

  public static List<ActivityStatistics> createMockActivityStatistics() {
    ActivityStatistics statistics = mock(ActivityStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);

    IncidentStatistics incidentStaticits = mock(IncidentStatistics.class);
    when(incidentStaticits.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incidentStaticits.getIncidentCount()).thenReturn(EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> exampleIncidentList = new ArrayList<>();
    exampleIncidentList.add(incidentStaticits);
    when(statistics.getIncidentStatistics()).thenReturn(exampleIncidentList);

    ActivityStatistics anotherStatistics = mock(ActivityStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);

    IncidentStatistics anotherIncidentStaticits = mock(IncidentStatistics.class);
    when(anotherIncidentStaticits.getIncidentType()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_TYPE);
    when(anotherIncidentStaticits.getIncidentCount()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> anotherExampleIncidentList = new ArrayList<>();
    anotherExampleIncidentList.add(anotherIncidentStaticits);
    when(anotherStatistics.getIncidentStatistics()).thenReturn(anotherExampleIncidentList);

    List<ActivityStatistics> activityResults = new ArrayList<>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);

    return activityResults;
  }

  // process definition
  public static List<ProcessDefinition> createMockDefinitions() {
    List<ProcessDefinition> mocks = new ArrayList<>();
    mocks.add(createMockDefinition());
    return mocks;
  }

  public static List<ProcessDefinition> createMockTwoDefinitions() {
    List<ProcessDefinition> mocks = new ArrayList<>();
    mocks.add(createMockDefinition());
    mocks.add(createMockAnotherDefinition());
    return mocks;
  }

  public static MockDefinitionBuilder mockDefinition() {
    return new MockDefinitionBuilder().id(EXAMPLE_PROCESS_DEFINITION_ID).category(EXAMPLE_PROCESS_DEFINITION_CATEGORY)
        .name(EXAMPLE_PROCESS_DEFINITION_NAME).key(EXAMPLE_PROCESS_DEFINITION_KEY).description(EXAMPLE_PROCESS_DEFINITION_DESCRIPTION)
        .versionTag(EXAMPLE_VERSION_TAG).version(EXAMPLE_PROCESS_DEFINITION_VERSION).resource(EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME).deploymentId(EXAMPLE_DEPLOYMENT_ID)
        .diagram(EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME).suspended(EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED);
  }

  public static ProcessDefinition createMockDefinition() {
    return mockDefinition().build();
  }

  public static ProcessDefinition createMockAnotherDefinition() {
    return mockDefinition().id(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID).build();
  }

  // deployments
  public static List<Deployment> createMockDeployments() {
    List<Deployment> mocks = new ArrayList<>();
    mocks.add(createMockDeployment());
    return mocks;
  }

  public static Deployment createMockDeployment() {
    return createMockDeployment(EXAMPLE_TENANT_ID);
  }

  public static Deployment createMockDeployment(String tenantId) {
    Deployment mockDeployment = mock(Deployment.class);
    when(mockDeployment.getId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockDeployment.getName()).thenReturn(EXAMPLE_DEPLOYMENT_NAME);
    when(mockDeployment.getDeploymentTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_DEPLOYMENT_TIME));
    when(mockDeployment.getSource()).thenReturn(EXAMPLE_DEPLOYMENT_SOURCE);
    when(mockDeployment.getTenantId()).thenReturn(tenantId);
    return mockDeployment;
  }

  public static DeploymentWithDefinitions createMockDeploymentWithDefinitions() {
    return createMockDeploymentWithDefinitions(EXAMPLE_TENANT_ID);
  }

  public static DeploymentWithDefinitions createMockDeploymentWithDefinitions(String tenantId) {
    DeploymentWithDefinitions mockDeployment = mock(DeploymentWithDefinitions.class);
    when(mockDeployment.getId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockDeployment.getName()).thenReturn(EXAMPLE_DEPLOYMENT_NAME);
    when(mockDeployment.getDeploymentTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_DEPLOYMENT_TIME));
    when(mockDeployment.getSource()).thenReturn(EXAMPLE_DEPLOYMENT_SOURCE);
    when(mockDeployment.getTenantId()).thenReturn(tenantId);
    List<ProcessDefinition> mockDefinitions = createMockDefinitions();
    when(mockDeployment.getDeployedProcessDefinitions()).thenReturn(mockDefinitions);

    List<CaseDefinition> mockCaseDefinitions = createMockCaseDefinitions();
    when(mockDeployment.getDeployedCaseDefinitions()).thenReturn(mockCaseDefinitions);

    List<DecisionDefinition> mockDecisionDefinitions = createMockDecisionDefinitions();
    when(mockDeployment.getDeployedDecisionDefinitions()).thenReturn(mockDecisionDefinitions);

    List<DecisionRequirementsDefinition> mockDecisionRequirementsDefinitions = createMockDecisionRequirementsDefinitions();
    when(mockDeployment.getDeployedDecisionRequirementsDefinitions()).thenReturn(mockDecisionRequirementsDefinitions);

    return mockDeployment;
  }

  public static Deployment createMockRedeployment() {
    Deployment mockDeployment = mock(Deployment.class);
    when(mockDeployment.getId()).thenReturn(EXAMPLE_RE_DEPLOYMENT_ID);
    when(mockDeployment.getName()).thenReturn(EXAMPLE_DEPLOYMENT_NAME);
    when(mockDeployment.getDeploymentTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_DEPLOYMENT_TIME));
    when(mockDeployment.getSource()).thenReturn(EXAMPLE_DEPLOYMENT_SOURCE);

    return mockDeployment;
  }

  // deployment resources
  public static List<Resource> createMockDeploymentResources() {
    List<Resource> mocks = new ArrayList<>();
    mocks.add(createMockDeploymentResource());
    return mocks;
  }

  public static Resource createMockDeploymentResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    return mockResource;
  }

  public static Resource createMockDeploymentSvgResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_SVG_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_SVG_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentPngResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_PNG_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_PNG_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentJpgResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_JPG_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_JPG_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentJpegResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentJpeResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_JPE_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_JPE_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentGifResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_GIF_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_GIF_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentTifResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_TIF_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_TIF_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentTiffResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentBpmnResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentBpmnXmlResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentCmmnResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentCmmnXmlResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentDmnResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_DMN_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_DMN_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentDmnXmlResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentXmlResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_XML_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_XML_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentJsonResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_JSON_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_JSON_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentGroovyResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentJavaResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentJsResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_JS_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_JS_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentPhpResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_PHP_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_PHP_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentPythonResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentRubyResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentHtmlResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_HTML_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_HTML_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentTxtResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_TXT_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_TXT_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentCamundaFormResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentResourceFilename() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_PATH + EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  public static Resource createMockDeploymentResourceFilenameBackslash() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_PATH_BACKSLASH + EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    return mockResource;
  }

  // user, groups and tenants

  public static Group createMockGroup() {
    return mockGroup().build();
  }

  public static MockGroupBuilder mockGroup() {
    return new MockGroupBuilder()
      .id(EXAMPLE_GROUP_ID)
      .name(EXAMPLE_GROUP_NAME)
      .type(EXAMPLE_GROUP_TYPE);
  }

  public static Group createMockGroupUpdate() {
    Group mockGroup = mock(Group.class);
    when(mockGroup.getId()).thenReturn(EXAMPLE_GROUP_ID);
    when(mockGroup.getName()).thenReturn(EXAMPLE_GROUP_NAME_UPDATE);

    return mockGroup;
  }

  public static List<Group> createMockGroups() {
    List<Group> mockGroups = new ArrayList<>();
    mockGroups.add(createMockGroup());
    return mockGroups;
  }

  public static User createMockUser() {
    return mockUser().build();
  }

  public static MockUserBuilder mockUser() {
    return new MockUserBuilder()
      .id(EXAMPLE_USER_ID)
      .firstName(EXAMPLE_USER_FIRST_NAME)
      .lastName(EXAMPLE_USER_LAST_NAME)
      .email(EXAMPLE_USER_EMAIL)
      .password(EXAMPLE_USER_PASSWORD);
  }

  public static Tenant createMockTenant() {
    Tenant mockTenant = mock(Tenant.class);
    when(mockTenant.getId()).thenReturn(EXAMPLE_TENANT_ID);
    when(mockTenant.getName()).thenReturn(EXAMPLE_TENANT_NAME);
    return mockTenant;
  }

  public static Authentication createMockAuthentication() {
    Authentication mockAuthentication = mock(Authentication.class);

    when(mockAuthentication.getUserId()).thenReturn(EXAMPLE_USER_ID);

    return mockAuthentication;
  }

  // jobs
  public static Job createMockJob() {
    return mockJob().tenantId(EXAMPLE_TENANT_ID).build();
  }

  public static MockJobBuilder mockJob() {
    return new MockJobBuilder()
      .id(EXAMPLE_JOB_ID)
      .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
      .executionId(EXAMPLE_EXECUTION_ID)
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY)
      .retries(EXAMPLE_JOB_RETRIES)
      .exceptionMessage(EXAMPLE_JOB_NO_EXCEPTION_MESSAGE)
      .dueDate(DateTimeUtil.parseDate(EXAMPLE_DUE_DATE))
      .suspended(EXAMPLE_JOB_IS_SUSPENDED)
      .priority(EXAMPLE_JOB_PRIORITY)
      .jobDefinitionId(EXAMPLE_JOB_DEFINITION_ID)
      .createTime(DateTimeUtil.parseDate(EXAMPLE_JOB_CREATE_TIME))
      .failedActivityId(EXAMPLE_JOB_FAILED_ACTIVITY_ID);
  }

  public static List<Job> createMockJobs() {
    List<Job> mockList = new ArrayList<>();
    mockList.add(createMockJob());
    return mockList;
  }

  public static List<Job> createMockEmptyJobList() {
    return new ArrayList<>();
  }

  public static User createMockUserUpdate() {
    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(EXAMPLE_USER_ID);
    when(mockUser.getFirstName()).thenReturn(EXAMPLE_USER_FIRST_NAME_UPDATE);
    when(mockUser.getLastName()).thenReturn(EXAMPLE_USER_LAST_NAME_UPDATE);
    when(mockUser.getEmail()).thenReturn(EXAMPLE_USER_EMAIL_UPDATE);
    when(mockUser.getPassword()).thenReturn(EXAMPLE_USER_PASSWORD);
    return mockUser;
  }

  public static List<User> createMockUsers() {
    ArrayList<User> list = new ArrayList<>();
    list.add(createMockUser());
    return list;
  }

  public static Authorization createMockGlobalAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_GLOBAL);
    when(mockAuthorization.getUserId()).thenReturn(Authorization.ANY);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_GRANT_PERMISSION_VALUES);

    when(mockAuthorization.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_AUTH_REMOVAL_TIME));
    when(mockAuthorization.getRootProcessInstanceId()).thenReturn(EXAMPLE_AUTH_ROOT_PROCESS_INSTANCE_ID);

    return mockAuthorization;
  }

  public static Authorization createMockGrantAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_GRANT);
    when(mockAuthorization.getUserId()).thenReturn(EXAMPLE_USER_ID);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_GRANT_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static Authorization createMockRevokeAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_REVOKE);
    when(mockAuthorization.getUserId()).thenReturn(EXAMPLE_USER_ID);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_REVOKE_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static List<Authorization> createMockAuthorizations() {
    return Arrays.asList(createMockGlobalAuthorization(), createMockGrantAuthorization(), createMockRevokeAuthorization());
  }

  public static List<Authorization> createMockGrantAuthorizations() {
    return Arrays.asList(createMockGrantAuthorization());
  }

  public static List<Authorization> createMockRevokeAuthorizations() {
    return Arrays.asList(createMockRevokeAuthorization());
  }

  public static List<Authorization> createMockGlobalAuthorizations() {
    return Arrays.asList(createMockGlobalAuthorization());
  }

  public static Date createMockDuedate() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, 3);
    return cal.getTime();
  } // process application

  public static ProcessApplicationInfo createMockProcessApplicationInfo() {
    ProcessApplicationInfo appInfo = mock(ProcessApplicationInfo.class);
    Map<String, String> mockAppProperties = new HashMap<>();
    String mockServletContextPath = MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH;
    mockAppProperties.put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, mockServletContextPath);
    when(appInfo.getProperties()).thenReturn(mockAppProperties);
    return appInfo;
  }

  // History
  public static List<HistoricActivityInstance> createMockHistoricActivityInstances() {
    List<HistoricActivityInstance> mockList = new ArrayList<>();
    mockList.add(createMockHistoricActivityInstance());
    return mockList;
  }

  public static HistoricActivityInstance createMockHistoricActivityInstance() {
    return createMockHistoricActivityInstance(EXAMPLE_TENANT_ID);
  }

  public static HistoricActivityInstance createMockHistoricActivityInstance(String tenantId) {
    HistoricActivityInstance mock = mock(HistoricActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID);
    when(mock.getParentActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_ACTIVITY_NAME);
    when(mock.getActivityType()).thenReturn(EXAMPLE_ACTIVITY_TYPE);
    when(mock.getProcessDefinitionKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getCalledCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID);
    when(mock.getAssignee()).thenReturn(EXAMPLE_TASK_ASSIGNEE_NAME);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME));
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_DURATION);
    when(mock.isCanceled()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED);
    when(mock.isCompleteScope()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_REMOVAL_TIME));
    when(mock.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_ROOT_PROCESS_INSTANCE_ID);

    return mock;
  }

  public static List<HistoricActivityInstance> createMockRunningHistoricActivityInstances() {
    List<HistoricActivityInstance> mockList = new ArrayList<>();
    mockList.add(createMockRunningHistoricActivityInstance());
    return mockList;
  }

  public static HistoricActivityInstance createMockRunningHistoricActivityInstance() {
    HistoricActivityInstance mock = mock(HistoricActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID);
    when(mock.getParentActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_ACTIVITY_NAME);
    when(mock.getActivityType()).thenReturn(EXAMPLE_ACTIVITY_TYPE);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getCalledCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID);
    when(mock.getAssignee()).thenReturn(EXAMPLE_TASK_ASSIGNEE_NAME);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME));
    when(mock.getEndTime()).thenReturn(null);
    when(mock.getDurationInMillis()).thenReturn(null);

    return mock;
  }

  public static List<HistoricCaseActivityInstance> createMockHistoricCaseActivityInstances() {
    ArrayList<HistoricCaseActivityInstance> mockList = new ArrayList<>();
    mockList.add(createMockHistoricCaseActivityInstance());
    return mockList;
  }

  public static HistoricCaseActivityInstance createMockHistoricCaseActivityInstance() {
    return createMockHistoricCaseActivityInstance(EXAMPLE_TENANT_ID);
  }

  public static HistoricCaseActivityInstance createMockHistoricCaseActivityInstance(String tenantId) {
    HistoricCaseActivityInstance mock = mock(HistoricCaseActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID);
    when(mock.getParentCaseActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_PARENT_CASE_ACTIVITY_INSTANCE_ID);
    when(mock.getCaseActivityId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_ID);
    when(mock.getCaseActivityName()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_NAME);
    when(mock.getCaseActivityType()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_TYPE);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_DEFINITION_ID);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getCaseExecutionId()).thenReturn(EXAMPLE_CASE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getCalledCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID);
    when(mock.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME));
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME));
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION);
    when(mock.isRequired()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_REQUIRED);
    when(mock.isAvailable()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_AVAILABLE);
    when(mock.isEnabled()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ENABLED);
    when(mock.isDisabled()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_DISABLED);
    when(mock.isActive()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ACTIVE);
    when(mock.isCompleted()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_COMPLETED);
    when(mock.isTerminated()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_TERMINATED);

    return mock;
  }

  public static List<HistoricCaseActivityInstance> createMockRunningHistoricCaseActivityInstances() {
    List<HistoricCaseActivityInstance> mockList = new ArrayList<>();
    mockList.add(createMockRunningHistoricCaseActivityInstance());
    return mockList;
  }

  public static HistoricCaseActivityInstance createMockRunningHistoricCaseActivityInstance() {
    HistoricCaseActivityInstance mock = createMockHistoricCaseActivityInstance();

    when(mock.getEndTime()).thenReturn(null);
    when(mock.getDurationInMillis()).thenReturn(null);
    when(mock.isAvailable()).thenReturn(false);
    when(mock.isEnabled()).thenReturn(false);
    when(mock.isDisabled()).thenReturn(false);
    when(mock.isActive()).thenReturn(true);
    when(mock.isCompleted()).thenReturn(false);
    when(mock.isTerminated()).thenReturn(false);

    return mock;
  }

  public static List<HistoricActivityStatistics> createMockHistoricActivityStatistics() {
    HistoricActivityStatistics statistics = mock(HistoricActivityStatistics.class);

    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES_LONG);
    when(statistics.getCanceled()).thenReturn(EXAMPLE_CANCELED_LONG);
    when(statistics.getFinished()).thenReturn(EXAMPLE_FINISHED_LONG);
    when(statistics.getCompleteScope()).thenReturn(EXAMPLE_COMPLETE_SCOPE_LONG);
    when(statistics.getOpenIncidents()).thenReturn(EXAMPLE_OPEN_INCIDENTS_LONG);
    when(statistics.getResolvedIncidents()).thenReturn(EXAMPLE_RESOLVED_INCIDENTS_LONG);
    when(statistics.getDeletedIncidents()).thenReturn(EXAMPLE_DELETED_INCIDENTS_LONG);

    HistoricActivityStatistics anotherStatistics = mock(HistoricActivityStatistics.class);

    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES_LONG);
    when(anotherStatistics.getCanceled()).thenReturn(ANOTHER_EXAMPLE_CANCELED_LONG);
    when(anotherStatistics.getFinished()).thenReturn(ANOTHER_EXAMPLE_FINISHED_LONG);
    when(anotherStatistics.getCompleteScope()).thenReturn(ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG);
    when(anotherStatistics.getOpenIncidents()).thenReturn(ANOTHER_EXAMPLE_OPEN_INCIDENTS_LONG);
    when(anotherStatistics.getResolvedIncidents()).thenReturn(ANOTHER_EXAMPLE_RESOLVED_INCIDENTS_LONG);
    when(anotherStatistics.getDeletedIncidents()).thenReturn(ANOTHER_EXAMPLE_DELETED_INCIDENTS_LONG);

    List<HistoricActivityStatistics> activityResults = new ArrayList<>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);

    return activityResults;
  }

  public static List<HistoricCaseActivityStatistics> createMockHistoricCaseActivityStatistics() {
    HistoricCaseActivityStatistics statistics = mock(HistoricCaseActivityStatistics.class);

    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(statistics.getActive()).thenReturn(EXAMPLE_ACTIVE_LONG);
    when(statistics.getAvailable()).thenReturn(EXAMPLE_AVAILABLE_LONG);
    when(statistics.getCompleted()).thenReturn(EXAMPLE_COMPLETED_LONG);
    when(statistics.getDisabled()).thenReturn(EXAMPLE_DISABLED_LONG);
    when(statistics.getEnabled()).thenReturn(EXAMPLE_ENABLED_LONG);
    when(statistics.getTerminated()).thenReturn(EXAMPLE_TERMINATED_LONG);

    HistoricCaseActivityStatistics anotherStatistics = mock(HistoricCaseActivityStatistics.class);

    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);
    when(anotherStatistics.getActive()).thenReturn(ANOTHER_EXAMPLE_ACTIVE_LONG);
    when(anotherStatistics.getAvailable()).thenReturn(ANOTHER_EXAMPLE_AVAILABLE_LONG);
    when(anotherStatistics.getCompleted()).thenReturn(ANOTHER_EXAMPLE_COMPLETED_LONG);
    when(anotherStatistics.getDisabled()).thenReturn(ANOTHER_EXAMPLE_DISABLED_LONG);
    when(anotherStatistics.getEnabled()).thenReturn(ANOTHER_EXAMPLE_ENABLED_LONG);
    when(anotherStatistics.getTerminated()).thenReturn(ANOTHER_EXAMPLE_TERMINATED_LONG);

    List<HistoricCaseActivityStatistics> activityResults = new ArrayList<>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);

    return activityResults;
  }

  public static List<HistoricProcessInstance> createMockHistoricProcessInstances() {
    List<HistoricProcessInstance> mockList = new ArrayList<>();
    mockList.add(createMockHistoricProcessInstance());
    return mockList;
  }

  public static HistoricProcessInstance createMockHistoricProcessInstance() {
    return createMockHistoricProcessInstance(EXAMPLE_TENANT_ID);
  }

  public static HistoricProcessInstance createMockHistoricProcessInstance(String tenantId) {
    HistoricProcessInstance mock = mock(HistoricProcessInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(mock.getProcessDefinitionName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(mock.getProcessDefinitionVersion()).thenReturn(EXAMPLE_PROCESS_DEFINITION_VERSION);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON);
    when(mock.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_PROCESS_INSTANCE_REMOVAL_TIME));
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME));
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS);
    when(mock.getStartUserId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID);
    when(mock.getStartActivityId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID);
    when(mock.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_ROOT_PROCESS_INSTANCE_ID);
    when(mock.getSuperProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID);
    when(mock.getSuperCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_CASE_INSTANCE_ID);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.getState()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_STATE);

    return mock;
  }

  public static List<HistoricProcessInstance> createMockRunningHistoricProcessInstances() {
    List<HistoricProcessInstance> mockList = new ArrayList<>();
    mockList.add(createMockHistoricProcessInstanceUnfinished());
    return mockList;
  }

  public static HistoricProcessInstance createMockHistoricProcessInstanceUnfinished() {
    HistoricProcessInstance mock = mock(HistoricProcessInstance.class);
    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON);
    when(mock.getEndTime()).thenReturn(null);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS);
    return mock;
  }

  public static List<DurationReportResult> createMockHistoricProcessInstanceDurationReportByMonth() {
    DurationReportResult mock = mock(DurationReportResult.class);
    when(mock.getAverage()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG);
    when(mock.getMinimum()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN);
    when(mock.getMaximum()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX);
    when(mock.getPeriod()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_PERIOD);
    when(mock.getPeriodUnit()).thenReturn(PeriodUnit.MONTH);

    List<DurationReportResult> mockList = new ArrayList<>();
    mockList.add(mock);
    return mockList;
  }

  public static List<DurationReportResult> createMockHistoricProcessInstanceDurationReportByQuarter() {
    DurationReportResult mock = mock(DurationReportResult.class);
    when(mock.getAverage()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG);
    when(mock.getMinimum()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN);
    when(mock.getMaximum()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX);
    when(mock.getPeriod()).thenReturn(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_PERIOD);
    when(mock.getPeriodUnit()).thenReturn(PeriodUnit.QUARTER);

    List<DurationReportResult> mockList = new ArrayList<>();
    mockList.add(mock);
    return mockList;
  }

  public static List<HistoricCaseInstance> createMockHistoricCaseInstances() {
    List<HistoricCaseInstance> mockList = new ArrayList<>();
    mockList.add(createMockHistoricCaseInstance());
    return mockList;
  }

  public static HistoricCaseInstance createMockHistoricCaseInstance() {
    return createMockHistoricCaseInstance(EXAMPLE_TENANT_ID);
  }

  public static HistoricCaseInstance createMockHistoricCaseInstance(String tenantId) {
    HistoricCaseInstance mock = mock(HistoricCaseInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_DEFINITION_ID);
    when(mock.getCaseDefinitionKey()).thenReturn(EXAMPLE_CASE_DEFINITION_KEY);
    when(mock.getCaseDefinitionName()).thenReturn(EXAMPLE_CASE_DEFINITION_NAME);
    when(mock.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_TIME));
    when(mock.getCloseTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSE_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_DURATION_MILLIS);
    when(mock.getCreateUserId()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_USER_ID);
    when(mock.getSuperCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID);
    when(mock.getSuperProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_PROCESS_INSTANCE_ID);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.isActive()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_ACTIVE);
    when(mock.isCompleted()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_COMPLETED);
    when(mock.isTerminated()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_TERMINATED);
    when(mock.isClosed()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_CLOSED);

    return mock;
  }

  public static List<HistoricCaseInstance> createMockRunningHistoricCaseInstances() {
    List<HistoricCaseInstance> mockList = new ArrayList<>();
    mockList.add(createMockHistoricCaseInstanceNotClosed());
    return mockList;
  }

  public static HistoricCaseInstance createMockHistoricCaseInstanceNotClosed() {
    HistoricCaseInstance mock = createMockHistoricCaseInstance();

    when(mock.getCloseTime()).thenReturn(null);
    when(mock.getDurationInMillis()).thenReturn(null);
    when(mock.isActive()).thenReturn(true);
    when(mock.isCompleted()).thenReturn(false);
    when(mock.isTerminated()).thenReturn(false);
    when(mock.isClosed()).thenReturn(false);

    return mock;
  }

  public static HistoricVariableInstance createMockHistoricVariableInstance() {
    return mockHistoricVariableInstance(EXAMPLE_TENANT_ID).build();
  }

  public static MockHistoricVariableInstanceBuilder mockHistoricVariableInstance() {
    return mockHistoricVariableInstance(EXAMPLE_TENANT_ID);
  }

  public static MockHistoricVariableInstanceBuilder mockHistoricVariableInstance(String tenantId) {
    return new MockHistoricVariableInstanceBuilder()
        .id(EXAMPLE_VARIABLE_INSTANCE_ID)
        .name(EXAMPLE_VARIABLE_INSTANCE_NAME)
        .typedValue(EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
        .processDefinitionKey(EXAMPLE_VARIABLE_INSTANCE_PROC_DEF_KEY)
        .processDefinitionId(EXAMPLE_VARIABLE_INSTANCE_PROC_DEF_ID)
        .processInstanceId(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID)
        .executionId(EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID)
        .activityInstanceId(EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID)
        .caseDefinitionKey(EXAMPLE_VARIABLE_INSTANCE_CASE_DEF_KEY)
        .caseDefinitionId(EXAMPLE_VARIABLE_INSTANCE_CASE_DEF_ID)
        .caseInstanceId(EXAMPLE_VARIABLE_INSTANCE_CASE_INST_ID)
        .caseExecutionId(EXAMPLE_VARIABLE_INSTANCE_CASE_EXECUTION_ID)
        .taskId(EXAMPLE_VARIABLE_INSTANCE_TASK_ID)
        .tenantId(tenantId)
        .errorMessage(null)
        .createTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_VARIABLE_INSTANCE_CREATE_TIME))
        .removalTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_VARIABLE_INSTANCE_REMOVAL_TIME))
        .rootProcessInstanceId(EXAMPLE_HISTORIC_VARIABLE_INSTANCE_ROOT_PROC_INST_ID);
  }

  public static List<ProcessInstance> createAnotherMockProcessInstanceList() {
    List<ProcessInstance> mockProcessInstanceList = new ArrayList<>();
    mockProcessInstanceList.add(createMockInstance());
    mockProcessInstanceList.add(createAnotherMockInstance());
    return mockProcessInstanceList;
  }

  public static ProcessInstance createAnotherMockInstance() {
    ProcessInstance mock = mock(ProcessInstance.class);

    when(mock.getId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isSuspended()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED);
    when(mock.isEnded()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_ENDED);

    return mock;
  }

  public static Set<String> createMockSetFromList(String list){
    return new HashSet<>(Arrays.asList(list.split(",")));
  }

  public static IdentityLink createMockUserAssigneeIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.ASSIGNEE);
    when(identityLink.getUserId()).thenReturn(EXAMPLE_TASK_ASSIGNEE_NAME);

    return identityLink;
  }

  public static IdentityLink createMockUserOwnerIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.OWNER);
    when(identityLink.getUserId()).thenReturn(EXAMPLE_TASK_OWNER);

    return identityLink;
  }

  public static IdentityLink createMockCandidateGroupIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_GROUP_ID);

    return identityLink;
  }

  public static IdentityLink createAnotherMockCandidateGroupIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_GROUP_ID2);

    return identityLink;
  }

  // Historic identity link
  public static HistoricIdentityLinkLog createMockHistoricIdentityLink() {
    return createMockHistoricIdentityLink(EXAMPLE_TENANT_ID);
  }

  public static HistoricIdentityLinkLog createMockHistoricIdentityLink(String tenantId) {
    HistoricIdentityLinkLog identityLink = mock(HistoricIdentityLinkLog.class);

    when(identityLink.getAssignerId()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_ASSIGNER_ID);
    when(identityLink.getTenantId()).thenReturn(tenantId);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_GROUP_ID);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_TASK_ID);
    when(identityLink.getUserId()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_USER_ID);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_GROUP_ID);
    when(identityLink.getType()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_TYPE);
    when(identityLink.getOperationType()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_OPERATION_TYPE);
    when(identityLink.getProcessDefinitionId()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_ID);
    when(identityLink.getProcessDefinitionKey()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_KEY);
    when(identityLink.getTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HIST_IDENTITY_LINK_TIME));
    when(identityLink.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HIST_IDENTITY_LINK_REMOVAL_TIME));
    when(identityLink.getRootProcessInstanceId()).thenReturn(EXAMPLE_HIST_IDENTITY_LINK_ROOT_PROC_INST_ID);

    return identityLink;
  }

  public static List<HistoricIdentityLinkLog> createMockHistoricIdentityLinks() {
    List<HistoricIdentityLinkLog> entries = new ArrayList<>();
    entries.add(createMockHistoricIdentityLink());
    return entries;
  }

  // job definition
  public static List<JobDefinition> createMockJobDefinitions() {
    List<JobDefinition> mocks = new ArrayList<>();
    mocks.add(createMockJobDefinition());
    return mocks;
  }

  public static JobDefinition createMockJobDefinition() {
    return mockJobDefinition().build();
  }

  public static MockJobDefinitionBuilder mockJobDefinition() {
    return new MockJobDefinitionBuilder()
      .id(EXAMPLE_JOB_DEFINITION_ID)
      .activityId(EXAMPLE_ACTIVITY_ID)
      .jobConfiguration(EXAMPLE_JOB_CONFIG)
      .jobType(EXAMPLE_JOB_TYPE)
      .jobPriority(EXAMPLE_JOB_DEFINITION_PRIORITY)
      .suspended(EXAMPLE_JOB_DEFINITION_IS_SUSPENDED)
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY)
      .deploymentId(EXAMPLE_JOB_DEFINITION_DEPLOYMENT_ID);
  }

  public static List<UserOperationLogEntry> createUserOperationLogEntries() {
    List<UserOperationLogEntry> entries = new ArrayList<>();
    entries.add(createUserOperationLogEntry());
    return entries;
  }

  private static UserOperationLogEntry createUserOperationLogEntry() {
    UserOperationLogEntry entry = mock(UserOperationLogEntry.class);
    when(entry.getId()).thenReturn(EXAMPLE_USER_OPERATION_LOG_ID);
    when(entry.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(entry.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(entry.getProcessDefinitionKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(entry.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(entry.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(entry.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_DEFINITION_ID);
    when(entry.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(entry.getCaseExecutionId()).thenReturn(EXAMPLE_CASE_EXECUTION_ID);
    when(entry.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(entry.getJobId()).thenReturn(EXAMPLE_JOB_ID);
    when(entry.getJobDefinitionId()).thenReturn(EXAMPLE_JOB_DEFINITION_ID);
    when(entry.getBatchId()).thenReturn(EXAMPLE_BATCH_ID);
    when(entry.getUserId()).thenReturn(EXAMPLE_USER_ID);
    when(entry.getTimestamp()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_USER_OPERATION_TIMESTAMP));
    when(entry.getOperationId()).thenReturn(EXAMPLE_USER_OPERATION_ID);
    when(entry.getOperationType()).thenReturn(EXAMPLE_USER_OPERATION_TYPE);
    when(entry.getEntityType()).thenReturn(EXAMPLE_USER_OPERATION_ENTITY);
    when(entry.getProperty()).thenReturn(EXAMPLE_USER_OPERATION_PROPERTY);
    when(entry.getOrgValue()).thenReturn(EXAMPLE_USER_OPERATION_ORG_VALUE);
    when(entry.getNewValue()).thenReturn(EXAMPLE_USER_OPERATION_NEW_VALUE);
    return entry;
  }

  // historic detail ////////////////////

  public static HistoricVariableUpdate createMockHistoricVariableUpdate() {
    return mockHistoricVariableUpdate(EXAMPLE_TENANT_ID).build();
  }

  public static MockHistoricVariableUpdateBuilder mockHistoricVariableUpdate() {
    return mockHistoricVariableUpdate(EXAMPLE_TENANT_ID);
  }

  public static MockHistoricVariableUpdateBuilder mockHistoricVariableUpdate(String tenantId) {
    return new MockHistoricVariableUpdateBuilder()
        .id(EXAMPLE_HISTORIC_VAR_UPDATE_ID)
        .processDefinitionKey(EXAMPLE_HISTORIC_VAR_UPDATE_PROC_DEF_KEY)
        .processDefinitionId(EXAMPLE_HISTORIC_VAR_UPDATE_PROC_DEF_ID)
        .processInstanceId(EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID)
        .activityInstanceId(EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID)
        .executionId(EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID)
        .taskId(EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID)
        .time(EXAMPLE_HISTORIC_VAR_UPDATE_TIME)
        .name(EXAMPLE_HISTORIC_VAR_UPDATE_NAME)
        .variableInstanceId(EXAMPLE_HISTORIC_VAR_UPDATE_VAR_INST_ID)
        .typedValue(EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
        .revision(EXAMPLE_HISTORIC_VAR_UPDATE_REVISION)
        .errorMessage(null)
        .caseDefinitionKey(EXAMPLE_HISTORIC_VAR_UPDATE_CASE_DEF_KEY)
        .caseDefinitionId(EXAMPLE_HISTORIC_VAR_UPDATE_CASE_DEF_ID)
        .caseInstanceId(EXAMPLE_HISTORIC_VAR_UPDATE_CASE_INST_ID)
        .caseExecutionId(EXAMPLE_HISTORIC_VAR_UPDATE_CASE_EXEC_ID)
        .tenantId(tenantId)
        .initial(false);
  }

  public static HistoricFormField createMockHistoricFormField() {
    return createMockHistoricFormField(EXAMPLE_TENANT_ID);
  }

  public static HistoricFormField createMockHistoricFormField(String tenantId) {
    HistoricFormField historicFromField = mock(HistoricFormField.class);

    when(historicFromField.getId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_ID);
    when(historicFromField.getProcessDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_PROC_DEF_KEY);
    when(historicFromField.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_PROC_DEF_ID);
    when(historicFromField.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID);
    when(historicFromField.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID);
    when(historicFromField.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID);
    when(historicFromField.getTaskId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID);
    when(historicFromField.getTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_FORM_FIELD_TIME));
    when(historicFromField.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_FORM_FIELD_TIME));
    when(historicFromField.getFieldId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID);
    when(historicFromField.getFieldValue()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_VALUE);
    when(historicFromField.getCaseDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_CASE_DEF_KEY);
    when(historicFromField.getCaseDefinitionId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_CASE_DEF_ID);
    when(historicFromField.getCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_CASE_INST_ID);
    when(historicFromField.getCaseExecutionId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_CASE_EXEC_ID);
    when(historicFromField.getTenantId()).thenReturn(tenantId);
    when(historicFromField.getUserOperationId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_OPERATION_ID);
    when(historicFromField.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_ROOT_PROCESS_INSTANCE_ID);

    return historicFromField;
  }

  public static List<HistoricFormField> createMockHistoricFormFields() {
    List<HistoricFormField> entries = new ArrayList<>();
    entries.add(createMockHistoricFormField());
    return entries;
  }

  public static List<HistoricDetail> createMockHistoricDetails() {
    return createMockHistoricDetails(EXAMPLE_TENANT_ID);
  }

  public static List<HistoricDetail> createMockHistoricDetails(String tenantId) {
    List<HistoricDetail> entries = new ArrayList<>();
    entries.add(mockHistoricVariableUpdate(tenantId).build());
    entries.add(createMockHistoricFormField(tenantId));
    return entries;
  }

  public static HistoricTaskInstance createMockHistoricTaskInstance() {
    return createMockHistoricTaskInstance(EXAMPLE_TENANT_ID);
  }

  public static HistoricTaskInstance createMockHistoricTaskInstance(String tenantId) {
    return createMockHistoricTaskInstance(tenantId, EXAMPLE_HISTORIC_TASK_INST_DUE_DATE);
  }

  public static HistoricTaskInstance createMockHistoricTaskInstance(String tenantId, String dueDateString) {
    HistoricTaskInstance taskInstance = mock(HistoricTaskInstance.class);

    when(taskInstance.getId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ID);
    when(taskInstance.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID);
    when(taskInstance.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID);
    when(taskInstance.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_EXEC_ID);
    when(taskInstance.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID);
    when(taskInstance.getProcessDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_KEY);
    when(taskInstance.getName()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_NAME);
    when(taskInstance.getDescription()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION);
    when(taskInstance.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON);
    when(taskInstance.getOwner()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_OWNER);
    when(taskInstance.getAssignee()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE);
    when(taskInstance.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_START_TIME));
    when(taskInstance.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_END_TIME));
    when(taskInstance.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DURATION);
    when(taskInstance.getTaskDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DEF_KEY);
    when(taskInstance.getPriority()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PRIORITY);
    when(taskInstance.getDueDate()).thenReturn(dueDateString == null ? null : DateTimeUtil.parseDate(dueDateString));
    when(taskInstance.getFollowUpDate()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE));
    when(taskInstance.getParentTaskId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID);
    when(taskInstance.getCaseDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_KEY);
    when(taskInstance.getCaseDefinitionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_ID);
    when(taskInstance.getCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_CASE_INST_ID);
    when(taskInstance.getCaseExecutionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_CASE_EXEC_ID);
    when(taskInstance.getTenantId()).thenReturn(tenantId);
    when(taskInstance.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_REMOVAL_TIME));
    when(taskInstance.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ROOT_PROC_INST_ID);

    return taskInstance;
  }

  public static List<HistoricTaskInstance> createMockHistoricTaskInstances() {
    List<HistoricTaskInstance> entries = new ArrayList<>();
    entries.add(createMockHistoricTaskInstance());
    return entries;
  }

  // Incident ///////////////////////////////////////

  public static Incident createMockIncident() {
    return createMockIncident(EXAMPLE_TENANT_ID);
  }

  public static Incident createMockIncident(String tenantId) {
    Incident incident = mock(Incident.class);

    when(incident.getId()).thenReturn(EXAMPLE_INCIDENT_ID);
    when(incident.getIncidentTimestamp()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_INCIDENT_TIMESTAMP));
    when(incident.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incident.getExecutionId()).thenReturn(EXAMPLE_INCIDENT_EXECUTION_ID);
    when(incident.getActivityId()).thenReturn(EXAMPLE_INCIDENT_ACTIVITY_ID);
    when(incident.getFailedActivityId()).thenReturn(EXAMPLE_INCIDENT_FAILED_ACTIVITY_ID);
    when(incident.getProcessInstanceId()).thenReturn(EXAMPLE_INCIDENT_PROC_INST_ID);
    when(incident.getProcessDefinitionId()).thenReturn(EXAMPLE_INCIDENT_PROC_DEF_ID);
    when(incident.getCauseIncidentId()).thenReturn(EXAMPLE_INCIDENT_CAUSE_INCIDENT_ID);
    when(incident.getRootCauseIncidentId()).thenReturn(EXAMPLE_INCIDENT_ROOT_CAUSE_INCIDENT_ID);
    when(incident.getConfiguration()).thenReturn(EXAMPLE_INCIDENT_CONFIGURATION);
    when(incident.getIncidentMessage()).thenReturn(EXAMPLE_INCIDENT_MESSAGE);
    when(incident.getTenantId()).thenReturn(tenantId);
    when(incident.getJobDefinitionId()).thenReturn(EXAMPLE_JOB_DEFINITION_ID);
    when(incident.getAnnotation()).thenReturn(EXAMPLE_USER_OPERATION_ANNOTATION);

    return incident;
  }

  public static List<Incident> createMockIncidents() {
    List<Incident> entries = new ArrayList<>();
    entries.add(createMockIncident());
    return entries;
  }

  // Historic Incident ///////////////////////////////////////
  public static HistoricIncident createMockHistoricIncident() {
    return createMockHistoricIncident(EXAMPLE_TENANT_ID);
  }

  public static HistoricIncident createMockHistoricIncident(String tenantId) {
    HistoricIncident incident = mock(HistoricIncident.class);

    when(incident.getId()).thenReturn(EXAMPLE_HIST_INCIDENT_ID);
    when(incident.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HIST_INCIDENT_CREATE_TIME));
    when(incident.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HIST_INCIDENT_END_TIME));
    when(incident.getIncidentType()).thenReturn(EXAMPLE_HIST_INCIDENT_TYPE);
    when(incident.getExecutionId()).thenReturn(EXAMPLE_HIST_INCIDENT_EXECUTION_ID);
    when(incident.getActivityId()).thenReturn(EXAMPLE_HIST_INCIDENT_ACTIVITY_ID);
    when(incident.getFailedActivityId()).thenReturn(EXAMPLE_HIST_INCIDENT_FAILED_ACTIVITY_ID);
    when(incident.getProcessInstanceId()).thenReturn(EXAMPLE_HIST_INCIDENT_PROC_INST_ID);
    when(incident.getProcessDefinitionId()).thenReturn(EXAMPLE_HIST_INCIDENT_PROC_DEF_ID);
    when(incident.getProcessDefinitionKey()).thenReturn(EXAMPLE_HIST_INCIDENT_PROC_DEF_KEY);
    when(incident.getCauseIncidentId()).thenReturn(EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID);
    when(incident.getRootCauseIncidentId()).thenReturn(EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID);
    when(incident.getConfiguration()).thenReturn(EXAMPLE_HIST_INCIDENT_CONFIGURATION);
    when(incident.getIncidentMessage()).thenReturn(EXAMPLE_HIST_INCIDENT_MESSAGE);
    when(incident.isOpen()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_OPEN);
    when(incident.isDeleted()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_DELETED);
    when(incident.isResolved()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_RESOLVED);
    when(incident.getTenantId()).thenReturn(tenantId);
    when(incident.getJobDefinitionId()).thenReturn(EXAMPLE_JOB_DEFINITION_ID);
    when(incident.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HIST_INCIDENT_REMOVAL_TIME));
    when(incident.getRootProcessInstanceId()).thenReturn(EXAMPLE_HIST_INCIDENT_ROOT_PROC_INST_ID);
    when(incident.getAnnotation()).thenReturn(EXAMPLE_USER_OPERATION_ANNOTATION);

    return incident;
  }

  public static List<HistoricIncident> createMockHistoricIncidents() {
    List<HistoricIncident> entries = new ArrayList<>();
    entries.add(createMockHistoricIncident());
    return entries;
  }

  // case definition
  public static List<CaseDefinition> createMockCaseDefinitions() {
    List<CaseDefinition> mocks = new ArrayList<>();
    mocks.add(createMockCaseDefinition());
    return mocks;
  }

  public static List<CaseDefinition> createMockTwoCaseDefinitions() {
    List<CaseDefinition> mocks = new ArrayList<>();
    mocks.add(createMockCaseDefinition());
    mocks.add(createAnotherMockCaseDefinition());
    return mocks;
  }

  public static MockCaseDefinitionBuilder mockCaseDefinition() {
    return new MockCaseDefinitionBuilder()
        .id(EXAMPLE_CASE_DEFINITION_ID)
        .category(EXAMPLE_CASE_DEFINITION_CATEGORY)
        .name(EXAMPLE_CASE_DEFINITION_NAME)
        .key(EXAMPLE_CASE_DEFINITION_KEY)
        .version(EXAMPLE_CASE_DEFINITION_VERSION)
        .resource(EXAMPLE_CASE_DEFINITION_RESOURCE_NAME)
        .diagram(EXAMPLE_CASE_DEFINITION_DIAGRAM_RESOURCE_NAME)
        .deploymentId(EXAMPLE_DEPLOYMENT_ID);
  }

  public static CaseDefinition createMockCaseDefinition() {
    return mockCaseDefinition().build();
  }

  public static CaseDefinition createAnotherMockCaseDefinition() {
    return mockCaseDefinition()
       .id(ANOTHER_EXAMPLE_CASE_DEFINITION_ID)
       .tenantId(ANOTHER_EXAMPLE_TENANT_ID)
       .build();
  }

  // case instance
  public static List<CaseInstance> createMockCaseInstances() {
    List<CaseInstance> mocks = new ArrayList<>();
    mocks.add(createMockCaseInstance());
    return mocks;
  }

  public static CaseInstance createMockCaseInstance() {
    return createMockCaseInstance(EXAMPLE_TENANT_ID);
  }

  public static CaseInstance createMockCaseInstance(String tenantId) {
    CaseInstance mock = mock(CaseInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.isActive()).thenReturn(EXAMPLE_CASE_INSTANCE_IS_ACTIVE);
    when(mock.isCompleted()).thenReturn(EXAMPLE_CASE_INSTANCE_IS_COMPLETED);
    when(mock.isTerminated()).thenReturn(EXAMPLE_CASE_INSTANCE_IS_TERMINATED);

    return mock;
  }

  // case execution
  public static List<CaseExecution> createMockCaseExecutions() {
    List<CaseExecution> mocks = new ArrayList<>();
    mocks.add(createMockCaseExecution());
    return mocks;
  }

  public static CaseExecution createMockCaseExecution() {
    return createMockCaseExecution(EXAMPLE_TENANT_ID);
  }

  public static CaseExecution createMockCaseExecution(String tenantId) {
    CaseExecution mock = mock(CaseExecution.class);

    when(mock.getId()).thenReturn(EXAMPLE_CASE_EXECUTION_ID);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_EXECUTION_CASE_INSTANCE_ID);
    when(mock.getParentId()).thenReturn(EXAMPLE_CASE_EXECUTION_PARENT_ID);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_EXECUTION_CASE_DEFINITION_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_CASE_EXECUTION_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_CASE_EXECUTION_ACTIVITY_NAME);
    when(mock.getActivityType()).thenReturn(EXAMPLE_CASE_EXECUTION_ACTIVITY_TYPE);
    when(mock.getActivityDescription()).thenReturn(EXAMPLE_CASE_EXECUTION_ACTIVITY_DESCRIPTION);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.isRequired()).thenReturn(EXAMPLE_CASE_EXECUTION_IS_REQUIRED);
    when(mock.isActive()).thenReturn(EXAMPLE_CASE_EXECUTION_IS_ACTIVE);
    when(mock.isEnabled()).thenReturn(EXAMPLE_CASE_EXECUTION_IS_ENABLED);
    when(mock.isDisabled()).thenReturn(EXAMPLE_CASE_EXECUTION_IS_DISABLED);

    return mock;
  }

  public static VariableMap createMockFormVariables() {
    VariableMap mock = Variables.createVariables();
    mock.putValueTyped(EXAMPLE_VARIABLE_INSTANCE_NAME, EXAMPLE_PRIMITIVE_VARIABLE_VALUE);
    return mock;
  }

  public static List<Filter> createMockFilters() {
    List<Filter> mocks = new ArrayList<>();
    mocks.add(createMockFilter(EXAMPLE_FILTER_ID));
    mocks.add(createMockFilter(ANOTHER_EXAMPLE_FILTER_ID));
    return mocks;
  }

  public static Filter createMockFilter() {
    return createMockFilter(EXAMPLE_FILTER_ID);
  }

  public static Filter createMockFilter(String id) {
    return createMockFilter(id, EXAMPLE_FILTER_QUERY);
  }

  public static Filter createMockFilter(String id, Query<?, ?> query) {
    Filter mock = mockFilter()
      .id(id)
      .resourceType(EXAMPLE_FILTER_RESOURCE_TYPE)
      .name(EXAMPLE_FILTER_NAME)
      .owner(EXAMPLE_FILTER_OWNER)
      .query(query)
      .properties(EXAMPLE_FILTER_PROPERTIES)
      .build();

    doThrow(new NotValidException("Name must not be null"))
      .when(mock).setName(null);
    doThrow(new NotValidException("Name must not be empty"))
      .when(mock).setName("");
    doThrow(new NotValidException("Query must not be null"))
      .when(mock).setQuery(null);

    return mock;
  }

  public static MockFilterBuilder mockFilter() {
    return new MockFilterBuilder()
      .id(EXAMPLE_FILTER_ID)
      .resourceType(EXAMPLE_FILTER_RESOURCE_TYPE)
      .name(EXAMPLE_FILTER_NAME)
      .owner(EXAMPLE_FILTER_OWNER)
      .query(EXAMPLE_FILTER_QUERY)
      .properties(EXAMPLE_FILTER_PROPERTIES);
  }

  public static FilterQuery createMockFilterQuery() {
    List<Filter> mockFilters = createMockFilters();

    FilterQuery query = mock(FilterQuery.class);

    when(query.list()).thenReturn(mockFilters);
    when(query.count()).thenReturn((long) mockFilters.size());
    when(query.filterId(any())).thenReturn(query);
    when(query.singleResult()).thenReturn(mockFilters.get(0));

    FilterQuery nonExistingQuery = mock(FilterQuery.class);
    when(query.filterId(NON_EXISTING_ID)).thenReturn(nonExistingQuery);
    when(nonExistingQuery.singleResult()).thenReturn(null);

    return query;

  }

  public static MetricsQuery createMockMeterQuery() {

    MetricsQuery query = mock(MetricsQuery.class);

    when(query.name(any())).thenReturn(query);
    when(query.reporter(any())).thenReturn(query);
    when(query.limit(anyInt())).thenReturn(query);
    when(query.offset(anyInt())).thenReturn(query);
    when(query.startDate(any())).thenReturn(query);
    when(query.endDate(any())).thenReturn(query);

    return query;

  }

  public static List<MetricIntervalValue> createMockMetricIntervalResult() {
    List<MetricIntervalValue> metrics = new ArrayList<>();

    MetricIntervalEntity entity1 = new MetricIntervalEntity(new Date(15 * 60 * 1000 * 1), EXAMPLE_METRICS_NAME, EXAMPLE_METRICS_REPORTER);
    entity1.setValue(21);

    MetricIntervalEntity entity2 = new MetricIntervalEntity(new Date(15 * 60 * 1000 * 2), EXAMPLE_METRICS_NAME, EXAMPLE_METRICS_REPORTER);
    entity2.setValue(22);

    MetricIntervalEntity entity3 = new MetricIntervalEntity(new Date(15 * 60 * 1000 * 3), EXAMPLE_METRICS_NAME, EXAMPLE_METRICS_REPORTER);
    entity3.setValue(23);

    metrics.add(entity3);
    metrics.add(entity2);
    metrics.add(entity1);

    return metrics;
  }

  // decision definition
  public static List<DecisionDefinition> createMockDecisionDefinitions() {
    List<DecisionDefinition> mocks = new ArrayList<>();
    mocks.add(createMockDecisionDefinition());
    return mocks;
  }

  public static List<DecisionDefinition> createMockTwoDecisionDefinitions() {
    List<DecisionDefinition> mocks = new ArrayList<>();
    mocks.add(createMockDecisionDefinition());
    mocks.add(createAnotherMockDecisionDefinition());
    return mocks;
  }

  public static MockDecisionDefinitionBuilder mockDecisionDefinition() {
    MockDecisionDefinitionBuilder builder = new MockDecisionDefinitionBuilder();

    return builder
      .id(EXAMPLE_DECISION_DEFINITION_ID)
      .category(EXAMPLE_DECISION_DEFINITION_CATEGORY)
      .name(EXAMPLE_DECISION_DEFINITION_NAME)
      .key(EXAMPLE_DECISION_DEFINITION_KEY)
      .version(EXAMPLE_DECISION_DEFINITION_VERSION)
      .resource(EXAMPLE_DECISION_DEFINITION_RESOURCE_NAME)
      .diagram(EXAMPLE_DECISION_DEFINITION_DIAGRAM_RESOURCE_NAME)
      .deploymentId(EXAMPLE_DEPLOYMENT_ID)
      .decisionRequirementsDefinitionId(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID)
      .decisionRequirementsDefinitionKey(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);
  }

  public static DecisionDefinition createMockDecisionDefinition() {
    return mockDecisionDefinition().build();
  }

  public static DecisionDefinition createAnotherMockDecisionDefinition() {
    return mockDecisionDefinition()
      .id(ANOTHER_EXAMPLE_DECISION_DEFINITION_ID)
      .tenantId(ANOTHER_EXAMPLE_TENANT_ID)
      .build();
  }

  // decision requirements definition
  public static MockDecisionRequirementsDefinitionBuilder mockDecisionRequirementsDefinition() {
    MockDecisionRequirementsDefinitionBuilder builder = new MockDecisionRequirementsDefinitionBuilder();

    return builder
      .id(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID)
      .category(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_CATEGORY)
      .name(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_NAME)
      .key(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY)
      .version(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_VERSION)
      .resource(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_RESOURCE_NAME)
      .diagram(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_DIAGRAM_RESOURCE_NAME)
      .deploymentId(EXAMPLE_DEPLOYMENT_ID);
  }

  public static DecisionRequirementsDefinition createMockDecisionRequirementsDefinition() {
    return mockDecisionRequirementsDefinition().build();
  }

  public static DecisionRequirementsDefinition createAnotherMockDecisionRequirementsDefinition() {
    return mockDecisionRequirementsDefinition()
      .id(ANOTHER_EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID)
      .tenantId(ANOTHER_EXAMPLE_TENANT_ID)
      .build();
  }

  public static List<DecisionRequirementsDefinition> createMockDecisionRequirementsDefinitions() {
    List<DecisionRequirementsDefinition> mocks = new ArrayList<>();
    mocks.add(createMockDecisionRequirementsDefinition());
    return mocks;
  }

  public static List<DecisionRequirementsDefinition> createMockTwoDecisionRequirementsDefinitions() {
    List<DecisionRequirementsDefinition> mocks = new ArrayList<>();
    mocks.add(createMockDecisionRequirementsDefinition());
    mocks.add(createAnotherMockDecisionRequirementsDefinition());
    return mocks;
  }

  // Historic job log

  public static List<HistoricJobLog> createMockHistoricJobLogs() {
    List<HistoricJobLog> mocks = new ArrayList<>();
    mocks.add(createMockHistoricJobLog());
    return mocks;
  }

  public static HistoricJobLog createMockHistoricJobLog() {
    return createMockHistoricJobLog(EXAMPLE_TENANT_ID);
  }

  public static HistoricJobLog createMockHistoricJobLog(String tenantId) {
    HistoricJobLog mock = mock(HistoricJobLog.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_ID);
    when(mock.getTimestamp()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_JOB_LOG_TIMESTAMP));
    when(mock.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_JOB_LOG_REMOVAL_TIME));

    when(mock.getJobId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_JOB_ID);
    when(mock.getJobDueDate()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_JOB_LOG_JOB_DUE_DATE));
    when(mock.getJobRetries()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_JOB_RETRIES);
    when(mock.getJobPriority()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_JOB_PRIORITY);
    when(mock.getJobExceptionMessage()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_JOB_EXCEPTION_MSG);

    when(mock.getJobDefinitionId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_ID);
    when(mock.getJobDefinitionType()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_TYPE);
    when(mock.getJobDefinitionConfiguration()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_CONFIG);

    when(mock.getActivityId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_ACTIVITY_ID);
    when(mock.getFailedActivityId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_FAILED_ACTIVITY_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_PROC_INST_ID);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_PROC_DEF_ID);
    when(mock.getProcessDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_PROC_DEF_KEY);
    when(mock.getDeploymentId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_DEPLOYMENT_ID);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_ROOT_PROC_INST_ID);
    when(mock.getHostname()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_HOSTNAME);

    when(mock.isCreationLog()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_IS_CREATION_LOG);
    when(mock.isFailureLog()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_IS_FAILURE_LOG);
    when(mock.isSuccessLog()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_IS_SUCCESS_LOG);
    when(mock.isDeletionLog()).thenReturn(EXAMPLE_HISTORIC_JOB_LOG_IS_DELETION_LOG);

    return mock;
  }

  // Historic decision instance

  public static List<HistoricDecisionInstance> createMockHistoricDecisionInstances() {
    List<HistoricDecisionInstance> mockList = new ArrayList<>();
    mockList.add(createMockHistoricDecisionInstance());
    return mockList;
  }

  public static HistoricDecisionInstance createMockHistoricDecisionInstanceBase() {
    return createMockHistoricDecisionInstanceBase(EXAMPLE_TENANT_ID);
  }

  public static HistoricDecisionInstance createMockHistoricDecisionInstanceBase(String tenantId) {
    HistoricDecisionInstance mock = mock(HistoricDecisionInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    when(mock.getDecisionDefinitionId()).thenReturn(EXAMPLE_DECISION_DEFINITION_ID);
    when(mock.getDecisionDefinitionKey()).thenReturn(EXAMPLE_DECISION_DEFINITION_KEY);
    when(mock.getDecisionDefinitionName()).thenReturn(EXAMPLE_DECISION_DEFINITION_NAME);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessDefinitionKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_DEFINITION_ID);
    when(mock.getCaseDefinitionKey()).thenReturn(EXAMPLE_CASE_DEFINITION_KEY);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID);
    when(mock.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID);
    when(mock.getEvaluationTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATION_TIME));
    when(mock.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_DECISION_INSTANCE_REMOVAL_TIME));
    when(mock.getUserId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_USER_ID);
    when(mock.getCollectResultValue()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_COLLECT_RESULT_VALUE);
    when(mock.getRootDecisionInstanceId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    when(mock.getRootProcessInstanceId()).thenReturn(EXAMPLE_ROOT_HISTORIC_PROCESS_INSTANCE_ID);
    when(mock.getDecisionRequirementsDefinitionId()).thenReturn(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    when(mock.getDecisionRequirementsDefinitionKey()).thenReturn(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);
    when(mock.getTenantId()).thenReturn(tenantId);

    return mock;
  }

  public static HistoricDecisionInstance createMockHistoricDecisionInstance() {
    HistoricDecisionInstance mock = createMockHistoricDecisionInstanceBase();
    when(mock.getInputs()).thenThrow(new ProcessEngineException("ENGINE-03060 The input instances for the historic decision instance are not fetched. You must call 'includeInputs()' on the query to enable fetching."));
    when(mock.getOutputs()).thenThrow(new ProcessEngineException("ENGINE-03061 The output instances for the historic decision instance are not fetched. You must call 'includeOutputs()' on the query to enable fetching."));
    return mock;
  }

  public static HistoricDecisionInstance createMockHistoricDecisionInstanceWithInputs() {
    List<HistoricDecisionInputInstance> inputs = createMockHistoricDecisionInputInstances();

    HistoricDecisionInstance mock = createMockHistoricDecisionInstanceBase();
    when(mock.getInputs()).thenReturn(inputs);
    when(mock.getOutputs()).thenThrow(new ProcessEngineException("ENGINE-03061 The output instances for the historic decision instance are not fetched. You must call 'includeOutputs()' on the query to enable fetching."));
    return mock;
  }

  public static HistoricDecisionInstance createMockHistoricDecisionInstanceWithOutputs() {
    List<HistoricDecisionOutputInstance> outputs = createMockHistoricDecisionOutputInstances();

    HistoricDecisionInstance mock = createMockHistoricDecisionInstanceBase();
    when(mock.getInputs()).thenThrow(new ProcessEngineException("ENGINE-03060 The input instances for the historic decision instance are not fetched. You must call 'includeInputs()' on the query to enable fetching."));
    when(mock.getOutputs()).thenReturn(outputs);
    return mock;
  }

  public static HistoricDecisionInstance createMockHistoricDecisionInstanceWithInputsAndOutputs() {
    List<HistoricDecisionInputInstance> inputs = createMockHistoricDecisionInputInstances();
    List<HistoricDecisionOutputInstance> outputs = createMockHistoricDecisionOutputInstances();

    HistoricDecisionInstance mock = createMockHistoricDecisionInstanceBase();
    when(mock.getInputs()).thenReturn(inputs);
    when(mock.getOutputs()).thenReturn(outputs);
    return mock;
  }

  public static List<HistoricDecisionInputInstance> createMockHistoricDecisionInputInstances() {
    List<HistoricDecisionInputInstance> mockInputs = new ArrayList<>();
    mockInputs.add(createMockHistoricDecisionInput(EXAMPLE_HISTORIC_DECISION_STRING_VALUE));
    mockInputs.add(createMockHistoricDecisionInput(EXAMPLE_HISTORIC_DECISION_BYTE_ARRAY_VALUE));
    mockInputs.add(createMockHistoricDecisionInput(EXAMPLE_HISTORIC_DECISION_SERIALIZED_VALUE));
    return mockInputs;
  }

  public static HistoricDecisionInputInstance createMockHistoricDecisionInput(TypedValue typedValue) {
    HistoricDecisionInputInstance input = mock(HistoricDecisionInputInstance.class);
    when(input.getId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_ID);
    when(input.getDecisionInstanceId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    when(input.getClauseId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CLAUSE_ID);
    when(input.getClauseName()).thenReturn(EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CLAUSE_NAME);
    when(input.getTypedValue()).thenReturn(typedValue);
    when(input.getErrorMessage()).thenReturn(null);
    when(input.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CREATE_TIME));
    when(input.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_REMOVAL_TIME));
    when(input.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INPUT_ROOT_PROCESS_INSTANCE_ID);

    return input;
  }

  public static List<HistoricDecisionOutputInstance> createMockHistoricDecisionOutputInstances() {
    List<HistoricDecisionOutputInstance> mockOutputs = new ArrayList<>();
    mockOutputs.add(createMockHistoricDecisionOutput(EXAMPLE_HISTORIC_DECISION_STRING_VALUE));
    mockOutputs.add(createMockHistoricDecisionOutput(EXAMPLE_HISTORIC_DECISION_BYTE_ARRAY_VALUE));
    mockOutputs.add(createMockHistoricDecisionOutput(EXAMPLE_HISTORIC_DECISION_SERIALIZED_VALUE));
    return mockOutputs;
  }

  public static HistoricDecisionOutputInstance createMockHistoricDecisionOutput(TypedValue typedValue) {
    HistoricDecisionOutputInstance output = mock(HistoricDecisionOutputInstance.class);
    when(output.getId()).thenReturn(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_ID);
    when(output.getDecisionInstanceId()).thenReturn(EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    when(output.getClauseId()).thenReturn(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CLAUSE_ID);
    when(output.getClauseName()).thenReturn(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CLAUSE_NAME);
    when(output.getRuleId()).thenReturn(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_RULE_ID);
    when(output.getRuleOrder()).thenReturn(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_RULE_ORDER);
    when(output.getVariableName()).thenReturn(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_VARIABLE_NAME);
    when(output.getTypedValue()).thenReturn(typedValue);
    when(output.getErrorMessage()).thenReturn(null);
    when(output.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CREATE_TIME));
    when(output.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_REMOVAL_TIME));
    when(output.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_DECISION_OUTPUT_ROOT_PROCESS_INSTANCE_ID);
    return output;
  }

  // external task

  public static MockExternalTaskBuilder mockExternalTask() {
    return new MockExternalTaskBuilder()
      .id(EXTERNAL_TASK_ID)
      .activityId(EXAMPLE_ACTIVITY_ID)
      .activityInstanceId(EXAMPLE_ACTIVITY_INSTANCE_ID)
      .errorMessage(EXTERNAL_TASK_ERROR_MESSAGE)
      .executionId(EXAMPLE_EXECUTION_ID)
      .lockExpirationTime(DateTimeUtil.parseDate(EXTERNAL_TASK_LOCK_EXPIRATION_TIME))
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY)
      .processDefinitionVersionTag(EXAMPLE_PROCESS_DEFINITION_VERSION_TAG)
      .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
      .retries(EXTERNAL_TASK_RETRIES)
      .suspended(EXTERNAL_TASK_SUSPENDED)
      .topicName(EXTERNAL_TASK_TOPIC_NAME)
      .workerId(EXTERNAL_TASK_WORKER_ID)
      .tenantId(EXAMPLE_TENANT_ID)
      .priority(EXTERNAL_TASK_PRIORITY)
      .businessKey(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);

  }

  public static ExternalTask createMockExternalTask() {
    return mockExternalTask().buildExternalTask();
  }

  public static LockedExternalTask createMockLockedExternalTask() {
    return mockExternalTask()
      .variable(EXAMPLE_VARIABLE_INSTANCE_NAME, EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
      .buildLockedExternalTask();
  }

  public static List<ExternalTask> createMockExternalTasks() {
    List<ExternalTask> mocks = new ArrayList<>();
    mocks.add(createMockExternalTask());
    return mocks;
  }

  public static MockDecisionResultBuilder mockDecisionResult() {
    return new MockDecisionResultBuilder()
        .resultEntries()
          .entry(EXAMPLE_DECISION_OUTPUT_KEY, EXAMPLE_DECISION_OUTPUT_VALUE)
          .endResultEntries();
  }

  public static DmnDecisionResult createMockDecisionResult() {
    return mockDecisionResult().build();
  }

  public static MockBatchBuilder mockBatch() {
    return new MockBatchBuilder()
      .id(EXAMPLE_BATCH_ID)
      .type(EXAMPLE_BATCH_TYPE)
      .totalJobs(EXAMPLE_BATCH_TOTAL_JOBS)
      .jobsCreated(EXAMPLE_BATCH_JOBS_CREATED)
      .batchJobsPerSeed(EXAMPLE_BATCH_JOBS_PER_SEED)
      .invocationsPerBatchJob(EXAMPLE_INVOCATIONS_PER_BATCH_JOB)
      .seedJobDefinitionId(EXAMPLE_SEED_JOB_DEFINITION_ID)
      .monitorJobDefinitionId(EXAMPLE_MONITOR_JOB_DEFINITION_ID)
      .batchJobDefinitionId(EXAMPLE_BATCH_JOB_DEFINITION_ID)
      .suspended()
      .createUserId(EXAMPLE_USER_ID)
      .startTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_BATCH_START_TIME))
      .executionStartTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_BATCH_START_TIME))
      .tenantId(EXAMPLE_TENANT_ID);
  }

  public static Batch createMockBatch() {
    return mockBatch().build();
  }

  public static List<Batch> createMockBatches() {
    List<Batch> mockList = new ArrayList<>();
    mockList.add(createMockBatch());
    return mockList;
  }

  public static MockHistoricBatchBuilder mockHistoricBatch() {
    return new MockHistoricBatchBuilder()
      .id(EXAMPLE_BATCH_ID)
      .type(EXAMPLE_BATCH_TYPE)
      .totalJobs(EXAMPLE_BATCH_TOTAL_JOBS)
      .batchJobsPerSeed(EXAMPLE_BATCH_JOBS_PER_SEED)
      .invocationsPerBatchJob(EXAMPLE_INVOCATIONS_PER_BATCH_JOB)
      .seedJobDefinitionId(EXAMPLE_SEED_JOB_DEFINITION_ID)
      .monitorJobDefinitionId(EXAMPLE_MONITOR_JOB_DEFINITION_ID)
      .batchJobDefinitionId(EXAMPLE_BATCH_JOB_DEFINITION_ID)
      .tenantId(EXAMPLE_TENANT_ID)
      .createUserId(EXAMPLE_USER_ID)
      .startTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_BATCH_START_TIME))
      .executionStartTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_BATCH_START_TIME))
      .endTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_BATCH_END_TIME))
      .removalTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_BATCH_REMOVAL_TIME));
  }

  public static HistoricBatch createMockHistoricBatch() {
    return mockHistoricBatch().build();
  }

  public static List<HistoricBatch> createMockHistoricBatches() {
    List<HistoricBatch> mockList = new ArrayList<>();
    mockList.add(createMockHistoricBatch());
    return mockList;
  }

  public static MockBatchStatisticsBuilder mockBatchStatistics() {
    return new MockBatchStatisticsBuilder()
      .id(EXAMPLE_BATCH_ID)
      .type(EXAMPLE_BATCH_TYPE)
      .size(EXAMPLE_BATCH_TOTAL_JOBS)
      .jobsCreated(EXAMPLE_BATCH_JOBS_CREATED)
      .batchJobsPerSeed(EXAMPLE_BATCH_JOBS_PER_SEED)
      .invocationsPerBatchJob(EXAMPLE_INVOCATIONS_PER_BATCH_JOB)
      .seedJobDefinitionId(EXAMPLE_SEED_JOB_DEFINITION_ID)
      .monitorJobDefinitionId(EXAMPLE_MONITOR_JOB_DEFINITION_ID)
      .batchJobDefinitionId(EXAMPLE_BATCH_JOB_DEFINITION_ID)
      .tenantId(EXAMPLE_TENANT_ID)
      .createUserId(EXAMPLE_USER_ID)
      .suspended()
      .startTime(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_BATCH_START_TIME))
      .remainingJobs(EXAMPLE_BATCH_REMAINING_JOBS)
      .completedJobs(EXAMPLE_BATCH_COMPLETED_JOBS)
      .failedJobs(EXAMPLE_BATCH_FAILED_JOBS);
  }

  public static BatchStatistics createMockBatchStatistics() {
    return mockBatchStatistics().build();
  }

  public static List<BatchStatistics> createMockBatchStatisticsList() {
    ArrayList<BatchStatistics> mockList = new ArrayList<>();
    mockList.add(createMockBatchStatistics());
    return mockList;
  }


  public static MessageCorrelationResult createMessageCorrelationResult(MessageCorrelationResultType type) {
    MessageCorrelationResult result = mock(MessageCorrelationResult.class);
    when(result.getResultType()).thenReturn(type);
    if (result.getResultType().equals(MessageCorrelationResultType.Execution)) {
      Execution ex = createMockExecution();
      when(result.getExecution()).thenReturn(ex);
    } else {
      ProcessInstance instance = createMockInstance();
      when(result.getProcessInstance()).thenReturn(instance);
    }
    return result;
  }

  public static MessageCorrelationResultWithVariables createMessageCorrelationResultWithVariables(MessageCorrelationResultType type) {
    MessageCorrelationResultWithVariables result = mock(MessageCorrelationResultWithVariables.class);
    when(result.getResultType()).thenReturn(type);
    if (result.getResultType().equals(MessageCorrelationResultType.Execution)) {
      Execution ex = createMockExecution();
      when(result.getExecution()).thenReturn(ex);
    } else {
      ProcessInstance instance = createMockInstance();
      when(result.getProcessInstance()).thenReturn(instance);
    }
    when(result.getVariables()).thenReturn(createMockSerializedVariables());
    return result;
  }

  public static List<MessageCorrelationResult> createMessageCorrelationResultList(MessageCorrelationResultType type) {
    List<MessageCorrelationResult> list = new ArrayList<>();
    list.add(createMessageCorrelationResult(type));
    list.add(createMessageCorrelationResult(type));
    return list;
  }

  public static List<HistoricDecisionInstanceStatistics> createMockHistoricDecisionStatistics() {
    HistoricDecisionInstanceStatistics statistics = mock(HistoricDecisionInstanceStatistics.class);

    when(statistics.getDecisionDefinitionKey()).thenReturn(EXAMPLE_DECISION_DEFINITION_KEY);
    when(statistics.getEvaluations()).thenReturn(1);


    HistoricDecisionInstanceStatistics anotherStatistics = mock(HistoricDecisionInstanceStatistics.class);

    when(anotherStatistics.getDecisionDefinitionKey()).thenReturn(ANOTHER_DECISION_DEFINITION_KEY);
    when(anotherStatistics.getEvaluations()).thenReturn(2);

    List<HistoricDecisionInstanceStatistics> decisionResults = new ArrayList<>();
    decisionResults.add(statistics);
    decisionResults.add(anotherStatistics);

    return decisionResults;
  }

  // historic external task log
  public static List<HistoricExternalTaskLog> createMockHistoricExternalTaskLogs() {
    List<HistoricExternalTaskLog> mocks = new ArrayList<>();
    mocks.add(createMockHistoricExternalTaskLog());
    return mocks;
  }

  public static HistoricExternalTaskLog createMockHistoricExternalTaskLog() {
    return createMockHistoricExternalTaskLog(EXAMPLE_TENANT_ID);
  }

  public static HistoricExternalTaskLog createMockHistoricExternalTaskLog(String tenantId) {
    HistoricExternalTaskLog mock = mock(HistoricExternalTaskLog.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID);
    when(mock.getTimestamp()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TIMESTAMP));
    when(mock.getRemovalTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_REMOVAL_TIME));

    when(mock.getExternalTaskId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXTERNAL_TASK_ID);
    when(mock.getTopicName()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TOPIC_NAME);
    when(mock.getWorkerId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_WORKER_ID);
    when(mock.getRetries()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_RETRIES);
    when(mock.getPriority()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PRIORITY);
    when(mock.getErrorMessage()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ERROR_MSG);

    when(mock.getActivityId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_ID);
    when(mock.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_INSTANCE_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_INST_ID);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_ID);
    when(mock.getProcessDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_KEY);
    when(mock.getTenantId()).thenReturn(tenantId);
    when(mock.getRootProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ROOT_PROC_INST_ID);

    when(mock.isCreationLog()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_CREATION_LOG);
    when(mock.isFailureLog()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_FAILURE_LOG);
    when(mock.isSuccessLog()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_SUCCESS_LOG);
    when(mock.isDeletionLog()).thenReturn(EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_DELETION_LOG);

    return mock;
  }

  public static SetJobRetriesByJobsAsyncBuilder createMockSetJobRetriesByJobsAsyncBuilder(ManagementService mockManagementService) {
    Batch batchEntity = MockProvider.createMockBatch();
    SetJobRetriesByJobsAsyncBuilder mockSetJobRetriesByJobsAsyncBuilder = mock(SetJobRetriesByJobsAsyncBuilder.class);
    when(mockManagementService.setJobRetriesByJobsAsync(anyInt())).thenReturn(mockSetJobRetriesByJobsAsyncBuilder);
    when(mockSetJobRetriesByJobsAsyncBuilder.jobIds(any())).thenReturn(mockSetJobRetriesByJobsAsyncBuilder);
    when(mockSetJobRetriesByJobsAsyncBuilder.jobQuery(any())).thenReturn(mockSetJobRetriesByJobsAsyncBuilder);
    when(mockSetJobRetriesByJobsAsyncBuilder.dueDate(any())).thenReturn(mockSetJobRetriesByJobsAsyncBuilder);
    when(mockSetJobRetriesByJobsAsyncBuilder.executeAsync()).thenReturn(batchEntity);
    return mockSetJobRetriesByJobsAsyncBuilder;
  }

  public static SetJobRetriesByProcessAsyncBuilder createMockSetJobRetriesByProcessAsyncBuilder(ManagementService mockManagementService) {
    Batch batchEntity = MockProvider.createMockBatch();
    SetJobRetriesByProcessAsyncBuilder mockSetJobRetriesByProcessAsyncBuilder = mock(SetJobRetriesByProcessAsyncBuilder.class);
    when(mockManagementService.setJobRetriesByProcessAsync(anyInt())).thenReturn(mockSetJobRetriesByProcessAsyncBuilder);
    when(mockSetJobRetriesByProcessAsyncBuilder.processInstanceIds(any())).thenReturn(mockSetJobRetriesByProcessAsyncBuilder);
    when(mockSetJobRetriesByProcessAsyncBuilder.processInstanceQuery(any())).thenReturn(mockSetJobRetriesByProcessAsyncBuilder);
    when(mockSetJobRetriesByProcessAsyncBuilder.historicProcessInstanceQuery(any())).thenReturn(mockSetJobRetriesByProcessAsyncBuilder);
    when(mockSetJobRetriesByProcessAsyncBuilder.dueDate(any())).thenReturn(mockSetJobRetriesByProcessAsyncBuilder);
    when(mockSetJobRetriesByProcessAsyncBuilder.executeAsync()).thenReturn(batchEntity);
    return mockSetJobRetriesByProcessAsyncBuilder;
  }

  public static SetJobRetriesBuilder createMockSetJobRetriesBuilder(ManagementService mockManagementService) {
    SetJobRetriesBuilder mockSetJobRetriesBuilder = mock(SetJobRetriesBuilder.class);
    when(mockManagementService.setJobRetries(anyInt())).thenReturn(mockSetJobRetriesBuilder);
    when(mockSetJobRetriesBuilder.jobId(any())).thenReturn(mockSetJobRetriesBuilder);
    when(mockSetJobRetriesBuilder.jobIds(any())).thenReturn(mockSetJobRetriesBuilder);
    when(mockSetJobRetriesBuilder.jobDefinitionId(any())).thenReturn(mockSetJobRetriesBuilder);
    when(mockSetJobRetriesBuilder.dueDate(any())).thenReturn(mockSetJobRetriesBuilder);
    return mockSetJobRetriesBuilder;
  }
}
