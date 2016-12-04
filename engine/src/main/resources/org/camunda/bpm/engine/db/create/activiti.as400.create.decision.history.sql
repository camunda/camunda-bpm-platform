-- create history decision instance table --
create table ACT_HI_DECINST (
    ID_ varchar(64) NOT NULL,
    DEC_DEF_ID_ varchar(64) NOT NULL,
    DEC_DEF_KEY_ varchar(255) NOT NULL,
    DEC_DEF_NAME_ varchar(255),
    PROC_DEF_KEY_ varchar(255),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    CASE_DEF_KEY_ varchar(255),
    CASE_DEF_ID_ varchar(64),
    CASE_INST_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    ACT_ID_ varchar(255),
    EVAL_TIME_ timestamp not null,
    COLLECT_VALUE_ double precision,
    USER_ID_ varchar(255),
    ROOT_DEC_INST_ID_ varchar(64),
    DEC_REQ_ID_ varchar(64),
    DEC_REQ_KEY_ varchar(255),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

-- create history decision input table --
create table ACT_HI_DEC_IN (
    ID_ varchar(64) NOT NULL,
    DEC_INST_ID_ varchar(64) NOT NULL,
    CLAUSE_ID_ varchar(64),
    CLAUSE_NAME_ varchar(255),
    VAR_TYPE_ varchar(100),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

-- create history decision output table --
create table ACT_HI_DEC_OUT (
    ID_ varchar(64) NOT NULL,
    DEC_INST_ID_ varchar(64) NOT NULL,
    CLAUSE_ID_ varchar(64),
    CLAUSE_NAME_ varchar(255),
    RULE_ID_ varchar(64),
    RULE_ORDER_ integer,
    VAR_NAME_ varchar(255),
    VAR_TYPE_ varchar(100),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    TENANT_ID_ varchar(64),
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

--- labels
label on table ACT_HI_DECINST is 'ACT_HI_DECINST';
label on table ACT_HI_DEC_IN is 'ACT_HI_DEC_IN';
label on table ACT_HI_DEC_OUT is 'ACT_HI_DEC_OUT';
label on index ACT_IDX_HI_DEC_INST_ID is 'ACT_IDX_HI_DEC_INST_ID';
label on index ACT_IDX_HI_DEC_INST_KEY is 'ACT_IDX_HI_DEC_INST_KEY';
label on index ACT_IDX_HI_DEC_INST_PI is 'ACT_IDX_HI_DEC_INST_PI';
label on index ACT_IDX_HI_DEC_INST_CI is 'ACT_IDX_HI_DEC_INST_CI';
label on index ACT_IDX_HI_DEC_INST_ACT is 'ACT_IDX_HI_DEC_INST_ACT';
label on index ACT_IDX_HI_DEC_INST_ACT_INST is 'ACT_IDX_HI_DEC_INST_ACT_INST';
label on index ACT_IDX_HI_DEC_INST_TIME is 'ACT_IDX_HI_DEC_INST_TIME';
label on index ACT_IDX_HI_DEC_INST_TENANT_ID is 'ACT_IDX_HI_DEC_INST_TENANT_ID';
label on index ACT_IDX_HI_DEC_INST_ROOT_ID is 'ACT_IDX_HI_DEC_INST_ROOT_ID';
label on index ACT_IDX_HI_DEC_INST_REQ_ID is 'ACT_IDX_HI_DEC_INST_REQ_ID';
label on index ACT_IDX_HI_DEC_INST_REQ_KEY is 'ACT_IDX_HI_DEC_INST_REQ_KEY';
label on index ACT_IDX_HI_DEC_IN_INST is 'ACT_IDX_HI_DEC_IN_INST';
label on index ACT_IDX_HI_DEC_IN_CLAUSE is 'ACT_IDX_HI_DEC_IN_CLAUSE';
label on index ACT_IDX_HI_DEC_OUT_INST is 'ACT_IDX_HI_DEC_OUT_INST';
label on index ACT_IDX_HI_DEC_OUT_RULE is 'ACT_IDX_HI_DEC_OUT_RULE';
