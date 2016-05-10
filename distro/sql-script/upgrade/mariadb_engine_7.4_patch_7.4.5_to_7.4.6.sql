-- INCREASE process def key column size https://app.camunda.com/jira/browse/CAM-4328 --
alter table ACT_RU_JOB
  MODIFY COLUMN PROCESS_DEF_KEY_ varchar(255);