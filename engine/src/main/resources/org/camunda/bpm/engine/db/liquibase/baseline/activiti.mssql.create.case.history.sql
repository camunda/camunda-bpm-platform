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
    SUPER_PROCESS_INSTANCE_ID_ nvarchar(64),
    TENANT_ID_ nvarchar(64),
    primary key (ID_),
    unique (CASE_INST_ID_)
);

create table ACT_HI_CASEACTINST (
    ID_ nvarchar(64) not null,
    PARENT_ACT_INST_ID_ nvarchar(64),
    CASE_DEF_ID_ nvarchar(64) not null,
    CASE_INST_ID_ nvarchar(64) not null,
    CASE_ACT_ID_ nvarchar(255) not null,
    TASK_ID_ nvarchar(64),
    CALL_PROC_INST_ID_ nvarchar(64),
    CALL_CASE_INST_ID_ nvarchar(64),
    CASE_ACT_NAME_ nvarchar(255),
    CASE_ACT_TYPE_ nvarchar(255),
    CREATE_TIME_ datetime2 not null,
    END_TIME_ datetime2,
    DURATION_ numeric(19,0),
    STATE_ tinyint,
    REQUIRED_ tinyint,
    TENANT_ID_ nvarchar(64),
    primary key (ID_)
);

create index ACT_IDX_HI_CAS_I_CLOSE on ACT_HI_CASEINST(CLOSE_TIME_);
create index ACT_IDX_HI_CAS_I_BUSKEY on ACT_HI_CASEINST(BUSINESS_KEY_);
create index ACT_IDX_HI_CAS_I_TENANT_ID on ACT_HI_CASEINST(TENANT_ID_);
create index ACT_IDX_HI_CAS_A_I_CREATE on ACT_HI_CASEACTINST(CREATE_TIME_);
create index ACT_IDX_HI_CAS_A_I_END on ACT_HI_CASEACTINST(END_TIME_);
create index ACT_IDX_HI_CAS_A_I_COMP on ACT_HI_CASEACTINST(CASE_ACT_ID_, END_TIME_, ID_);
create index ACT_IDX_HI_CAS_A_I_TENANT_ID on ACT_HI_CASEACTINST(TENANT_ID_);
