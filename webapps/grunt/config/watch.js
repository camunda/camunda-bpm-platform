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
        '<%= pkg.gruntConfig.clientDir %>/scripts/**/*.{js,html}'
      ],
      tasks: [
        'requirejs:scripts'
      ]
    },

    dependencies: {
      files: [
        './../camunda-bpm-webapp/webapp/target/webapp/plugin/**/*.js',
        'node_modules/{camunda-commons-ui,camunda-bpm-sdk-js}/lib/**/*.{js,html}'
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
      files: '**/*'
    }
  };
};
