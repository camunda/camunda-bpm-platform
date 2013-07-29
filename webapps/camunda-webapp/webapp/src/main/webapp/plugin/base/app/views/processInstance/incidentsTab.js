ngDefine('cockpit.plugin.base.views', function(module) {

   function IncidentsController ($scope, $http, $location, Uri) {

    // input: processInstanceId, selection, processInstance

    $scope.stacktraceDialog = new Dialog();

    var pages = $scope.pages = { size: 50, total: 0 };

    var activityIds = null;
    var alreadyUpdated = false;

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

    $scope.$watch(function () { return $location.search().bpmnElements; }, function (newValue) {
      activityIds = [];

      if (newValue && angular.isString(newValue)) {
        activityIds = newValue.split(',');
      } else if (newValue && angular.isArray(newValue)) {
        activityIds = newValue;
      }

      // always reset the current page to null
      $location.search('page', null);
      if ($scope.processInstance.activityIdToBpmnElementMap && 
          $scope.processInstance.activityIdToInstancesMap) {
        alreadyUpdated = true;
        updateView(1);    
      }

    });

    // This $watch on 'processInstance.activityIdToBpmnElementMap' is necessary due to race
    // conditions. It can happen, that the view will be updated before the the properpty
    // instanceIdToInstanceMap of the variable processInstance has been set.
    $scope.$watch('processInstance.activityIdToBpmnElementMap', function (newValue) {
      if (newValue && activityIds && !alreadyUpdated && $scope.processInstance.activityIdToInstancesMap) {
        alreadyUpdated = true;
        updateView(1);
      }

    });

    // This $watch on 'processInstance.activityIdToInstancesMap' is necessary due to race
    // conditions. It can happen, that the view will be updated before the the properpty
    // instanceIdToInstanceMap of the variable processInstance has been set.
    $scope.$watch('processInstance.activityIdToInstancesMap', function (newValue) {
      if (newValue && activityIds && !alreadyUpdated && $scope.processInstance.activityIdToBpmnElementMap) {
        alreadyUpdated = true;
        updateView(1);
      }

    });

    function updateView(page) {
      $scope.incidents = null;

      var count = pages.size;
      var firstResult = (page - 1) * count;

      // get the 'count' of incidents
      $http.post(Uri.appUri('plugin://base/:engine/incident/count'), {
        'processInstanceIdIn' : [ $scope.processInstanceId ],
        'activityIdIn' :  activityIds
      })
      .success(function(data) {
        pages.total = Math.ceil(data.count / pages.size);
      });

      // get the incidents
      $http.post(Uri.appUri('plugin://base/:engine/incident'), {
        'processInstanceIdIn' : [ $scope.processInstanceId ],
        'activityIdIn' :  activityIds
      }, {
        params: {firstResult: firstResult, maxResults: count}
      })
      .success(function(data) { 
        angular.forEach(data, function (incident) {
          var activityId = incident.activityId;
          var bpmnElement = $scope.processInstance.activityIdToBpmnElementMap[activityId];
          incident.activityName = bpmnElement.name || bpmnElement.id;
          incident.linkable = $scope.processInstance.activityIdToInstancesMap[activityId] && $scope.processInstance.activityIdToInstancesMap[activityId].length > 0;
        });

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
