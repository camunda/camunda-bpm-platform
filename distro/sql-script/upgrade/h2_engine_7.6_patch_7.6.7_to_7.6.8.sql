-- https://app.camunda.com/jira/browse/CAM-7477
drop index ACT_IDX_VARIABLE_TASK_ID;

alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_TASK
    foreign key (TASK_ID_)
    references ACT_RU_TASK(ID_);