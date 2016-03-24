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

alter table ACT_RU_TASK
    drop foreign key ACT_FK_TASK_CASE_EXE;

alter table ACT_RU_TASK
    drop foreign key ACT_FK_TASK_CASE_DEF;

alter table ACT_RU_CASE_SENTRY_PART
    drop foreign key ACT_FK_CASE_SENTRY_CASE_INST;

alter table ACT_RU_CASE_SENTRY_PART
    drop foreign key ACT_FK_CASE_SENTRY_CASE_EXEC;

-- indexes for concurrency problems - https://app.camunda.com/jira/browse/CAM-1646 --
drop index ACT_IDX_CASE_EXEC_CASE;
drop index ACT_IDX_CASE_EXEC_PARENT;
drop index ACT_IDX_VARIABLE_CASE_EXEC;
drop index ACT_IDX_VARIABLE_CASE_INST;
drop index ACT_IDX_TASK_CASE_EXEC;
drop index ACT_IDX_TASK_CASE_DEF_ID;
drop index ACT_IDX_CASE_SENTRY_CASE_INST;
drop index ACT_IDX_CASE_SENTRY_CASE_EXEC;

drop index ACT_IDX_CASE_DEF_TENANT_ID;
drop index ACT_IDX_CASE_EXEC_TENANT_ID;

drop table ACT_RE_CASE_DEF;
drop table ACT_RU_CASE_EXECUTION;
drop table ACT_RU_CASE_SENTRY_PART;
