'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope', 'exposeScopeProperties', 'search', '$location', 'params',
  ExternalTaskActivityLink
];

function ExternalTaskActivityLink($scope, exposeScopeProperties, search, $location, params) {
  exposeScopeProperties($scope, this, ['activityId']);

  this.search = search;
  this.params = params;
  this.path = $location.path();
}

ExternalTaskActivityLink.prototype.getLink = function() {
  //search returns object that when modified changes query parameters
  //which is not desired here, hence it is needed to actually copy this object.
  var params = angular.copy(this.search());

  params.activityIds = this.activityId;

  return '#' + this.path + '?' + this.params(params);
};
