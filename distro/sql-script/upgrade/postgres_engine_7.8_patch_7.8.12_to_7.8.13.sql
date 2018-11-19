-- https://app.camunda.com/jira/browse/CAM-9525
create index ACT_IDX_HI_OP_LOG_TIMESTAMP on ACT_HI_OP_LOG(TIMESTAMP_);

-- https://app.camunda.com/jira/browse/CAM-9523
create index ACT_IDX_HI_TASK_INST_START on ACT_HI_TASKINST(START_TIME_);
create index ACT_IDX_HI_TASK_INST_END on ACT_HI_TASKINST(END_TIME_);
