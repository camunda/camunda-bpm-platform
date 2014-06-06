drop index ACT_IDX_CASE_EXEC_BUSKEY;

alter table ACT_RU_CASE_EXECUTION
    drop foreign key ACT_FK_CASE_EXE_CASE_INST;

alter table ACT_RU_CASE_EXECUTION
    drop foreign key ACT_FK_CASE_EXE_PARENT;

alter table ACT_RU_CASE_EXECUTION 
    drop foreign key ACT_FK_CASE_EXE_CASE_DEF;

alter table ACT_RU_VARIABLE
    drop foreign key ACT_FK_VAR_CASE_EXE;

alter table ACT_RU_VARIABLE 
    drop foreign key ACT_FK_VAR_CASE_INST;

drop table ACT_RE_CASE_DEF;
drop table ACT_RU_CASE_EXECUTION;