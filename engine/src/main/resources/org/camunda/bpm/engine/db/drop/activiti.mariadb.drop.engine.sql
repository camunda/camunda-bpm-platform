--
-- Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. Camunda licenses this file to you under the Apache License,
-- Version 2.0; you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

drop index ACT_IDX_BYTEARRAY_ROOT_PI on ACT_GE_BYTEARRAY;
drop index ACT_IDX_BYTEARRAY_RM_TIME on ACT_GE_BYTEARRAY;
drop index ACT_IDX_EXEC_ROOT_PI on ACT_RU_EXECUTION;
drop index ACT_IDX_EXEC_BUSKEY on ACT_RU_EXECUTION;
drop index ACT_IDX_TASK_CREATE on ACT_RU_TASK;
drop index ACT_IDX_TASK_ASSIGNEE on ACT_RU_TASK;
drop index ACT_IDX_TASK_OWNER on ACT_RU_TASK;
drop index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK;
drop index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK;
drop index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE;
drop index ACT_IDX_VARIABLE_TASK_NAME_TYPE on ACT_RU_VARIABLE;
drop index ACT_IDX_INC_CONFIGURATION on ACT_RU_INCIDENT;
drop index ACT_IDX_JOB_PROCINST on ACT_RU_JOB;
drop index ACT_IDX_AUTH_GROUP_ID on ACT_RU_AUTHORIZATION;

-- new metric milliseconds column
DROP INDEX ACT_IDX_METER_LOG_MS ON ACT_RU_METER_LOG;
DROP INDEX ACT_IDX_METER_LOG_NAME_MS ON ACT_RU_METER_LOG;
DROP INDEX ACT_IDX_METER_LOG_REPORT ON ACT_RU_METER_LOG;

-- old metric timestamp column
DROP INDEX ACT_IDX_METER_LOG_TIME ON ACT_RU_METER_LOG;
DROP INDEX ACT_IDX_METER_LOG ON ACT_RU_METER_LOG;

-- task metric timestamp column
drop index ACT_IDX_TASK_METER_LOG_TIME on ACT_RU_TASK_METER_LOG;


drop index ACT_IDX_EXT_TASK_TOPIC on ACT_RU_EXT_TASK;

alter table ACT_GE_BYTEARRAY
    drop FOREIGN KEY ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop FOREIGN KEY ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION
    drop FOREIGN KEY ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION
    drop FOREIGN KEY ACT_FK_EXE_SUPER;

alter table ACT_RU_EXECUTION
    drop FOREIGN KEY ACT_FK_EXE_PROCDEF;

alter table ACT_RU_IDENTITYLINK
    drop FOREIGN KEY ACT_FK_TSKASS_TASK;

alter table ACT_RU_IDENTITYLINK
    drop FOREIGN KEY ACT_FK_ATHRZ_PROCEDEF;

alter table ACT_RU_TASK
	drop FOREIGN KEY ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop FOREIGN KEY ACT_FK_TASK_PROCINST;

alter table ACT_RU_TASK
	drop FOREIGN KEY ACT_FK_TASK_PROCDEF;

alter table ACT_RU_VARIABLE
    drop FOREIGN KEY ACT_FK_VAR_EXE;

alter table ACT_RU_VARIABLE
	drop FOREIGN KEY ACT_FK_VAR_PROCINST;

alter table ACT_RU_VARIABLE
    drop FOREIGN KEY ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop FOREIGN KEY ACT_FK_JOB_EXCEPTION;

alter table ACT_RU_EVENT_SUBSCR
    drop FOREIGN KEY ACT_FK_EVENT_EXEC;

alter table ACT_RU_INCIDENT
    drop FOREIGN KEY ACT_FK_INC_EXE;

alter table ACT_RU_INCIDENT
    drop FOREIGN KEY ACT_FK_INC_PROCINST;

alter table ACT_RU_INCIDENT
    drop FOREIGN KEY ACT_FK_INC_PROCDEF;

alter table ACT_RU_INCIDENT
    drop FOREIGN KEY ACT_FK_INC_CAUSE;

alter table ACT_RU_INCIDENT
    drop FOREIGN KEY ACT_FK_INC_RCAUSE;

alter table ACT_RU_INCIDENT
    drop FOREIGN KEY ACT_FK_INC_JOB_DEF;

alter table ACT_RU_EXT_TASK
    drop FOREIGN KEY ACT_FK_EXT_TASK_EXE;

alter table ACT_RU_BATCH
    drop FOREIGN KEY ACT_FK_BATCH_SEED_JOB_DEF;

alter table ACT_RU_BATCH
    drop FOREIGN KEY ACT_FK_BATCH_MONITOR_JOB_DEF;

alter table ACT_RU_BATCH
    drop FOREIGN KEY ACT_FK_BATCH_JOB_DEF;

alter table ACT_RU_EXT_TASK
    drop FOREIGN KEY ACT_FK_EXT_TASK_ERROR_DETAILS;

alter table ACT_RU_VARIABLE
    drop FOREIGN KEY ACT_FK_VAR_BATCH;

