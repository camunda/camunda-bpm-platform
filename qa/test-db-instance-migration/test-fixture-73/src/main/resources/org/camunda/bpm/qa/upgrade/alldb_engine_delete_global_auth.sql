-- Related to https://app.camunda.com/jira/browse/CAM-10341
-- the script is valid for all databases
DELETE FROM ACT_RU_AUTHORIZATION
WHERE USER_ID_ = '*' AND RESOURCE_ID_ = '*';