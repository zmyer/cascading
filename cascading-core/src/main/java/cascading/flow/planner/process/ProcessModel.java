/*
 * Copyright (c) 2007-2014 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package cascading.flow.planner.process;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import cascading.flow.FlowElement;
import cascading.flow.planner.graph.ElementGraph;
import cascading.pipe.Group;
import cascading.tap.Tap;

/**
 *
 */
public interface ProcessModel
  {
  String getID();

  int getOrdinal();

  String getName();

  Collection<Group> getGroups();

  Set<Tap> getSourceTaps();

  Set<Tap> getSinkTaps();

  int getSubmitPriority();

  Set<FlowElement> getSinkElements();

  Set<FlowElement> getSourceElements();

  Map<String, Tap> getTrapMap();

  ElementGraph getElementGraph();

  /**
   * Hides Extents in Graph.
   *
   * @return
   */
  ElementGraph getMaskedElementGraph();
  }
