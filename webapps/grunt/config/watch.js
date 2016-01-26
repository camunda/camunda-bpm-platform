module.exports = function(config, watchConf) {
  'use strict';

  var options = {
      livereload: config.livereloadPort || false,
      spawn: false
  };

  watchConf.webapp_core = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/base/**/*.*'
    ],
    tasks: [
      'requirejs:webapp_core'
    ]
  };
  watchConf.webapp_jobDefinition = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/jobDefinition/**/*.*'
    ],
    tasks: [
      'requirejs:webapp_jobDefinition'
    ]
  };
  watchConf.webapp_standaloneTask = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/standaloneTask/**/*.*'
    ],
    tasks: [
      'requirejs:webapp_standaloneTask'
    ]
  };

  watchConf.webapp_decisionList = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/decisionList/**/*.*'
    ],
    tasks: [
      'requirejs:webapp_decisionList'
    ]
  };
};
