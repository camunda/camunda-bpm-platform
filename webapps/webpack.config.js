const HtmlWebPackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const path = require('path');
const webpack = require('webpack');

const {version} = require(path.resolve(__dirname, './package.json'));

let jsLoaders = ['babel-loader'];
let entry = {
  /* Cockpit */
  'app/cockpit/camunda-cockpit-ui': {
    import: path.resolve(__dirname, 'ui/cockpit/client/scripts/camunda-cockpit-ui.js'),
    dependOn: 'lib/deps',
  },
  'plugin/cockpit/app/plugin': {
    import: path.resolve(__dirname, 'ui/cockpit/plugins/cockpitPlugins.js'),
    dependOn: 'lib/deps',
  },

  /* Tasklist */
  'app/tasklist/camunda-tasklist-ui': {
    import: path.resolve(__dirname, 'ui/tasklist/client/scripts/camunda-tasklist-ui.js'),
    dependOn: 'lib/deps',
  },
  'plugin/tasklist/app/plugin': {
    import: path.resolve(__dirname, 'ui/tasklist/plugins/tasklistPlugins.js'),
    dependOn: 'lib/deps',
  },

  /* Admin */
  'app/admin/camunda-admin-ui': {
    import: path.resolve(__dirname, 'ui/admin/client/scripts/camunda-admin-ui.js'),
    dependOn: 'lib/deps',
  },
  'plugin/admin/app/plugin': {
    import: path.resolve(__dirname, 'ui/admin/plugins/adminPlugins.js'),
    dependOn: 'lib/deps',
  },

  /* Welcome */
  'app/welcome/camunda-welcome-ui': {
    import: path.resolve(__dirname, 'ui/welcome/client/scripts/camunda-welcome-ui.js'),
    dependOn: 'lib/deps',
  },

  /* Shared */
  'lib/jquery': {
    import: path.resolve(__dirname, 'ui/common/lib/jquery.js'),
  },
  'lib/ngDefine': {
    import: path.resolve(__dirname, 'ui/common/lib/ngDefine.js'),
    dependOn: 'lib/deps',
  },
  'lib/requirejs': {
    import: path.resolve(__dirname, 'ui/common/lib/requirejs.js'),
    dependOn: 'lib/deps',
  },
  'lib/deps': [
    'angular',
    'moment',
    'camunda-bpm-sdk-js/lib/angularjs/index',
    'camunda-bpm-sdk-js',
    'q',
    'angular-animate',
    'angular-cookies',
    'angular-data-depend',
    'angular-loader',
    'angular-mocks',
    'angular-moment',
    'angular-resource',
    'angular-route',
    'angular-sanitize',
    'angular-touch',
    'angular-translate',
    'bpmn-js',
    'bpmn-js/lib/NavigatedViewer',
    'core-js',
    '@bpmn-io/form-js',
    '@bpmn-io/form-js-editor',
    '@bpmn-io/form-js-viewer',
    '@bpmn-io/dmn-migrate',
    'dmn-moddle',
    'clipboard',
    'events',
    'dom4',
    'cmmn-js',
    'dmn-js',
    'bootstrap',
    'lodash',
    'dmn-js-shared/lib/base/Manager',
    'dmn-js-drd/lib/NavigatedViewer',
    'dmn-js-decision-table/lib/Viewer',
    'dmn-js-literal-expression/lib/Viewer',
    'dmn-js-shared/lib/util/ModelUtil',
    'dmn-js-shared/lib/util/DiUtil',
    'dmn-js/lib/Modeler',
    'angular-ui-bootstrap',
    'jquery-ui',
    'jquery-ui/ui/widgets/mouse',
    'jquery-ui/ui/data',
    'jquery-ui/ui/plugin',
    'jquery-ui/ui/safe-active-element',
    'jquery-ui/ui/safe-blur',
    'jquery-ui/ui/scroll-parent',
    'jquery-ui/ui/version',
    'jquery-ui/ui/widget',
    'jquery-ui/ui/widgets/draggable',
  ],
};

let optimization = {};

