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

module.exports = `<div class="navbar-header">
  <button type="button"
          class="navbar-toggle"
          ng-class="{open: !!navbarOpen}"
          ng-click="navbarOpen = !navbarOpen">
    <em class="sr-only">{{ toggleNavigation }}</em>
    <span></span>
    <span></span>
    <span></span>
  </button>

  <a class="navbar-brand"
     href="#/"
     title="{{ brandName }} {{ appName }}">
    <span class="brand-logo"></span>
    <span class="brand-name" ng-cloak>{{ brandName }}</span>
  </a>

  <div class="small-screen-warning">
    <span class="glyphicon glyphicon-exclamation-sign"
          uib-tooltip="{{ smallScreenWarning | translate }}"
          tooltip-placement="bottom"></span>
  </div>
</div>

<nav class="cam-nav app-menu">
  <ul ng-class="{collapse: !navbarOpen}">

    <li engine-select></li>

    <li class="account dropdown"
        ng-if="authentication.name"
        ng-cloak
        uib-dropdown>
      <a href
         class="dropdown-toggle"
         uib-dropdown-toggle>
        <span class="glyphicon glyphicon-user "></span>
        {{ (userName || authentication.name) }}
      </a>

      <ul class="dropdown-menu dropdown-menu-right" uib-dropdown-menu>
        <li class="profile"
            ng-if="currentApp !== 'welcome'">
          <a ng-href="{{ '../../welcome/:engine/' | uri }}">
            {{ myProfile | translate }}
          </a>
        </li>

        <li class="divider"
            ng-if="currentApp !== 'welcome'"></li>

        <li class="logout">
          <a href
             ng-click="logout()">
            {{ signOut | translate }}
          </a>
        </li>
      </ul>
    </li>

    <li class="divider-vertical"
        ng-if="authentication.name"
        ng-cloak></li>

    <li class="app-switch dropdown"
        ng-if="showAppDropDown"
        uib-dropdown>
      <a href
         class="dropdown-toggle"
         uib-dropdown-toggle>
        <span class="glyphicon glyphicon-home"></span>
        <span class="caret"></span>
      </a>

      <ul class="dropdown-menu dropdown-menu-right" uib-dropdown-menu>
        <li ng-repeat="(appName, app) in apps"
            ng-class="appName">
          <a ng-href="{{ ('../../' + appName + '/:engine/' | uri) + getTargetRoute() }}">
            {{ app.label }}
          </a>
        </li>
      </ul>
    </li>
  </ul>
</nav>

<div ng-transclude
     class="sections-menu"
     ng-class="{collapse: !navbarOpen}"></div>
`;
