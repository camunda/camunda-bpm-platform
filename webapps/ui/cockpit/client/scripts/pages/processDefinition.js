'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/process-definition.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');
var routeUtil = require('../../../../common/scripts/util/routeUtil');
var searchWidgetUtils = require('../../../../common/scripts/util/search-widget-utils');
var camCommons = require('camunda-commons-ui/lib');

var ngModule = angular.module('cam.cockpit.pages.processDefinition', ['dataDepend', camCommons.name]);

var Controller = [
  '$location', '$scope', '$rootScope', '$q', 'search', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'Views', 'Data', 'Transform', 'Variables', 'dataDepend', 'processDefinition', 'page',
  function($location, $scope,   $rootScope,   $q,   search,   ProcessDefinitionResource,   ProcessInstanceResource,   Views,   Data,   Transform,   Variables,   dataDepend,   processDefinition,   page
  ) {
    var processData = $scope.processData = dataDepend.create($scope);

    // utilities ///////////////////////

    $scope.hovered = null;
    $scope.hoverTitle = function(id) {
      $scope.hovered = id || null;
    };

    $scope.$on('$locationChangeSuccess', function() {
      var newFilter = parseFilterFromUri();

      if ($location.path().indexOf(processDefinition.id) > -1) {

        if (searchWidgetUtils.shouldUpdateFilter(newFilter, currentFilter, ['activityIds', 'parentProcessDefinitionId'])) {
          processData.set('filter', newFilter);
        }

        setDefaultTab($scope.processDefinitionTabs);
      }
    });

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

      filter = {
        activityIds: searchWidgetUtils.getActivityIdsFromUrlParams('activityIdIn', params),
        parentProcessDefinitionId: params.parentProcessDefinitionId
      };

      return filter;
    }

    function serializeFilterToUri(filter) {
      var activityIds = filter.activityIds;
      var parentProcessDefinitionId = filter.parentProcessDefinitionId;
      var urlParams = search();
      var searches = JSON.parse(urlParams.searchQuery || '[]');

      //when there is no searchQuery present and there is no ids to add to searchQuery don't change anything
      if (!urlParams.searchQuery && !activityIds.length) {
        searches = null;
      } else {
        searches = searchWidgetUtils.replaceActivitiesInSearchQuery(searches, 'activityIdIn', activityIds);
      }

      search.updateSilently({
        parentProcessDefinitionId: parentProcessDefinitionId || null,
        searchQuery: searches ? JSON.stringify(searches) : null
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

      var queryParams = { processDefinitionKey : definition.key };

      if(definition.tenantId) {
        queryParams.tenantIdIn = [ definition.tenantId ];
      } else {
        queryParams.withoutTenantId = true;
      }

      return ProcessInstanceResource.count(queryParams).$promise;
    }]);

    processData.provide('instances.current', [ 'processDefinition', function(definition) {
      return ProcessInstanceResource.count({ processDefinitionId : definition.id }).$promise;
    }]);

    processData.provide('bpmn20Xml', [ 'processDefinition', function(definition) {
      return ProcessDefinitionResource.getBpmn20Xml({ id : definition.id}).$promise;
    }]);

    processData.provide('parsedBpmn20', [ 'bpmn20Xml', function(bpmn20Xml) {
      return Transform.transformBpmn20Xml(bpmn20Xml.bpmn20Xml);
    }]);

    processData.provide('bpmnElements', [ 'parsedBpmn20', function(parsedBpmn20) {
      return parsedBpmn20.bpmnElements;
    }]);

    processData.provide('bpmnDefinition', [ 'parsedBpmn20', function(parsedBpmn20) {
      return parsedBpmn20.definitions;
    }]);

    processData.provide('allProcessDefinitions', [ 'processDefinition', function(definition) {

      var queryParams = {
        'key' : definition.key,
        'sortBy': 'version',
        'sortOrder': 'desc' };

      if(definition.tenantId) {
        queryParams.tenantIdIn = [ definition.tenantId ];
      } else {
        queryParams.withoutTenantId = true;
      }

      return ProcessDefinitionResource.query(queryParams).$promise;
    }]);

    // processDiagram /////////////////////

    processData.provide('processDiagram', [ 'bpmnDefinition', 'bpmnElements', function(bpmnDefinition, bpmnElements) {
      var diagram = $scope.processDiagram = $scope.processDiagram || {};

      angular.extend(diagram, {
        bpmnDefinition: bpmnDefinition,
        bpmnElements: bpmnElements
      });

      return diagram;
    }]);

    // end data definition /////////////////////////


    // begin data usage ////////////////////////////
    $rootScope.showBreadcrumbs = true;

    processData.observe('allProcessDefinitions', function(allDefinitions) {
      $scope.allDefinitions = allDefinitions;
    });

    $scope.breadcrumbData = processData.observe([ 'processDefinition', 'parent' ], function(definition, parent) {
      page.breadcrumbsClear();

      page.breadcrumbsAdd({
        label: 'Processes',
        href: '#/processes/'
      });

      if (parent) {
        page.breadcrumbsAdd({
          type: 'processDefinition',
          label: parent.name || (parent.id.slice(0, 8) +'…'),
          href: '#/process-definition/'+ parent.id +'/runtime',
          processDefinition: parent
        });
      }


      var plugins = Views.getProviders({ component: 'cockpit.processDefinition.view' });

      page.breadcrumbsAdd({
        type: 'processDefinition',
        label: definition.name || definition.key || definition.id,
        href: '#/process-definition/'+ definition.id +'/runtime',
        processDefinition: definition,

        choices: plugins.sort(function(a, b) {
          return a.priority < b.priority ? -1 : (a.priority > b.priority ? 1 : 0);
        }).map(function(plugin) {
          return {
            active: plugin.id === 'runtime',
            label: plugin.label,
            href: '#/process-definition/' + definition.id + '/' + plugin.id
          };
        })
      });

      page.titleSet((definition.name || definition.key || definition.id).toString());
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

      newFilter.activityIds = getNewActivityIds(activityId, activityIds, ctrl, selected);

      processData.set('filter', newFilter);
    };

    function getNewActivityIds(activityId, activityIds, ctrl, selected) {
      if (!activityId) {
        return [];
      }

      if (ctrl & selected) {
        return activityIds.filter(function(id) {
          return id !== activityId && id !== activityId + '#multiInstanceBody';
        });
      }

      var newIds = [activityId];

      if (isMultipleInstance(activityId)) {
        newIds.push(activityId+'#multiInstanceBody');
      }

      return (ctrl ? activityIds: []).concat(newIds);
    }

    function isMultipleInstance(activityId) {
      return $scope.processDiagram.bpmnElements[activityId].loopCharacteristics;
    }

    $scope.processDefinition = processDefinition;

    $scope.processDefinitionVars = { read: [ 'processDefinition', 'selection', 'processData', 'filter' ] };
    $scope.processDefinitionTabs = Views.getProviders({ component: 'cockpit.processDefinition.runtime.tab' });
    $scope.processDefinitionActions = Views.getProviders({ component: 'cockpit.processDefinition.runtime.action' });


    // extend the current scope to instantiate
    // with process definition data providers
    Data.instantiateProviders('cockpit.processDefinition.data', { $scope: $scope, processData : processData });

    $scope.hasReportPlugin = Views.getProviders({ component: 'cockpit.report' }).length > 0;
    $scope.hasMigrationPlugin = false;
    try {
      $scope.hasMigrationPlugin = !!angular.module('cockpit.plugin.migration');
    }
    catch (e) {
      // do nothing
    }

    // INITIALIZE PLUGINS
    var processPlugins = (
        Views.getProviders({ component: 'cockpit.processDefinition.runtime.tab' })).concat(
        Views.getProviders({ component: 'cockpit.processDefinition.runtime.action' })).concat(
        Views.getProviders({ component: 'cockpit.processDefinition.view' })).concat(
        Views.getProviders({ component: 'cockpit.processDefinition.diagram.overlay' })).concat(
        Views.getProviders({ component: 'cockpit.jobDefinition.action' }));

    var initData = {
      processDefinition : processDefinition,
      processData       : processData
    };

    for(var i = 0; i < processPlugins.length; i++) {
      if(typeof processPlugins[i].initialize === 'function') {
        processPlugins[i].initialize(initData);
      }
    }

    $scope.selectTab = function(tabProvider) {
      $scope.selectedTab = tabProvider;

      search.updateSilently({
        detailsTab: tabProvider.id
      });
    };

    function setDefaultTab(tabs) {
      var selectedTabId = search().detailsTab;

      if (!tabs || !tabs.length) {
        return;
      }

      if (selectedTabId) {
        var provider = Views.getProvider({ component: 'cockpit.processDefinition.runtime.tab', id: selectedTabId });
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

    $scope.getDeploymentUrl = function() {
      var path = '#/repository';

      var deploymentId = processDefinition.deploymentId;
      var searches = {
        deployment: deploymentId,
        resourceName: processDefinition.resource,
        deploymentsQuery: JSON.stringify([{
          type     : 'id',
          operator : 'eq',
          value    : deploymentId
        }])
      };

      return routeUtil.redirectTo(path, searches, [ 'deployment', 'resourceName', 'deploymentsQuery' ]);
    };

    $scope.isLatestVersion = function() {
      return $scope.processDefinition && $scope.processDefinition.version === getLatestVersion();
    };

    function getLatestVersion()  {
      if ($scope.allDefinitions) {
        return Math.max.apply(null, $scope.allDefinitions.map(function(def) {
          return def.version;
        }));
      }
    }

    $scope.getMigrationUrl = function() {
      var path = '#/migration';

      var latestVersion = getLatestVersion();

      var searches = {
        sourceKey: $scope.processDefinition.key,
        targetKey: $scope.processDefinition.key,
        sourceVersion: $scope.isLatestVersion() ? $scope.processDefinition.version - 1 : $scope.processDefinition.version,
        targetVersion: $scope.isLatestVersion() ? $scope.processDefinition.version : latestVersion
      };

      return routeUtil.redirectTo(path, searches, [ 'sourceKey', 'targetKey', 'sourceVersion', 'targetVersion' ]);
    };

  }];

var RouteConfig = [
  '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider
    .when('/process-definition/:id', {
      redirectTo: routeUtil.redirectToRuntime
    })
    .when('/process-definition/:id/runtime', {
      template: template,

      controller: Controller,
      authentication: 'required',
      resolve: {
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
    id: 'runtime',
    priority: 20,
    label: 'Runtime',
    keepSearchParams: [
      'parentProcessDefinitionId',
      'businessKey',
      'variables',
      'startedAfter',
      'startedBefore',
      'viewbox'
    ]
  });
}];

ngModule
    .config(RouteConfig)
    .config(ViewConfig);

module.exports = ngModule;
