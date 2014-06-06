/* global ngDefine: false, angular: false */
ngDefine('cockpit.plugin.base.views', function(module) {
  'use strict';
  module.controller('JobRetriesController', [
           '$scope', '$q', 'Notifications', 'JobResource', '$modalInstance', 'processData', 'processInstance',
  function ($scope,   $q,   Notifications,   JobResource,   $modalInstance,   processData,   processInstance) {

    var jobRetriesData = processData.newChild($scope);

    var jobPages = $scope.jobPages = { size: 5, total: 0 };
    var summarizePages = $scope.summarizePages = { size: 5, total: 0 };

    var jobIdToFailedJobMap = {};
    var selectedFailedJobIds = $scope.selectedFailedJobIds = [];

    var finishedWithFailures = false;

    var retryFailed = false;
    $scope.allJobsSelected = false;

    var FINISHED = 'finished',
        PERFORM = 'performing',
        SUCCESS = 'successful',
        FAILED = 'failed';

    var executionIdToInstanceMap = jobRetriesData.observe('executionIdToInstanceMap', function (executionMap) {
      executionIdToInstanceMap = executionMap;
    });

    $scope.$on('$routeChangeStart', function () {
      $modalInstance.close($scope.status);
    });

    $scope.$watch('jobPages.current', function(newValue, oldValue) {
      if (!newValue) {
        jobPages.current = 1;
        return;
      }

      if (newValue === oldValue) {
        return;
      }

      jobPages.current = newValue;
      updateJobTable(newValue);
    });

    function updateJobTable(page) {
      $scope.failedJobs = null;
      $scope.allJobsSelected = false;

      var count = jobPages.size;
      var firstResult = (page - 1) * count;

      JobResource.query({
        firstResult: firstResult,
        maxResults: count
      },{
        processInstanceId: processInstance.id,
        withException: true,
        noRetriesLeft: true
      }).$promise.then(function (response) {
        // for (var i = 0, job; !!(job = response.data[i]); i++) {
        for (var i = 0, job; !!(job = response[i]); i++) {
          jobIdToFailedJobMap[job.id] = job;
          var instance = executionIdToInstanceMap[job.executionId];
          job.instance = instance;

          var index = selectedFailedJobIds.indexOf(job.id);
          job.selected = index !== -1;
        }
        // $scope.failedJobs = response.data;
        $scope.failedJobs = response;
      });

      JobResource.count({
        processInstanceId: processInstance.id,
        withException: true
      }).$promise.then(function(data) {
        // jobPages.total = (Math.ceil(data.data.count / jobPages.size));
        // jobPages.total = (Math.ceil(data.count / jobPages.size));
        jobPages.total = data.count;
      });
    }

    $scope.$watch('summarizePages.current', function(newValue) {
      if (!newValue) {
        return;
      }

      updateSummarizeTable(newValue);

    });

    function updateSummarizeTable (page) {
      var count = summarizePages.size;
      var firstResult = (page - 1) * count;

      var showJobsRetried = $scope.showJobsRetried = [];

      for (var i = 0; i < count; i++) {
        var jobId = selectedFailedJobIds[i + firstResult];
        var job = jobIdToFailedJobMap[jobId];
        if (job) {
          showJobsRetried.push(job);
        }
      }
    }

    $scope.selectAllJobs = function (allJobsSelected) {
      // var selected = allJobsSelected;
      angular.forEach($scope.failedJobs, function (job) {
        job.selected = allJobsSelected;
        selectFailedJob(job);
      });
    };

    var selectFailedJob = $scope.selectFailedJob = function (failedJob) {
      var index = selectedFailedJobIds.indexOf(failedJob.id);

      if (failedJob.selected === true) {
        if (index === -1) {
          selectedFailedJobIds.push(failedJob.id);
        }
        return;
      }

      if (failedJob.selected === false) {
        selectedFailedJobIds.splice(index, 1);
        if ($scope.allJobsSelected === true) {
          $scope.allJobsSelected = false;
        }
        return;
      }
    };

    $scope.retryFailedJobs = function (selectedFailedJobIds) {
      $scope.status = PERFORM;

      summarizePages.total = (Math.ceil(selectedFailedJobIds.length / summarizePages.size));
      summarizePages.current = 1;

      doRetry(selectedFailedJobIds).then(function () {
        if (!finishedWithFailures) {
          Notifications.addMessage({
            status: 'Finished',
            message: 'Incrementing the number of retries finished.',
            exclusive: true
          });
        } else {
          Notifications.addError({
            status: 'Finished',
            message: 'Incrementing the number of retries finished with failures.',
            exclusive: true
          });
        }

        $scope.status = FINISHED;
       });
    };

    function doRetry (selectedFailedJobIds) {
      var deferred = $q.defer();

      var count = selectedFailedJobIds.length;

      function retryJob(job) {
        job.status = PERFORM;
        JobResource.setRetries({
          id: job.id
        }, {
          retries: 1
        }, function () {
          job.status = SUCCESS;

          // we want to show a summarize, when all requests
          // responded, that's why we uses a counter
          count = count - 1;
          if (count === 0) {
            deferred.resolve();
          }

        }, function (error) {
          finishedWithFailures = true;

          job.status = FAILED;
          job.retryError = error;
          retryFailed = true;

          // we want to show a summarize, when all requests
          // responded, that's why we uses a counter
          count = count - 1;
          if (count === 0) {
            deferred.resolve();
          }
         });
      }

      for (var i = 0, jobId; !!(jobId = selectedFailedJobIds[i]); i++) {
        var job = jobIdToFailedJobMap[jobId];
        retryJob(job);
      }

      return deferred.promise;
    }


    $scope.close = function (status) {
      $modalInstance.close(status);
    };
  }]);
});
