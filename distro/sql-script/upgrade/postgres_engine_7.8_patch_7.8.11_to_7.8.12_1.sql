-- https://app.camunda.com/jira/browse/CAM-9435
create index ACT_IDX_HI_DETAIL_TASK_BYTEAR on ACT_HI_DETAIL(BYTEARRAY_ID_, TASK_ID_);
