--
-- Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
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

-- https://app.camunda.com/jira/browse/CAM-9920
ALTER TABLE ACT_HI_OP_LOG
  ADD CATEGORY_ nvarchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD EXTERNAL_TASK_ID_ nvarchar(64);

  create table ACT_GE_SCHEMA_LOG (
    ID_ nvarchar(64),
    TIMESTAMP_ datetime2,
    VERSION_ nvarchar(255),
    primary key (ID_)
);

insert into ACT_GE_SCHEMA_LOG
values ('0', CURRENT_TIMESTAMP, '7.11.0');