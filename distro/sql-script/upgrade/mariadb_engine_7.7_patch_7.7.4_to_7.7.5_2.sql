-- increase the field length https://app.camunda.com/jira/browse/CAM-8177 --
ALTER TABLE ACT_RU_AUTHORIZATION 
  MODIFY COLUMN RESOURCE_ID_ varchar(255);