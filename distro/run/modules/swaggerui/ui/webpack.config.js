const writeThirdPartyNotice = require('./tasks/license').writeThirdPartyNotice;
const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
const TerserPlugin = require("terser-webpack-plugin");
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");


const config = {
    entry: './src/index.js',
    output: {
        path: path.resolve(__dirname, '..', 'target', 'classes', 'swaggerui'),
        filename: 'bundle.js',
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            }
        ]
    },
    plugins: [
        new CopyPlugin({
            patterns: [{from: 'src/index.html'}, {from: 'src/favicon.ico'}],
        }),
        new LicenseCheckerWebpackPlugin({
            outputFilename: path.join( "..", "..", "THIRD-PARTY-NOTICE.json"),
            outputWriter: writeThirdPartyNotice,
            override: {
                "swagger-ui@~3.43.0": { repository: "https://github.com/swagger-api/swagger-ui" },
                "css-loader@~5.1.1": { repository: "https://github.com/webpack-contrib/css-loader" },
                "style-loader@~2.0.0": { repository: "https://github.com/webpack-contrib/style-loader" }
            },
        })
    ],
    optimization: {
        minimize: true,
        minimizer: [
            new TerserPlugin({
                extractComments: false,
            }),
        ],
    },
};

module.exports = config;