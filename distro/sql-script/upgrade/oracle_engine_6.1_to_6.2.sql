-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- rename table ACT_HI_PROCVARIABLE -> ACT_HI_VARINST --

create table ACT_HI_VARINST (
    ID_ NVARCHAR2(64) not null,
    PROC_INST_ID_ NVARCHAR2(64),
    EXECUTION_ID_ NVARCHAR2(64),
    TASK_ID_ NVARCHAR2(64),
    NAME_ NVARCHAR2(255) not null,
    VAR_TYPE_ NVARCHAR2(100),
    REV_ INTEGER,
    BYTEARRAY_ID_ NVARCHAR2(64),
    DOUBLE_ NUMBER(*,10),
    LONG_ NUMBER(19,0),
    TEXT_ NVARCHAR2(2000),
    TEXT2_ NVARCHAR2(2000),
    primary key (ID_)
);

-- copy old values --
insert into ACT_HI_VARINST
	select ID_, PROC_INST_ID_, NULL, NULL, NAME_, VAR_TYPE_, REV_, BYTEARRAY_ID_, DOUBLE_, LONG_, TEXT_, TEXT2_ FROM ACT_HI_PROCVARIABLE;

-- drop old indices --
drop index ACT_IDX_HI_PROCVAR_PROC_INST;
drop index ACT_IDX_HI_PROCVAR_NAME_TYPE;

-- drop old table --
drop table ACT_HI_PROCVARIABLE;

-- create new indices --
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);

-- not migrating --
--   -    DUEDATE_ timestamp null,
--   +    DUEDATE_ timestamp,

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_HI_TASKINST --

alter table ACT_HI_TASKINST
    modify OWNER_ NVARCHAR2(255);

alter table ACT_HI_TASKINST
    modify ASSIGNEE_ NVARCHAR2(255);


-- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_RU_TASK --

alter table ACT_RU_TASK
    modify OWNER_ NVARCHAR2(255);

alter table ACT_RU_TASK
    modify ASSIGNEE_ NVARCHAR2(255);

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_RU_IDENTITYLINK --

drop index ACT_IDX_IDENT_LNK_GROUP;
alter table ACT_RU_IDENTITYLINK
    modify GROUP_ID_ NVARCHAR2(255);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);

drop index ACT_IDX_IDENT_LNK_USER;
alter table ACT_RU_IDENTITYLINK
    modify USER_ID_ NVARCHAR2(255);
create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- change column types in ACT_RU_JOB (remove NOT NULL constraint)
alter table ACT_RU_JOB
	MODIFY DUEDATE_ TIMESTAMP(6);

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -
-- revert introduction of new history level --

update ACT_GE_PROPERTY
  set VALUE_ = VALUE_ - 1,
      REV_ = REV_ + 1
  where NAME_ = 'historyLevel' and VALUE_ >= 2;

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- Additional index on PROC_INST_ID_ and ACT_ID_ for historic activity

create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);