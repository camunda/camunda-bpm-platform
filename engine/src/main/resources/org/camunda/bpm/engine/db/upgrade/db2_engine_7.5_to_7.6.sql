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

-- AUTHORIZATION --

-- add grant authorizations for group camunda-admin:
INSERT INTO
  ACT_RU_AUTHORIZATION (ID_, TYPE_, GROUP_ID_, RESOURCE_TYPE_, RESOURCE_ID_, PERMS_, REV_)
VALUES
  ('camunda-admin-grant-drd', 1, 'camunda-admin', 14, '*', 2147483647, 1);

-- decision requirements definition --

ALTER TABLE ACT_RE_DECISION_DEF
  ADD DEC_REQ_ID_ varchar(64);

ALTER TABLE ACT_RE_DECISION_DEF
  ADD DEC_REQ_KEY_ varchar(255);

ALTER TABLE ACT_RU_CASE_SENTRY_PART
  ADD VARIABLE_EVENT_ varchar(255);

ALTER TABLE ACT_RU_CASE_SENTRY_PART
  ADD VARIABLE_NAME_ varchar(255);

create table ACT_RE_DECISION_REQ_DEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

alter table ACT_RE_DECISION_DEF
    add constraint ACT_FK_DEC_REQ
    foreign key (DEC_REQ_ID_)
    references ACT_RE_DECISION_REQ_DEF(ID_);

create index ACT_IDX_DEC_DEF_REQ_ID on ACT_RE_DECISION_DEF(DEC_REQ_ID_);
create index ACT_IDX_DEC_REQ_DEF_TENANT_ID on ACT_RE_DECISION_REQ_DEF(TENANT_ID_);

ALTER TABLE ACT_HI_DECINST
  ADD ROOT_DEC_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_DECINST
  ADD DEC_REQ_ID_ varchar(64);

ALTER TABLE ACT_HI_DECINST
  ADD DEC_REQ_KEY_ varchar(255);

create index ACT_IDX_HI_DEC_INST_ROOT_ID on ACT_HI_DECINST(ROOT_DEC_INST_ID_);
create index ACT_IDX_HI_DEC_INST_REQ_ID on ACT_HI_DECINST(DEC_REQ_ID_);
create index ACT_IDX_HI_DEC_INST_REQ_KEY on ACT_HI_DECINST(DEC_REQ_KEY_);

-- remove not null from ACT_HI_DEC tables --
alter table ACT_HI_DEC_OUT
  alter column CLAUSE_ID_ drop not null
  alter column RULE_ID_ drop not null;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ACT_HI_DEC_OUT');

alter table ACT_HI_DEC_IN
  alter column CLAUSE_ID_ drop not null;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ACT_HI_DEC_IN');

-- CAM-5914
create index ACT_IDX_JOB_EXECUTION_ID on ACT_RU_JOB(EXECUTION_ID_);
create index ACT_IDX_JOB_HANDLER on ACT_RU_JOB(HANDLER_TYPE_,HANDLER_CFG_);

ALTER TABLE ACT_RU_EXT_TASK
  ADD ERROR_DETAILS_ID_ varchar(64);

alter table ACT_RU_EXT_TASK
    add constraint ACT_FK_EXT_TASK_ERROR_DETAILS
    foreign key (ERROR_DETAILS_ID_)
    references ACT_GE_BYTEARRAY (ID_);

ALTER TABLE ACT_HI_PROCINST
  ADD STATE_ varchar(255);

update ACT_HI_PROCINST set STATE_ = 'ACTIVE' where END_TIME_ is null;
update ACT_HI_PROCINST set STATE_ = 'COMPLETED' where END_TIME_ is not null;

-- add indexes on PROC_DEF_KEY_ columns in history tables CAM-6679
create index ACT_IDX_HI_ACT_INST_PROC_DEF_KEY on ACT_HI_ACTINST(PROC_DEF_KEY_);
create index ACT_IDX_HI_DETAIL_PROC_DEF_KEY on ACT_HI_DETAIL(PROC_DEF_KEY_);
create index ACT_IDX_HI_IDENT_LNK_PROC_DEF_KEY on ACT_HI_IDENTITYLINK(PROC_DEF_KEY_);
create index ACT_IDX_HI_INCIDENT_PROC_DEF_KEY on ACT_HI_INCIDENT(PROC_DEF_KEY_);
create index ACT_IDX_HI_JOB_LOG_PROC_DEF_KEY on ACT_HI_JOB_LOG(PROCESS_DEF_KEY_);
create index ACT_IDX_HI_PRO_INST_PROC_DEF_KEY on ACT_HI_PROCINST(PROC_DEF_KEY_);
create index ACT_IDX_HI_TASK_INST_PROC_DEF_KEY on ACT_HI_TASKINST(PROC_DEF_KEY_);
create index ACT_IDX_HI_VAR_INST_PROC_DEF_KEY on ACT_HI_VARINST(PROC_DEF_KEY_);

-- CAM-6725
ALTER TABLE ACT_RU_METER_LOG
 ADD MILLISECONDS_ bigint DEFAULT 0;

alter table ACT_RU_METER_LOG
  alter column TIMESTAMP_ drop not null;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ACT_RU_METER_LOG');

-- new metric milliseconds column
CREATE INDEX ACT_IDX_METER_LOG_MS ON ACT_RU_METER_LOG(MILLISECONDS_);
CREATE INDEX ACT_IDX_METER_LOG_REPORT ON ACT_RU_METER_LOG(NAME_, REPORTER_, MILLISECONDS_);
CREATE INDEX ACT_IDX_METER_LOG_NAME_MS ON ACT_RU_METER_LOG(NAME_, MILLISECONDS_);

-- old metric timestamp column
CREATE INDEX ACT_IDX_METER_LOG_TIME ON ACT_RU_METER_LOG(TIMESTAMP_);
