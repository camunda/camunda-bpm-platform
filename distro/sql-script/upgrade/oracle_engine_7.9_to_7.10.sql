-- https://app.camunda.com/jira/browse/CAM-9084
ALTER TABLE ACT_RE_PROCDEF
  ADD STARTABLE_ NUMBER(1,0) DEFAULT 1 NOT NULL CHECK (STARTABLE_ IN (1,0));

-- https://app.camunda.com/jira/browse/CAM-9153
ALTER TABLE ACT_HI_VARINST
  ADD CREATE_TIME_ TIMESTAMP(6);
