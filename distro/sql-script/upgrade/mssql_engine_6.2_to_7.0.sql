create table ACT_RU_INCIDENT (
  ID_ nvarchar(64) not null,
  INCIDENT_TIMESTAMP_ datetime2 not null,
  INCIDENT_MSG_ nvarchar(4000),
  INCIDENT_TYPE_ nvarchar(255) not null,
  EXECUTION_ID_ nvarchar(64),
  ACTIVITY_ID_ nvarchar(255),
  PROC_INST_ID_ nvarchar(64),
  PROC_DEF_ID_ nvarchar(64),
  CAUSE_INCIDENT_ID_ nvarchar(64),
  ROOT_CAUSE_INCIDENT_ID_ nvarchar(64),
  CONFIGURATION_ nvarchar(255),
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
    add ACT_INST_ID_ nvarchar(64);


/** populate ACT_INST_ID_ from history */

/** get from history for active activity instances */
UPDATE
    ACT_RU_EXECUTION
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
FROM
    ACT_RU_EXECUTION E
WHERE
    E.ACT_INST_ID_ is null
AND
    E.ACT_ID_ is not null;


/** set act_inst_id for inactive parents of scope executions */
UPDATE
    ACT_RU_EXECUTION
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
                SCOPE.IS_SCOPE_ = 1
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
FROM
    ACT_RU_EXECUTION E
WHERE
    E.ACT_INST_ID_ is null;

/** remaining executions get id from parent  */
UPDATE
    ACT_RU_EXECUTION
SET
    ACT_INST_ID_  = (
        SELECT
          ACT_INST_ID_ FROM ACT_RU_EXECUTION PARENT
        WHERE
            PARENT.ID_ = E.PARENT_ID_
        AND
            PARENT.ACT_ID_ = E.ACT_ID_
    )
FROM
    ACT_RU_EXECUTION E
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
    ACT_RU_EXECUTION
SET
    ACT_INST_ID_  = ID_
WHERE
    ACT_INST_ID_ is null;


/** mark MI-scope executions in temporary column */
alter table ACT_RU_EXECUTION
    add IS_MI_SCOPE_ tinyint;


UPDATE
    ACT_RU_EXECUTION
SET
    IS_MI_SCOPE_ = 1
FROM
    ACT_RU_EXECUTION MI_SCOPE
WHERE
    MI_SCOPE.IS_SCOPE_ = 1
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
        MI_CONCUR.IS_SCOPE_ = 0
    AND
        MI_CONCUR.IS_CONCURRENT_ = 1
    AND
        MI_CONCUR.ACT_ID_ = MI_SCOPE.ACT_ID_
);

/** set IS_ACTIVE to false for MI-Scopes: */
UPDATE
    ACT_RU_EXECUTION
SET
    IS_ACTIVE_ = 0
WHERE
    IS_MI_SCOPE_ = 1;

/** set correct root for mi-parallel:
    CASE 1: process instance (use ID_) */
UPDATE
    ACT_RU_EXECUTION
SET
    ACT_INST_ID_  = MI_ROOT.ID_
FROM
    ACT_RU_EXECUTION MI_ROOT
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
        MI_SCOPE.IS_MI_SCOPE_ = 1
);

/**
    CASE 2: scopes below process instance (use ACT_INST_ID_ from parent) */
UPDATE
    ACT_RU_EXECUTION
SET
    ACT_INST_ID_  =  (
        SELECT
            ACT_INST_ID_
        FROM
            ACT_RU_EXECUTION PARENT
        WHERE
            PARENT.ID_ = MI_ROOT.PARENT_ID_
    )
FROM
    ACT_RU_EXECUTION MI_ROOT
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
        MI_SCOPE.IS_MI_SCOPE_ = 1
);

alter table ACT_RU_EXECUTION
    drop COLUMN IS_MI_SCOPE_;

/** add SUSPENSION_STATE_ column to task table */
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ int;

UPDATE ACT_RU_TASK
SET SUSPENSION_STATE_ = (
  SELECT SUSPENSION_STATE_
  FROM ACT_RU_EXECUTION E
  WHERE E.ID_ = EXECUTION_ID_
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

create unique index ACT_UNIQ_AUTH_USER on ACT_RU_AUTHORIZATION (TYPE_,USER_ID_,RESOURCE_TYPE_,RESOURCE_ID_) where USER_ID_ is not null;
create unique index ACT_UNIQ_AUTH_GROUP on ACT_RU_AUTHORIZATION (TYPE_,GROUP_ID_,RESOURCE_TYPE_,RESOURCE_ID_) where GROUP_ID_ is not null;

/** add deployment ids to jobs **/
alter table ACT_RU_JOB
    add DEPLOYMENT_ID_ nvarchar(64);

/** add parent act inst ID */
alter table ACT_HI_ACTINST
    add PARENT_ACT_INST_ID_ nvarchar(64);