drop index ACT_IDX_ATHRZ_PROCEDEF on ACT_RU_IDENTITYLINK;
drop index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR;

-- indexes for deadlock problems - https://app.camunda.com/jira/browse/CAM-2567
drop index ACT_IDX_INC_CAUSEINCID on ACT_RU_INCIDENT;
drop index ACT_IDX_INC_EXID on ACT_RU_INCIDENT;
drop index ACT_IDX_INC_PROCDEFID on ACT_RU_INCIDENT;
drop index ACT_IDX_INC_PROCINSTID on ACT_RU_INCIDENT;
drop index ACT_IDX_INC_ROOTCAUSEINCID on ACT_RU_INCIDENT;
drop index ACT_IDX_INC_JOB_DEF on ACT_RU_INCIDENT;
drop index ACT_IDX_AUTH_RESOURCE_ID on ACT_RU_AUTHORIZATION;
drop index ACT_IDX_EXT_TASK_EXEC on ACT_RU_EXT_TASK;

drop index ACT_IDX_BYTEARRAY_NAME on ACT_GE_BYTEARRAY;
drop index ACT_IDX_DEPLOYMENT_NAME on ACT_RE_DEPLOYMENT;
drop index ACT_IDX_JOBDEF_PROC_DEF_ID ON ACT_RU_JOBDEF;
drop index ACT_IDX_JOB_HANDLER_TYPE ON ACT_RU_JOB;
drop index ACT_IDX_EVENT_SUBSCR_EVT_NAME ON ACT_RU_EVENT_SUBSCR;
drop index ACT_IDX_PROCDEF_DEPLOYMENT_ID ON ACT_RE_PROCDEF;

drop index ACT_IDX_EXT_TASK_TENANT_ID on ACT_RU_EXT_TASK;
drop index ACT_IDX_EXT_TASK_PRIORITY on ACT_RU_EXT_TASK;
drop index ACT_IDX_EXT_TASK_ERR_DETAILS on ACT_RU_EXT_TASK;
drop index ACT_IDX_INC_TENANT_ID on ACT_RU_INCIDENT;
drop index ACT_IDX_JOBDEF_TENANT_ID ON ACT_RU_JOBDEF;
drop index ACT_IDX_JOB_TENANT_ID ON ACT_RU_JOB;
drop index ACT_IDX_EVENT_SUBSCR_TENANT_ID on ACT_RU_EVENT_SUBSCR;
drop index ACT_IDX_VARIABLE_TENANT_ID ON ACT_RU_VARIABLE;
drop index ACT_IDX_TASK_TENANT_ID ON ACT_RU_TASK;
drop index ACT_IDX_EXEC_TENANT_ID ON ACT_RU_EXECUTION;
drop index ACT_IDX_PROCDEF_TENANT_ID ON ACT_RE_PROCDEF;
drop index ACT_IDX_DEPLOYMENT_TENANT_ID ON ACT_RE_DEPLOYMENT;

drop index ACT_IDX_JOB_JOB_DEF_ID on ACT_RU_JOB;
drop index ACT_IDX_BATCH_SEED_JOB_DEF on ACT_RU_BATCH;
drop index ACT_IDX_BATCH_MONITOR_JOB_DEF on ACT_RU_BATCH;
drop index ACT_IDX_BATCH_JOB_DEF on ACT_RU_BATCH;

drop index ACT_IDX_PROCDEF_VER_TAG on ACT_RE_PROCDEF;

drop index ACT_IDX_JOB_EXECUTION_ID on ACT_RU_JOB;
drop index ACT_IDX_JOB_HANDLER on ACT_RU_JOB;

drop index ACT_IDX_AUTH_ROOT_PI on ACT_RU_AUTHORIZATION;
drop index ACT_IDX_AUTH_RM_TIME on ACT_RU_AUTHORIZATION;

drop index ACT_IDX_BATCH_ID on ACT_RU_VARIABLE;

drop table if exists ACT_GE_PROPERTY;
drop table if exists ACT_RU_VARIABLE;
drop table if exists ACT_GE_BYTEARRAY;
drop table if exists ACT_RE_DEPLOYMENT;
drop table if exists ACT_RU_IDENTITYLINK;
drop table if exists ACT_RU_TASK;
drop table if exists ACT_RE_PROCDEF;
drop table if exists ACT_RE_CAMFORMDEF;
drop table if exists ACT_RU_EXECUTION;
drop table if exists ACT_RU_JOB;
drop table if exists ACT_RU_JOBDEF;
drop table if exists ACT_RU_EVENT_SUBSCR;
drop table if exists ACT_RU_INCIDENT;
drop table if exists ACT_RU_AUTHORIZATION;
drop table if exists ACT_RU_FILTER;
drop table if exists ACT_RU_METER_LOG;
drop table if exists ACT_RU_TASK_METER_LOG;
drop table if exists ACT_RU_EXT_TASK;
drop table if exists ACT_RU_BATCH;
drop table if exists ACT_GE_SCHEMA_LOG;
