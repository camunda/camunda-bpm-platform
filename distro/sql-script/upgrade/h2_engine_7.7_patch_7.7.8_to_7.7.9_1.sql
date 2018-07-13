-- https://app.camunda.com/jira/browse/CAM-8833
create index ACT_IDX_HI_PI_PDEFID_END_TIME on ACT_HI_PROCINST(PROC_DEF_ID_, END_TIME_);
create index ACT_IDX_HI_AI_PDEFID_END_TIME on ACT_HI_ACTINST(PROC_DEF_ID_, END_TIME_);