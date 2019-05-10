--
-- Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. Camunda licenses this file to you under the Apache License,
-- Version 2.0; you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

/** 03.08.2012 */
alter table ACT_RU_EXECUTION add CACHED_ENT_STATE_ INTEGER;
update ACT_RU_EXECUTION set CACHED_ENT_STATE_ = 7;

alter table ACT_RU_IDENTITYLINK
add PROC_DEF_ID_ NVARCHAR2(64);

create index ACT_IDX_ATHRZ_PROCEDEF on ACT_RU_IDENTITYLINK(PROC_DEF_ID_);

alter table ACT_RE_PROCDEF
    modify KEY_ not null;

alter table ACT_RE_PROCDEF
    modify VERSION_ not null;

alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_);

create table ACT_HI_PROCVARIABLE (
    ID_ NVARCHAR2(64) not null,
    PROC_INST_ID_ NVARCHAR2(64) not null,
    NAME_ NVARCHAR2(255) not null,
    VAR_TYPE_ NVARCHAR2(100),
    REV_ INTEGER,
    BYTEARRAY_ID_ NVARCHAR2(64),
    DOUBLE_ NUMBER(*,10),
    LONG_ NUMBER(19,0),
    TEXT_ NVARCHAR2(2000),
    TEXT2_ NVARCHAR2(2000),
    primary key (ID_)
);

create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_PROCVARIABLE(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_PROCVARIABLE(NAME_, VAR_TYPE_);

alter table ACT_HI_ACTINST
add TASK_ID_ NVARCHAR2(64);

alter table ACT_HI_ACTINST
add CALL_PROC_INST_ID_ NVARCHAR2(64);

/** 17.08.2012 */

/**  fill table ACT_HI_PROCVARIABLE when HISTORY_LEVEL FULL is set, could take a long time depending on the amount of data! */
insert into ACT_HI_PROCVARIABLE
  (ID_,PROC_INST_ID_,NAME_,VAR_TYPE_,REV_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_)
  select d.ID_,d.PROC_INST_ID_,d.NAME_,d.VAR_TYPE_,d.REV_,d.BYTEARRAY_ID_,d.DOUBLE_,d.LONG_,d.TEXT_,d.TEXT2_
  from ACT_HI_DETAIL d
  inner join
    (
      select de.PROC_INST_ID_, de.NAME_, MAX(de.TIME_) as MAXTIME
      from ACT_HI_DETAIL de
      inner join ACT_HI_PROCINST p on de.PROC_INST_ID_ = p.ID_
      where p.END_TIME_ is not NULL
      group by de.PROC_INST_ID_, de.NAME_
    )
  groupeddetail on d.PROC_INST_ID_ = groupeddetail.PROC_INST_ID_
  and d.NAME_ = groupeddetail.NAME_
  and d.TIME_ = groupeddetail.MAXTIME
  and (select prop.VALUE_ from ACT_GE_PROPERTY prop where prop.NAME_ = 'historyLevel') = 3;

/** update history level to retain old history level */
update ACT_GE_PROPERTY
set VALUE_ = VALUE_ + 1,
    REV_ = REV_ + 1
where NAME_ = 'historyLevel' and VALUE_ >= 2;

create index ACT_IDX_EXE_PROCDEF on ACT_RU_EXECUTION(PROC_DEF_ID_);
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_HI_DETAIL
  modify PROC_INST_ID_ NVARCHAR2(64) null;

alter table ACT_HI_DETAIL
  modify EXECUTION_ID_ NVARCHAR2(64) null;
