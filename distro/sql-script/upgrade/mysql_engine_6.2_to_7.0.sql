create table ACT_RU_INCIDENT (
  ID_ varchar(64) not null,
  INCIDENT_TIMESTAMP_ timestamp not null,
  INCIDENT_TYPE_ varchar(255) not null,
  EXECUTION_ID_ varchar(64),
  ACTIVITY_ID_ varchar(255),
  PROC_INST_ID_ varchar(64),
  PROC_DEF_ID_ varchar(64),
  CAUSE_INCIDENT_ID_ varchar(64),
  ROOT_CAUSE_INCIDENT_ID_ varchar(64),
  CONFIGURATION_ varchar(255),
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

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
    
create index ACT_IDX_HI_ACT_INST_COMP on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_, END_TIME_, ID_);

/** add ACT_INST_ID_ column to execution table */
alter table ACT_RU_EXECUTION
    add ACT_INST_ID_ nvarchar(64);
    
    
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
            HAI.END_TIME_ is null            
    )
WHERE 
    E.ACT_INST_ID_ is null
AND 
    E.ACT_ID_ is not null;

/** set act_inst_id for inactive scope executions */
UPDATE 
    ACT_RU_EXECUTION E
SET 
    E.ACT_INST_ID_  = (
        SELECT 
            MIN(HAI.ID_)
        FROM 
            ACT_HI_ACTINST HAI          
        WHERE
            HAI.END_TIME_ is null
        AND 
            EXISTS (
                SELECT 
                    ID_ 
                FROM 
                    (SELECT * FROM ACT_RU_EXECUTION) SCOPE 
                WHERE 
                    HAI.EXECUTION_ID_ = SCOPE.ID_
                AND 
                    SCOPE.PARENT_ID_ = E.ID_
                AND 
                    SCOPE.IS_SCOPE_ = true            
            )    
        AND 
            NOT EXISTS (
                SELECT 
                    ACT_INST_ID_
                FROM 
                    (SELECT * FROM ACT_RU_EXECUTION) CHILD
                WHERE 
                    CHILD.ACT_INST_ID_ = HAI.ID_
                AND 
                    CHILD.ACT_ID_ is not null
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
          ACT_INST_ID_ 
        FROM 
            (SELECT * FROM ACT_RU_EXECUTION) PARENT 
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
    ACT_RU_EXECUTION
SET 
    ACT_INST_ID_  = ID_
WHERE
    ACT_INST_ID_ is null;


/** mark MI-scope executions in temporary column */
alter table ACT_RU_EXECUTION
    add IS_MI_SCOPE_ bit;
    
UPDATE
    ACT_RU_EXECUTION E
SET 
    IS_MI_SCOPE_ = true        
WHERE 
    E.IS_SCOPE_ = true
AND
    E.ACT_ID_ is not null
AND EXISTS (
    SELECT 
        ID_ 
    FROM 
        (SELECT * FROM ACT_RU_EXECUTION) MI_CONCUR 
    WHERE  
        MI_CONCUR.PARENT_ID_ = E.ID_
    AND
        MI_CONCUR.IS_SCOPE_ = false
    AND
        MI_CONCUR.IS_CONCURRENT_ = true
    AND 
        MI_CONCUR.ACT_ID_ = E.ACT_ID_
);
    
/** set IS_ACTIVE to false for MI-Scopes: */
UPDATE
    ACT_RU_EXECUTION
SET 
    IS_ACTIVE_ = false    
WHERE
    IS_MI_SCOPE_ = true;

/** set correct root for mi-parallel: 
    CASE 1: process instance (use ID_) */    
UPDATE 
    ACT_RU_EXECUTION E
SET 
    ACT_INST_ID_  = E.ID_
WHERE 
    E.ID_ = E.PROC_INST_ID_ 
AND EXISTS (
    SELECT 
        ID_ 
    FROM 
        (SELECT * FROM ACT_RU_EXECUTION) MI_SCOPE 
    WHERE  
        MI_SCOPE.PARENT_ID_ = E.ID_
    AND
        MI_SCOPE.IS_MI_SCOPE_ = true
);

/**     
    CASE 2: scopes below process instance (use ACT_INST_ID_ from parent) */    
UPDATE 
    ACT_RU_EXECUTION E
SET 
    ACT_INST_ID_  =  (
        SELECT 
            ACT_INST_ID_ 
        FROM
            (SELECT * FROM ACT_RU_EXECUTION) PARENT
        WHERE 
            PARENT.ID_ = E.PARENT_ID_
    )    
WHERE 
    E.ID_ != E.PROC_INST_ID_ 
AND EXISTS (
    SELECT 
        ID_ 
    FROM 
        (SELECT * FROM ACT_RU_EXECUTION) MI_SCOPE 
    WHERE  
        MI_SCOPE.PARENT_ID_ = E.ID_
    AND
        MI_SCOPE.IS_MI_SCOPE_ = true
);

alter table ACT_RU_EXECUTION
    drop IS_MI_SCOPE_;

/** add SUSPENSION_STATE_ column to task table */
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ integer;
