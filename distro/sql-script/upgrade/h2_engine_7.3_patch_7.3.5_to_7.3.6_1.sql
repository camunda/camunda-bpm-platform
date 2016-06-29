-- INCREASE process def key column size https://app.camunda.com/jira/browse/CAM-4328 --
ALTER TABLE ACT_RU_JOB
  alter column PROCESS_DEF_KEY_ varchar(255);