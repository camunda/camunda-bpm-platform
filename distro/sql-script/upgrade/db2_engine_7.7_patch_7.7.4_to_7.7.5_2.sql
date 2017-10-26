-- increase the field length https://app.camunda.com/jira/browse/CAM-8177 --
ALTER TABLE ACT_RU_AUTHORIZATION 
  ALTER COLUMN RESOURCE_ID_ 
  SET DATA TYPE varchar(255);