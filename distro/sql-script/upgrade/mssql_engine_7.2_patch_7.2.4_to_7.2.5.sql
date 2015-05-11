-- index for deadlock problem - https://app.camunda.com/jira/browse/CAM-3565 --
create index ACT_IDX_EXECUTION_PROCINST on ACT_RU_EXECUTION(PROC_INST_ID_);