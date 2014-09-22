drop index ACT_IDX_CASE_EXEC_BUSKEY;

alter table ACT_RE_CASE_DEF
    drop constraint ACT_UNIQ_CASE_DEF;

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

alter table ACT_RU_TASK
    drop constraint ACT_FK_TASK_CASE_EXE;

alter table ACT_RU_TASK
    drop constraint ACT_FK_TASK_CASE_DEF;

alter table ACT_RU_CASE_SENTRY_PART
    drop constraint ACT_FK_CASE_SENTRY_CASE_INST;

alter table ACT_RU_CASE_SENTRY_PART
    drop constraint ACT_FK_CASE_SENTRY_CASE_EXEC;

drop table ACT_RE_CASE_DEF if exists;
drop table ACT_RU_CASE_EXECUTION if exists;
drop table ACT_RU_CASE_SENTRY_PART if exists;