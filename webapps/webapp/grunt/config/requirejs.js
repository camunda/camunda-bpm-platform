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
      'text': '<%= pkg.gruntConfig.pluginSourceDir.split("/").map(function () { return ".." }).join("/") %>/node_modules/requirejs-text/text'
    }
  };

  requireJsConf.webapp_core = {
    options: _.extend({}, options, {
      out: '<%= pkg.gruntConfig.pluginBuildTarget %>/base/app/plugin.js',
      include: ['base/app/plugin'],
      exclude: ['text'],
      insertRequire: ['base/app/plugin']
    })
  };


  requireJsConf.webapp_jobDefinition = {
    options: _.extend({}, options, {
      out: '<%= pkg.gruntConfig.pluginBuildTarget %>/jobDefinition/app/plugin.js',
      include: ['jobDefinition/app/plugin'],
      exclude: ['text'],
      insertRequire: ['jobDefinition/app/plugin']
    })
  };

  requireJsConf.webapp_standaloneTask = {
    options: _.extend({}, options, {
      out: '<%= pkg.gruntConfig.pluginBuildTarget %>/standaloneTask/app/plugin.js',
      include: ['standaloneTask/app/plugin'],
      exclude: ['text'],
      insertRequire: ['standaloneTask/app/plugin']
    })
  };

};
