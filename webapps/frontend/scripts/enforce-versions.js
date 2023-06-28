/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const versionDmnJs = require('dmn-js/package.json').version;
const versionFormJs = require('@bpmn-io/form-js/package.json').version;

if (!versionDmnJs.startsWith('14')) {
  console.error("Only dmn-js versions >= 14 and < 15 supported.");
  process.exit(1);
}

if (!versionFormJs.startsWith('0.14')) {
  console.error("Only @bpmn-io/form-js versions >= 0.14.0 and < 0.15.0 supported.");
  process.exit(1);
}
