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

insert into ACT_GE_SCHEMA_LOG
values ('1000', CURRENT_TIMESTAMP, '7.21.0');

alter table ACT_RU_EXT_TASK
  add CREATE_TIME_ datetime2;

alter table ACT_RU_JOB
  add ROOT_PROC_INST_ID_ nvarchar(64);

--https://github.com/camunda/camunda-bpm-platform/pull/4030
create index ACT_IDX_TASK_PARENT_TASK_ID on ACT_RU_TASK(PARENT_TASK_ID_);
create index ACT_IDX_JOB_ROOT_PROCINST on ACT_RU_JOB(ROOT_PROC_INST_ID_);