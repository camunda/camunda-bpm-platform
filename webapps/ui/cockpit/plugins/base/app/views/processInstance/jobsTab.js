'use strict';

var fs = require('fs');

var jobsTemplate = fs.readFileSync(__dirname + '/jobs-tab.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
    id: 'jobs-tab',
    label: 'PLUGIN_JOBS_LABEL',
    template: jobsTemplate,
    priority: 0,
    controller:  [
      '$scope', 'camAPI', 'Notifications', '$translate',
      function($scope, camAPI, Notifications, $translate) {

        var jobProvider = camAPI.resource('job');
        var processInstance = $scope.processInstance;

        $scope.pages = {size: 50, total: 0, current: 1};
        $scope.options = {useJobCreationDate: true};

        $scope.onPaginationChange = function(pages) {
          $scope.pages.current = pages.current;
          updateView();
        };

        var updateView = function() {
          var page =  $scope.pages.current,
              count =  $scope.pages.size,
              firstResult = (page - 1) * count;
          
          var queryParams = {
            'processInstanceId': processInstance.id, 
            'timers': true,
            firstResult: firstResult,
            maxResults: count
          };

          //get 'count' of jobs
          jobProvider.count(queryParams, function(error, response) {
            $scope.pages.total = response;
          });

          //get current page and display it
          $scope.loadingState = 'LOADING';
          jobProvider.list(queryParams, jobCallback);
          
        }; 

        var jobCallback = function(err, res) {
          if(err) {
            Notifications.addError({
              status: $translate.instant('PLUGIN_JOBS'),
              message: $translate.instant('PLUGIN_JOBS_LOADING_ERROR')
            });
          } 
          else {
            $scope.loadingState = res.length ? 'LOADED' : 'EMPTY';
            $scope.jobs = res;
          }
        };

        var recalculateDate = function(job, useCreationDate) {
          jobProvider.recalculateDuedate({id: job.id, creationDateBased: useCreationDate}, function(err) {
            if(err) {
              Notifications.addError({
                status: $translate.instant('PLUGIN_JOBS_RECALCULATE_ERROR'),
                message: $translate.instant('PLUGIN_JOBS_RECALCULATE_ERROR_MESSAGE'),
                duration: 5000
              });
            } 
            else {
              Notifications.addMessage({
                status: $translate.instant('PLUGIN_JOBS_RECALCULATE_SUCCESS'),
                message: $translate.instant('PLUGIN_JOBS_RECALCULATE_SUCCESS_MESSAGE'),
                duration: 5000
              });
            }
          });
        };

        $scope.recalculateDateFromCreationTime = function(job) {
          recalculateDate(job, true);
        };

        $scope.recalculateDateFromCurrentTime = function(job) {
          recalculateDate(job, false);
        };

        $scope.loadingState = 'LOADING';
        
      }]
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
