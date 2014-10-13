create table ACT_HI_CASEINST (
    ID_ nvarchar(64) not null,
    CASE_INST_ID_ nvarchar(64) not null,
    BUSINESS_KEY_ nvarchar(255),
    CASE_DEF_ID_ nvarchar(64) not null,
    CREATE_TIME_ datetime2 not null,
    CLOSE_TIME_ datetime2,
    DURATION_ numeric(19,0),
    STATE_ tinyint,
    CREATE_USER_ID_ nvarchar(255),
    SUPER_CASE_INSTANCE_ID_ nvarchar(64),
    primary key (ID_),
    unique (CASE_INST_ID_)
);

create index ACT_IDX_HI_CAS_I_CLOSE on ACT_HI_CASEINST(CLOSE_TIME_);
create index ACT_IDX_HI_CAS_I_BUSKEY on ACT_HI_CASEINST(BUSINESS_KEY_);
