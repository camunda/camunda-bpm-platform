drop index ACT_RU_CASE_EXECUTION.ACT_IDX_CASE_EXEC_BUSKEY;

-- indexes for concurrency problems - https://app.camunda.com/jira/browse/CAM-1646 --
drop index ACT_RU_VARIABLE.ACT_IDX_VARIABLE_CASE_EXEC;
drop index ACT_RU_VARIABLE.ACT_IDX_VARIABLE_CASE_INST;

alter table ACT_RU_CASE_EXECUTION 
    drop constraint ACT_FK_CASE_EXE_CASE_INST;

alter table ACT_RU_CASE_EXECUTION 
    drop constraint ACT_FK_CASE_EXE_PARENT;

alter table ACT_RU_CASE_EXECUTION 
    drop constraint ACT_FK_CASE_EXE_CASE_DEF;

alter table ACT_RU_VARIABLE 
    drop constraint ACT_FK_VAR_CASE_EXE;
  
alter table ACT_RU_VARIABLE 
    drop constraint ACT_FK_VAR_CASE_INST;

if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RE_CASE_DEF') drop table ACT_RE_CASE_DEF;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_CASE_EXECUTION') drop table ACT_RU_CASE_EXECUTION;