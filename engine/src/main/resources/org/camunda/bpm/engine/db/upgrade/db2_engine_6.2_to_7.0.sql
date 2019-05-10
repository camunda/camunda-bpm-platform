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


create table ACT_RU_INCIDENT (
  ID_ varchar(64) not null,
  INCIDENT_TIMESTAMP_ timestamp not null,
  INCIDENT_MSG_ varchar(4000),
  INCIDENT_TYPE_ varchar(255) not null,
  EXECUTION_ID_ varchar(64),
  ACTIVITY_ID_ varchar(255),
  PROC_INST_ID_ varchar(64),
  PROC_DEF_ID_ varchar(64),
  CAUSE_INCIDENT_ID_ varchar(64),
  ROOT_CAUSE_INCIDENT_ID_ varchar(64),
  CONFIGURATION_ varchar(255),
  primary key (ID_)
);

create index ACT_IDX_INC_CONFIGURATION on ACT_RU_INCIDENT(CONFIGURATION_);

alter table ACT_RU_INCIDENT
    add constraint ACT_FK_INC_EXE
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_INCIDENT
    add constraint ACT_FK_INC_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_INCIDENT
    add constraint ACT_FK_INC_PROCDEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_INCIDENT
    add constraint ACT_FK_INC_CAUSE
    foreign key (CAUSE_INCIDENT_ID_)
    references ACT_RU_INCIDENT (ID_);

alter table ACT_RU_INCIDENT
    add constraint ACT_FK_INC_RCAUSE
    foreign key (ROOT_CAUSE_INCIDENT_ID_)
    references ACT_RU_INCIDENT (ID_);



-- add ACT_INST_ID_ column to execution table --
alter table ACT_RU_EXECUTION
    add ACT_INST_ID_ varchar(64);

-- populate ACT_INST_ID_ from history --

-- get from history for active activity instances --

UPDATE
    ACT_RU_EXECUTION E
SET
    ACT_INST_ID_  = (
        SELECT
            MAX(ID_)
        FROM
            ACT_HI_ACTINST HAI
        WHERE
            HAI.EXECUTION_ID_ = E.ID_
        AND
            END_TIME_ is null
    )
WHERE
    E.ACT_INST_ID_ is null
AND
    E.ACT_ID_ is not null;

-- remaining executions use execution id as activity instance id --
UPDATE
    ACT_RU_EXECUTION E
SET
    ACT_INST_ID_  = E.ID_
WHERE
    E.ACT_INST_ID_ is null;


-- add SUSPENSION_STATE_ column to task table --
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ integer;

UPDATE ACT_RU_TASK T
SET T.SUSPENSION_STATE_ = (
  SELECT SUSPENSION_STATE_
  FROM ACT_RU_EXECUTION E
  WHERE E.ID_ = T.EXECUTION_ID_
);

UPDATE ACT_RU_TASK
SET SUSPENSION_STATE_ = 1
WHERE SUSPENSION_STATE_ is null;


-- add authorizations -- -- -- -- -- -- -- -- -- -- -- -- --

create table ACT_RU_AUTHORIZATION (
  ID_ varchar(64) not null,
  REV_ integer not null,
  TYPE_ integer not null,
  GROUP_ID_ varchar(255),
  USER_ID_ varchar(255),
  RESOURCE_TYPE_ integer not null,
  RESOURCE_ID_ varchar(64),
  PERMS_ integer,
  primary key (ID_),
  UNI_USER_ID_ varchar (255) not null generated always as (case when "USER_ID_" is null then "ID_" else "USER_ID_" end),
  UNI_GROUP_ID_ varchar (255) not null generated always as (case when "GROUP_ID_" is null then "ID_" else "GROUP_ID_" end),
  UNI_RESOURCE_ID_ varchar (64) not null generated always as (case when "RESOURCE_ID_" is null then "ID_" else "RESOURCE_ID_" end)
);

create unique index ACT_UNIQ_AUTH_USER on ACT_RU_AUTHORIZATION(TYPE_,UNI_USER_ID_,RESOURCE_TYPE_,UNI_RESOURCE_ID_);
create unique index ACT_UNIQ_AUTH_GROUP on ACT_RU_AUTHORIZATION(TYPE_,UNI_GROUP_ID_,RESOURCE_TYPE_,UNI_RESOURCE_ID_);

-- add deployment id -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

/** add deployment id to job table */
alter table ACT_RU_JOB
    add DEPLOYMENT_ID_ varchar(64);

/** add parent act inst ID */
alter table ACT_HI_ACTINST
    add PARENT_ACT_INST_ID_ varchar(64);