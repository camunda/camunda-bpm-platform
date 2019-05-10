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

-- case management --

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD SUPER_EXEC_ nvarchar(64);

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD REQUIRED_ tinyint;

-- history --

ALTER TABLE ACT_HI_ACTINST
  ADD CALL_CASE_INST_ID_ nvarchar(64);

ALTER TABLE ACT_HI_PROCINST
  ADD SUPER_CASE_INSTANCE_ID_ nvarchar(64);

ALTER TABLE ACT_HI_CASEINST
  ADD SUPER_PROCESS_INSTANCE_ID_ nvarchar(64);

ALTER TABLE ACT_HI_CASEACTINST
  ADD REQUIRED_ tinyint;

ALTER TABLE ACT_HI_OP_LOG
  ADD JOB_ID_ nvarchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD JOB_DEF_ID_ nvarchar(64);

create table ACT_HI_JOB_LOG (
    ID_ nvarchar(64) not null,
    TIMESTAMP_ datetime2 not null,
    JOB_ID_ nvarchar(64) not null,
    JOB_DUEDATE_ datetime2,
    JOB_RETRIES_ integer,
    JOB_EXCEPTION_MSG_ nvarchar(4000),
    JOB_EXCEPTION_STACK_ID_ nvarchar(64),
    JOB_STATE_ integer,
    JOB_DEF_ID_ nvarchar(64),
    JOB_DEF_TYPE_ nvarchar(255),
    JOB_DEF_CONFIGURATION_ nvarchar(255),
    ACT_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    PROCESS_INSTANCE_ID_ nvarchar(64),
    PROCESS_DEF_ID_ nvarchar(64),
    PROCESS_DEF_KEY_ nvarchar(255),
    DEPLOYMENT_ID_ nvarchar(64),
    SEQUENCE_COUNTER_ numeric(19,0),
    primary key (ID_)
);

create index ACT_IDX_HI_JOB_LOG_PROCINST on ACT_HI_JOB_LOG(PROCESS_INSTANCE_ID_);
create index ACT_IDX_HI_JOB_LOG_PROCDEF on ACT_HI_JOB_LOG(PROCESS_DEF_ID_);

-- history: add columns PROC_DEF_KEY_, PROC_DEF_ID_, CASE_DEF_KEY_, CASE_DEF_ID_ --

ALTER TABLE ACT_HI_PROCINST
  ADD PROC_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_ACTINST
  ADD PROC_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_TASKINST
  ADD PROC_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_TASKINST
  ADD CASE_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_VARINST
  ADD PROC_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_VARINST
  ADD PROC_DEF_ID_ nvarchar(64);

ALTER TABLE ACT_HI_VARINST
  ADD CASE_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_VARINST
  ADD CASE_DEF_ID_ nvarchar(64);

ALTER TABLE ACT_HI_DETAIL
  ADD PROC_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_DETAIL
  ADD PROC_DEF_ID_ nvarchar(64);

ALTER TABLE ACT_HI_DETAIL
  ADD CASE_DEF_KEY_ nvarchar(255);

ALTER TABLE ACT_HI_DETAIL
  ADD CASE_DEF_ID_ nvarchar(64);

ALTER TABLE ACT_HI_INCIDENT
  ADD PROC_DEF_KEY_ nvarchar(255);

-- sequence counter

ALTER TABLE ACT_RU_EXECUTION
  ADD SEQUENCE_COUNTER_ numeric(19,0);

ALTER TABLE ACT_HI_ACTINST
  ADD SEQUENCE_COUNTER_ numeric(19,0);

ALTER TABLE ACT_RU_VARIABLE
  ADD SEQUENCE_COUNTER_ numeric(19,0);

ALTER TABLE ACT_HI_DETAIL
  ADD SEQUENCE_COUNTER_ numeric(19,0);

ALTER TABLE ACT_RU_JOB
  ADD SEQUENCE_COUNTER_ numeric(19,0);

-- AUTHORIZATION --

-- add grant authorizations for group camunda-admin:
INSERT INTO
  ACT_RU_AUTHORIZATION (ID_, TYPE_, GROUP_ID_, RESOURCE_TYPE_, RESOURCE_ID_, PERMS_, REV_)
VALUES
  ('camunda-admin-grant-process-definition', 1, 'camunda-admin', 6, '*', 2147483647, 1),
  ('camunda-admin-grant-task', 1, 'camunda-admin', 7, '*', 2147483647, 1),
  ('camunda-admin-grant-process-instance', 1, 'camunda-admin', 8, '*', 2147483647, 1),
  ('camunda-admin-grant-deployment', 1, 'camunda-admin', 9, '*', 2147483647, 1);

-- add global grant authorizations for new authorization resources:
-- DEPLOYMENT
-- PROCESS_DEFINITION
-- PROCESS_INSTANCE
-- TASK
-- with ALL permissions

INSERT INTO
  ACT_RU_AUTHORIZATION (ID_, TYPE_, USER_ID_, RESOURCE_TYPE_, RESOURCE_ID_, PERMS_, REV_)
VALUES
  ('global-grant-process-definition', 0, '*', 6, '*', 2147483647, 1),
  ('global-grant-task', 0, '*', 7, '*', 2147483647, 1),
  ('global-grant-process-instance', 0, '*', 8, '*', 2147483647, 1),
  ('global-grant-deployment', 0, '*', 9, '*', 2147483647, 1);

-- variables --

ALTER TABLE ACT_RU_VARIABLE
  ADD IS_CONCURRENT_LOCAL_ tinyint;

-- metrics --

create table ACT_RU_METER_LOG (
  ID_ nvarchar(64) not null,
  NAME_ nvarchar(64) not null,
  VALUE_ numeric(19,0),
  TIMESTAMP_ datetime2 not null,
  primary key (ID_)
);

create index ACT_IDX_METER_LOG on ACT_RU_METER_LOG(NAME_,TIMESTAMP_);