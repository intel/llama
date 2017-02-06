/**
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
package com.cloudera.llama.am.impl;

import com.cloudera.llama.am.api.NodeInfo;
import com.cloudera.llama.am.spi.RMConnector;
import com.cloudera.llama.am.spi.RMListener;
import com.cloudera.llama.am.spi.RMResource;
import com.cloudera.llama.util.LlamaException;
import com.cloudera.llama.util.UUID;
import com.codahale.metrics.MetricRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RecordingMockRMConnector implements RMConnector {
  List<String> invoked = new ArrayList<String>();
  List<Object> args = new ArrayList<Object>();

  RMListener callback;

  public List<String> getInvoked() {
    return invoked;
  }

  @Override
  public void setRMListener(RMListener listener) {
    this.callback = listener;
    invoked.add("setRMListener");
    args.add(null);
  }

  @Override
  public void start() throws LlamaException {
    invoked.add("start");
    args.add(null);
  }

  @Override
  public void stop() {
    invoked.add("stop");
    args.add(null);
  }

  @Override
  public void register(String queue) throws LlamaException {
    invoked.add("register");
    args.add(queue);
  }

  @Override
  public void unregister() {
    invoked.add("unregister");
    args.add(null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<NodeInfo> getNodes() throws LlamaException {
    invoked.add("getNodes");
    args.add(null);
    List<NodeInfo> nodes = Arrays.asList(new NodeInfo[]{
        new NodeInfo("n1", 8, 8096), new NodeInfo("n2", 8, 8096)
    });
    return nodes;
  }

  @Override
  public void reserve(Collection<RMResource> resources)
      throws LlamaException {
    invoked.add("reserve");
    args.add(resources);
  }

  @Override
  public void release(Collection<RMResource> resources, boolean doNotCache)
      throws LlamaException {
    invoked.add("release");
    args.add(resources);
  }

  @Override
  public boolean reassignResource(Object rmResourceId, UUID resourceId) {
    invoked.add("reassignResource");
    args.add(null);
    return true;
  }

  @Override
  public void emptyCache() throws LlamaException {
  }

  @Override
  public void setMetricRegistry(MetricRegistry registry) {
  }

  @Override
  public boolean hasResources() {
    return false;
  }

  @Override
  public void deleteAllReservations() {
    invoked.add("deleteAllReservations");
  }
}
