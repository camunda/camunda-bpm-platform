ngDefine('cockpit.pages.processDefinition', [
  'angular',
  'cockpit/util/routeUtil',
  'module:dataDepend:angular-data-depend'
], function(module, angular) {

  var routeUtil = require('cockpit/util/routeUtil');

  var Controller = [
    '$scope', '$rootScope', 'search', '$q', 'Notifications', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'Views', 'Data', 'Transform', 'Variables', 'dataDepend', 'processDefinition',
    function($scope, $rootScope, search, $q, Notifications, ProcessDefinitionResource, ProcessInstanceResource, Views, Data, Transform, Variables, dataDepend, processDefinition) {

    var processData = $scope.processData = dataDepend.create($scope);


    // utilities ///////////////////////

    var internalUpdateLocation;

    $scope.$on('$routeChanged', function() {
      processData.set('filter', parseFilterFromUri());
    });

    function collect(elements, fn) {
      var result = [];

      angular.forEach(elements, function(e) {
        try {
          var c = fn(e);

          if (c !== undefined) {
            result.push(c);
          }
        } catch (ex) {
          // safe collect -> error skips element
        }
      });

      return result;
    }

    var currentFilter = null;

    /**
     * Auto complete a filter based on input and 
     * make the change persistent by serializing it into the url.
     * 
     * @param  {Object} filter the filter to auto complete
     */
    function autoCompleteFilter(filter) {

      // only apply when external (non completed)
      // filter changes occur
      if (currentFilter === filter) {
        return;
      }

      var activityIds = filter.activityIds,
          scrollTo = null,
          changed = false;

      if (activityIds && activityIds.length) {
        scrollTo = activityIds[activityIds.length - 1];
      }

      if (filter.scrollToBpmnElement !== scrollTo) {
        changed = true;
      }

      if (filter != currentFilter) {
        serializeFilterToUri(filter);
      }

      $scope.filter = currentFilter = angular.extend({}, filter, {
        scrollToBpmnElement: scrollTo
      });


      if (changed) {
        // update filter
        processData.set('filter', currentFilter);
      }

      // serialize to uri
      serializeFilterToUri(currentFilter);
    }

    function parseFilterFromUri() {

      var params = search(),
          filter;

      function parseArray(str) {
        if (!str) {
          return [];
        }

        return str.split(/,/);
      }

      function parseVariables(vars) {
        return collect(vars, Variables.parse);
      }

      var activityIds = parseArray(params.activityIds);

      filter = {
        activityIds: activityIds,
        parentProcessDefinitionId: params.parentProcessDefinitionId,
        businessKey: params.businessKey,
        variables: parseVariables(parseArray(params.variables)),
        page: parseInt(params.page) || undefined
      };

      return filter;
    }

    function serializeFilterToUri(filter) {
      var businessKey = filter.businessKey,
          activityIds = filter.activityIds,
          parentProcessDefinitionId = filter.parentProcessDefinitionId,
          variables = filter.variables;

      function nonEmpty(array) {
        return array && array.length;
      }

      search.updateSilently({
        businessKey: businessKey || null,
        activityIds: nonEmpty(activityIds) ? activityIds.join(',') : null,
        variables: nonEmpty(variables) ? collect(variables, Variables.toString).join(',') : null,
        parentProcessDefinitionId: parentProcessDefinitionId || null
      });

      currentFilter = filter;
    }

    // end utilities ///////////////////////


    // begin data definition //////////////////////

    processData.provide('processDefinition', processDefinition);

    processData.provide('filter', parseFilterFromUri());

    processData.provide('parentId', [ 'filter', function(filter) { return filter.parentProcessDefinitionId; } ]);

    processData.provide('parent', [ 'parentId', function(parentId) {
      if (!parentId) {
        return null;
      } else {
        return ProcessDefinitionResource.get({ id : parentId }).$promise;
      }
    }]);

    processData.provide('instances.all', [ 'processDefinition', function(definition) {
      return ProcessInstanceResource.count({ processDefinitionKey : definition.key }).$promise;
    }]);

    processData.provide('instances.current', [ 'processDefinition', function(definition) {
      return ProcessInstanceResource.count({ processDefinitionId : definition.id }).$promise;
    }]);

    processData.provide('semantic', [ 'processDefinition', function(definition) {
      return ProcessDefinitionResource.getBpmn20Xml({ id : definition.id}).$promise.then(function(value) {
        return Transform.transformBpmn20Xml(value.bpmn20Xml);
      });
    }]);

    processData.provide('bpmnElements', [ 'processDefinition', 'semantic', function(definition, semantic) {
      return getBpmnElements(definition, semantic);
    }]);

    processData.provide('allProcessDefinitions', [ 'processDefinition', function(definition) {
      return ProcessDefinitionResource.query({ 'key' : definition.key, 'sortBy': 'version', 'sortOrder': 'asc' }).$promise;
    }]);

    // processDiagram /////////////////////

    processData.provide('processDiagram', [ 'semantic', 'processDefinition', 'bpmnElements', function (semantic, processDefinition, bpmnElements) {
      var diagram = $scope.processDiagram = $scope.processDiagram || {};

      angular.extend(diagram, {
        semantic: semantic,
        processDefinition: processDefinition,
        bpmnElements: bpmnElements
      });

      return diagram;
    }]);

    // end data definition /////////////////////////


    // begin data usage ////////////////////////////

    $scope.breadcrumbData = processData.observe([ 'processDefinition', 'parent' ], function(definition, parent) {
      $rootScope.clearBreadcrumbs();

      if (parent) {
        $rootScope.addBreadcrumb({ type: 'processDefinition', processDefinition: parent });
      }

      $rootScope.addBreadcrumb({ type: 'processDefinition', processDefinition: definition });

      $rootScope.pageTitle = [
        'camunda Cockpit',
        definition.name || definition.id,
        'Definition View'
      ].join(' | ');
    });

    $scope.instanceStatistics = processData.observe([ 'instances.all', 'instances.current' ], function(allCount, currentCount) {
      $scope.instanceStatistics.all = allCount;
      $scope.instanceStatistics.current = currentCount;
    });

    $scope.processDiagramData = processData.observe('processDiagram', function(processDiagram) {
      $scope.processDiagram = processDiagram;
    });

    processData.observe('filter', autoCompleteFilter);

    $scope.handleBpmnElementSelection = function(activityId, event) {
      var newFilter = angular.copy(currentFilter),
          ctrl = event.ctrlKey,
          activityIds = angular.copy(newFilter.activityIds) || [],
          idx = activityIds.indexOf(activityId),
          selected = idx !== -1;

      if (!activityId) {
        activityIds = null;

      } else {

        if (ctrl) {
          if (selected) {
            activityIds.splice(idx, 1);

          } else {
            activityIds.push(activityId);
          }

        } else {
          activityIds = [ activityId ];
        }
      }

      newFilter.activityIds = activityIds;

      processData.set('filter', newFilter);
    };

    function getBpmnElements (processDefinition, semantic) {
      var result = {};

      var key = processDefinition.key;

      var diagram = null;

      for (var i = 0; i < semantic.length; i++) {
        var currentDiagram = semantic[i];
        if (currentDiagram.type === 'process') {

          if (currentDiagram.id === key) {
            diagram = currentDiagram;
            break;
          }
        }
      }

      getBpmnElementsHelper(diagram, result);

      return result;
    }

    function getBpmnElementsHelper(element, result) {
      result[element.id] = element;

      if (element.baseElements) {
        angular.forEach(element.baseElements, function(baseElement) {
          getBpmnElementsHelper(baseElement, result);
        });
      }
    }

    $scope.processDefinition = processDefinition;

    $scope.processDefinitionVars = { read: [ 'processDefinition', 'selection', 'processData', 'filter' ] };
    $scope.processDefinitionTabs = Views.getProviders({ component: 'cockpit.processDefinition.live.tab' });
    $scope.processDefinitionActions = Views.getProviders({ component: 'cockpit.processDefinition.live.action' });


    // extend the current scope to instantiate
    // with process definition data providers
    Data.instantiateProviders('cockpit.processDefinition.data', { $scope: $scope, processData : processData });


    $scope.selectTab = function(tabProvider) {
      $scope.selectedTab = tabProvider;

      search.updateSilently({
        detailsTab: tabProvider.id
      });
    };

    function setDefaultTab(tabs) {
      var selectedTabId = search().detailsTab;

      if (!tabs.length) {
        return;
      }

      if (selectedTabId) {
        var provider = Views.getProvider({ component: 'cockpit.processDefinition.live.tab', id: selectedTabId });
        if (provider && tabs.indexOf(provider) != -1) {
          $scope.selectedTab = provider;
          return;
        }
      }

      search.updateSilently({
        detailsTab: null
      });

      $scope.selectedTab = tabs[0];
    }

    setDefaultTab($scope.processDefinitionTabs);
  }];

  var ProcessDefinitionFilterController = [ '$scope', 'debounce', 'Variables', function($scope, debounce, Variables) {

    var processData = $scope.processData.newChild($scope),
        filterData,
        cachedFilter;

    function createRefs(elements) {
      var result = [];

      angular.forEach(elements, function(e, i) {
        result.push({
          value: e
        });
      });

      return result;
    }

    function createActivities(ids, bpmnElements) {
      var result = [];

      angular.forEach(ids, function(id) {
        result.push({ id: id, name: bpmnElements[id].name || id });
      });

      return result;
    }

    processData.provide('filterData', [ 'processDefinition', 'allProcessDefinitions', 'filter', 'parent', 'bpmnElements', function(definition, allDefinitions, filter, parent, bpmnElements) {

      if (!filterData || filterData.filter != filter) {
        return {
          definition: definition,
          allDefinitions: allDefinitions,
          businessKey: filter.businessKey ? { value: filter.businessKey } : null,
          parent: parent,
          filter: filter,
          variables: createRefs(filter.variables),
          activities: createActivities(filter.activityIds, bpmnElements)
        };
      } else {
        return filterData;
      }
    }]);

    processData.observe([ 'filterData' ], function(_filterData) {
      $scope.filterData = filterData = _filterData;
    });

    $scope.operators = Variables.operators;

    $scope.filterChanged = debounce(function() {

      if ($scope.filterForm.$invalid) {
        return;
      }

      var variables = filterData.variables,
          activities = filterData.activities,
          parent = filterData.parent,
          businessKey = filterData.businessKey,
          newFilterVariables = [],
          newFilterActivityIds = [],
          newFilter = {};

      // business key
      if (businessKey) {
        newFilter.businessKey = businessKey.value;
      }

      // variables
      angular.forEach(variables, function(v) {
        if (v.value) {
          newFilterVariables.push(v.value);
        }
      });

      if (newFilterVariables.length) {
        newFilter.variables = newFilterVariables;
      }

      // parentId
      if (parent) {
        newFilter.parentProcessDefinitionId = parent.id;
      }

      // activityIds
      angular.forEach(activities, function(a) {
        newFilterActivityIds.push(a.id);
      });

      if (newFilterActivityIds.length) {
        newFilter.activityIds = newFilterActivityIds;
      }

      // update cached filter
      filterData.filter = newFilter;

      processData.set('filter', newFilter);
    }, 2000);

    $scope.toggleVariableFilterHelp = function() {
      $scope.showVariableFilterHelp = !$scope.showVariableFilterHelp;
    }

    $scope.addVariableFilter = function() {
      filterData.variables.push({});
    };

    $scope.addBusinessKeyFilter = function() {
      $scope.filterData.businessKey = { };
    };

    $scope.removeBusinessKeyFilter = function() {
      filterData.businessKey = null;
      $scope.filterChanged();
    };

    $scope.removeParentFilter = function() {
      filterData.parent = null;
      $scope.filterChanged();
    };

    $scope.removeVariableFilter = function(variable) {
      var variables = filterData.variables,
          idx = variables.indexOf(variable);

      if (idx !== -1) {
        variables.splice(idx, 1);
      }

      $scope.filterChanged();
    };

    $scope.removeActivityFilter = function(activity) {
      var activities = filterData.activities,
          idx = activities.indexOf(activity);

      if (idx !== -1) {
        activities.splice(idx, 1);
      }

      $scope.filterChanged();
    };
  }];

  var ProcessVariableFilter = [ 'Variables', function(Variables) {

    return {

      require: 'ngModel',
      link: function (scope, element, attrs, ngModel) {

        function parseText(text) {
          var variable;

          try {
            variable = Variables.parse(text);
          } catch (e) {
            // ok, failed to parse variable
          }

          ngModel.$setValidity('processVariableFilter', !!variable);
          return variable;
        }

        ngModel.$parsers.push(parseText);
        ngModel.$formatters.push(Variables.toString);
      }
    };
  }];

  var RouteConfig = [
    '$routeProvider',
    'AuthenticationServiceProvider',
  function(
    $routeProvider,
    AuthenticationServiceProvider
  ) {

    $routeProvider
    .when('/process-definition/:id', {
      redirectTo: routeUtil.redirectToLive
    })
    .when('/process-definition/:id/live', {
      templateUrl: 'pages/process-definition.html',
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
        processDefinition: [ 'ResourceResolver', 'ProcessDefinitionResource',
          function(ResourceResolver, ProcessDefinitionResource) {
            return ResourceResolver.getByRouteParam('id', {
              name: 'process definition',
              resolve: function(id) {
                return ProcessDefinitionResource.get({ id : id });
              }
            });
          }]
      },
      reloadOnSearch: false
    });
  }];

  var ViewConfig = [ 'ViewsProvider', function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
      id: 'live',
      priority: 20,
      label: 'Live'
    });
  }];

  module
    .controller('ProcessDefinitionFilterController', ProcessDefinitionFilterController)
    .directive('processVariable', ProcessVariableFilter)
    .config(RouteConfig)
    .config(ViewConfig);
});
