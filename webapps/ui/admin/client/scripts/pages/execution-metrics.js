'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/execution-metrics.html', 'utf8');
var CamSDK = require('camunda-commons-ui/vendor/camunda-bpm-sdk');

var Controller = [
  '$scope',
  '$filter',
  'Uri',
  'camAPI',
  'fixDate',
  function($scope, $filter, Uri, camAPI, fixDate) {

    var MetricsResource = camAPI.resource('metrics');


    // date variables
    var now = new Date();
    var dateFilter = $filter('date');
    var dateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss';

    // initial scope data
    $scope.startDate = dateFilter(now.getFullYear() + '-01-01T00:00:00.000', dateFormat);
    $scope.endDate =   dateFilter(now.getFullYear() + '-12-31T23:59:59.999', dateFormat);
    $scope.loadingState = 'INITIAL';

    
    // sets loading state to error and updates error message
    function setLoadingError(error) {
      $scope.loadingState = 'ERROR';
      $scope.loadingError = error;
    }

    // called every time date input changes
    $scope.handleDateChange = function handleDateChange() {
      var form = $scope.form;
      if(form.$valid) {
        return load();
      } else if(form.startDate.$error.datePattern || form.endDate.$error.datePattern) {
        setLoadingError('Supported pattern \'yyyy-MM-ddTHH:mm:ss\'.');
      } else if(form.startDate.$error.dateValue || form.endDate.$error.dateValue) {
        setLoadingError('Invalid Date Value.');
      }
    };

    var load = $scope.load = function() {
      $scope.loadingState = 'LOADING';
      // promises??? NOPE!
      CamSDK.utils.series({
        flowNodes: function(cb) {
          MetricsResource.sum({
            name: 'activity-instance-start',
            startDate: fixDate($scope.startDate),
            endDate: fixDate($scope.endDate)
          }, function(err, res) {
            cb(err, !err ? res.result : null);
          });
        },
        decisionElements: function(cb) {
          MetricsResource.sum({
            name: 'executed-decision-elements',
            startDate: fixDate($scope.startDate),
            endDate: fixDate($scope.endDate)
          }, function(err, res) {
            cb(err, !err ? res.result : null);
          });
        }
      }, function(err, res) {
        $scope.loadingState = 'LOADED';
        if (err) {
          setLoadingError('Could not set start and end dates.');
          $scope.loadingState = 'ERROR';
          return;
        }
        $scope.metrics = res;
      });

    };

    load();

  }];

module.exports = ['ViewsProvider', function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('admin.system', {
    id: 'system-settings-metrics',
    label: 'Execution Metrics',
    template: template,
    controller: Controller,
    priority: 900
  });
}];
