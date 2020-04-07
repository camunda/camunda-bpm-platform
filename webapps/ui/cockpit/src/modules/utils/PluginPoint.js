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

import React, { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { getPlugins } from "utils/config";

export default function PluginPoint({
  location,
  renderIn = "div",
  wrapPlugins: PluginWrapper = ({ children }) => children,
  filter = () => true,
  additionalData: data = {}
}) {
  const plugins = getPlugins();

  const pluginsToShow = plugins
    .filter(({ pluginPoint }) => pluginPoint === location)
    .filter(filter)
    .sort((a, b) => b.priority - a.priority);

  return pluginsToShow.map(plugin => (
    <PluginWrapper key={plugin.id} {...plugin}>
      <Plugin renderIn={renderIn} data={data} {...plugin} />
    </PluginWrapper>
  ));
}

function Plugin({
  render,
  cleanup,
  renderIn: ContainerComponent,
  data,
  pluginPoint
}) {
  const urlParams = useParams();

  const params = pluginPoint === "cockpit.route" ? urlParams : data;

  const ref = useRef(null);
  const [reactContent, setReactContent] = useState();

  useEffect(() => {
    setReactContent(render(ref.current, params));
    return cleanup;
  }, [render, cleanup, params]);

  return <ContainerComponent ref={ref}>{reactContent}</ContainerComponent>;
}
