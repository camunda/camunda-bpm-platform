/* global ngDefine: false */
ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  'use strict';

  var breadcrumpsTemplate =
    '<ul class="breadcrumb">' +
      '<li>' +
        '<a href="#">Home</a>' +
      '</li>' +
      '<li ng-repeat="breadcrumb in breadcrumbs" ng-class="{ active: $last }" ng-switch="breadcrumb.type">' +
        '<span class="divider">{{ breadcrumb.divider || divider }}</span>' +
        '<span ng-switch-when="processDefinition">' +
         '<a ng-if="!$last" href="{{ breadcrumb.href }}">{{ breadcrumb.label }}</a>' +
         '<span ng-if="$last">{{breadcrumb.label}}</span>' +
        '</span>' +
        '<span ng-switch-when="processInstance">' +
         '<a ng-if="!$last" href="{{ breadcrumb.href }}" title="{{ breadcrumb.label }}">{{breadcrumb.label | shorten:8 }}</a>' +
          '<span ng-if="$last" title="{{ breadcrumb.label }}">{{ breadcrumb.label | shorten:8 }}</span>' +
        '</span>' +
        '<span ng-switch-when="expand">' +
          '<a ng-click="expand(breadcrumb)" href title="Expand">...</a>' +
        '</span>' +
      '</li>' +
    '</ul>';


  module.directive('camBreadcrumbsPanel', [
  function () {
    return {
      scope: {
        divider: '@'
      },
      restrict: 'A',
      template: breadcrumpsTemplate,

      link: function(scope) {
        // event triggered by the breadcrumbs service when the breadcrumbs are alterated
        scope.$on('page.breadcrumbs.changed', function(ev, breadcrumbs) {
          scope.breadcrumbs = breadcrumbs;
        });
      },

      controller: [
        '$scope',
        'page',
        'ProcessInstanceResource',
        'ProcessDefinitionResource',
      function(
        $scope,
        page,
        ProcessInstanceResource,
        ProcessDefinitionResource
      ) {
        // initialize the $scope breadcrumbs from the service
        $scope.breadcrumbs = page.breadcrumbsGet();
        
        $scope.expand = function (crumb) {
          if (crumb.processInstance) {
            $scope.breadcrumbs.splice($scope.breadcrumbs.indexOf(crumb), 1);
            fetchAllSuperProcessInstances(crumb.processInstance.id);
          }
        };

        /**
         * Fetch the information about potential super processes
         * NOTE: I believe this should go into the page service
         * @param {string} subProcessInstanceId - the id of the child/sub process
         */
        function fetchAllSuperProcessInstances (subProcessInstanceId) {
          // makes a HTTP request
          ProcessInstanceResource.query({'subProcessInstance': subProcessInstanceId}).$then(function (response) {
            // no need to go further
            if (!response.data.length) {
              return;
            }

            // use the first responded record of a super instance...
            var superProcessInstance = response.data[0];

            // ... and fetch its process definition
            ProcessDefinitionResource.get({'id': superProcessInstance.definitionId}).$then(function (response) {
              var processDefinition = response.data;

              // prepend (concat) ...
              $scope.breadcrumbs = [
                // ... the process definition link ...
                {
                  'type': 'processDefinition',
                  'processDefinition': processDefinition,
                  'label': processDefinition.name || processDefinition.key || processDefinition.id,
                  'href': '#/process-definition/' + processDefinition.id,
                  'divider': '/'
                },
                // ... the process instance link ...
                {
                  'type': 'processInstance',
                  'processDefinition': processDefinition,
                  'processInstance': superProcessInstance,
                  'label': superProcessInstance.id,
                  'divider': ':',
                  'href': '#/process-instance/' + superProcessInstance.id
                }
                // ... to the actual breacrumbs
              ].concat($scope.breadcrumbs);

              // and continue so further
              return fetchAllSuperProcessInstances(superProcessInstance.id);
            });
          });
        }
      }]
    };
  }]);

});
