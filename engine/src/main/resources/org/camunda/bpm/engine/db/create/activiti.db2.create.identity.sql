create table ACT_ID_GROUP (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    TYPE_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_MEMBERSHIP (
    USER_ID_ varchar(64) not null,
    GROUP_ID_ varchar(64) not null,
    primary key (USER_ID_, GROUP_ID_)
);

create table ACT_ID_USER (
    ID_ varchar(64) not null,
    REV_ integer,
    FIRST_ varchar(255),
    LAST_ varchar(255),
    EMAIL_ varchar(255),
    PWD_ varchar(255),
    SALT_ varchar(255),
    LOCK_EXP_TIME_ timestamp,
    ATTEMPTS_ integer,
    PICTURE_ID_ varchar(64),
    primary key (ID_)
);

create table ACT_ID_INFO (
    ID_ varchar(64) not null,
    REV_ integer,
    USER_ID_ varchar(64),
    TYPE_ varchar(64),
    KEY_ varchar(255),
    VALUE_ varchar(255),
    PASSWORD_ BLOB,
    PARENT_ID_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_TENANT (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_TENANT_MEMBER (
    ID_ varchar(64) not null,
    TENANT_ID_ varchar(64) not null,
    USER_ID_ varchar(64),
    GROUP_ID_ varchar(64),
    primary key (ID_),
    UNI_USER_ID_ varchar (255) not null generated always as (case when "USER_ID_" is null then "ID_" else "USER_ID_" end),
    UNI_GROUP_ID_ varchar (255) not null generated always as (case when "GROUP_ID_" is null then "ID_" else "GROUP_ID_" end)
);

alter table ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_GROUP
    foreign key (GROUP_ID_)
    references ACT_ID_GROUP (ID_);

alter table ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_USER
    foreign key (USER_ID_)
    references ACT_ID_USER (ID_);

alter table ACT_ID_TENANT_MEMBER
    add constraint ACT_FK_TENANT_MEMB
    foreign key (TENANT_ID_)
    references ACT_ID_TENANT (ID_);

alter table ACT_ID_TENANT_MEMBER
    add constraint ACT_FK_TENANT_MEMB_USER
    foreign key (USER_ID_)
    references ACT_ID_USER (ID_);

alter table ACT_ID_TENANT_MEMBER
    add constraint ACT_FK_TENANT_MEMB_GROUP
    foreign key (GROUP_ID_)
    references ACT_ID_GROUP (ID_);

create unique index ACT_UNIQ_TENANT_MEMB_USER on ACT_ID_TENANT_MEMBER(TENANT_ID_,UNI_USER_ID_);
create unique index ACT_UNIQ_TENANT_MEMB_GROUP on ACT_ID_TENANT_MEMBER(TENANT_ID_,UNI_GROUP_ID_);
