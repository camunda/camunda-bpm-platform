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
values ('100', CURRENT_TIMESTAMP, '7.12.0');

-- https://app.camunda.com/jira/browse/CAM-10665
ALTER TABLE ACT_HI_OP_LOG
  ADD ANNOTATION_ nvarchar(4000);

-- https://app.camunda.com/jira/browse/CAM-9855
ALTER TABLE ACT_RU_JOB
  ADD REPEAT_OFFSET_ numeric(19,0) default 0;

-- https://app.camunda.com/jira/browse/CAM-10672
ALTER TABLE ACT_HI_INCIDENT
  ADD HISTORY_CONFIGURATION_ nvarchar(255);

-- https://app.camunda.com/jira/browse/CAM-10600
create index ACT_IDX_HI_DETAIL_VAR_INST_ID on ACT_HI_DETAIL(VAR_INST_ID_);
