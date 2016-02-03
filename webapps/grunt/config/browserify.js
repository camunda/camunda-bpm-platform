var _ = require('lodash');

module.exports = function(config, browserifyConfig) {
  'use strict';

  var options = {
    watch: true,
    transform: [
      'brfs',
      [ 'exposify',
          {
            expose: {
             'angular': 'angular',
             'jquery': 'jquery',
             'camunda-commons-ui': 'camunda-commons-ui',
             'camunda-bpm-sdk-js': 'camunda-bpm-sdk-js',
             'angular-data-depend': 'angular-data-depend'
            }
          }
      ]
    ]
  }

  browserifyConfig.tasklist_plugins = {
    options: _.extend({}, options, {
      browserifyOptions: {
        standalone: 'TasklistPlugins',
        debug: true
      }
    }),
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/tasklist/plugins/tasklistPlugins.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/tasklist/app/plugin.js'
  };

  browserifyConfig.cockpit_plugins = {
    options: _.extend({}, options, {
      browserifyOptions: {
        standalone: 'CockpitPlugins',
        debug: true
      }
    }),
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/cockpitPlugins.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/cockpit/app/plugin.js'
  };


};
