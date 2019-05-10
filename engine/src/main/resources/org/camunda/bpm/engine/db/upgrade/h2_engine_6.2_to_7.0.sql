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



/** add ACT_INST_ID_ column to execution table */
alter table ACT_RU_EXECUTION
    add ACT_INST_ID_ varchar(64);

/** populate ACT_INST_ID_ from history */

/** get from history for active activity instances */
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

/** set act_inst_id for inactive parents of scope executions */
UPDATE
    ACT_RU_EXECUTION E
SET
    ACT_INST_ID_  = (
        SELECT
            MIN(HAI.ID_)
        FROM
            ACT_HI_ACTINST HAI
        INNER JOIN
            ACT_RU_EXECUTION SCOPE
            ON
                HAI.EXECUTION_ID_ = SCOPE.ID_
            AND
                SCOPE.PARENT_ID_ = E.ID_
            AND
                SCOPE.IS_SCOPE_ = true
        WHERE
            HAI.END_TIME_ is null
        AND
            NOT EXISTS (
                SELECT
                    ACT_INST_ID_
                FROM
                    ACT_RU_EXECUTION CHILD
                WHERE
                    CHILD.ACT_INST_ID_ = HAI.ID_
                AND
                    E.ACT_ID_ is not null
            )
    )
WHERE
    E.ACT_INST_ID_ is null;

/** remaining executions get id from parent  */
UPDATE
    ACT_RU_EXECUTION E
SET
    ACT_INST_ID_  = (
        SELECT
          ACT_INST_ID_ FROM ACT_RU_EXECUTION PARENT
        WHERE
            PARENT.ID_ = E.PARENT_ID_
        AND
            PARENT.ACT_ID_ = E.ACT_ID_
    )
WHERE
    E.ACT_INST_ID_ is null;
/**AND
    not exists (
        SELECT
            ID_
        FROM
            ACT_RU_EXECUTION CHILD
        WHERE
            CHILD.PARENT_ID_ = E.ID_
    );*/

/** remaining executions use execution id as activity instance id */
UPDATE
    ACT_RU_EXECUTION E
SET
    ACT_INST_ID_  = E.ID_
WHERE
    E.ACT_INST_ID_ is null;


/** mark MI-scope executions in temporary column */
alter table ACT_RU_EXECUTION
    add IS_MI_SCOPE_ bit;

UPDATE
    ACT_RU_EXECUTION MI_SCOPE
SET
    MI_SCOPE.IS_MI_SCOPE_ = true
WHERE
    MI_SCOPE.IS_SCOPE_ = true
AND
    MI_SCOPE.ACT_ID_ is not null
AND EXISTS (
    SELECT
        ID_
    FROM
        ACT_RU_EXECUTION MI_CONCUR
    WHERE
        MI_CONCUR.PARENT_ID_ = MI_SCOPE.ID_
    AND
        MI_CONCUR.IS_SCOPE_ = false
    AND
        MI_CONCUR.IS_CONCURRENT_ = true
    AND
        MI_CONCUR.ACT_ID_ = MI_SCOPE.ACT_ID_
);

/** set IS_ACTIVE to false for MI-Scopes: */
UPDATE
    ACT_RU_EXECUTION MI_SCOPE
SET
    MI_SCOPE.IS_ACTIVE_ = false
WHERE
    MI_SCOPE.IS_MI_SCOPE_ = true;

/** set correct root for mi-parallel:
    CASE 1: process instance (use ID_) */
UPDATE
    ACT_RU_EXECUTION MI_ROOT
SET
    ACT_INST_ID_  = MI_ROOT.ID_
WHERE
    MI_ROOT.ID_ = MI_ROOT.PROC_INST_ID_
AND EXISTS (
    SELECT
        ID_
    FROM
        ACT_RU_EXECUTION MI_SCOPE
    WHERE
        MI_SCOPE.PARENT_ID_ = MI_ROOT.ID_
    AND
        MI_SCOPE.IS_MI_SCOPE_ = true
);

/**
    CASE 2: scopes below process instance (use ACT_INST_ID_ from parent) */
UPDATE
    ACT_RU_EXECUTION MI_ROOT
SET
    ACT_INST_ID_  =  (
        SELECT
            ACT_INST_ID_
        FROM
            ACT_RU_EXECUTION PARENT
        WHERE
            PARENT.ID_ = MI_ROOT.PARENT_ID_
    )
WHERE
    MI_ROOT.ID_ != MI_ROOT.PROC_INST_ID_
AND EXISTS (
    SELECT
        ID_
    FROM
        ACT_RU_EXECUTION MI_SCOPE
    WHERE
        MI_SCOPE.PARENT_ID_ = MI_ROOT.ID_
    AND
        MI_SCOPE.IS_MI_SCOPE_ = true
);

alter table ACT_RU_EXECUTION
    drop IS_MI_SCOPE_;

/** add SUSPENSION_STATE_ column to task table */
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



/** add authorizations **/

create table ACT_RU_AUTHORIZATION (
  ID_ varchar(64) not null,
  REV_ integer not null,
  TYPE_ integer not null,
  GROUP_ID_ varchar(255),
  USER_ID_ varchar(255),
  RESOURCE_TYPE_ integer not null,
  RESOURCE_ID_ varchar(64),
  PERMS_ integer,
  primary key (ID_)
);

alter table ACT_RU_AUTHORIZATION
    add constraint ACT_UNIQ_AUTH_USER
    unique (TYPE_,USER_ID_,RESOURCE_TYPE_,RESOURCE_ID_);

alter table ACT_RU_AUTHORIZATION
    add constraint ACT_UNIQ_AUTH_GROUP
    unique (TYPE_,GROUP_ID_,RESOURCE_TYPE_,RESOURCE_ID_);

/** add deployment id to job table */
alter table ACT_RU_JOB
  add DEPLOYMENT_ID_ varchar(64);

/** add parent act inst ID */
alter table ACT_HI_ACTINST
    add PARENT_ACT_INST_ID_ varchar(64);