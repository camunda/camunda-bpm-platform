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

  browserifyConfig.tasklist_standaloneTask = {
    options: _.extend({}, options, {
      browserifyOptions: {
        standalone: 'StandaloneTaskPlugin',
        debug: true
      }
    }),
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/tasklist/plugins/standaloneTask/app/plugin.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/standaloneTask/app/plugin.js'
  };

  browserifyConfig.cockpit_base = {
    options: _.extend({}, options, {
      browserifyOptions: {
        standalone: 'BasePlugin',
        debug: true
      }
    }),
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/base/app/plugin.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/base/app/plugin.js'
  };

  browserifyConfig.cockpit_jobDefinition = {
    options: _.extend({}, options, {
      browserifyOptions: {
        standalone: 'JobDefinitionPlugin',
        debug: true
      }
    }),
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/jobDefinition/app/plugin.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/jobDefinition/app/plugin.js'
  };

  browserifyConfig.cockpit_decisionList = {
    options: _.extend({}, options, {
      browserifyOptions: {
        standalone: 'DecisionListPlugin',
        debug: true
      }
    }),
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/decisionList/app/plugin.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/decisionList/app/plugin.js'
  };

};
