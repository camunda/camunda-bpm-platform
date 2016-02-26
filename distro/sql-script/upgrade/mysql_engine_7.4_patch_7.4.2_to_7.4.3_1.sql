-- index to improve historic activity instance query - https://app.camunda.com/jira/browse/CAM-5257 --
create index ACT_IDX_HI_ACT_INST_STATS on ACT_HI_ACTINST(PROC_DEF_ID_, ACT_ID_, END_TIME_, ACT_INST_STATE_);
