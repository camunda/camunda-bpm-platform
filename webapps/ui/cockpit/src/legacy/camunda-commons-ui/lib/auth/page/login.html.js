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

module.exports = `<!-- # CE - camunda-commons-ui/lib/auth/page/login.html -->
<div class="form-signin-container">
  <form class="form-signin"
        ng-submit="login()"
        name="signinForm"
        request-aware>

      <div class="login-header">
        <div class="login-logo" ng-bind-html="logo"></div>
  
        <div class="app-name">
          {{ appName }}
        </div>
      </div>

    <div notifications-panel
         ng-if="signinForm.$dirty"
         class="notifications-panel"></div>

    <input autofocus
           tabindex="1"
           type="text"
           class="form-control"
           placeholder="{{ 'PAGE_LOGIN_USERNAME' | translate }}"
           auto-fill
           required
           ng-model="username"></input>
    <input tabindex="2"
           type="password"
           class="form-control"
           placeholder="{{ 'PAGE_LOGIN_PASSWORD' | translate }}"
           auto-fill
           required
           ng-model="password"></input>
    <button tabindex="3"
            class="btn btn-lg btn-primary"
            type="submit"
            ng-disabled="status === 'LOADING'">{{ 'PAGE_LOGIN_SIGN_IN_ACTION' | translate }}</button>

    <div ng-if="showFirstLogin"
         class="alert-info alert"
         style="margin-top: 75px; margin-bottom: 0px">
      <div>
        <button type="button" class="close" ng-click="dismissInfoBox()">×</button>
        <strong class="status">{{ 'FIRST_LOGIN_HEADING' | translate }}</strong>
        <span class="message" ng-bind-html="FirstLoginMessage" style="display: block"></span>
      </div>
    </div>
  </form>
</div>
<!-- / CE - camunda-commons-ui/lib/auth/page/login.html -->
`;
