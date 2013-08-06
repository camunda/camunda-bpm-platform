ngDefine('cockpit.pages.processDefinition', [ 
  'angular',
  'module:dataDepend:angular-data-depend'
], function(module, angular) {

  var Controller = [
    '$scope', '$rootScope', 'search', '$q', 'Notifications', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'Views', 'Transform', 'dataDependFactory', 'processDefinition',
    function($scope, $rootScope, search, $q, Notifications, ProcessDefinitionResource, ProcessInstanceResource, Views, Transform, dataDependFactory, processDefinition) {

    var processData = $scope.processData = dataDependFactory.create($scope);


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
        } catch (e) {
          ; // safe collect -> error skips element
        }
      });

      return result;
    }

    var currentFilter = null;

    function parseFilterFromUri() {

      var params = search();

      function parseArray(str) {
        if (!str) {
          return [];
        }

        return str.split(/,/);
      }

      function parseVariables(vars) {
        return collect(vars, Variables.parse);
      }

      currentFilter = {
        activityIds: parseArray(params.activity), 
        parentProcessDefinitionId: params.parentProcessDefinitionId,
        businessKey: params.businessKey, 
        variables: parseVariables(parseArray(params.variables)),
        page: parseInt(params.page) || undefined
      };

      return currentFilter;
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

    processData.set('processDefinition', processDefinition);

    processData.set('filter', parseFilterFromUri());

    processData.set('parentId', [ 'filter', function(filter) { return filter.parentProcessDefinitionId; } ]);

    processData.set('parent', [ 'parentId', function(parentId) {
      if (!parentId) {
        return null;
      } else {
        return ProcessDefinitionResource.get({ id : parentId }).$promise;
      }
    }]);

    processData.set('instances.all', [ 'processDefinition', function(definition) {
      return ProcessInstanceResource.count({ processDefinitionKey : definition.key }).$promise;
    }]);

    processData.set('instances.current', [ 'processDefinition', function(definition) {
      return ProcessInstanceResource.count({ processDefinitionId : definition.id }).$promise;
    }]);

    processData.set('semantic', [ 'processDefinition', function(definition) {
      return ProcessDefinitionResource.getBpmn20Xml({ id : definition.id}).$promise.then(function(value) {
        return Transform.transformBpmn20Xml(value.bpmn20Xml);
      });
    }]);

    processData.set('bpmnElements', [ 'processDefinition', 'semantic', function(definition, semantic) {
      return getBpmnElements(definition, semantic);
    }]);

    processData.set('allProcessDefinitions', [ 'processDefinition', function(definition) {
      return ProcessDefinitionResource.query({ 'key' : definition.key, 'sortBy': 'version', 'sortOrder': 'asc' }).$promise;
    }]);

    // end data definition /////////////////////////


    // begin data usage ////////////////////////////

    $scope.breadcrumbData = processData.get([ 'processDefinition', 'parent' ], function(definition, parent) {
      $rootScope.clearBreadcrumbs();

      if (parent) {
        $rootScope.addBreadcrumb({ type: 'processDefinition', processDefinition: parent });
      }

      $rootScope.addBreadcrumb({ type: 'processDefinition', processDefinition: definition });  
    });

    $scope.instanceStatistics = processData.get([ 'instances.all', 'instances.current' ], function(allCount, currentCount) {
      $scope.instanceStatistics.all = allCount;
      $scope.instanceStatistics.current = currentCount;
    });

    // processDiagram /////////////////////
    
    processData.set('processDiagram', [ 'semantic', 'processDefinition', 'bpmnElements', function (semantic, processDefinition, bpmnElements) {
      var diagram = $scope.processDiagram = $scope.processDiagram || {};

      angular.extend(diagram, {
        semantic: semantic,
        processDefinition: processDefinition,
        bpmnElements: bpmnElements
      });

      return diagram;
    }]);

    processData.set([ 'activityInstanceStatistics', 'incidentStatistics', 'clickableElements' ], [ 'processDefinition', function(definition) {

      return ProcessDefinitionResource.queryActivityStatistics({ id : definition.id, incidents: true }).$promise.then(function(stats) {
        var activityStatistics = [],
            incidentStatistics = [],
            clickableElements = [];

        angular.forEach(stats, function(currentStats) {
          var id = currentStats.id,
              stats = { id: id, count: currentStats.instances };

          activityStatistics.push(stats);
          clickableElements.push(id);
          
          var incident = { id: id, incidents: currentStats.incidents };
          incidentStatistics.push(incident);
        });
        
        return [ activityStatistics, incidentStatistics, clickableElements ];
      });
    }]);

    processData.set('processDiagramOverlay', [ 'processDiagram', 'activityInstanceStatistics', 'clickableElements', 'incidentStatistics', function (processDiagram, activityInstanceStatistics, clickableElements, incidentStatistics) {
      return {
        annotations: activityInstanceStatistics,
        incidents: incidentStatistics,
        clickableElements: clickableElements
      };
    }]);

    $scope.processDiagramData = processData.get([ 'processDiagram', 'processDiagramOverlay' ], function(processDiagram, processDiagramOverlay) {
      $scope.processDiagram = processDiagram;
      $scope.processDiagramOverlay = processDiagramOverlay;
    });

    processData.get('filter', function(filter) {
      if (filter != currentFilter) {
        console.log('filter changed -> ', filter);
        
        serializeFilterToUri(filter);
      }
    });

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

    $scope.processDefinitionVars = { read: [ 'processDefinition', 'selection', 'processData' ] };
    $scope.processDefinitionViews = Views.getProviders({ component: 'cockpit.processDefinition.view' });

    $scope.selectView = function(view) {
      $scope.selectedView = view;

      search.updateSilently({
        detailsTab: view.id
      });
    };

    function setDefaultTab(tabs) {
      var selectedTabId = search().detailsTab;

      if (!tabs.length) {
        return;
      }

      if (selectedTabId) {
        var provider = Views.getProvider({ component: 'cockpit.processDefinition.view', id: selectedTabId });
        if (provider && tabs.indexOf(provider) != -1) {
          $scope.selectedView = provider;
          return;
        }
      }

      search.updateSilently({
        detailsTab: null
      });

      $scope.selectedView = tabs[0];
    }

    setDefaultTab($scope.processDefinitionViews);
  }];
  
  var ProcessDefinitionFilterController = [ '$scope', 'debounce', function($scope, debounce) {

    var processData = $scope.processData,
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

    processData.set('filterData', [ 'processDefinition', 'allProcessDefinitions', 'filter', 'parent', function(definition, allDefinitions, filter, parent) {

      if (!filterData || filterData.filter != filter) {
        return {
          definition: definition,
          allDefinitions: allDefinitions,
          businessKey: filter.businessKey ? { value: filter.businessKey } : null,
          parent: parent, 
          filter: filter,
          variables: createRefs(filter.variables),
          activityIds: filter.activityIds
        };
      } else {
        return filterData;
      }
    }]);

    processData.get([ 'filterData' ], function(_filterData) {
      $scope.filterData = filterData = _filterData;
    });

    $scope.operators = Variables.operators;

    $scope.filterChanged = debounce(function() {

      if ($scope.filterForm.$invalid) {
        return;
      }

      var variables = filterData.variables,
          activityIds = filterData.activityIds,
          parent = filterData.parent,
          businessKey = filterData.businessKey,
          newFilterVariables = [], 
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
      newFilter.activityIds = activityIds;

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
  }];


  function ProcessVariableFilter() {

    return {

      require: 'ngModel',
      link: function (scope, element, attrs, ngModel) {

        function parseText(text) {
          var variable;

          try {
            variable = Variables.parse(text);
          } catch (e) {
            ; // ok, failed to parse variable
          }

          ngModel.$setValidity('processVariableFilter', !!variable);
          return variable;
        }

        ngModel.$parsers.push(parseText);
        ngModel.$formatters.push(Variables.toString);
      }
    }
  }

  var Variables = (function() {

    // variable specific stuff //////////////
    
    function reverse(hash) {
      var result = {};

      for (var key in hash) {
        result[hash[key]] = key;
      }

      return result;
    }

    function keys(hash) {
      var keys = [];

      for (var key in hash) {
        keys.push(key);
      }

      return keys;
    }

    var OPS = {
      eq: '=',
      neq: '!=',
      gt : '>',
      gteq : '>=',
      lt : '<',
      lteq : '<=',
      like: 'like'
    };

    var SYM_TO_OPS = reverse(OPS);
    
    function operatorName(op) {
      return OPS[op];
    }

    var PATTERN = new RegExp('^(\\S+)\\s(' + keys(SYM_TO_OPS).join('|') + ')\\s(.+)$');

    /**
     * Tries to guess the type of the input string
     * and returns the appropriate representation 
     * in the guessed type.
     *
     * @param value {string}
     * @return value {string|boolean|number} the interpolated value
     */
    function typed(value) {

      // is a string ( "asdf" )
      if (/^".*"\s*$/.test(value)) {
        return value.substring(1, value.length - 1);
      }

      if ((parseFloat(value) + '') === value) {
        return parseFloat(value);
      }

      if (value === 'true' || value === 'false') {
        return value === 'true';
      }

      throw new Error('Cannot infer type of value ' + value);
    }

    function typedString(value) {

      if (!value) {
        return value;
      }

      if (typeof value === 'string') {
        return '"' + value + '"';
      }

      if (typeof value === 'boolean') {
        return value ? 'true' : 'false';
      }

      if (typeof value === 'number') {
        return value;
      }


      throw new Error('Cannot infer type of value ' + value);
    }

    /**
     * Public API of Variables utility
     */
    return {

      /**
       * Parse a string into a variableFilter { name: ..., operator: ..., value: ... }
       * @param  {string} str the string to parse
       * @return {object}     the parsed variableFilter object
       */
      parse: function(str) {

        var match = PATTERN.exec(str),
            value;

        if (!match) {
          throw new Error('Invalid variable syntax: ' + str);
        }

        value = typed(match[3]);

        return {
          name: match[1],
          operator: SYM_TO_OPS[match[2]],
          value: value
        };
      },

      toString: function(variable) {
        if (!variable) {
          return '';
        }

        return variable.name + ' ' + operatorName(variable.operator) + ' ' + typedString(variable.value);
      },

      operators: keys(SYM_TO_OPS)
    };
  })();

  var SearchFactory = [ '$location', '$rootScope', function($location, $rootScope) {

    var silent = false;

    $rootScope.$on('$routeUpdate', function(e, lastRoute) {
      if (silent) {
        console.log('silenced $routeUpdate');
        silent = false;
      } else {
        $rootScope.$broadcast('$routeChanged', lastRoute);
      }
    });

    $rootScope.$on('$routeChangeSuccess', function(e, lastRoute) {
      silent = false;
    });

    var search = function() {
      var args = Array.prototype.slice(arguments);

      return $location.search.apply($location, arguments);
    }

    search.updateSilently = function(params) {
      angular.forEach(params, function(value, key) {
        $location.search(key, value);
      });

      silent = true;
    };

    return search;
  }];

  module.factory('search', SearchFactory);

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId', {
      templateUrl: 'pages/process-definition.html',
      controller: Controller,
      resolve: {
        processDefinition: ['ResourceResolver', 'ProcessDefinitionResource',
          function(ResourceResolver, ProcessDefinitionResource) {
            return ResourceResolver.getByRouteParam('processDefinitionId', {
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

  module
    .controller('ProcessDefinitionFilterController', ProcessDefinitionFilterController)
    .directive('processVariableFilter', ProcessVariableFilter)
    .config(RouteConfig);
});
