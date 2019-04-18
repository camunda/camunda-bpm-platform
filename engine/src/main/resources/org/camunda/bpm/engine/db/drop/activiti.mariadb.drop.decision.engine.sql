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

alter table ACT_RE_DECISION_DEF
    drop FOREIGN KEY ACT_FK_DEC_REQ;

drop index ACT_IDX_DEC_DEF_TENANT_ID on ACT_RE_DECISION_DEF;
drop index ACT_IDX_DEC_DEF_REQ_ID on ACT_RE_DECISION_DEF;
drop index ACT_IDX_DEC_REQ_DEF_TENANT_ID on ACT_RE_DECISION_REQ_DEF;

drop table if exists ACT_RE_DECISION_DEF;
drop table if exists ACT_RE_DECISION_REQ_DEF;
