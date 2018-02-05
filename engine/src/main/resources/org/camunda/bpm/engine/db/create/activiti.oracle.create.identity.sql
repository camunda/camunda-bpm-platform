create table ACT_ID_GROUP (
    ID_ NVARCHAR2(64),
    REV_ INTEGER,
    NAME_ NVARCHAR2(255),
    TYPE_ NVARCHAR2(255),
    primary key (ID_)
);

create table ACT_ID_MEMBERSHIP (
    USER_ID_ NVARCHAR2(64),
    GROUP_ID_ NVARCHAR2(64),
    primary key (USER_ID_, GROUP_ID_)
);

create table ACT_ID_USER (
    ID_ NVARCHAR2(64),
    REV_ INTEGER,
    FIRST_ NVARCHAR2(255),
    LAST_ NVARCHAR2(255),
    EMAIL_ NVARCHAR2(255),
    PWD_ NVARCHAR2(255),
    SALT_ NVARCHAR2(255),
    LOCK_EXP_TIME_ TIMESTAMP(6),
    ATTEMPTS_ INTEGER,
    PICTURE_ID_ NVARCHAR2(64),
    primary key (ID_)
);

create table ACT_ID_INFO (
    ID_ NVARCHAR2(64),
    REV_ INTEGER,
    USER_ID_ NVARCHAR2(64),
    TYPE_ NVARCHAR2(64),
    KEY_ NVARCHAR2(255),
    VALUE_ NVARCHAR2(255),
    PASSWORD_ BLOB,
    PARENT_ID_ NVARCHAR2(255),
    primary key (ID_)
);

create table ACT_ID_TENANT (
    ID_ NVARCHAR2(64),
    REV_ INTEGER,
    NAME_ NVARCHAR2(255),
    primary key (ID_)
);

create table ACT_ID_TENANT_MEMBER (
    ID_ NVARCHAR2(64) not null,
    TENANT_ID_ NVARCHAR2(64) not null,
    USER_ID_ NVARCHAR2(64),
    GROUP_ID_ NVARCHAR2(64),
    primary key (ID_)
);

create index ACT_IDX_MEMB_GROUP on ACT_ID_MEMBERSHIP(GROUP_ID_);
alter table ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_GROUP
    foreign key (GROUP_ID_)
    references ACT_ID_GROUP (ID_);

create index ACT_IDX_MEMB_USER on ACT_ID_MEMBERSHIP(USER_ID_);
alter table ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_USER
    foreign key (USER_ID_)
    references ACT_ID_USER (ID_);

create index ACT_IDX_TENANT_MEMB on ACT_ID_TENANT_MEMBER(TENANT_ID_);
alter table ACT_ID_TENANT_MEMBER
    add constraint ACT_FK_TENANT_MEMB
    foreign key (TENANT_ID_)
    references ACT_ID_TENANT (ID_);

create index ACT_IDX_TENANT_MEMB_USER on ACT_ID_TENANT_MEMBER(USER_ID_);
alter table ACT_ID_TENANT_MEMBER
    add constraint ACT_FK_TENANT_MEMB_USER
    foreign key (USER_ID_)
    references ACT_ID_USER (ID_);

create index ACT_IDX_TENANT_MEMB_GROUP on ACT_ID_TENANT_MEMBER(GROUP_ID_);
alter table ACT_ID_TENANT_MEMBER
    add constraint ACT_FK_TENANT_MEMB_GROUP
    foreign key (GROUP_ID_)
    references ACT_ID_GROUP (ID_);

create unique index ACT_UNIQ_TENANT_MEMB_USER on ACT_ID_TENANT_MEMBER
   (case when USER_ID_ is null then null else TENANT_ID_ end,
    case when USER_ID_ is null then null else USER_ID_ end);

create unique index ACT_UNIQ_TENANT_MEMB_GROUP on ACT_ID_TENANT_MEMBER
   (case when GROUP_ID_ is null then null else TENANT_ID_ end,
    case when GROUP_ID_ is null then null else GROUP_ID_ end);

