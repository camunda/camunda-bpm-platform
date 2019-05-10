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

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- rename table ACT_HI_PROCVARIABLE -> ACT_HI_VARINST --

create table ACT_HI_VARINST (
    ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(100),
    REV_ int,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

-- copy old values --
insert into ACT_HI_VARINST
	select ID_, PROC_INST_ID_, NULL, NULL, NAME_, VAR_TYPE_, REV_, BYTEARRAY_ID_, DOUBLE_, LONG_, TEXT_, TEXT2_ FROM ACT_HI_PROCVARIABLE;

-- drop old indices --
drop index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_PROCVARIABLE;
drop index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_PROCVARIABLE;

-- drop old table --
drop table ACT_HI_PROCVARIABLE;

-- create new indices --
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);

-- not migrating --
--   -    DUEDATE_ timestamp null,
--   +    DUEDATE_ timestamp,

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_HI_TASKINST --

alter table ACT_HI_TASKINST
	alter column OWNER_ varchar(255);

alter table ACT_HI_TASKINST
	alter column ASSIGNEE_ varchar(255);


-- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_RU_TASK --

alter table ACT_RU_TASK
	alter column OWNER_ varchar(255);

alter table ACT_RU_TASK
	alter column ASSIGNEE_ varchar(255);


-- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_RU_IDENTITYLINK --

drop index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK;
alter table ACT_RU_IDENTITYLINK
	alter column USER_ID_ varchar(255);
create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);

drop index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK;
alter table ACT_RU_IDENTITYLINK
	alter column GROUP_ID_ varchar(255);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_RU_JOB (remove NOT NULL constraint)
alter table ACT_RU_JOB
	alter column DUEDATE_ datetime2;

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- revert introduction of new history level --

update ACT_GE_PROPERTY
  set VALUE_ = VALUE_ - 1,
      REV_ = REV_ + 1
  where NAME_ = 'historyLevel' and VALUE_ >= 2;


-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- change column types of type datetime to datetime2 in runtime & history --

-- runtime
alter table ACT_RE_DEPLOYMENT ALTER COLUMN DEPLOY_TIME_ datetime2;
alter table ACT_RU_JOB ALTER COLUMN LOCK_EXP_TIME_ datetime2;
alter table ACT_RU_JOB ALTER COLUMN DUEDATE_ datetime2 NULL;

drop index ACT_IDX_TASK_CREATE on ACT_RU_TASK;
alter table ACT_RU_TASK ALTER COLUMN CREATE_TIME_ datetime2;
create index ACT_IDX_TASK_CREATE on ACT_RU_TASK(CREATE_TIME_);
alter table ACT_RU_TASK ALTER COLUMN DUE_DATE_ datetime2;

alter table ACT_RU_EVENT_SUBSCR ALTER COLUMN CREATED_ datetime2;

--history
alter table ACT_HI_PROCINST ALTER COLUMN START_TIME_ datetime2;

drop index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST;
alter table ACT_HI_PROCINST ALTER COLUMN END_TIME_ datetime2;
create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);

drop index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST;
alter table ACT_HI_ACTINST ALTER COLUMN START_TIME_ datetime2 not null;
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);

drop index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST;
alter table ACT_HI_ACTINST ALTER COLUMN END_TIME_ datetime2;
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);

alter table ACT_HI_TASKINST ALTER COLUMN START_TIME_ datetime2 not null;
alter table ACT_HI_TASKINST ALTER COLUMN END_TIME_ datetime2;
alter table ACT_HI_TASKINST ALTER COLUMN DUE_DATE_ datetime2;

drop index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL;
alter table ACT_HI_DETAIL ALTER COLUMN TIME_ datetime2 not null;
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
alter table ACT_HI_COMMENT ALTER COLUMN TIME_ datetime2 not null;

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- Additional index on PROC_INST_ID_ and ACT_ID_ for historic activity

create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);