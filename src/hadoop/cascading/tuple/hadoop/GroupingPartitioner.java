/*
 * Copyright (c) 2007-2011 Concurrent, Inc. All Rights Reserved.
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

package cascading.tuple.hadoop;

import cascading.tuple.Tuple;
import cascading.tuple.TuplePair;
import org.apache.hadoop.mapred.Partitioner;

/** Class GroupingPartitioner is an implementation of {@link Partitioner}. */
public class GroupingPartitioner extends HasherPartitioner implements Partitioner<TuplePair, Tuple>
  {
  public int getPartition( TuplePair key, Tuple value, int numReduceTasks )
    {
    return ( hash( key.getLhs() ) & Integer.MAX_VALUE ) % numReduceTasks;
    }
  }
