module.exports = function() {
  'use strict';
  return {
    options: {
      livereload: false,
      spawn: false
    },

    scripts: {
      files: [
        '<%= pkg.gruntConfig.pluginDir %>/**/*.*'
      ],
      tasks: [
        'requirejs'
      ]
    }
  };
};
