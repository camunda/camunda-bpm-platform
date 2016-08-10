'use strict';
var _links = [
  {
    label: 'Documentation',
    href: 'https://docs.camunda.org/manual/latest/webapps/',
    // image: './../assets/image/camunda.plain.svg',
    description: 'Camunda webapps user documentation',
    priority: 99
  }
];

module.exports = [
  '$window',
  function($window) {
    return $window.camWelcomeConf && $window.camWelcomeConf.links ? $window.camWelcomeConf.links : _links;
  }];