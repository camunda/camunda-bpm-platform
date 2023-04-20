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

alter table ACT_GE_BYTEARRAY
    drop constraint ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop constraint ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION
    drop constraint ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION
    drop constraint ACT_FK_EXE_SUPER;

alter table ACT_RU_EXECUTION
    drop constraint ACT_FK_EXE_PROCDEF;

alter table ACT_RU_IDENTITYLINK
    drop constraint ACT_FK_TSKASS_TASK;

alter table ACT_RU_IDENTITYLINK
    drop constraint ACT_FK_ATHRZ_PROCEDEF;

alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_PROCINST;

alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_PROCDEF;

alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_EXE;

alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_PROCINST;

alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop constraint ACT_FK_JOB_EXCEPTION;

alter table ACT_RU_EVENT_SUBSCR
    drop constraint ACT_FK_EVENT_EXEC;

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_EXE;

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_PROCINST;

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_PROCDEF;

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_CAUSE;

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_RCAUSE;

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_JOB_DEF;

alter table ACT_RU_EXT_TASK
    drop constraint ACT_FK_EXT_TASK_EXE;

alter table ACT_RU_BATCH
    drop constraint ACT_FK_BATCH_SEED_JOB_DEF;

alter table ACT_RU_BATCH
    drop constraint ACT_FK_BATCH_MONITOR_JOB_DEF;

alter table ACT_RU_BATCH
    drop constraint ACT_FK_BATCH_JOB_DEF;

alter table ACT_RU_EXT_TASK
    drop CONSTRAINT ACT_FK_EXT_TASK_ERROR_DETAILS;

alter table ACT_RU_VARIABLE
    drop CONSTRAINT ACT_FK_VAR_BATCH;

-- A UNIQUE constraint in CRDB creates an index in the background.
-- There is a dependency between the UNIQUE constraint and the index,
-- so CRDB requires DROP INDEX CASCADE to be used for the removal of
-- these types of constraints.
drop index ACT_UNIQ_AUTH_GROUP cascade;
drop index ACT_UNIQ_AUTH_USER cascade;
drop index ACT_UNIQ_VARIABLE cascade;

drop index ACT_IDX_BYTEARRAY_RM_TIME;
drop index ACT_IDX_BYTEARRAY_ROOT_PI;
drop index ACT_IDX_BYTEAR_DEPL ;
drop index ACT_IDX_EXE_ROOT_PI;
drop index ACT_IDX_EXE_PROCINST ;
drop index ACT_IDX_EXE_PARENT ;
drop index ACT_IDX_EXE_SUPER;
drop index ACT_IDX_EXE_PROCDEF;
drop index ACT_IDX_TSKASS_TASK;
drop index ACT_IDX_TASK_EXEC;
drop index ACT_IDX_TASK_PROCINST;
drop index ACT_IDX_TASK_PROCDEF;
drop index ACT_IDX_VAR_EXE;
drop index ACT_IDX_VAR_PROCINST;
drop index ACT_IDX_VAR_BYTEARRAY;
drop index ACT_IDX_JOB_EXCEPTION;
drop index ACT_IDX_JOB_PROCINST;
drop index ACT_IDX_INC_CONFIGURATION;
drop index ACT_IDX_AUTH_GROUP_ID;

drop index ACT_IDX_EXEC_BUSKEY;
drop index ACT_IDX_TASK_CREATE;
drop index ACT_IDX_TASK_LAST_UPDATED;
drop index ACT_IDX_TASK_ASSIGNEE;
drop index ACT_IDX_TASK_OWNER;
drop index ACT_IDX_IDENT_LNK_USER;
drop index ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_IDX_VARIABLE_TASK_ID;
drop index ACT_IDX_VARIABLE_TASK_NAME_TYPE;

-- new metric milliseconds column
DROP INDEX ACT_IDX_METER_LOG_MS;
DROP INDEX ACT_IDX_METER_LOG_NAME_MS;
DROP INDEX ACT_IDX_METER_LOG_REPORT;

