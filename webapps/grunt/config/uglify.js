module.exports = function(config, uglifyConfig) {
  'use strict';

  uglifyConfig.webapp_libraries = {
    files: {
      '<%= pkg.gruntConfig.libTargetDir %>/ngDefine.js': ['<%= pkg.gruntConfig.libTargetDir %>/ngDefine.js'],
      '<%= pkg.gruntConfig.libTargetDir %>/globalize.js': ['<%= pkg.gruntConfig.libTargetDir %>/globalize.js'],
      '<%= pkg.gruntConfig.libTargetDir %>/require.js': ['<%= pkg.gruntConfig.libTargetDir %>/require.js']
    }
  };

  uglifyConfig.webapp_plugins = {
    files: [{
            expand: true,
            src: '**/*.js',
            dest: '<%= pkg.gruntConfig.pluginBuildTarget %>',
            cwd: '<%= pkg.gruntConfig.pluginBuildTarget %>'
        }]
  }
};
