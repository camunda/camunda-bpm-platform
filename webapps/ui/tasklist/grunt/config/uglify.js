module.exports = function(config, uglifyConfig) {
  'use strict';

  uglifyConfig.tasklist_scripts = {
    files: {
      '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/camunda-tasklist-ui.js': ['<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/camunda-tasklist-ui.js'],
      '<%= pkg.gruntConfig.tasklistBuildTarget %>/camunda-tasklist-bootstrap.js': ['<%= pkg.gruntConfig.tasklistBuildTarget %>/camunda-tasklist-bootstrap.js']
    }
  };
};
