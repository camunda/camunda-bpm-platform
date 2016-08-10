module.exports = function(config, uglifyConfig) {
  'use strict';

  uglifyConfig.welcome_scripts = {
    files: {
      '<%= pkg.gruntConfig.welcomeBuildTarget %>/scripts/camunda-welcome-ui.js': ['<%= pkg.gruntConfig.welcomeBuildTarget %>/scripts/camunda-welcome-ui.js'],
      '<%= pkg.gruntConfig.welcomeBuildTarget %>/camunda-welcome-bootstrap.js': ['<%= pkg.gruntConfig.welcomeBuildTarget %>/camunda-welcome-bootstrap.js']
    }
  };
};
