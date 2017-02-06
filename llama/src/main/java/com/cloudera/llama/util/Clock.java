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
package com.cloudera.llama.util;

import com.cloudera.llama.util.ParamChecker;

public class Clock {

  public interface Impl {

    public long currentTimeMillis();

    public void sleep(long millis) throws InterruptedException;

  }

  public static final Impl SYSTEM = new Impl() {
    @Override
    public long currentTimeMillis() {
      return System.currentTimeMillis();
    }

    @Override
    public void sleep(long millis) throws InterruptedException {
      Thread.sleep(millis);
    }
  };
  
  private static Impl clock = SYSTEM;

  public static void setClock(Impl clock) {
    Clock.clock = ParamChecker.notNull(clock, "clock");
  }

  private Clock() {
  }

  public static long currentTimeMillis() {
    return clock.currentTimeMillis();
  }

  public static void sleep(long millis) throws InterruptedException {
    clock.sleep(millis);
  }
  
  public static class Mock implements Impl {
    private long time;
    
    @Override
    public long currentTimeMillis() {
      return time;
    }
    
    @Override
    public void sleep(long millis) {
      time += millis;
    }
  }

}
