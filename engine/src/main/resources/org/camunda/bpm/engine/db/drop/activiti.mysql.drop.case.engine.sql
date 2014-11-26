drop index ACT_IDX_CASE_EXEC_BUSKEY on ACT_RU_CASE_EXECUTION;

alter table ACT_RU_CASE_EXECUTION
    drop FOREIGN KEY ACT_FK_CASE_EXE_CASE_INST;

alter table ACT_RU_CASE_EXECUTION
    drop FOREIGN KEY ACT_FK_CASE_EXE_PARENT;

alter table ACT_RU_CASE_EXECUTION
    drop FOREIGN KEY ACT_FK_CASE_EXE_CASE_DEF;

alter table ACT_RU_VARIABLE
    drop FOREIGN KEY ACT_FK_VAR_CASE_EXE;

alter table ACT_RU_VARIABLE
    drop FOREIGN KEY ACT_FK_VAR_CASE_INST;

alter table ACT_RU_TASK
    drop foreign key ACT_FK_TASK_CASE_EXE;

alter table ACT_RU_TASK
    drop foreign key ACT_FK_TASK_CASE_DEF;

alter table ACT_RU_CASE_SENTRY_PART
    drop foreign key ACT_FK_CASE_SENTRY_CASE_INST;

alter table ACT_RU_CASE_SENTRY_PART
    drop foreign key ACT_FK_CASE_SENTRY_CASE_EXEC;

drop table if exists ACT_RE_CASE_DEF;
drop table if exists ACT_RU_CASE_EXECUTION;
drop table if exists ACT_RU_CASE_SENTRY_PART;