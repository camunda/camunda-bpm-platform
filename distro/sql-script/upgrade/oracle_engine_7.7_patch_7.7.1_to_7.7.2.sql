-- https://app.camunda.com/jira/browse/CAM-7477
alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_TASK
    foreign key (TASK_ID_)
    references ACT_RU_TASK(ID_);