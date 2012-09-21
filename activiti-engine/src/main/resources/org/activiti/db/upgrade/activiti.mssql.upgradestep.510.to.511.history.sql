create table ACT_HI_VARINST (
    ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64) not null,
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(100),
    REV_ int,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);
  
update ACT_GE_PROPERTY
  set VALUE_ = VALUE_ + 1,
      REV_ = REV_ + 1
  where NAME_ = 'historyLevel' and VALUE_ >= 2;

alter table ACT_HI_ACTINST
	add column TASK_ID_ nvarchar(64);

alter table ACT_HI_ACTINST
	add column CALL_PROC_INST_ID_ nvarchar(64);	

alter table ACT_HI_DETAIL
	alter column PROC_INST_ID_ nvarchar(64) null;

alter table ACT_HI_DETAIL
	alter column EXECUTION_ID_ nvarchar(64) null;