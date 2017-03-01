-- index to prevent deadlock on fk constraint - https://app.camunda.com/jira/browse/CAM-7263 --
create index ACT_IDX_EXT_TASK_ERR_DETAILS ON ACT_RU_EXT_TASK(ERROR_DETAILS_ID_);