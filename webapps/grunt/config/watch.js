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
        '<%= pkg.gruntConfig.clientDir %>/styles/**/*.{css,less}',
        '<%= pkg.gruntConfig.clientDir %>/scripts/*/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        'node_modules/camunda-*/client/scripts/**/*.{js,html}',
        '<%= pkg.gruntConfig.clientDir %>/scripts/**/*.{js,html}'
      ],
      tasks: [
        'requirejs:scripts'
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
