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

import React, { createContext, useContext, useState, useEffect } from "react";
import { withRouter } from "react-router-dom";

const LocationContext = createContext();

function _PreviousLocationProvider({ children, location }) {
  const [previousLocation, setPreviousLocation] = useState(null);
  const [currentLocation, setCurrentLocation] = useState(null);

  useEffect(() => {
    if (
      currentLocation !== location &&
      JSON.stringify(currentLocation) !== JSON.stringify(location) // If the objects are different, check the content
    ) {
      setPreviousLocation(currentLocation);
      setCurrentLocation(location);
    }
  }, [currentLocation, location]);

  return (
    <LocationContext.Provider value={{ previousLocation }}>
      {children}
    </LocationContext.Provider>
  );
}

export const PreviousLocationProvider = withRouter(_PreviousLocationProvider);

export default Component => props => (
  <Component {...useContext(LocationContext)} {...props} />
);
