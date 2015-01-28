module.exports = function(config) {
  'use strict';
  return {
    options: {
      livereload: false
    },

    assets: {
      files: [
        '<%= pkg.gruntConfig.clientDir %>/{fonts,images}/**/*',
        '<%= pkg.gruntConfig.clientDir %>/index.html',
        '<%= pkg.gruntConfig.clientDir %>/favicon.ico'
      ],
      tasks: [
        'newer:copy:assets'
      ]
    },

    styles: {
      files: [
        'node_modules/camunda-commons-ui/lib/widgets/**/*.less',
        'node_modules/camunda-commons-ui/resources/less/**/*.less',
        'node_modules/camunda-*/client/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.clientDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.clientDir %>/scripts/*/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        'node_modules/camunda-commons-ui/index.js',
        'grunt/config/requirejs.js',
        '<%= pkg.gruntConfig.clientDir %>/scripts/**/*.{js,html}'
      ],
      tasks: [
        'requirejs'
      ]
    },

    dependencies: {
      files: [
        'node_modules/camunda-commons-ui/index.js',
        'grunt/config/requirejs.js'
      ],
      tasks: [
        'requirejs:dependencies'
      ]
    },

    served: {
      options: {
        cwd: '<%= buildTarget %>/',
        livereload: config.livereloadPort || false
      },
      files: '**/*.{css,html,js}'
    }
  };
};
