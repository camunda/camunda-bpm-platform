'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-filter-modal-form.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

var isArray = angular.isArray;

var noop = function() {};

var GENERAL_ACCORDION =       'general',
    PERMISSION_ACCORDION =    'permission',
    CRITERIA_ACCORDION =      'criteria',
    VARIABLE_ACCORDION =      'variable';

module.exports = [function() {

  return {

    restrict: 'A',
    scope: {
      filter: '=',
      filterModalData: '=',
      registerIsValidProvider: '&',
      registerPostFilterSavedProvider: '&'
    },

    template: template,

    controller: [
      '$scope',
      function(
        $scope
      ) {

        // init ////////////////////////////////////////////////////////////////////////

        var filterModalFormData = $scope.filterModalFormData = $scope.filterModalData.newChild($scope);

        $scope.registerIsValidProvider = $scope.registerIsValidProvider() || noop;
        $scope.registerPostFilterSavedProvider = $scope.registerPostFilterSavedProvider() || noop;

        var opened = GENERAL_ACCORDION;
        $scope.accordion = {
          general       : opened === GENERAL_ACCORDION,
          permission    : opened === PERMISSION_ACCORDION,
          criteria      : opened === CRITERIA_ACCORDION,
          variable      : opened === VARIABLE_ACCORDION
        };

        // observe //////////////////////////////////////////////////////////////////////

        filterModalFormData.observe('accesses', function(accesses) {
          $scope.accesses = accesses;
        });

        // init isValidProvider ////////////////////////////////////////////////////////

        var isValidProvider = function() {
          return $scope.filterForm.$valid;
        };

        $scope.registerIsValidProvider(isValidProvider);

        // handle hints ////////////////////////////////////////////////////////////////

        var hintProvider = {};
        this.registerHintProvider = function(formName, fn) {
          fn = fn || noop;
          hintProvider[formName] = fn;
        };

        $scope.showHint = function(formName) {
          var provider = hintProvider[formName];
          return provider && provider();
        };

        // handle submit after filter has been saved succesfully //////////////////////

        var postFilterSavedProviders = [];
        this.registerPostFilterSavedProvider = function(provider) {
          postFilterSavedProviders.push(provider || function(filter, callback) { return callback(); } );
        };

        var postFilterSavedProvider = function(filter, callback) {

          var count = postFilterSavedProviders.length;

          if (count === 0) {
            return callback();
          }

          var errors = [];
          var localCallback = function(err) {
            count = count - 1;

            if (err) {
              if (isArray(err)) {
                if (err.length) {
                  errors = errors.concat(err);
                }
              }
              else {
                errors.push(err);
              }
            }

            if (count === 0) {
              if (errors.length === 1) {
                return callback(errors[0]);
              }
              else if (errors.length) {
                return callback(errors);
              }
              else {
                callback();
              }
            }

          };

          for (var i = 0, provider; (provider = postFilterSavedProviders[i]); i++) {
            provider(filter, localCallback);
          }

        };

        $scope.registerPostFilterSavedProvider(postFilterSavedProvider);

        // helper ///////////////////////////////////////////////////////////////////////

        this.removeArrayItem = function(arr, delta) {
          var newArr = [];
          for (var key in arr) {
            if (key != delta) {
              newArr.push(arr[key]);
            }
          }
          return newArr;
        };

      }]

  };

}];
