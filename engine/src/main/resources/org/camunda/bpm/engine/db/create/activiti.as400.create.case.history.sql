create table ACT_HI_CASEINST (
    ID_ varchar(64) not null,
    CASE_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    CASE_DEF_ID_ varchar(64) not null,
    CREATE_TIME_ timestamp not null,
    CLOSE_TIME_ timestamp,
    DURATION_ bigint,
    STATE_ integer,
    CREATE_USER_ID_ varchar(255),
    SUPER_CASE_INSTANCE_ID_ varchar(64),
    SUPER_PROCESS_INSTANCE_ID_ varchar(64),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

alter table ACT_HI_CASEINST add constraint CASE_INST_ID_ unique(CASE_INST_ID_);

create table ACT_HI_CASEACTINST (
    ID_ varchar(64) not null,
    PARENT_ACT_INST_ID_ varchar(64),
    CASE_DEF_ID_ varchar(64) not null,
    CASE_INST_ID_ varchar(64) not null,
    CASE_ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    CALL_CASE_INST_ID_ varchar(64),
    CASE_ACT_NAME_ varchar(255),
    CASE_ACT_TYPE_ varchar(255),
    CREATE_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    STATE_ integer,
    REQUIRED_ smallint check(REQUIRED_ in (1,0)),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

create index ACT_IDX_HI_CAS_I_CLOSE on ACT_HI_CASEINST(CLOSE_TIME_);
create index ACT_IDX_HI_CAS_I_BUSKEY on ACT_HI_CASEINST(BUSINESS_KEY_);
create index ACT_IDX_HI_CAS_I_TENANT_ID on ACT_HI_CASEINST(TENANT_ID_);
create index ACT_IDX_HI_CAS_A_I_CREATE on ACT_HI_CASEACTINST(CREATE_TIME_);
create index ACT_IDX_HI_CAS_A_I_END on ACT_HI_CASEACTINST(END_TIME_);
create index ACT_IDX_HI_CAS_A_I_COMP on ACT_HI_CASEACTINST(CASE_ACT_ID_, END_TIME_, ID_);
create index ACT_IDX_HI_CAS_A_I_TENANT_ID on ACT_HI_CASEACTINST(TENANT_ID_);

-- labels
label on table ACT_HI_CASEINST is 'ACT_HI_CASEINST';
label on table ACT_HI_CASEACTINST is 'ACT_HI_CASEACTINST';
label on index ACT_IDX_HI_CAS_I_CLOSE is 'ACT_IDX_HI_CAS_I_CLOSE';
label on index ACT_IDX_HI_CAS_I_BUSKEY is 'ACT_IDX_HI_CAS_I_BUSKEY';
label on index ACT_IDX_HI_CAS_I_TENANT_ID is 'ACT_IDX_HI_CAS_I_TENANT_ID';
label on index ACT_IDX_HI_CAS_A_I_CREATE is 'ACT_IDX_HI_CAS_A_I_CREATE';
label on index ACT_IDX_HI_CAS_A_I_END is 'ACT_IDX_HI_CAS_A_I_END';
label on index ACT_IDX_HI_CAS_A_I_COMP is 'ACT_IDX_HI_CAS_A_I_COMP';
label on index ACT_IDX_HI_CAS_A_I_TENANT_ID is 'ACT_IDX_HI_CAS_A_I_TENANT_ID';

