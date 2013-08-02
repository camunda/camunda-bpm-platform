ngDefine('cockpit.plugin.base.views', function(module) {

   function IncidentsController ($scope, $http, $location, Uri) {

    // input: selection, processInstance, processData

    var processData = $scope.processData;

    $scope.stacktraceDialog = new Dialog();

    var pages = $scope.pages = { size: 50, total: 0 };

    var bpmnElements = [];
    var alreadyUpdated = false;

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      pages.current = parseInt(newValue) || 1;
    });

    $scope.$watch('pages.current', function(newValue) {
      var currentPage = newValue || 1;
      var search = $location.search().page;

      if (search || currentPage !== 1) {

        $scope.updateLocation(function (location) {
          location.search('page', currentPage);
        });
      }

      updateView(currentPage);  
    });

    processData.get('filter', function (filter) {
      if (!filter) {
        return;
      }

      bpmnElements = filter.bpmnElements || [];
      updateView(filter.page || 1);
    });

    function updateView(page) {
      $scope.incidents = null;

      var count = pages.size;
      var firstResult = (page - 1) * count;

      // get the 'count' of incidents
      $http.post(Uri.appUri('plugin://base/:engine/incident/count'), {
        'processInstanceIdIn' : [ $scope.processInstance.id ],
        'activityIdIn' :  bpmnElements
      })
      .success(function(data) {
        pages.total = Math.ceil(data.count / pages.size);
      });

      // get the incidents
      $http.post(Uri.appUri('plugin://base/:engine/incident'), {
        'processInstanceIdIn' : [ $scope.processInstance.id ],
        'activityIdIn' :  bpmnElements
      }, {
        params: {firstResult: firstResult, maxResults: count}
      })
      .success(function(data) { 
        processData.get([ 'bpmnElements', 'activityIdToInstancesMap'], function (bpmnElements, activityIdToInstancesMap) {
          angular.forEach(data, function (incident) {
            var activityId = incident.activityId;
            var bpmnElement = bpmnElements[activityId];
            incident.activityName = bpmnElement.name || bpmnElement.id;
            incident.linkable = bpmnElements[activityId] && activityIdToInstancesMap[activityId].length > 0;
          });
        })

        $scope.incidents = data;
      });
    };    

    $scope.getIncidentType = function (incident) {
      if (incident.incidentType === 'failedJob') {
        return 'Failed Job';
      }

      return incident.incidentType;
    };

    $scope.openStacktraceDialog = function (incident) {
      $scope.selectedIncidentToShowStacktrace = incident;
      $scope.stacktraceDialog.open();
    };

    $scope.selectActivity = function (incident) {
      var activityId = incident.activityId;
      var bpmnElement = $scope.processInstance.activityIdToBpmnElementMap[activityId];
      $scope.selection.view = {'bpmnElements': [ bpmnElement ], 'scrollTo': {'activityId': bpmnElement.id }, 'selectedBpmnElement': {'element': bpmnElement}};
    };

    $scope.getJobStacktraceUrl = function (incident) {

      return Uri.appUri('engine://engine/:engine/job/' + incident.rootCauseIncidentConfiguration + '/stacktrace');

    };

  };

  module.controller('IncidentsController', [ '$scope', '$http', '$location', 'Uri', IncidentsController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.instanceDetails', {
      id: 'incidents-tab',
      label: 'Incidents',
      url: 'plugin://base/static/app/views/processInstance/incidents-tab.html',
      controller: 'IncidentsController',
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
