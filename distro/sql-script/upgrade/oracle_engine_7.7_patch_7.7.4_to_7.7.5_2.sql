-- increase the field length https://app.camunda.com/jira/browse/CAM-8177 --
DROP INDEX ACT_UNIQ_AUTH_USER;
DROP INDEX ACT_UNIQ_AUTH_GROUP;

ALTER TABLE ACT_RU_AUTHORIZATION 
  MODIFY RESOURCE_ID_ nvarchar2(255);

create unique index ACT_UNIQ_AUTH_USER on ACT_RU_AUTHORIZATION
   (case when USER_ID_ is null then null else TYPE_ end,
    case when USER_ID_ is null then null else RESOURCE_TYPE_ end,
    case when USER_ID_ is null then null else RESOURCE_ID_ end,
    case when USER_ID_ is null then null else USER_ID_ end);

create unique index ACT_UNIQ_AUTH_GROUP on ACT_RU_AUTHORIZATION
   (case when GROUP_ID_ is null then null else TYPE_ end,
    case when GROUP_ID_ is null then null else RESOURCE_TYPE_ end,
    case when GROUP_ID_ is null then null else RESOURCE_ID_ end,
    case when GROUP_ID_ is null then null else GROUP_ID_ end);
