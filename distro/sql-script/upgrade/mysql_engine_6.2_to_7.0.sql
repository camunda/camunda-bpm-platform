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
    
/** remaining executions use execution id as activity instance id */
UPDATE 
    ACT_RU_EXECUTION
SET 
    ACT_INST_ID_  = ID_
WHERE
    ACT_INST_ID_ is null;

/** add SUSPENSION_STATE_ column to task table */
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ integer;
