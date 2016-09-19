-- create history decision instance table --
create table ACT_HI_DECINST (
    ID_ nvarchar(64) NOT NULL,
    DEC_DEF_ID_ nvarchar(64) NOT NULL,
    DEC_DEF_KEY_ nvarchar(255) NOT NULL,
    DEC_DEF_NAME_ nvarchar(255),
    PROC_DEF_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    CASE_DEF_KEY_ nvarchar(255),
    CASE_DEF_ID_ nvarchar(64),
    CASE_INST_ID_ nvarchar(64),
    ACT_INST_ID_ nvarchar(64),
    ACT_ID_ nvarchar(255),
    EVAL_TIME_ datetime2 not null,
    COLLECT_VALUE_ double precision,
    USER_ID_ nvarchar(255),
    ROOT_DEC_INST_ID_ nvarchar(64),
    DEC_REQ_ID_ nvarchar(64),
    DEC_REQ_KEY_ nvarchar(255),
    TENANT_ID_ nvarchar(64),
    primary key (ID_)
);

-- create history decision input table --
create table ACT_HI_DEC_IN (
    ID_ nvarchar(64) NOT NULL,
    DEC_INST_ID_ nvarchar(64) NOT NULL,
    CLAUSE_ID_ nvarchar(64),
    CLAUSE_NAME_ nvarchar(255),
    VAR_TYPE_ nvarchar(100),
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    TENANT_ID_ nvarchar(64),
    primary key (ID_)
);

-- create history decision output table --
create table ACT_HI_DEC_OUT (
    ID_ nvarchar(64) NOT NULL,
    DEC_INST_ID_ nvarchar(64) NOT NULL,
    CLAUSE_ID_ nvarchar(64),
    CLAUSE_NAME_ nvarchar(255),
    RULE_ID_ nvarchar(64),
    RULE_ORDER_ int,
    VAR_NAME_ nvarchar(255),
    VAR_TYPE_ nvarchar(100),
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    TENANT_ID_ nvarchar(64),
    primary key (ID_)
);


create index ACT_IDX_HI_DEC_INST_ID on ACT_HI_DECINST(DEC_DEF_ID_);
create index ACT_IDX_HI_DEC_INST_KEY on ACT_HI_DECINST(DEC_DEF_KEY_);
create index ACT_IDX_HI_DEC_INST_PI on ACT_HI_DECINST(PROC_INST_ID_);
create index ACT_IDX_HI_DEC_INST_CI on ACT_HI_DECINST(CASE_INST_ID_);
create index ACT_IDX_HI_DEC_INST_ACT on ACT_HI_DECINST(ACT_ID_);
create index ACT_IDX_HI_DEC_INST_ACT_INST on ACT_HI_DECINST(ACT_INST_ID_);
create index ACT_IDX_HI_DEC_INST_TIME on ACT_HI_DECINST(EVAL_TIME_);
create index ACT_IDX_HI_DEC_INST_TENANT_ID on ACT_HI_DECINST(TENANT_ID_);
create index ACT_IDX_HI_DEC_INST_ROOT_ID on ACT_HI_DECINST(ROOT_DEC_INST_ID_);
create index ACT_IDX_HI_DEC_INST_REQ_ID on ACT_HI_DECINST(DEC_REQ_ID_);
create index ACT_IDX_HI_DEC_INST_REQ_KEY on ACT_HI_DECINST(DEC_REQ_KEY_);

create index ACT_IDX_HI_DEC_IN_INST on ACT_HI_DEC_IN(DEC_INST_ID_);
create index ACT_IDX_HI_DEC_IN_CLAUSE on ACT_HI_DEC_IN(DEC_INST_ID_, CLAUSE_ID_);

create index ACT_IDX_HI_DEC_OUT_INST on ACT_HI_DEC_OUT(DEC_INST_ID_);
create index ACT_IDX_HI_DEC_OUT_RULE on ACT_HI_DEC_OUT(RULE_ORDER_, CLAUSE_ID_);
