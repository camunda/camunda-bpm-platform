-- Related to https://app.camunda.com/jira/browse/CAM-7676
drop index ACT_IDX_JOB_HANDLER;

-- https://app.camunda.com/jira/browse/CAM-9084
ALTER TABLE ACT_RE_PROCDEF
  ADD STARTABLE_ smallint DEFAULT 1 not null check(STARTABLE_ in (1,0));

-- https://app.camunda.com/jira/browse/CAM-9153
ALTER TABLE ACT_HI_VARINST
  ADD CREATE_TIME_ TIMESTAMP;
