-- add deployment.lock row to property table --
INSERT INTO ACT_GE_PROPERTY
  VALUES ('deployment.lock', '0', 1);

-- add revision column to incident table --
ALTER TABLE ACT_RU_INCIDENT
  ADD REV_ INTEGER;

-- set initial incident revision to 1 --
UPDATE
  ACT_RU_INCIDENT
SET
  REV_ = 1;

-- set incident revision column to not null --
ALTER TABLE ACT_RU_INCIDENT
  MODIFY (REV_ NOT NULL);

-- case management

create table ACT_RE_CASE_DEF (
    ID_ NVARCHAR2(64) NOT NULL,
    REV_ INTEGER,
    CATEGORY_ NVARCHAR2(255),
    NAME_ NVARCHAR2(255),
    KEY_ NVARCHAR2(255) NOT NULL,
    VERSION_ INTEGER NOT NULL,
    DEPLOYMENT_ID_ NVARCHAR2(64),
    RESOURCE_NAME_ NVARCHAR2(2000),
    primary key (ID_)
);

alter table ACT_RE_CASE_DEF
    add constraint ACT_UNIQ_CASE_DEF
    unique (KEY_,VERSION_);
