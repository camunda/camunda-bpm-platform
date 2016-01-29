var _ = require('lodash');

module.exports = function(config, requireJsConf) {
  'use strict';

  var options = {
    stubModules: ['text'],

    preserveLicenseComments: false,
    generateSourceMaps: true,

    baseUrl: '<%= pkg.gruntConfig.pluginSourceDir %>/',

    paths: {
      'angular': 'empty:',
      'angular-data-depend': 'empty:',
      'camunda-bpm-sdk-js': 'empty:',
      'camunda-commons-ui': 'empty:',
      'text': '<%= pkg.gruntConfig.pluginSourceDir.split("/").map(function () { return ".." }).join("/") %>/node_modules/requirejs-text/text'
    },
    shim: {
      angular: {exports: 'angular'}
    }
  };

  requireJsConf.webapp_core = {
    options: _.extend({}, options, {
      out: '<%= pkg.gruntConfig.pluginBuildTarget %>/base/app/plugin.js',
      include: ['cockpit/plugins/base/app/plugin'],
      exclude: ['angular'],
      insertRequire: ['cockpit/plugins/base/app/plugin']
    })
  };


  requireJsConf.webapp_jobDefinition = {
    options: _.extend({}, options, {
      out: '<%= pkg.gruntConfig.pluginBuildTarget %>/jobDefinition/app/plugin.js',
      include: ['cockpit/plugins/jobDefinition/app/plugin'],
      exclude: ['angular'],
      insertRequire: ['cockpit/plugins/jobDefinition/app/plugin']
    })
  };

  requireJsConf.webapp_decisionList = {
    options: _.extend({}, options, {
      out: '<%= pkg.gruntConfig.pluginBuildTarget %>/decisionList/app/plugin.js',
      include: ['cockpit/plugins/decisionList/app/plugin'],
      exclude: ['angular'],
      insertRequire: ['cockpit/plugins/decisionList/app/plugin']
    })
  };

};