let rules = [
  {
    test: /\.html$/,
    loader: 'string-replace-loader',
    options: {
      multiple: [{search: /\$VERSION/g, replace: version}],
    },
  },
  {
    test: /\.html$/,
    use: [
      {
        loader: 'ejs-loader',
        options: {
          esModule: false,
        },
      },
      {
        loader: 'extract-loader'
      },
      {
        loader: 'html-loader',
        options: {
          minimize: false,
        },
      },
    ],
  },
  {
    test: /\.less$/i,
    use: [
      // compiles Less to CSS
      'style-loader',
      'css-loader',
      'less-loader',
    ],
  },
  {
    test: /\.js$/,
    exclude: /node_modules/,
    use: jsLoaders,
  },
];

module.exports = (_env, argv = {}) => {
  const isDevMode = argv.mode === 'development';
  const isTestMode = argv.mode === 'test';
  const eeBuild = !!argv.eeBuild;
  const output = {};
  let htmlPluginOpts = {
    publicPath: '/camunda',
  };

  if (isDevMode) {
    jsLoaders = ['ng-hot-reload-loader', ...jsLoaders];
    entry = {
      'dev-server': 'webpack-dev-server/client?http://localhost:8081',
      'hot-only-dev-server': 'webpack/hot/only-dev-server',
      ...entry,
    };
    const webapps = [
      {
        name: 'cockpit',
        indexFile: /ui\/cockpit\/client\/scripts\/index\.html$/,
      },
      {
        name: 'admin',
        indexFile: /ui\/admin\/client\/scripts\/index\.html$/,
      },
      {name: 'tasklist', indexFile: /ui\/tasklist\/client\/index\.html$/},
      {
        name: 'welcome',
        indexFile: /ui\/welcome\/client\/scripts\/index\.html$/,
      },
    ];

    rules = [
      ...rules,
      ...webapps.map(({name, indexFile}) => {
        const pluginDependencies = [];
        if (name !== 'welcome') {
          pluginDependencies.push({
            ngModuleName: `${name}.plugin.${name}Plugins`,
            requirePackageName: `${name}-plugin-${name}Plugins`
          });
          if (eeBuild) {
            pluginDependencies.push({
              ngModuleName: `${name}.plugin.${name}EE`,
              requirePackageName: `${name}-plugin-${name}EE`
            });
          }
        }
        const pluginPackages = [];
        if (name !== 'welcome') {
          pluginPackages.push({
            name: `${name}-plugin-${name}Plugins`,
            location: `/plugin/${name}/app/`,
            main: 'plugin.js'
          });
          if (eeBuild) {
            pluginPackages.push({
              name: `${name}-plugin-${name}EE`,
              location: `/plugin/${name}EE/app/`,
              main: 'plugin.js'
            });
          }
        }

        return {
          test: indexFile,
          loader: 'string-replace-loader',
          options: {
            multiple: [
              {search: /\$APP_ROOT/g, replace: '/camunda'},
              {search: /\$BASE/g, replace: `/camunda/app/${name}/{ENGINE}/`},
              {search: /\$PLUGIN_DEPENDENCIES/g, replace: JSON.stringify(pluginDependencies)},
              {search: /\$PLUGIN_PACKAGES/g, replace: JSON.stringify(pluginPackages)},
            ],
          },
        };
      }),
    ];
  } else {
    htmlPluginOpts = {
      publicPath: '$APP_ROOT',
    };
    output.clean = true;
    optimization = {
      minimize: true,
      minimizer: [
        new TerserPlugin({
          extractComments: {
            condition: /license/i,
          },
          exclude: /scripts\/config\.js/,
        }),
      ],
      runtimeChunk: 'single',
      ...optimization,
    };
  }

  console.log('[Webpack Config] NODE_ENV:', process.env.NODE_ENV);
  console.log('[Webpack Config] argv.mode:', argv.mode);

  const addEngines = (engines) => {
    return engines.reduce((acc, engine) => {
        acc[`/camunda/app/*/${engine}/`] = {
          target: 'http://localhost:8081/',
          pathRewrite: (path) => {
            return path.replace(`/${engine}`, '').replace('/camunda', '');
          },
        };
        acc[`/camunda/app/*/${engine}/setup/`] = {
          target: 'http://localhost:8081/',
          pathRewrite: (path) => {
            return path
              .replace(`/${engine}`, '')
              .replace('/camunda', '')
              .replace('/setup', '');
          },
        };
        return acc;
    }, {});
  };

  return {
    entry,
    stats: {
      errorDetails: true,
    },
    devtool: isDevMode ? 'source-map' : false,
    output: {
      library: '[name]',
      libraryTarget: 'umd',
      filename: '[name].js',
      assetModuleFilename: 'assets/[name]-[hash][ext]',
      path: path.resolve(__dirname, 'target/webapp'),
      ...output,
    },
    devServer: {
      port: 8081,
      static: {
        directory: path.resolve(__dirname, './public'),
        publicPath: '/app',
      },
      hot: true,
      //hotOnly: true,
      https: false,
      proxy: {
        '/api': {
          target: 'http://localhost:8080/camunda/api',
          logLevel: 'debug',
          pathRewrite: {
            '^/api': '',
          },
        },
        ...addEngines(['default', 'engine2', 'engine3']),
        '/camunda/*': {
          target: 'http://localhost:8081/',
          logLevel: 'debug',
          pathRewrite: (path) => {
            return path.replace('/camunda', '');
          },
        },
        '/camunda/api/*': {
          target: 'http://localhost:8081/',
          logLevel: 'debug',
          pathRewrite: (path) => {
            return path.replace('/camunda', '');
          },
        },
      },
      open: ['/camunda/app/cockpit/default/'],
    },
    resolve: {
      fallback: {
        fs: false,
      },
      extensions: ['.js', '.less'],
      alias: {
        'camunda-commons-ui': path.resolve(__dirname, 'camunda-commons-ui'),
        'ui': path.resolve(__dirname, 'ui'),
        'camunda-bpm-sdk-js': path.resolve(__dirname, 'camunda-bpm-sdk-js'),
        'cam-common': path.resolve(__dirname, 'ui/common/scripts/module'),
      },
    },
    module: {
      rules,
    },
    plugins: [
      new HtmlWebPackPlugin({
        minify: false,
        template: path.resolve(__dirname, 'ui/cockpit/client/scripts/index.html'),
        filename: 'app/cockpit/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        ...htmlPluginOpts,
      }),
      new HtmlWebPackPlugin({
        minify: false,
        template: path.resolve(__dirname, 'ui/tasklist/client/index.html'),
        filename: 'app/tasklist/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        ...htmlPluginOpts,
      }),
      new HtmlWebPackPlugin({
        minify: false,
        template: path.resolve(__dirname, 'ui/admin/client/scripts/index.html'),
        filename: 'app/admin/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        ...htmlPluginOpts,
      }),
      new HtmlWebPackPlugin({
        minify: false,
        template: path.resolve(__dirname, 'ui/welcome/client/scripts/index.html'),
        filename: 'app/welcome/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        ...htmlPluginOpts,
      }),
      new HtmlNoncePlugin(),
      /*new MiniCssExtractPlugin({
        // both options are optional, similar to the same options in webpackOptions.output
        filename: isDevMode ? '[name].css' : '[name].[hash].css',
        chunkFilename: isDevMode ? '[id].css' : '[id].[hash].css',
      }),*/
      new webpack.HotModuleReplacementPlugin(),
      new webpack.DefinePlugin({
        // define custom global variables
        TEST_MODE: isTestMode,
        DEV_MODE: isDevMode,
        CAMUNDA_VERSION: `'${version}'`,
      }),
      new webpack.ProvidePlugin({
        DEV_MODE: isDevMode,
      }),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: path.resolve(__dirname, 'public'),
            to: 'app',
          },
        ],
      }),
      //new require('webpack-bundle-analyzer').BundleAnalyzerPlugin,
    ],
    optimization,
  };
};

class HtmlNoncePlugin {
  apply(compiler) {
    compiler.hooks.compilation.tap(HtmlNoncePlugin.name, (compilation) => {
      HtmlWebPackPlugin.getHooks(compilation).alterAssetTags.tap(
        HtmlNoncePlugin.name,
        (config) => {
          config.assetTags.scripts.forEach((script) => {
            script.attributes['$CSP_NONCE'] = true;
          });
          return config;
        }
      );
    });
  }
}
