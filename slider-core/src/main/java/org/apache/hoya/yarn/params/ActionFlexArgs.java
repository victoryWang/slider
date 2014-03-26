/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hoya.yarn.params;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.hoya.exceptions.BadCommandArgumentsException;
import org.apache.hoya.yarn.HoyaActions;

import java.util.List;
import java.util.Map;

@Parameters(commandNames = {HoyaActions.ACTION_FLEX},
            commandDescription = HoyaActions.DESCRIBE_ACTION_FLEX)

public class ActionFlexArgs extends AbstractActionArgs {

  @Override
  public String getActionName() {
    return HoyaActions.ACTION_FLEX;
  }
  
  @ParametersDelegate
  public ComponentArgsDelegate componentDelegate = new ComponentArgsDelegate();

  /**
   * Get the component mapping (may be empty, but never null)
   * @return mapping
   * @throws BadCommandArgumentsException parse problem
   */
  public Map<String, String> getComponentMap() throws BadCommandArgumentsException {
    return componentDelegate.getComponentMap();
  }

  public List<String> getComponentTuples() {
    return componentDelegate.getComponentTuples();
  }

}
