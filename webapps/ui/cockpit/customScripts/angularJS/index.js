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

import angular from "angular";

var template =
  '<div ng-controller="searchProcessViewController">' +
  '<section class="processes-dashboard" ng-class="{\'section-collapsed\': !activeSection}">' +
  '<div class="inner">' +
  '<button tooltip="Toggle this section" class="section-toggle btn btn-link btn-sm" ng-click="toggleSection()">' +
  "<span class=\"glyphicon\" ng-class=\"{'glyphicon-menu-down': !activeSection, 'glyphicon-menu-up': activeSection}\"></span></button>" +
  "<h2>Search Process By Process Instance Id</h2>" +
  '<div ng-if="activeSection"><br/>Enter a Process Instance Id: <input ng-keydown="pressEnter($event)" id="processInstanceId" ng-model="processInstanceId" type="text" size="80"/> ' +
  '<a id="searchProcessGo" href="#/process-instance/{{processInstanceId}}"><button>Search</button></a></div>' +
  "</div></section>" +
  "</div>";

export default {
  id: "cockpit.searchProcess",
  pluginPoint: "cockpit.processes.dashboard",
  priority: 9,
  label: "Search Processes",
  render: node => {
    var searchProcessViewController = [
      "$scope",
      "$timeout",
      function($scope, $timeout) {
        $scope.pressEnter = function(event) {
          if (event.keyCode === 13) {
            $timeout(function() {
              document.querySelector("#searchProcessGo").click();
            }, 0);
          }
        };

        $scope.activeSection = true;

        $scope.toggleSection = function toggleSection() {
          $scope.activeSection = !$scope.activeSection;
        };
      }
    ];

    var ngModule = angular.module("cockpit.searchProcess", []);
    ngModule.controller(
      "searchProcessViewController",
      searchProcessViewController
    );

    node.innerHTML = template;

    angular.bootstrap(node, [ngModule.name]);
  }
};
