-----------------------------------------------------------------
-- change column types of type datetime to datetime2 in runtime & history --

-- runtime
alter table ACT_RE_DEPLOYMENT ALTER COLUMN DEPLOY_TIME_ datetime2;
alter table ACT_RU_JOB ALTER COLUMN LOCK_EXP_TIME_ datetime2;
alter table ACT_RU_JOB ALTER COLUMN DUEDATE_ datetime2 NULL;

drop index ACT_RU_TASK.ACT_IDX_TASK_CREATE;
alter table ACT_RU_TASK ALTER COLUMN CREATE_TIME_ datetime2;
create index ACT_IDX_TASK_CREATE on ACT_RU_TASK(CREATE_TIME_);
alter table ACT_RU_TASK ALTER COLUMN DUE_DATE_ datetime2;

alter table ACT_RU_EVENT_SUBSCR ALTER COLUMN CREATED_ datetime2;

--history
alter table ACT_HI_PROCINST ALTER COLUMN START_TIME_ datetime2;

drop index ACT_HI_PROCINST.ACT_IDX_HI_PRO_INST_END;
alter table ACT_HI_PROCINST ALTER COLUMN END_TIME_ datetime2;
create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);

drop index ACT_HI_ACTINST.ACT_IDX_HI_ACT_INST_START;
alter table ACT_HI_ACTINST ALTER COLUMN START_TIME_ datetime2 not null;
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);

drop index ACT_HI_ACTINST.ACT_IDX_HI_ACT_INST_END;
alter table ACT_HI_ACTINST ALTER COLUMN END_TIME_ datetime2;
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);

alter table ACT_HI_TASKINST ALTER COLUMN START_TIME_ datetime2 not null;
alter table ACT_HI_TASKINST ALTER COLUMN END_TIME_ datetime2;
alter table ACT_HI_TASKINST ALTER COLUMN DUE_DATE_ datetime2;

alter table ACT_HI_DETAIL ALTER COLUMN TIME_ datetime2 not null;
alter table ACT_HI_COMMENT ALTER COLUMN TIME_ datetime2 not null;