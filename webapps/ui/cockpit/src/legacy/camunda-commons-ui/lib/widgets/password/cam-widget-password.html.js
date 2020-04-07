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

module.exports = `<div class="password-input right-addon">
  <i ng-if="loadingState === 'NOT_OK' && password.length > 0" class="glyphicon glyphicon-remove-circle"></i>
  <i ng-if="loadingState === 'OK'" class="glyphicon glyphicon-ok-circle"></i>
  <i ng-if="loadingState === 'LOADING'" class="glyphicon glyphicon-refresh animate-spin"></i>
  <input id="inputPassword"
    name="inputPassword"
    class="form-control"
    type="password"
    ng-model="password"
    ng-invalid="true"
    uib-popover-html="tooltipText"
    popover-enable="loadingState === 'NOT_OK'"
    popover-is-open="loadingState === 'NOT_OK' && password.length > 0"
    popover-trigger="'focus'"
    required></input>
</div>
`;
