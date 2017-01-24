module.exports = function(config, copyConf) {
  'use strict';
  var grunt = config.grunt;
  var productionRemoveExp = /<!-- #production-remove([\s\S.]*)\/production-remove -->/igm;

  var path = require('path');
  var now = (new Date()).getTime();
  var version = grunt.file.readJSON(path.resolve(__dirname, '../../../../package.json')).version;
  version = (version.indexOf('-SNAPSHOT') > -1 ? (version +'-'+ now) : version);

  function prod () {
    return grunt.config('buildMode') === 'prod';
  }

  function cacheBust(content, srcpath) {
    if (srcpath.slice(-4) !== 'html') { return content; }
    return content.split('$GRUNT_CACHE_BUST').join(prod() ? version : now);
  }

  function productionRemove(content) {
    if (!prod()) { return content; }
    grunt.log.writeln('Removing development snippets');
    return content.replace(productionRemoveExp, '');
  }


  function livereloadPort(content, srcpath) {
    if (srcpath.slice(-4) !== 'html' || prod()) {
      return content;
    }

    grunt.log.writeln('Replacing "LIVERELOAD_PORT" with "'+ config.livereloadPort +'"');
    return content.replace('LIVERELOAD_PORT', config.livereloadPort);
  }


  function appConf(content, srcpath) {
    if (srcpath.slice(-4) !== 'html') { return content; }

    var tasklistConf = 'var tasklistConf = '+ JSON.stringify({
      apiUri: '$APP_ROOT/api/engine',
      mock: false,

      // overrides the settings above
      resources: {
      }
    }, null, 2) +';';

    grunt.log.writeln('Wrote application configuration');
    return content.replace('var tasklistConf = {};', tasklistConf);
  }


  function copyReplace(content, srcpath) {
    content = cacheBust(content, srcpath);
    content = productionRemove(content, srcpath);
    content = appConf(content, srcpath);
    content = livereloadPort(content, srcpath);
    return content;
  }

  copyConf.tasklist_index = {
      options: {
        process: copyReplace
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.tasklistSourceDir %>',
          src: [
            'index.html'
          ],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/',
        }
      ]
  };

  copyConf.tasklist_bootstrap = {
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/',
          src: [
            'camunda-tasklist-bootstrap.js'
          ],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/',
        }
      ]
  };

  copyConf.tasklist_assets = {
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.tasklistSourceDir %>',
          src: [
            '*.{ico,txt}'
          ],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/',
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/vendor/fonts',
          src: ['*.{eot,svg,ttf,woff,woff2}'],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/fonts/'
        },
        {
          expand: true,
          cwd: 'node_modules/bootstrap/fonts',
          src: ['**'],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/fonts/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/resources/img',
          src: ['**'],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/assets/images/'
        },
        // bpmn fonts
        {
          expand: true,
          cwd: 'node_modules/bpmn-font/dist/font',
          src: [
            '*.{eot,ttf,svg,woff}'
          ],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/fonts/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.tasklistSourceDir %>/images',
          src: ['**'],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/assets/images/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.tasklistSourceDir %>/styles',
          src: ['*.css'],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/styles/'
        },

        // placeholder shims
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/vendor',
          src: ['placeholders.*'],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/'
        }
      ]
  };

  copyConf.tasklist_config = {
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/config',
          src: ['config.js'],
          dest: '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/'
        }
      ]
  };

};
