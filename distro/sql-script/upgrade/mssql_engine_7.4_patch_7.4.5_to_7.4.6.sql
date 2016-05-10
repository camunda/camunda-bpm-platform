-- INCREASE process def key column size https://app.camunda.com/jira/browse/CAM-4328 --
alter table ACT_RU_JOB
  alter column PROCESS_DEF_KEY_ nvarchar(255);