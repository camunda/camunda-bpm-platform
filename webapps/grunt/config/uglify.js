module.exports = function(config, uglifyConfig) {
  'use strict';

  uglifyConfig.cockpit_scripts = {
    files: {
      '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/camunda-cockpit-ui.js': ['<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/camunda-cockpit-ui.js'],
      '<%= pkg.gruntConfig.cockpitBuildTarget %>/camunda-cockpit-bootstrap.js': ['<%= pkg.gruntConfig.cockpitBuildTarget %>/camunda-cockpit-bootstrap.js']
    }
  };
};
