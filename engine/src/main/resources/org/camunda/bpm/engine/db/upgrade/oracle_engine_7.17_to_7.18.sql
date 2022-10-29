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
values ('700', CURRENT_TIMESTAMP, '7.18.0');

-- https://jira.camunda.com/browse/CAM-14303 --
ALTER TABLE ACT_RU_TASK
  ADD LAST_UPDATED_ TIMESTAMP(6);
create index ACT_IDX_TASK_LAST_UPDATED on ACT_RU_TASK(LAST_UPDATED_);

-- https://jira.camunda.com/browse/CAM-14721
ALTER TABLE ACT_RU_BATCH
    ADD START_TIME_ TIMESTAMP(6);

-- https://jira.camunda.com/browse/CAM-14722
ALTER TABLE ACT_RU_BATCH
    ADD EXEC_START_TIME_ TIMESTAMP(6);
ALTER TABLE ACT_HI_BATCH
    ADD EXEC_START_TIME_ TIMESTAMP(6);