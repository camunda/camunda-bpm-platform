ngDefine('cockpit.pages', function(module, $) {

  function CancelProcessInstanceController ($scope, $location, Notifications, ProcessInstanceResource) {

    var BEFORE_CANCEL = 'beforeCancellation',
        PERFORM_CANCEL = 'performCancellation',
        CANCEL_SUCCESS = 'cancellationSuccess',
        CANCEL_FAILED = 'cancellationFailed',
        LOADING_FAILED = 'loadingFailed';

    var cancelProcessInstanceData = $scope.processData.newChild($scope);

    $scope.$on('$routeChangeStart', function () {
      $scope.cancelProcessInstanceDialog.close();
    });

    cancelProcessInstanceData.provide('subProcessInstances', function () {
      return ProcessInstanceResource.query({'firstResult': 0, 'maxResults': 5}, {'superProcessInstance': $scope.processInstance.id}).$promise;
    });

    cancelProcessInstanceData.provide('subProcessInstancesCount', function () {
      return ProcessInstanceResource.count({'superProcessInstance': $scope.processInstance.id}).$promise;
    });

    cancelProcessInstanceData.observe([ 'subProcessInstancesCount', 'subProcessInstances' ], function (subProcessInstancesCount, subProcessInstances) {
      $scope.subProcessInstancesCount = subProcessInstancesCount.count;
      $scope.subProcessInstances = subProcessInstances;
      
      $scope.status = BEFORE_CANCEL;

    });

    $scope.cancelProcessInstance = function () {
      $scope.status = PERFORM_CANCEL;

      $scope.processInstance.$delete(function (response) {
        //success
        $scope.status = CANCEL_SUCCESS;
        Notifications.addMessage({'status': 'Canceled', 'message': 'The cancellation of the process instance was successful.'});

      }, function (error) {
        // failure
        $scope.status = CANCEL_FAILED;
        Notifications.addError({'status': 'Failed', 'message': 'The cancellation of the process instance failed.', 'exclusive': ['type']});
      });
    };

    $scope.close = function (status) {
      $scope.cancelProcessInstanceDialog.close();

      // if the cancellation of the process instance was successfull,
      // then redirect to the corresponding process definition overview.
      if (status === CANCEL_SUCCESS) {
        $location.url('/process-definition/' + $scope.processInstance.definitionId);
        $location.replace();
      }

    };
  };

  module.controller('CancelProcessInstanceController', [ '$scope',
                                                         '$location',
                                                         'Notifications',
                                                         'ProcessInstanceResource',
                                                         CancelProcessInstanceController ]);

});