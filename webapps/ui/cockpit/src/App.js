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

import React, { useState, useEffect } from "react";
import { HashRouter, Switch, Route, Redirect } from "react-router-dom";

import AngularApp from "./AngularApp";

import { Footer, Header, Login } from "./components";
import { LoadingIndicator, Notifications } from "components";
import { ActivityProvider } from "./components/ProcessInstance/HOC/withActivityInstanceMap";

import RedirectToLoginIfUnauthenticated from "./RedirectToLoginIfUnauthenticated";

import {
  batch,
  dashboard,
  decisions,
  decisionDefinition,
  decisionInstance,
  processDefinition,
  processInstance,
  repository,
  tasks,
  processes
} from "./angularBridges";
import PluginPoint from "utils/PluginPoint";
import { UserProvider, PreviousLocationProvider } from "./modules/HOC";
import { loadConfig } from "utils/config";
import withProcessInstance, {
  ProcessInstanceProvider
} from "./components/ProcessInstance/HOC/withProcessInstance";
import { BpmnProvider as OriginialBpmnProvider } from "./components/ProcessInstance/HOC/withBpmn";

import "./App.scss";

const BpmnProvider = withProcessInstance(({ processInstance, children }) => (
  <OriginialBpmnProvider processDefinitionId={processInstance.definitionId}>
    {children}
  </OriginialBpmnProvider>
));

const DefaultWrapper = ({ children }) => children;
function AngularRoute({
  component,
  wrapApp: AppWrapper = DefaultWrapper,
  ...props
}) {
  return (
    <Route
      {...props}
      render={props => {
        return (
          <AppWrapper params={props.match.params}>
            <AngularApp component={component} />
          </AppWrapper>
        );
      }}
    />
  );
}

function App() {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    (async () => {
      await loadConfig();
      setIsLoading(false);
    })();
  }, []);

  if (isLoading) {
    return <LoadingIndicator fullscreen={true} />;
  }

  return (
    <HashRouter>
      <UserProvider>
        <PreviousLocationProvider>
          <RedirectToLoginIfUnauthenticated />
          <div className="App">
            <Header />
            <Switch>
              <Route exact path="/">
                <Redirect to="/dashboard" />
              </Route>
              <Route path="/login">
                <Login />
              </Route>
              <AngularRoute path="/dashboard" component={dashboard} />
              <AngularRoute path="/processes" component={processes} />
              <Route
                exact
                path="/process-definition/:id/"
                render={props => (
                  <Redirect
                    to={`/process-definition/${props.match.params.id}/runtime`}
                  />
                )}
              />
              <AngularRoute
                path="/process-definition/:id/runtime"
                component={processDefinition}
              />

              <Route
                exact
                path="/process-instance/:id/"
                render={props => (
                  <Redirect
                    to={`/process-instance/${props.match.params.id}/runtime`}
                  />
                )}
              />
              <AngularRoute
                path="/process-instance/:id/runtime"
                component={processInstance}
                wrapApp={({ children, params }) => {
                  return (
                    <ProcessInstanceProvider processInstanceId={params.id}>
                      <ActivityProvider processInstanceId={params.id}>
                        <BpmnProvider>{children}</BpmnProvider>
                      </ActivityProvider>
                    </ProcessInstanceProvider>
                  );
                }}
              />
              <AngularRoute path="/decisions" component={decisions} />
              <AngularRoute
                path="/decision-definition"
                component={decisionDefinition}
              />
              <AngularRoute
                path="/decision-instance"
                component={decisionInstance}
              />
              <AngularRoute path="/tasks" component={tasks} />
              <AngularRoute path="/repository" component={repository} />
              <AngularRoute exact path="/batch" component={batch} />
              <Route>
                <PluginPoint
                  location="cockpit.route"
                  wrapPlugins={({ children, properties }) => (
                    <Route exact path={properties.path}>
                      {children}
                    </Route>
                  )}
                />
              </Route>
            </Switch>
            <Notifications className="page-notifications" />
            <Footer />
          </div>
        </PreviousLocationProvider>
      </UserProvider>
    </HashRouter>
  );
}

export default App;
