module.exports = function(config, uglifyConfig) {
  'use strict';

  uglifyConfig.webapp_libraries = {
    files: {
      // '<%= pkg.gruntConfig.libTargetDir %>/angular.js': ['<%= pkg.gruntConfig.libTargetDir %>/angular.js'],
      '<%= pkg.gruntConfig.libTargetDir %>/ngDefine.js': ['<%= pkg.gruntConfig.libTargetDir %>/ngDefine.js'],
      '<%= pkg.gruntConfig.libTargetDir %>/globalize.js': ['<%= pkg.gruntConfig.libTargetDir %>/globalize.js'],
      '<%= pkg.gruntConfig.libTargetDir %>/require.js': ['<%= pkg.gruntConfig.libTargetDir %>/require.js']
    }
  };
};
