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

-- create history decision instance table --
create table ACT_HI_DECINST (
    ID_ NVARCHAR2(64) NOT NULL,
    DEC_DEF_ID_ NVARCHAR2(64) NOT NULL,
    DEC_DEF_KEY_ NVARCHAR2(255) NOT NULL,
    DEC_DEF_NAME_ NVARCHAR2(255),
    PROC_DEF_KEY_ NVARCHAR2(255),
    PROC_DEF_ID_ NVARCHAR2(64),
    PROC_INST_ID_ NVARCHAR2(64),
    CASE_DEF_KEY_ NVARCHAR2(255),
    CASE_DEF_ID_ NVARCHAR2(64),
    CASE_INST_ID_ NVARCHAR2(64),
    ACT_INST_ID_ NVARCHAR2(64),
    ACT_ID_ NVARCHAR2(255),
    EVAL_TIME_ TIMESTAMP(6) not null,
    REMOVAL_TIME_ TIMESTAMP(6),
    COLLECT_VALUE_ NUMBER(*,10),
    USER_ID_ NVARCHAR2(255),
    ROOT_DEC_INST_ID_ NVARCHAR2(64),
    ROOT_PROC_INST_ID_ NVARCHAR2(64),
    DEC_REQ_ID_ NVARCHAR2(64),
    DEC_REQ_KEY_ NVARCHAR2(255),
    TENANT_ID_ NVARCHAR2(64),
    primary key (ID_)
);

-- create history decision input table --
create table ACT_HI_DEC_IN (
    ID_ NVARCHAR2(64) NOT NULL,
    DEC_INST_ID_ NVARCHAR2(64) NOT NULL,
    CLAUSE_ID_ NVARCHAR2(64),
    CLAUSE_NAME_ NVARCHAR2(255),
    VAR_TYPE_ NVARCHAR2(100),
    BYTEARRAY_ID_ NVARCHAR2(64),
    DOUBLE_ NUMBER(*,10),
    LONG_ NUMBER(19,0),
    TEXT_ NVARCHAR2(2000),
    TEXT2_ NVARCHAR2(2000),
    TENANT_ID_ NVARCHAR2(64),
    CREATE_TIME_ TIMESTAMP(6),
    ROOT_PROC_INST_ID_ NVARCHAR2(64),
    REMOVAL_TIME_ TIMESTAMP(6),
    primary key (ID_)
);

-- create history decision output table --
create table ACT_HI_DEC_OUT (
    ID_ NVARCHAR2(64) NOT NULL,
    DEC_INST_ID_ NVARCHAR2(64) NOT NULL,
    CLAUSE_ID_ NVARCHAR2(64),
    CLAUSE_NAME_ NVARCHAR2(255),
    RULE_ID_ NVARCHAR2(64),
    RULE_ORDER_ INTEGER,
    VAR_NAME_ NVARCHAR2(255),
    VAR_TYPE_ NVARCHAR2(100),
    BYTEARRAY_ID_ NVARCHAR2(64),
    DOUBLE_ NUMBER(*,10),
    LONG_ NUMBER(19,0),
    TEXT_ NVARCHAR2(2000),
    TEXT2_ NVARCHAR2(2000),
    TENANT_ID_ NVARCHAR2(64),
    CREATE_TIME_ TIMESTAMP(6),
    ROOT_PROC_INST_ID_ NVARCHAR2(64),
    REMOVAL_TIME_ TIMESTAMP(6),
    primary key (ID_)
);


create index ACT_IDX_HI_DEC_INST_ID on ACT_HI_DECINST(DEC_DEF_ID_);
create index ACT_IDX_HI_DEC_INST_KEY on ACT_HI_DECINST(DEC_DEF_KEY_);
create index ACT_IDX_HI_DEC_INST_PI on ACT_HI_DECINST(PROC_INST_ID_);
create index ACT_IDX_HI_DEC_INST_CI on ACT_HI_DECINST(CASE_INST_ID_);
create index ACT_IDX_HI_DEC_INST_ACT on ACT_HI_DECINST(ACT_ID_);
create index ACT_IDX_HI_DEC_INST_ACT_INST on ACT_HI_DECINST(ACT_INST_ID_);
create index ACT_IDX_HI_DEC_INST_TIME on ACT_HI_DECINST(EVAL_TIME_);
create index ACT_IDX_HI_DEC_INST_TENANT_ID on ACT_HI_DECINST(TENANT_ID_, 0);
create index ACT_IDX_HI_DEC_INST_ROOT_ID on ACT_HI_DECINST(ROOT_DEC_INST_ID_);
create index ACT_IDX_HI_DEC_INST_REQ_ID on ACT_HI_DECINST(DEC_REQ_ID_);
create index ACT_IDX_HI_DEC_INST_REQ_KEY on ACT_HI_DECINST(DEC_REQ_KEY_);
create index ACT_IDX_HI_DEC_INST_ROOT_PI on ACT_HI_DECINST(ROOT_PROC_INST_ID_);
create index ACT_IDX_HI_DEC_INST_RM_TIME on ACT_HI_DECINST(REMOVAL_TIME_);

create index ACT_IDX_HI_DEC_IN_INST on ACT_HI_DEC_IN(DEC_INST_ID_);
create index ACT_IDX_HI_DEC_IN_CLAUSE on ACT_HI_DEC_IN(DEC_INST_ID_, CLAUSE_ID_);
create index ACT_IDX_HI_DEC_IN_ROOT_PI on ACT_HI_DEC_IN(ROOT_PROC_INST_ID_);
create index ACT_IDX_HI_DEC_IN_RM_TIME on ACT_HI_DEC_IN(REMOVAL_TIME_);

create index ACT_IDX_HI_DEC_OUT_INST on ACT_HI_DEC_OUT(DEC_INST_ID_);
create index ACT_IDX_HI_DEC_OUT_RULE on ACT_HI_DEC_OUT(RULE_ORDER_, CLAUSE_ID_);
create index ACT_IDX_HI_DEC_OUT_ROOT_PI on ACT_HI_DEC_OUT(ROOT_PROC_INST_ID_);
create index ACT_IDX_HI_DEC_OUT_RM_TIME on ACT_HI_DEC_OUT(REMOVAL_TIME_);