-- old metric timestamp column
DROP INDEX ACT_IDX_METER_LOG_TIME;
DROP INDEX ACT_IDX_METER_LOG;

-- task metric timestamp column
drop index ACT_IDX_TASK_METER_LOG_TIME;

drop index ACT_IDX_EXT_TASK_TOPIC;

drop index ACT_IDX_JOB_EXECUTION_ID;
drop index ACT_IDX_JOB_HANDLER;

drop index ACT_IDX_EVENT_SUBSCR_CONFIG_;
drop index ACT_IDX_EVENT_SUBSCR;
drop index ACT_IDX_ATHRZ_PROCEDEF;

-- indexes for deadlock problems - https://app.camunda.com/jira/browse/CAM-2567
drop index ACT_IDX_INC_CAUSEINCID;
drop index ACT_IDX_INC_EXID;
drop index ACT_IDX_INC_PROCDEFID;
drop index ACT_IDX_INC_PROCINSTID;
drop index ACT_IDX_INC_ROOTCAUSEINCID;
drop index ACT_IDX_INC_JOB_DEF;
drop index ACT_IDX_AUTH_RESOURCE_ID;
drop index ACT_IDX_EXT_TASK_EXEC;

drop index ACT_IDX_BYTEARRAY_NAME;
drop index ACT_IDX_DEPLOYMENT_NAME;
drop index ACT_IDX_JOBDEF_PROC_DEF_ID;
drop index ACT_IDX_JOB_HANDLER_TYPE;
drop index ACT_IDX_EVENT_SUBSCR_EVT_NAME;
drop index ACT_IDX_PROCDEF_DEPLOYMENT_ID;

drop index ACT_IDX_EXT_TASK_TENANT_ID;
drop index ACT_IDX_EXT_TASK_PRIORITY;
drop index ACT_IDX_EXT_TASK_ERR_DETAILS;
drop index ACT_IDX_INC_TENANT_ID;
drop index ACT_IDX_JOBDEF_TENANT_ID;
drop index ACT_IDX_JOB_TENANT_ID;
drop index ACT_IDX_EVENT_SUBSCR_TENANT_ID;
drop index ACT_IDX_VARIABLE_TENANT_ID;
drop index ACT_IDX_TASK_TENANT_ID;
drop index ACT_IDX_EXEC_TENANT_ID;
drop index ACT_IDX_PROCDEF_TENANT_ID;
drop index ACT_IDX_DEPLOYMENT_TENANT_ID;

drop index ACT_IDX_JOB_JOB_DEF_ID;
drop index ACT_IDX_BATCH_SEED_JOB_DEF;
drop index ACT_IDX_BATCH_MONITOR_JOB_DEF;
drop index ACT_IDX_BATCH_JOB_DEF;

drop index ACT_IDX_PROCDEF_VER_TAG;

drop index ACT_IDX_AUTH_ROOT_PI;
drop index ACT_IDX_AUTH_RM_TIME;

drop index ACT_IDX_BATCH_ID;

drop table ACT_GE_PROPERTY;
drop table ACT_GE_BYTEARRAY;
drop table ACT_RE_DEPLOYMENT;
drop table ACT_RE_PROCDEF;
drop table ACT_RE_CAMFORMDEF;
drop table ACT_RU_EXECUTION;
drop table ACT_RU_JOB;
drop table ACT_RU_JOBDEF;
drop table ACT_RU_TASK;
drop table ACT_RU_IDENTITYLINK;
drop table ACT_RU_VARIABLE;
drop table ACT_RU_EVENT_SUBSCR;
drop table ACT_RU_INCIDENT;
drop table ACT_RU_AUTHORIZATION;
drop table ACT_RU_FILTER;
drop table ACT_RU_METER_LOG;
drop table ACT_RU_TASK_METER_LOG;
drop table ACT_RU_EXT_TASK;
drop table ACT_RU_BATCH;
drop table ACT_GE_SCHEMA_LOG;
