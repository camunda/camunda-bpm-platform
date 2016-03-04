'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-form-embedded.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');
var $ = require('jquery');

  module.exports = [
    'CamForm',
    'camAPI',
    '$timeout',
  function(
    CamForm,
    camAPI,
    $timeout
  ){

    return {

      restrict: 'A',

      require: '^camTasklistForm',

      scope: true,

      template: template,

      link : function($scope, $element, attrs, formController) {

        var container = $($element[0]).find('.form-container');
        var camForm = null;
        var form = $scope.form = {
          '$valid': false,
          '$invalid': true
        };

        $scope.$watch('tasklistForm', function (value) {
          if (value) {
            showForm(container, value, formController.getParams());
          }
        });

        $scope.$watch(function() {
          return form && form.$valid;
        }, function(value) {
          formController.notifyFormValidated(!value);
        });

        // watch for changes in the form
        $scope.$watch(function() {
          return form && form.$dirty;
        }, function(value) {
          formController.notifyFormDirty(value);
        });

        function showForm(container, tasklistForm, params) {
          var formUrl = tasklistForm.key;

          params = angular.copy(params);

          delete params.processDefinitionKey;

          angular.extend(params, {
            containerElement: container,
            client: camAPI,
            formUrl: formUrl,
            done: done
          });

          camForm = new CamForm(params);

        }

        var done = function (err, _camForm) {
          if (err) {
            return formController.notifyFormInitializationFailed(err);
          }
          camForm = _camForm;

          var formName = _camForm.formElement.attr('name');
          var camFormScope = _camForm.formElement.scope();

          if (!camFormScope) {
            return;
          }

          form = camFormScope[formName];
          form.$setPristine();
          formController.notifyFormInitialized();

          $scope.$root.$broadcast('embedded.form.rendered');
        };

        var complete = function (callback) {
          camForm.submit(callback);
        };

        var save = function(evt) {
          form.$setPristine();
          camForm.store();

          // manually trigger a mouseleave event to make the tooltip disappear
          $timeout(function(){
            angular.element(evt.target).triggerHandler($.Event('mouseleave'));
          });
        };

        formController.registerCompletionHandler(complete);
        formController.registerSaveHandler(save);

        $scope.$on('authentication.login.required', function() {
          save();
        });

      }

    };

  }];
