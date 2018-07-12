-- https://app.camunda.com/jira/browse/CAM-9165
create index ACT_IDX_CASE_EXE_CASE_INST on ACT_RU_CASE_EXECUTION(CASE_INST_ID_);
