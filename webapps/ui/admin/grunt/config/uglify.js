module.exports = function(config, uglifyConfig) {
  'use strict';

  uglifyConfig.admin_scripts = {
    files: {
      '<%= pkg.gruntConfig.adminBuildTarget %>/scripts/camunda-admin-ui.js': ['<%= pkg.gruntConfig.adminBuildTarget %>/scripts/camunda-admin-ui.js'],
      '<%= pkg.gruntConfig.adminBuildTarget %>/camunda-admin-bootstrap.js': ['<%= pkg.gruntConfig.adminBuildTarget %>/camunda-admin-bootstrap.js']
    }
  };
};
