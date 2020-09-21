/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

module.exports = `<!-- # CE - camunda-bpm-webapp/ui/cockpit/src/legacy/camunda-commons-ui/lib/analytics/modal.html.js -->
<div class="analytics-body">
    <div class="camunda-logo navigation-element" ng-class="{white: page === 1 || page === 5}" ng-bind-html="logo"></div>
    <span class="glyphicon glyphicon-remove-sign modal-close" ng-class="{white: page === 1 || page === 5}"
        ng-click="close()"></span>

    <div class="splash" ng-show="page === 1">
        <div class="title">
            {{'TELEMETRY_CAMUNDA_NEEDS_YOU' | translate}}
        </div>

        <button class="btn btn-default begin" ng-click="next()">
            {{'TELEMETRY_BEGIN' | translate}}
        </button>
    </div>

    <div class="content" ng-show="page === 2">
        <h3>{{'TELEMETRY_INTRODUCTION_HEADER' | translate}}</h3>
        <p>{{'TELEMETRY_INTRODUCTION_EXPLANATION_1' | translate}}</p>
        <p>{{'TELEMETRY_INTRODUCTION_EXPLANATION_2' | translate}}</p>
        <p>{{'TELEMETRY_INTRODUCTION_EXPLANATION_3' | translate}}</p>
        <ul class="checkmark">
            <li>{{'TELEMETRY_INTRODUCTION_COMMUNITY' | translate}}</li>
            <li>{{'TELEMETRY_INTRODUCTION_IMPROVE' | translate}}</li>
        </ul>

        <button class="btn btn-default next" ng-click="next()">
            {{'TELEMETRY_NEXT' | translate}}
        </button>
    </div>
    <div class="content" ng-show="page === 3">
        <h3>{{'TELEMETRY_DETAILS_HEADER' | translate}}</h3>
        <ul class="triangle collection-details">
            <li>
                <b>{{'TELEMETRY_DETAILS_META_DATA' | translate}}</b>
                <p>{{'TELEMETRY_DETAILS_META_DATA_WHAT' | translate}}</p>
                <i>{{'TELEMETRY_DETAILS_META_DATA_WHY' | translate}}</i>
            </li>
        </ul>
        <button class="btn btn-default next" ng-click="next()">
            {{'TELEMETRY_NEXT' | translate}}
        </button>
    </div>

    <div class="content" ng-show="page === 4">
        <h3>{{'TELEMETRY_PREFERENCES_HEADER' | translate}}</h3>
        <p>{{'TELEMETRY_PREFERENCES_DETAILS' | translate}}</p>
        <form class="form-inline">
            <div class="row">
                <div class="col-xs-1 col-sm-1 text-right form-control-static">
                    <input type="checkbox" ng-model="enableUsage" class="form-control usage-statistics"
                        id="usage-statistics">
                </div>
                <div class="col-xs-10 col-sm-10 form-control-static ">
                    <label for="usage-statistics">{{'TELEMETRY_PREFERENCES_META' | translate}}</label>
                    <p class="details">{{'TELEMETRY_PREFERENCES_META_DETAILS' | translate}}</p>

                </div>
            </div>
        </form>
        <p class="disclaimer">
            {{'TELEMETRY_PREFERENCES_DISCLAIMER' | translate}}
        </p>
        <button class="btn btn-default next" ng-click="save()" ng-disabled="loadingState === 'LOADING'">
            {{'TELEMETRY_SAVE' | translate}}
        </button>
    </div>

    <div class="splash" ng-show="page === 5">
        <div class="title">
            {{'TELEMETRY_THANKS' | translate}}
        </div>

        <button class="btn btn-default begin" ng-click="close()">
            {{'TELEMETRY_FINISH' | translate}}
        </button>
    </div>

    <div class="footer" ng-show="page > 1 && page < 5">
        <span>{{'TELEMETRY_FOOTER_LEARN_MORE' | translate}}&nbsp;</span>
        <a href="https://camunda.com/legal/privacy/" target="_blank">
            {{'TELEMETRY_FOOTER_PRIVACY_POLICY' | translate}}
        </a>
    </div>
</div>
<!-- / CE - camunda-bpm-webapp/ui/cockpit/src/legacy/camunda-commons-ui/lib/analytics/modal.html.js -->`;
