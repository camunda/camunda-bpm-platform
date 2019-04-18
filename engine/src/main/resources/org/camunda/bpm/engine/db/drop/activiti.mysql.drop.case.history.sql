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

drop index ACT_IDX_HI_CAS_I_CLOSE on ACT_HI_CASEINST;
drop index ACT_IDX_HI_CAS_I_BUSKEY on ACT_HI_CASEINST;
drop index ACT_IDX_HI_CAS_I_TENANT_ID on ACT_HI_CASEINST;
drop index ACT_IDX_HI_CAS_A_I_CREATE on ACT_HI_CASEACTINST;
drop index ACT_IDX_HI_CAS_A_I_END on ACT_HI_CASEACTINST;
drop index ACT_IDX_HI_CAS_A_I_COMP on ACT_HI_CASEACTINST;
drop index ACT_IDX_HI_CAS_A_I_CASEINST on ACT_HI_CASEACTINST;
drop index ACT_IDX_HI_CAS_A_I_TENANT_ID on ACT_HI_CASEACTINST;

drop table if exists ACT_HI_CASEINST;
drop table if exists ACT_HI_CASEACTINST;
