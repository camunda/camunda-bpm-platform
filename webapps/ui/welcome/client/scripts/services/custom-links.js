'use strict';
var _links = [
  {
    label: 'DOCUMENTATION',
    href: 'https://docs.camunda.org/manual/latest/webapps/',
    description: 'DOCUMENTATION_DESCRIPTION'
  }
];

module.exports = [
  '$window',
  function($window) {
    return $window.camWelcomeConf && $window.camWelcomeConf.links ? $window.camWelcomeConf.links : _links;
  }];
