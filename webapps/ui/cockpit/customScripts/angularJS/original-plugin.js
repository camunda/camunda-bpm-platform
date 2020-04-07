/* eslint-disable */

define(["angular"], function(angular) {
  var searchProcessViewController = [
    "$scope",
    "$timeout",
    "localConf",
    function($scope, $timeout, localConf) {
      $scope.pressEnter = function(event) {
        if (event.keyCode === 13) {
          $timeout(function() {
            document.querySelector("#searchProcessGo").click();
          }, 0);
        }
      };

      $scope.activeSection = localConf.get("searchProcessActive", true);

      $scope.toggleSection = function toggleSection() {
        $scope.activeSection = !$scope.activeSection;
        localConf.set("searchProcessActive", $scope.activeSection);
      };
    }
  ];

  var ngModule = angular.module("cockpit.searchProcess", []);

  ngModule.config([
    "ViewsProvider",
    function(ViewsProvider) {
      ViewsProvider.registerDefaultView("cockpit.processes.dashboard", {
        id: "cockpit.searchProcess",
        label: "Search Process By Process Instance Id",
        priority: 9001,
        template:
          '<section class="processes-dashboard" ng-class="{\'section-collapsed\': !activeSection}">' +
          '<div class="inner">' +
          '<button tooltip="Toggle this section" class="section-toggle btn btn-link btn-sm" ng-click="toggleSection()">' +
          "<span class=\"glyphicon\" ng-class=\"{'glyphicon-menu-down': !activeSection, 'glyphicon-menu-up': activeSection}\"></span></button>" +
          "<h2>Search Process By Process Instance Id</h2>" +
          '<div ng-if="activeSection"><br/>Enter a Process Instance Id: <input ng-keydown="pressEnter($event)" id="processInstanceId" ng-model="processInstanceId" type="text" size="80"/> ' +
          '<a id="searchProcessGo" href="#/process-instance/{{processInstanceId}}"><button>Search</button></a></div>' +
          "</div></section>",
        controller: searchProcessViewController
      });
    }
  ]);

  return ngModule;
});
