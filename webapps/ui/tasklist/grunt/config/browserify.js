module.exports = function(config, browserifyConfig) {
  'use strict';

  browserifyConfig.tasklist_scripts = {
    options: {
      browserifyOptions: {
        standalone: 'CamundaTasklistUi',
        debug: true
      },
      watch: true,
      postBundleCB: function(err, src, next) {

        console.log('post bundling', err);

        var buildMode = config.grunt.config('buildMode');
        var livereloadPort = config.grunt.config('pkg.gruntConfig.livereloadPort');
        if (buildMode !== 'prod' && livereloadPort) {
          config.grunt.log.writeln('Enabling livereload for tasklist on port: ' + livereloadPort);
          //var contents = grunt.file.read(data.path);
          var contents = src.toString();

          contents = contents
                      .replace(/\/\* live-reload/, '/* live-reload */')
                      .replace(/LIVERELOAD_PORT/g, livereloadPort);

          next(err, new Buffer(contents));
        } else {
          next(err, src);
        }

      }
    },
    src: ['./<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/camunda-tasklist-ui.js'],
    dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/camunda-tasklist-ui.js'
  };

  browserifyConfig.tasklist_plugins = {
    options: {
      watch: true,
      transform: [
        [ 'exposify',
            {
              expose: {
               'angular': 'angular',
               'jquery': 'jquery',
               'camunda-commons-ui': 'camunda-commons-ui',
               'camunda-bpm-sdk-js': 'camunda-bpm-sdk-js',
               'angular-data-depend': 'angular-data-depend',
               'cam-common': 'cam-common'
              }
            }
        ]
      ],
      browserifyOptions: {
        standalone: 'TasklistPlugins',
        debug: true
      }
    },
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/tasklist/plugins/tasklistPlugins.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/tasklist/app/plugin.js'
  };


};
