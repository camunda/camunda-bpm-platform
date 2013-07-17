'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {

  function DirectiveController($scope, $element, $attrs, $rootScope, ProcessInstanceResource, ProcessDefinitionResource) {

    $rootScope.breadcrumbs = [];

    $scope.expand = function (breadcrumb) {
      if (breadcrumb.processInstance) {
        $rootScope.breadcrumbs.splice($rootScope.breadcrumbs.indexOf(breadcrumb), 1);
        fetchAllSuperProcessInstances(breadcrumb.processInstance.id);
      }
    };

    $rootScope.clearBreadcrumbs = function () {
      $rootScope.breadcrumbs = [];
    };

    $rootScope.addBreadcrumb = function (breadcrumb) {
      if (isProcessDefinition(breadcrumb)) {
        var processDefinition = breadcrumb.processDefinition;
        breadcrumb.label = processDefinition.name || processDefinition.key || processDefinition.id;
        breadcrumb.href = '/process-definition/' + processDefinition.id;
        $rootScope.breadcrumbs = [ breadcrumb ];
        return;
      }

      if (isProcessInstance(breadcrumb)) {
        var processInstance = breadcrumb.processInstance;
        var processDefinition = breadcrumb.processDefinition;

        breadcrumb.label = processInstance.id;
        breadcrumb.href = '/process-definition/' + processDefinition.id + '/process-instance/' + processInstance.id;

        ProcessInstanceResource.count({ 'subProcessInstance': processInstance.id }).$then(function(response) {
          var count = response.data.count;
          if (count === 1) {
            $rootScope.breadcrumbs.unshift({'type': 'expand', 'processInstance': processInstance});
          }
        });

        $rootScope.breadcrumbs.push(breadcrumb);
        return;
      }
    };

    function fetchAllSuperProcessInstances (subProcessInstanceId) {
      ProcessInstanceResource.getSuperProcessInstance({}, {'subProcessInstance': subProcessInstanceId}).$then(function (response) {

        if (response.data.length > 0) {
          var superProcessIntance = response.data[0];

          ProcessDefinitionResource.get({'id': superProcessIntance.definitionId}).$then(function (response) {
            var processDefinition = response.data;            


            var processDefinitionBreadcrumb = {
              'type': 'processDefinition',
              'processDefinition': processDefinition,
              'label': processDefinition.name || processDefinition.key || processDefinition.id,
              'href': '/process-definition/' + processDefinition.id
            };

            var processInstanceBreadcrumb = {
              'type': 'processInstance',
              'processDefinition': processDefinition,
              'processInstance': superProcessIntance,
              'label': superProcessIntance.id,
              'href': '/process-definition/' + processDefinition.id + '/process-instance/' + superProcessIntance.id
            };

            $rootScope.breadcrumbs.unshift(processInstanceBreadcrumb);
            $rootScope.breadcrumbs.unshift(processDefinitionBreadcrumb);

            return fetchAllSuperProcessInstances(superProcessIntance.id);
          });
        }
      });
    }

    // possible breadcrumb types
    var isProcessDefinition = $scope.isProcessDefinition = function (breadcrumb) {
      return breadcrumb.type === 'processDefinition';
    };

    var isProcessInstance = $scope.isProcessInstance = function (breadcrumb) {
      return breadcrumb.type === 'processInstance';
    };

    var isExpand = $scope.isExpand = function (breadcrumb) {
      return breadcrumb.type === 'expand';
    };

  }

  var breadcrumpsTemplate =
    '<ul class="breadcrumb">' +
    '  <li>' + 
    '    <a href="#">Home</a>' +
    '  </li>' +
    '  <li ng-repeat="breadcrumb in breadcrumbs"  ng-class="{active: $last}" ng-cloak>' +
    '    <span ng-if="isProcessDefinition(breadcrumb)">' +
    '      <span class="divider">/</span>' +
    '      <!-- process definition breadcrumb -->'+
    '      <a ng-if="!$last" href="#{{ breadcrumb.href }}">' +
    '        {{ breadcrumb.label }}' +
    '      </a>' +
    '      <span ng-if="$last">{{breadcrumb.label}}</span>' +
    '    </span>' +    
    '    <!-- process instance breadcrumb -->' +
    '    <span ng-if="isProcessInstance(breadcrumb)">' +
    '      <span>:</span>' +    
    '      <a ng-if="!$last" href="#{{ breadcrumb.href }}" title="{{ breadcrumb.label }}">' +
    '        {{breadcrumb.label | shorten:8 }}' + 
    '      </a>' +    
    '      <span ng-if="$last" title="{{ breadcrumb.label }}">' +
    '        {{breadcrumb.label | shorten:8 }}' + 
    '      </span>' +
    '    </span>' +
    '    <!-- expand breadcrumb -->' +
    '    <span ng-if="isExpand(breadcrumb)">' +
    '      <span class="divider">/</span>' +
    '      <!-- process definition breadcrumb -->'+
    '      <a ng-if="isExpand(breadcrumb)" ng-click="expand(breadcrumb)" href title="Expand">' +
    '        ...' +
    '      </a> ' +   
    '    </span>' +
    '  </li>' +
    '</ul>';

  var Directive = function (ProcessInstanceResource, ProcessDefinitionResource) {
    return {
      restrict: 'EAC',
      template: breadcrumpsTemplate,
      controller: DirectiveController
    };
  };

  Directive.$inject = [ 'ProcessInstanceResource', 'ProcessDefinitionResource' ];

  module
    .directive('breadcrumbs', Directive);
  
});
