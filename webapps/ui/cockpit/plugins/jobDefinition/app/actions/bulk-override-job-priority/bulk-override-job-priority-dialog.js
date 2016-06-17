'use strict';

var angular = require('angular');

module.exports = [
  '$scope', '$q', 'Notifications', 'JobDefinitionResource', '$modalInstance', 'jobDefinitions',
  function($scope,   $q,   Notifications,   JobDefinitionResource,   $modalInstance,   jobDefinitions) {

    $scope.status;
    var FINISHED = 'FINISHED',
        PERFORM = 'PERFORMING',
        SUCCESS = 'SUCCESS',
        FAILED = 'FAILED';

    var finishedWithFailures = false;

    var summarizePages = $scope.summarizePages = { size: 5, total: jobDefinitions.length, current: 1 };

    var data = $scope.data = {
      priority: null,
      includeJobs: false
    };

    $scope.setJobPriority = true;

    $scope.$on('$routeChangeStart', function() {
      var response = {};
      response.status = $scope.status;
      $modalInstance.close(response);
    });

    $scope.$watch('summarizePages.current', function(newValue) {
      if (!newValue) {
        return;
      }

      updateSummarizeTable(newValue);
    });

    function updateSummarizeTable(page) {
      var count = summarizePages.size;
      var firstResult = (page - 1) * count;

      var showJobDefinitions = $scope.showJobDefinitions = [];

      for (var i = 0; i < count; i++) {
        var jobDefinition = jobDefinitions[i + firstResult];
        if (jobDefinition) {
          showJobDefinitions.push(jobDefinition);
        }
      }
    }

    $scope.submit = function() {
      var setJobPriority = $scope.setJobPriority;
      if (!setJobPriority) {
        data = {};
      }

      overrideJobPriority(jobDefinitions);
    };

    function overrideJobPriority(jobDefinitions) {
      $scope.status = PERFORM;

      doOverride(jobDefinitions).then(function() {
        if (!finishedWithFailures) {

          if ($scope.setJobPriority) {
            Notifications.addMessage({
              status: 'Finished',
              message: 'Overriding the priority completed successfully.',
              exclusive: true
            });
          }
          else {
            Notifications.addMessage({
              status: 'Finished',
              message: 'Clearing the priority completed successfully.',
              exclusive: true
            });
          }
        }
        else {
          if ($scope.setJobPriority) {
            Notifications.addError({
              status: 'Finished',
              message: 'Overriding the priority was not successfully.',
              exclusive: true
            });
          }
          else {
            Notifications.addError({
              status: 'Finished',
              message: 'Clearing the priority was not successfully.',
              exclusive: true
            });
          }
        }

        $scope.status = FINISHED;
      });
    }

    function doOverride(jobDefinitions) {
      var deferred = $q.defer();

      var count = jobDefinitions.length;

      function setJobPriority(jobDefinition) {
        jobDefinition.status = PERFORM;
        JobDefinitionResource.setJobPriority({
          id: jobDefinition.id
        }, data, function() {
          jobDefinition.status = SUCCESS;

          // we want to show a summarize, when all requests
          // responded, that's why we uses a counter
          count = count - 1;
          if (count === 0) {
            deferred.resolve();
          }

        }, function(error) {
          finishedWithFailures = true;

          jobDefinition.status = FAILED;
          jobDefinition.error = error;

          // we want to show a summarize, when all requests
          // responded, that's why we uses a counter
          count = count - 1;
          if (count === 0) {
            deferred.resolve();
          }
        });
      }

      for (var i = 0, jobDefinition; (jobDefinition = jobDefinitions[i]); i++) {
        setJobPriority(jobDefinition);
      }

      return deferred.promise;
    }

    $scope.isValid = function() {
      var formScope = angular.element('[name="overrideJobPriorityForm"]').scope();
      return !$scope.setJobPriority || ((formScope && formScope.overrideJobPriorityForm) ? formScope.overrideJobPriorityForm.$valid : false);
    };

    $scope.close = function(status) {
      var response = {};
      response.status = status;
      $modalInstance.close(response);
    };

  }];
