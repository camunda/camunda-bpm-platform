module.exports = function(config) {
  'use strict';
  var grunt = config.grunt;
  var productionRemoveExp = /<!-- #production-remove([\s\S.]*)\/production-remove -->/igm;
  function prod () {
    return grunt.config('buildTarget') === 'dist';
  };


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

    content = productionRemove(content, srcpath);
    content = appConf(content, srcpath);
    content = livereloadPort(content, srcpath);
    return content;
  }

  return {
    options: {},

    index: {
      options: {
        process: copyReplace
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>',
          src: [
            'index.html'
          ],
          dest: '<%= buildTarget %>/',
        }
      ]
    },

    assets: {
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>',
          src: [
            '*.{ico,txt}'
          ],
          dest: '<%= buildTarget %>/',
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/fonts',
          src: ['*/*.{eot,svg,ttf,woff}'],
          dest: '<%= buildTarget %>/fonts/'
        },
        {
          expand: true,
          cwd: 'node_modules/camunda-commons-ui/node_modules/bootstrap/fonts',
          src: ['**'],
          dest: '<%= buildTarget %>/fonts/bootstrap/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/images',
          src: ['**'],
          dest: '<%= buildTarget %>/images/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/styles',
          src: ['*.css'],
          dest: '<%= buildTarget %>/styles/'
        }
      ]
    },

    config: {
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/config',
          src: ['config.js'],
          dest: '<%= buildTarget %>/scripts/'
        }
      ]
    }
  };
};
