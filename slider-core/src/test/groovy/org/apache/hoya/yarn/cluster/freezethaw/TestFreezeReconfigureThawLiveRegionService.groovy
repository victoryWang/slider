/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.hoya.yarn.cluster.freezethaw

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem as HadoopFS
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.ClusterStatus
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.apache.hoya.api.ClusterDescription
import org.apache.hoya.api.OptionKeys
import org.apache.hoya.core.build.InstanceIO
import org.apache.hoya.providers.hbase.HBaseKeys
import org.apache.hoya.tools.ConfigHelper
import org.apache.hoya.tools.HoyaFileSystem
import org.apache.hoya.tools.HoyaUtils
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.hoya.yarn.providers.hbase.HBaseMiniClusterTestBase
import org.junit.Test

/**
 * Test of RM creation. This is so the later test's prereq's can be met
 */
@CompileStatic
@Slf4j
class TestFreezeReconfigureThawLiveRegionService
    extends HBaseMiniClusterTestBase {

  @Test
  public void testFreezeReconfigureThawLiveRegionService() throws Throwable {
    String clustername = "test_freeze_reconfigure_thaw_live_regionservice"
    int regionServerCount = 4
    int nodemanagers = 3
    YarnConfiguration conf = getConfiguration()
    //one vcore per node
    conf.setInt("yarn.nodemanager.resource.cpu-vcores", 1)
    createMiniCluster(clustername, conf, nodemanagers, true)
    describe(
        "Create a $regionServerCount node cluster, freeze it, patch the configuration files," +
        " thaw it and verify that it came back with the new settings")

    ServiceLauncher launcher = createHBaseCluster(
        clustername,
        regionServerCount,
        [],
        true,
        true)
    HoyaClient hoyaClient = (HoyaClient) launcher.service
    addToTeardown(hoyaClient);
    ClusterDescription status = hoyaClient.getClusterDescription(clustername)
    log.info("${status.toJsonString()}")

    ClusterStatus clustat = basicHBaseClusterStartupSequence(hoyaClient)

    clustat = waitForHBaseRegionServerCount(
        hoyaClient,
        clustername,
        regionServerCount,
        HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)
    describe("Cluster status")
    log.info(hbaseStatusToString(clustat));


    clusterActionFreeze(hoyaClient, clustername)
    killAllRegionServers();

    //reconfigure time

    //get the location of the cluster
    HadoopFS dfs = HadoopFS.get(new URI(fsDefaultName), miniCluster.config)
    HoyaFileSystem hoyaFileSystem = new HoyaFileSystem(dfs, miniCluster.config)
    Path clusterDir = hoyaFileSystem.buildHoyaClusterDirPath(clustername);
    def instanceDefinition = InstanceIO.loadInstanceDefinitionUnresolved(
        hoyaFileSystem,
        clusterDir)

    def snapshotPath = instanceDefinition.getInternalOperations().get(
        OptionKeys.INTERNAL_SNAPSHOT_CONF_PATH)
    assert snapshotPath != null

    Path confdir = new Path(snapshotPath);
    Path hbaseSiteXML = new Path(confdir, HBaseKeys.SITE_XML)
    Configuration originalConf = ConfigHelper.loadTemplateConfiguration(
        dfs,
        hbaseSiteXML,
        "");
    //patch
    String patchedText = "patched-after-freeze"
    originalConf.setBoolean(patchedText, true);
    //save
    ConfigHelper.saveConfig(dfs, hbaseSiteXML, originalConf);

    //now let's start the cluster up again
    ServiceLauncher launcher2 = thawHoyaCluster(clustername, [], true);
    HoyaClient thawed = launcher2.service as HoyaClient
    clustat = basicHBaseClusterStartupSequence(thawed)

    //get the options
    ClusterDescription stat = thawed.clusterDescription
    Map<String, String> properties = stat.clientProperties
    log.info("Cluster properties: \n" + HoyaUtils.stringifyMap(properties));
    assert properties[patchedText] == "true";

  }


}
