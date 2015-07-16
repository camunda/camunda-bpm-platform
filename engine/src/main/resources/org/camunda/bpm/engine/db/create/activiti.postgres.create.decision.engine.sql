-- create decision definition table --
create table ACT_RE_DECISION_DEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    primary key (ID_)
);

-- create unique constraint on ACT_RE_DECISION_DEF --
alter table ACT_RE_DECISION_DEF
    add constraint ACT_UNIQ_DECISION_DEF
    unique (KEY_,VERSION_);
