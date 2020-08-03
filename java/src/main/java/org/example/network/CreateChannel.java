/****************************************************** 
 *  Copyright 2018 IBM Corporation 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package org.example.network;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.utils.IOUtils;
import org.example.chaincode.invocation.InvokeChaincode;
import org.example.client.CAClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.user.UserContext;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

/**
 * 
 * @author Balaji Kadambi
 *
 */

public class CreateChannel {

	public static void main(String[] args) {
		try {
			CryptoSuite.Factory.getCryptoSuite();
			Util.cleanUp();
			// Construct Channel
			UserContext adminUser = new UserContext();
			adminUser.setName(Config.ADMIN);
			adminUser.setAffiliation(Config.ORG2);
			adminUser.setMspId(Config.ORG2_MSP);
			File f = new File (InvokeChaincode.class.getResource("/ca.crt").getPath());
			String certficate = new String (IOUtils.toByteArray(new FileInputStream(f)),"UTF-8");
			Properties properties = new Properties();
			properties.put("pemBytes", certficate.getBytes());
			properties.setProperty("pemFile", f.getAbsolutePath());
			properties.setProperty("allowAllHostNames", "true");
			CAClient caclient=new  CAClient(Config.CA_ORG2_URL, properties);
			caclient.setAdminUserContext(adminUser);
			adminUser =  caclient.enrollAdminUserTLS("admin", "adminpw");

			/*UserContext adminUser1 = new UserContext();
			adminUser.setName(Config.ADMIN);
			adminUser.setAffiliation(Config.ORG1);
			adminUser.setMspId(Config.ORG1_MSP);
			File f1 = new File (InvokeChaincode.class.getResource("/ca1.crt").getPath());
			String certficate1 = new String (IOUtils.toByteArray(new FileInputStream(f)),"UTF-8");
			Properties properties1 = new Properties();
			properties.put("pemBytes", certficate1.getBytes());
			properties.setProperty("pemFile", f.getAbsolutePath());
			properties.setProperty("allowAllHostNames", "true");
			CAClient caclient1=new  CAClient(Config.CA_ORG1_URL, properties1);
			caclient1.setAdminUserContext(adminUser);
			adminUser1 =  caclient.enrollAdminUserTLS("admin", "adminpw");*/



			/*UserContext org1Admin = new UserContext();
			Enrollment enrollOrg1Admin = Util.getEnrollment("D:\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\key1\\org1_key", null,
					"D:\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\cert1\\org1_cert", null);
			org1Admin.setEnrollment(enrollOrg1Admin);
			org1Admin.setMspId(Config.ORG1_MSP);
			org1Admin.setName(Config.ADMIN);

			UserContext org2Admin = new UserContext();
			Enrollment enrollOrg2Admin = Util.getEnrollment("D:\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\key2\\org2_key", null,
					"D:\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\cert2\\org2_cert", null);
			org2Admin.setEnrollment(enrollOrg2Admin);
			org2Admin.setMspId(Config.ORG2_MSP);
			org2Admin.setName(Config.ADMIN);*/

			FabricClient fabClient = new FabricClient(adminUser);

			// Create a new channel
            File fpOder = new File (InvokeChaincode.class.getResource("/order.crt").getPath());
            String certficatepOder = new String (IOUtils.toByteArray(new FileInputStream(fpOder)),"UTF-8");
            Properties peer_propertiesOder = new Properties();
            peer_propertiesOder.put("pemBytes", certficatepOder.getBytes());
            peer_propertiesOder.setProperty("sslProvider", "openSSL");
            peer_propertiesOder.setProperty("negotiationType", "TLS");
            Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL,peer_propertiesOder);
			ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File("D:\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\mychannel.tx"));

			byte[] channelConfigurationSignatures = fabClient.getInstance()
					.getChannelConfigurationSignature(channelConfiguration, adminUser);

			Channel mychannel = fabClient.getInstance().newChannel("mychannel", orderer, channelConfiguration,
					channelConfigurationSignatures);


            File fp = new File (InvokeChaincode.class.getResource("/server-org2p0.crt").getPath());
            String certficatep = new String (IOUtils.toByteArray(new FileInputStream(fp)),"UTF-8");
            Properties peer_properties = new Properties();
            peer_properties.put("pemBytes", certficatep.getBytes());
            peer_properties.setProperty("sslProvider", "openSSL");
            peer_properties.setProperty("negotiationType", "TLS");
            Peer peer0_org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL,peer_properties);
            //EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");


            File fp2 = new File (InvokeChaincode.class.getResource("/server-org1p0.crt").getPath());
            String certficatep2 = new String (IOUtils.toByteArray(new FileInputStream(fp2)),"UTF-8");
            Properties peer_properties2= new Properties();
            peer_properties2.put("pemBytes", certficatep2.getBytes());
            peer_properties2.setProperty("sslProvider", "openSSL");
            peer_properties2.setProperty("negotiationType", "TLS");
            Peer peer0_org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL,peer_properties2);
			mychannel.joinPeer(peer0_org1);
			mychannel.joinPeer(peer0_org2);
			
			mychannel.addOrderer(orderer);

			mychannel.initialize();
			
			/*fabClient.getInstance().setUserContext(adminUser1);
			mychannel = fabClient.getInstance().getChannel("mychannel");
			mychannel.joinPeer(peer0_org1);
			mychannel.joinPeer(peer0_org2);*/
			
			Logger.getLogger(CreateChannel.class.getName()).log(Level.INFO, "Channel created "+mychannel.getName());
            Collection peers = mychannel.getPeers();
            Iterator peerIter = peers.iterator();
            while (peerIter.hasNext())
            {
            	  Peer pr = (Peer) peerIter.next();
            	  Logger.getLogger(CreateChannel.class.getName()).log(Level.INFO,pr.getName()+ " at " + pr.getUrl());
            }
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
