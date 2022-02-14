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
package org.camunda.bpm.engine;

/**
 * @author Sebastian Menski
 */
public class EntityTypes {

  public static final String APPLICATION = "Application";
  public static final String ATTACHMENT = "Attachment";
  public static final String AUTHORIZATION = "Authorization";
  public static final String FILTER = "Filter";
  public static final String GROUP = "Group";
  public static final String GROUP_MEMBERSHIP = "Group membership";
  public static final String IDENTITY_LINK = "IdentityLink";
  public static final String TASK = "Task";
  public static final String HISTORIC_TASK = "HistoricTask";
  public static final String USER = "User";
  public static final String PROCESS_INSTANCE = "ProcessInstance";
  public static final String HISTORIC_PROCESS_INSTANCE = "HistoricProcessInstance";
  public static final String PROCESS_DEFINITION = "ProcessDefinition";
  public static final String JOB = "Job";
  public static final String JOB_DEFINITION = "JobDefinition";
  public static final String VARIABLE = "Variable";
  public static final String DEPLOYMENT = "Deployment";
  public static final String DECISION_DEFINITION = "DecisionDefinition";
  public static final String CASE_DEFINITION = "CaseDefinition";
  public static final String EXTERNAL_TASK = "ExternalTask";
  public static final String TENANT = "Tenant";
  public static final String TENANT_MEMBERSHIP = "TenantMembership";
  public static final String BATCH = "Batch";
  public static final String DECISION_REQUIREMENTS_DEFINITION = "DecisionRequirementsDefinition";
  public static final String DECISION_INSTANCE = "DecisionInstance";
  public static final String REPORT = "Report";
  public static final String DASHBOARD = "Dashboard";
  public static final String METRICS = "Metrics";
  public static final String TASK_METRICS = "TaskMetrics";
  public static final String CASE_INSTANCE = "CaseInstance";
  public static final String PROPERTY = "Property";
  public static final String OPERATION_LOG_CATEGORY = "OperationLogCatgeory";
  public static final String OPTIMIZE = "Optimize";
  public static final String OPERATION_LOG = "OperationLog";
  public static final String INCIDENT = "Incident";
  public static final String SYSTEM = "System";
}
