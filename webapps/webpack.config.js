const HtmlWebPackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const path = require('path');
const webpack = require('webpack');

const {version} = require('./package.json');

let jsLoaders = ['babel-loader'];
let entry = {
  /* Cockpit */
  'app/cockpit/camunda-cockpit-ui': {
    import: './ui/cockpit/client/scripts/camunda-cockpit-ui.js',
    dependOn: 'lib/deps',
  },
  'plugin/cockpit/app/plugin': {
    import: './ui/cockpit/plugins/cockpitPlugins.js',
    dependOn: 'lib/deps',
  },

  /* Tasklist */
  'app/tasklist/camunda-tasklist-ui': {
    import: './ui/tasklist/client/scripts/camunda-tasklist-ui.js',
    dependOn: 'lib/deps',
  },
  'plugin/tasklist/app/plugin': {
    import: './ui/tasklist/plugins/tasklistPlugins.js',
    dependOn: 'lib/deps',
  },

  /* Admin */
  'app/admin/camunda-admin-ui': {
    import: './ui/admin/client/scripts/camunda-admin-ui.js',
    dependOn: 'lib/deps',
  },
  'plugin/admin/app/plugin': {
    import: './ui/admin/plugins/adminPlugins.js',
    dependOn: 'lib/deps',
  },

  /* Welcome */
  'app/welcome/camunda-welcome-ui': {
    import: './ui/welcome/client/scripts/camunda-welcome-ui.js',
    dependOn: 'lib/deps',
  },

  /* Shared */
  'lib/jquery': {
    import: './ui/common/lib/jquery.js',
  },
  'lib/ngDefine': {
    import: './ui/common/lib/ngDefine.js',
    dependOn: 'lib/deps',
  },
  'lib/requirejs': {
    import: './ui/common/lib/requirejs.js',
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
    use: [
      {
        loader: 'ejs-loader',
        options: {
          esModule: false,
        },
      },
      {
        loader: 'extract-loader',
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
  const output = {};
  const htmlPluginOpts = {};

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
        const packageName = `${name}-plugin-${name}Plugins`;
        return {
          test: indexFile,
          loader: 'string-replace-loader',
          options: {
            multiple: [
              {search: /\$VERSION/g, replace: version},
              {search: /\$APP_ROOT/g, replace: ''},
              {search: /\$BASE/g, replace: `/app/${name}/`},
              {
                search: /\$PLUGIN_DEPENDENCIES/g,
                replace:
                  name === 'welcome'
                    ? '[]'
                    : `
                [{
                  ngModuleName: '${name}.plugin.${name}Plugins',
                  requirePackageName: '${packageName}'
                }]`,
              },
              {
                search: /\$PLUGIN_PACKAGES/g,
                replace:
                  name === 'welcome'
                    ? '[]'
                    : `
                [{
                  name: '${packageName}',
                  location: '/plugin/${name}/app/',
                  main: 'plugin.js'
                }]`,
              },
            ],
          },
        };
      }),
    ];
  } else {
    htmlPluginOpts.publicPath = '$APP_ROOT';
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
          cookiePathRewrite: '/',
        },
      },
      open: ['/app/cockpit/'],
    },
    resolve: {
      fallback: {
        fs: false,
      },
      extensions: ['.js'],
      alias: {
        '@': path.resolve('src'),
        'camunda-bpm-sdk-js': path.resolve('./camunda-bpm-sdk-js'),
        'cam-common': path.resolve('./ui/common/scripts/module'),
      },
    },
    module: {
      rules,
    },
    plugins: [
      new HtmlWebPackPlugin({
        minify: false,
        template: 'ui/cockpit/client/scripts/index.html',
        filename: 'app/cockpit/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: './ui/common/images/favicon.ico',
        ...htmlPluginOpts,
      }),
      new HtmlWebPackPlugin({
        minify: false,
        template: 'ui/tasklist/client/index.html',
        filename: 'app/tasklist/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: './ui/common/images/favicon.ico',
        ...htmlPluginOpts,
      }),
      new HtmlWebPackPlugin({
        minify: false,
        template: 'ui/admin/client/scripts/index.html',
        filename: 'app/admin/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: './ui/common/images/favicon.ico',
        ...htmlPluginOpts,
      }),
      new HtmlWebPackPlugin({
        minify: false,
        template: 'ui/welcome/client/scripts/index.html',
        filename: 'app/welcome/index.html',
        chunks: ['lib/jquery', 'lib/requirejs', 'lib/deps'],
        favicon: './ui/common/images/favicon.ico',
        ...htmlPluginOpts,
      }),
      new HtmlNoncePlugin(),
      new MiniCssExtractPlugin({
        // both options are optional, similar to the same options in webpackOptions.output
        filename: isDevMode ? '[name].css' : '[name].[hash].css',
        chunkFilename: isDevMode ? '[id].css' : '[id].[hash].css',
      }),
      new webpack.HotModuleReplacementPlugin(),
      new webpack.DefinePlugin({
        // define custom global variables
        TEST_MODE: isTestMode,
        DEV_MODE: isDevMode,
        'process.env.CAMUNDA_VERSION': "'7.18.0-SNAPSHOT'",
      }),
      new webpack.ProvidePlugin({
        DEV_MODE: isDevMode,
      }),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: 'public',
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
