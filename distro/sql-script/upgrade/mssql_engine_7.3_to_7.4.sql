-- metrics --

ALTER TABLE ACT_RU_METER_LOG
  ADD REPORTER_ nvarchar(255);

-- job prioritization --
  
ALTER TABLE ACT_RU_JOB
  ADD PRIORITY_ int NOT NULL
  DEFAULT 0;

ALTER TABLE ACT_RU_JOBDEF
  ADD JOB_PRIORITY_ int;
  
ALTER TABLE ACT_HI_JOB_LOG
  ADD JOB_PRIORITY_ int NOT NULL
  DEFAULT 0;

-- create decision definition table --
create table ACT_RE_DECISION_DEF (
    ID_ nvarchar(64) not null,
    REV_ int,
    CATEGORY_ nvarchar(255),
    NAME_ nvarchar(255),
    KEY_ nvarchar(255) not null,
    VERSION_ int not null,
    DEPLOYMENT_ID_ nvarchar(64),
    RESOURCE_NAME_ nvarchar(4000),
    DGRM_RESOURCE_NAME_ nvarchar(4000),
    primary key (ID_)
);

-- create unique constraint on ACT_RE_DECISION_DEF --
alter table ACT_RE_DECISION_DEF
    add constraint ACT_UNIQ_DECISION_DEF
    unique (KEY_,VERSION_);

-- case execution repetition rule --

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD REPEATABLE_ tinyint;

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD REPETITION_ tinyint;

-- historic case activity instance repetition rule --

ALTER TABLE ACT_HI_CASEACTINST
  ADD REPEATABLE_ tinyint;

ALTER TABLE ACT_HI_CASEACTINST
  ADD REPETITION_ tinyint;

-- case sentry part source --

ALTER TABLE ACT_RU_CASE_SENTRY_PART
  ADD SOURCE_ nvarchar(255);
