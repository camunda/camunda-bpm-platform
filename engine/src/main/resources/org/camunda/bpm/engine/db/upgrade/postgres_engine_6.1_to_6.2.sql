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
-- rename table ACT_HI_PROCVARIABLE -> ACT_HI_VARINST

create table ACT_HI_VARINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);

-- copy old values --
insert into ACT_HI_VARINST
    select ID_, PROC_INST_ID_, NULL, NULL, NAME_, VAR_TYPE_, REV_, BYTEARRAY_ID_, DOUBLE_, LONG_, TEXT_, TEXT2_ FROM ACT_HI_PROCVARIABLE;

-- drop old indices --
drop index ACT_IDX_HI_PROCVAR_PROC_INST;
drop index ACT_IDX_HI_PROCVAR_NAME_TYPE;

-- drop old table --
drop table ACT_HI_PROCVARIABLE;

-- create new indices --
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);

-- not migrating --
--   -    DUEDATE_ timestamp null,
--   +    DUEDATE_ timestamp,

-- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- change column types in ACT_HI_TASKINST

alter table ACT_HI_TASKINST ALTER COLUMN OWNER_ TYPE varchar(255);

alter table ACT_HI_TASKINST ALTER COLUMN ASSIGNEE_ TYPE varchar(255);


-- -- -- -- -- -- -- -- -- -- -- -- -- --
-- change column types in ACT_RU_TASK --

alter table ACT_RU_TASK ALTER COLUMN OWNER_ TYPE varchar(255);

alter table ACT_RU_TASK ALTER COLUMN ASSIGNEE_ TYPE varchar(255);


-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_RU_IDENTITYLINK --

drop index ACT_IDX_IDENT_LNK_GROUP;
alter table ACT_RU_IDENTITYLINK ALTER COLUMN GROUP_ID_ TYPE varchar(255);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);

drop index ACT_IDX_IDENT_LNK_USER;
alter table ACT_RU_IDENTITYLINK ALTER COLUMN USER_ID_ TYPE varchar(255);
create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);


-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- change column types in ACT_RU_JOB (remove NOT NULL constraint)
alter table ACT_RU_JOB ALTER COLUMN DUEDATE_ TYPE timestamp;

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- revert introduction of new history level --

update ACT_GE_PROPERTY set VALUE_ = CAST ( VALUE_ AS integer )- 1, REV_ = REV_ + 1 where NAME_ = 'historyLevel' and CAST (VALUE_ as integer) >= 2;

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- Additional index on PROC_INST_ID_ and ACT_ID_ for historic activity --
create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);