ngDefine('cockpit.plugin.base.views', function(module) {

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

  var Controller = function ($scope, $routeParams, $location, $http, debounce, PluginProcessInstanceResource, Uri, ProcessInstanceResource) {

    // input processDefinition, selection

    var processDefinitionId = $scope.processDefinition.id;
    var parentProcessDefinitionId = $location.search().parentProcessDefinitionId || null;
    var activityIds = null;
    var alreadyUpdated = false;

    var pages = $scope.pages = { size: 50, total: 0 };

    var filter = $scope.filter = {
      active: false,
      search: {},
      variables: [],
    };

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      pages.current = parseInt(newValue) || 1;
    });

    $scope.$watch('pages.current', function(newValue) {
      var currentPage = newValue || 1;
      var search = $location.search().page;

      if (search || currentPage !== 1) {
        $location.search('page', currentPage);
        updateView(currentPage);
      }
      
    });

    $scope.$watch(function() {return $location.search().bpmnElements; }, function (newValue) {
      if (!newValue) {
        activityIds = [];
      } else if (angular.isString(newValue)) {
        activityIds = newValue.split(',');
      } else if (angular.isArray(newValue)) {
        activityIds = newValue;
      }

      updateView();
    });

    function deferredUpdateView(delay) {
      return debounce(updateView, delay);
    };

    $scope.$watch('filter.search.businessKey', deferredUpdateView(500));
    $scope.$watch('filter.search.variables', deferredUpdateView(1000));

    function updateView() {

      var page = pages.current,
          count = pages.size,
          firstResult = (page - 1) * count;

      var defaultParams = {
        processDefinitionId: processDefinitionId,
        parentProcessDefinitionId: parentProcessDefinitionId,
        activityIdIn: activityIds
      };

      var pagingParams = {
        firstResult: firstResult,
        maxResults: count,
        sortBy: 'startTime',
        sortOrder: 'asc'
      };

      var clear = function(obj) {
        for (var key in obj) {
          if (!obj[key]) {
            delete obj[key];
          }
        }

        return obj;
      }

      var countParams = angular.extend({}, clear(filter.search || {}), defaultParams);
      var params = angular.extend(countParams, pagingParams);

      $scope.processInstances = null;

      PluginProcessInstanceResource.query(params).$then(function(data) {
        $scope.processInstances = data.resource;
      });

      PluginProcessInstanceResource.count(countParams).$then(function(data) {
        pages.total = Math.ceil(data.data.count / pages.size);
      });
    };

    $scope.toggleFilter = function() {
      filter.open = !filter.open;
    };

    $scope.applyFilter = function() {
      filter.active = true;
    };

    $scope.clearFilter = function() {
      filter.active = false;
      filter.search = null;
    };

    $scope.addVariable = function() {
      filter.variables.push({});
    };

    $scope.removeVariable = function(variable) {
      var variables = filter.variables,
          idx = variables.indexOf(variable);

      if (idx !== -1) {
        variables.splice(idx, 1);
      }

      $scope.variablesChanged();
    };

    $scope.operators = keys(SYM_TO_OPS).join(', ');
    
    $scope.variablesChanged = debounce(function() {
      var variables = filter.variables,
          filterVars = [];

      angular.forEach(variables, function(v) {
        if (v.input) {
          filterVars.push(v.input);
        }
      });

      $scope.filter.search.variables = filterVars;
    }, 500);
  };

  Controller.$inject = [ '$scope', '$routeParams', '$location', '$http', 'debounce', 'PluginProcessInstanceResource', 'Uri', 'ProcessInstanceResource' ];

  module.directive('processVariableFilter', function() {

    return {

      require: 'ngModel',
      link: function (scope, element, attrs, ngModel) {

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

          // is a string ( "asdf", 'asdf' )
          if (/^".*"\s*|'.*'\s*$/.test(value)) {
            return value.substring(1, value.length - 2);
          }

          if ((parseFloat(value) + "") === value) {
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

          if (typeof value === "string") {
            return '"' + value + '"';
          }

          if (typeof value === "boolean") {
            return value ? 'true' : 'false';
          }

          if (typeof value === "number") {
            return value;
          }


          throw new Error('Cannot infer type string of value ' + value);
        }

        function parseText(text) {
          var match = PATTERN.exec(text),
              value;

          ngModel.$setValidity('processVariableFilter', !!match);

          if (!match) {
            return;
          }

          try {
            value = typed(match[3]);
          } catch (e) {
            ngModel.$setValidity('processVariableFilter', false);
            return;
          }

          return {
            name: match[1],
            operator: SYM_TO_OPS[match[2]],
            value: value
          };
        }

        function operatorName(op) {
          return OPS[op];
        }

        function formatFilter(filter) {
          if (!filter) {
            return '';
          }

          return filter.name + ' ' + operatorName(filter.operator) + ' ' + typedString(filter.value);
        }

        ngModel.$parsers.push(parseText);
        ngModel.$formatters.push(formatFilter);
      }
    }
  });

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
      id: 'process-instances-table',
      label: 'Process Instances',
      url: 'plugin://base/static/app/views/processDefinition/process-instance-table.html',
      controller: Controller,
      priority: 10
    }); 
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
