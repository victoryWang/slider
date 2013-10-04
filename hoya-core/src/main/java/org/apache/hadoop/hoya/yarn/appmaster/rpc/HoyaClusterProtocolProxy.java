/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *    http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License. See accompanying LICENSE file.
 */

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

package org.apache.hadoop.hoya.yarn.appmaster.rpc;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.apache.hadoop.ha.protocolPB.HAServiceProtocolPB;
import org.apache.hadoop.hoya.api.HoyaClusterProtocol;
import org.apache.hadoop.hoya.api.proto.Messages;
import org.apache.hadoop.ipc.ProtobufHelper;
import org.apache.hadoop.ipc.ProtocolSignature;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;

public class HoyaClusterProtocolProxy implements HoyaClusterProtocol {

  final HoyaClusterProtocolPB endpoint;
  private static final RpcController NULL_CONTROLLER = null;
  
  public HoyaClusterProtocolProxy(HoyaClusterProtocolPB endpoint) {
    this.endpoint = endpoint;
  }

  private IOException convert(ServiceException se) {
    return ProtobufHelper.getRemoteException(se);
  }
  
  @Override
  public Messages.StopClusterResponseProto stopCluster(Messages.StopClusterRequestProto request) throws
                                                                                                 IOException,
                                                                                                 YarnException {
    try {
      return endpoint.stopCluster(NULL_CONTROLLER, request);
    } catch (ServiceException e) {
      throw convert(e);
    }
  }

  @Override
  public Messages.FlexClusterResponseProto flexCluster(Messages.FlexClusterRequestProto request) throws
                                                                                                 IOException,
                                                                                                 YarnException {
    try {
      return endpoint.flexCluster(NULL_CONTROLLER, request);
    } catch (ServiceException e) {
      throw convert(e);
    }
  }

  @Override
  public Messages.GetJSONClusterStatusResponseProto getJSONClusterStatus(
    Messages.GetJSONClusterStatusRequestProto request) throws
                                                       IOException,
                                                       YarnException {
    try {
      return endpoint.getJSONClusterStatus(NULL_CONTROLLER, request);
    } catch (ServiceException e) {
      throw convert(e);
    }
  }

  @Override
  public Messages.ListNodeUUIDsByRoleResponseProto listNodeUUIDsByRole(Messages.ListNodeUUIDsByRoleRequestProto request) throws
                                                                                                                         IOException,
                                                                                                                         YarnException {
    try {
      return endpoint.listNodeUUIDsByRole(NULL_CONTROLLER, request);
    } catch (ServiceException e) {
      throw convert(e);
    }
  }

  @Override
  public Messages.GetNodeResponseProto getNode(Messages.GetNodeRequestProto request) throws
                                                                                     IOException,
                                                                                     YarnException {
    try {
      return endpoint.getNode(NULL_CONTROLLER, request);
    } catch (ServiceException e) {
      throw convert(e);
    }
  }

  @Override
  public Messages.GetClusterNodesResponseProto getClusterNodes(Messages.GetClusterNodesRequestProto request) throws
                                                                                                             IOException,
                                                                                                             YarnException {
    try {
      return endpoint.getClusterNodes(NULL_CONTROLLER, request);
    } catch (ServiceException e) {
      throw convert(e);
    }
  }


  @Override
  public ProtocolSignature getProtocolSignature(String protocol,
                                                long clientVersion,
                                                int clientMethodsHash) throws
                                                                       IOException {
    if (!protocol.equals(RPC.getProtocolName(HoyaClusterProtocolPB.class))) {
      throw new IOException("Serverside implements " +
                            RPC.getProtocolName(HoyaClusterProtocolPB.class) +
                            ". The following requested protocol is unknown: " +
                            protocol);
    }

    return ProtocolSignature.getProtocolSignature(clientMethodsHash,
                                                  RPC.getProtocolVersion(
                                                    HoyaClusterProtocol.class),
                                                  HoyaClusterProtocol.class);
  }

  @Override
  public long getProtocolVersion(String protocol, long clientVersion) throws
                                                                      IOException {
    return HoyaClusterProtocol.versionID;
  }
}
