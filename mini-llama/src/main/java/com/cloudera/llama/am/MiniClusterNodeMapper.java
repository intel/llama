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

import com.cloudera.llama.am.api.NodeInfo;
import com.cloudera.llama.util.FastFormat;
import com.cloudera.llama.server.NodeMapper;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MiniClusterNodeMapper implements NodeMapper, Configurable {

  //java ...MiniClusterNodeMapper DNHOST:PORT=NMHOST:PORT DNHOST:PORT=NMHOST:PORT

  /**
   * Convenience Main class for integration testing that takes several arguments
   * with the DNHOST:DNPORT=NMHOST:NMPORT and returns a string with an encoded
   * <code>Properties</code> instance with those arguments. That string can be
   * used for setting the {@link #MAPPING_KEY} property in the
   * <code>llama-site.xml</code> when using the
   * <code>MiniClusterNodeMapper</code>.
   *
   * @param args multiple arguments, all with the DNHOST:DNPORT=NMHOST:NMPORT
   * pattern.
   * @throws Exception throw if there was an error parsing the arguments or
   * encoding them as a <code>Properties</code> string.
   */
  public static void main(String[] args)  throws Exception {
    if (args.length > 0) {
      Map<String, String> map = new HashMap<String, String>();
      for (String arg : args) {
        String s[] = arg.split("=");
        map.put(s[0], s[1]);
      }
      System.out.println(encodeMapAsPropertiesString(map));
      System.exit(0);
    } else {
      System.err.println("Usage: minillamanodemapper [DNHOST:DNPORT=NMHOST:NMPORT]+");
      System.exit(1);
    }
  }

  private static String encodeMapAsPropertiesString(Map<String, String> map)
      throws Exception {
    Properties props = new Properties();
    props.putAll(map);
    StringWriter writer = new StringWriter();
    props.store(writer, "");
    writer.close();
    return writer.toString();
  }

  public static final String MAPPING_KEY =
      "llama.minicluster.node.mapper.mapping";

  public static void addMapping(Configuration conf,
      Map<String, String> mapping) {
    try {
      conf.set(MAPPING_KEY, encodeMapAsPropertiesString(mapping));
    } catch (Throwable ex) {
      throw new RuntimeException(ex);
    }
  }

  private Configuration conf;
  private Map<String, String> dn2nm;
  private Map<String, String> nm2dn;

  @Override
  @SuppressWarnings("unchecked")
  public void setConf(Configuration conf) {
    this.conf = conf;
    try {
      String str = conf.get(MAPPING_KEY);
      if (str != null) {
        StringReader reader = new StringReader(str);
        Properties props = new Properties();
        props.load(reader);
        dn2nm = new HashMap<String, String>((Map) props);
        nm2dn = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : dn2nm.entrySet()) {
          nm2dn.put(entry.getValue(), entry.getKey());
        }
      } else {
        throw new RuntimeException(FastFormat.format(
            "Mapping property '{}' not set in the configuration", MAPPING_KEY));
      }
    } catch (Throwable ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public String getNodeManager(String dataNode) {
    String name = dn2nm.get(dataNode);
    if (name == null) {
      throw new IllegalArgumentException(FastFormat.format(
          "DataNode '{}' does not have a mapping", dataNode));
    }
    return name;
  }

  @Override
  public List<String> getNodeManagers(List<String> dataNodes) {
    List<String> list = new ArrayList<String>();
    for (String dataNode : dataNodes) {
      list.add(getNodeManager(dataNode));
    }
    return list;
  }

  @Override
  public String getDataNode(String nodeManager) {
    String name = nm2dn.get(nodeManager);
    if (name == null) {
      throw new IllegalArgumentException(FastFormat.format(
          "NodeManager '{}' does not have a mapping", nodeManager));
    }
    return name;
  }

  @Override
  public List<NodeInfo> getDataNodes(List<NodeInfo> nodeManagers) {
    List<NodeInfo> list = new ArrayList<NodeInfo>(nodeManagers.size());
    for (NodeInfo node : nodeManagers) {
      list.add(new NodeInfo(getDataNode(node.getLocation()), node.getCpusVCores(),
          node.getMemoryMB()));
    }
    return list;
  }
}
