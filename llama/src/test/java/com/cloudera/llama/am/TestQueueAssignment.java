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
package com.cloudera.llama.am;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.QueueACL;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.AllocationConfiguration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.QueuePlacementPolicy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cloudera.llama.am.api.LlamaAM;
import com.cloudera.llama.thrift.TLlamaAMReservationRequest;
import com.cloudera.llama.util.ErrorCode;
import com.cloudera.llama.util.LlamaException;
import org.mockito.Mockito;

public class TestQueueAssignment {
  private LlamaAMServiceImpl amService;
  private AllocationConfiguration allocConf;
  private QueuePlacementPolicy placementPolicy;
  
  @Before
  public void setUp() {
    allocConf = Mockito.mock(AllocationConfiguration.class);
    placementPolicy = Mockito.mock(QueuePlacementPolicy.class);
    Mockito.when(allocConf.hasAccess(Mockito.anyString(), Mockito.any(QueueACL.class),
        Mockito.any(UserGroupInformation.class))).thenReturn(true);
    Mockito.when(allocConf.getPlacementPolicy()).thenReturn(placementPolicy);
    amService = new LlamaAMServiceImpl(Mockito.mock(LlamaAM.class), null, null,
        new AtomicReference<AllocationConfiguration>(allocConf));
  }
  
  @Test
  public void testAccepted() throws Exception {
    TLlamaAMReservationRequest request = mockRequest("queue", true, "user");
    Mockito.when(placementPolicy.assignAppToQueue(Mockito.eq("queue"), Mockito.anyString()))
        .thenReturn("resolved");
    String queue = amService.assignToQueue(request);
    amService.checkAccess(request.getUser(), queue, request.getQueue());
    Assert.assertEquals("resolved", queue);
  }
  
  @Test
  public void testRequestedQueueNotSet() throws Exception {
    TLlamaAMReservationRequest request = mockRequest(null, false, "user");
    Mockito.when(placementPolicy.assignAppToQueue(Mockito.eq(YarnConfiguration.DEFAULT_QUEUE_NAME),
        Mockito.anyString())).thenReturn("resolved");
    String queue = amService.assignToQueue(request);
    amService.checkAccess(request.getUser(), queue, request.getQueue());
    Assert.assertEquals("resolved", queue);
  }
  
  @Test
  public void testRequestedQueueNull() throws Exception {
    TLlamaAMReservationRequest request = mockRequest(null, true, "user");
    Mockito.when(placementPolicy.assignAppToQueue(Mockito.eq(YarnConfiguration.DEFAULT_QUEUE_NAME),
        Mockito.anyString())).thenReturn("resolved");
    String queue = amService.assignToQueue(request);
    amService.checkAccess(request.getUser(), queue, request.getQueue());
    Assert.assertEquals("resolved", queue);
  }
  
  @Test
  public void testRejectedByPolicy() throws Exception {
    try {
      TLlamaAMReservationRequest request = mockRequest("queue", true, "user");
      Mockito.when(placementPolicy.assignAppToQueue(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(null);
      String queue = amService.assignToQueue(request);
      amService.checkAccess(request.getUser(), queue, request.getQueue());
      Assert.fail("Should have hit exception");
    } catch (LlamaException ex) {
      Assert.assertEquals(ErrorCode.RESERVATION_USER_TO_QUEUE_MAPPING_NOT_FOUND.getCode(),
          ex.getErrorCode());
    }
  }
  
  @Test
  public void testRejectedByAcls() throws Exception {
    try {
      TLlamaAMReservationRequest request = mockRequest("queue", true, "user");
      Mockito.when(placementPolicy.assignAppToQueue(Mockito.anyString(), Mockito.anyString()))
          .thenReturn("resolved");
      Mockito.when(allocConf.hasAccess(Mockito.anyString(), Mockito.any(QueueACL.class),
          Mockito.any(UserGroupInformation.class))).thenReturn(false);
      String queue = amService.assignToQueue(request);
      amService.checkAccess(request.getUser(), queue, request.getQueue());
      Assert.fail("Should have hit exception");
    } catch (LlamaException ex) {
      Assert.assertEquals(ErrorCode.RESERVATION_USER_NOT_ALLOWED_IN_QUEUE.getCode(),
          ex.getErrorCode());
    }
  }
  
  private TLlamaAMReservationRequest mockRequest(String queue,
      boolean isSetQueue, String user) {
    TLlamaAMReservationRequest request = Mockito.mock(TLlamaAMReservationRequest.class);
    Mockito.when(request.isSetQueue()).thenReturn(isSetQueue);
    Mockito.when(request.getQueue()).thenReturn(queue);
    Mockito.when(request.getUser()).thenReturn(user);
    return request;
  }
}
