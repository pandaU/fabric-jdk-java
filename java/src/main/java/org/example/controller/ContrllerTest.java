package org.example.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.example.chaincode.invocation.QueryChaincode;
import org.example.client.CAClient;
import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.user.UserContext;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class ContrllerTest {

    @RequestMapping("/lists")
    public Object list(){
        try {
            Util.cleanUp();
            UserContext adminUser = new UserContext();
            adminUser.setName(Config.ADMIN);
            adminUser.setAffiliation(Config.ORG2);
            adminUser.setMspId(Config.ORG1_MSP);
            File f = new File ("C:\\Users\\xxrib\\Desktop\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\ca.crt");
            String certficate = new String (IOUtils.toByteArray(new FileInputStream(f)),"UTF-8");
            Properties properties = new Properties();
            properties.put("pemBytes", certficate.getBytes());
            properties.setProperty("pemFile", f.getAbsolutePath());
            properties.setProperty("allowAllHostNames", "true");
            CAClient caclient=new  CAClient(Config.CA_ORG2_URL, properties);
            caclient.setAdminUserContext(adminUser);
            adminUser =  caclient.enrollAdminUserTLS("admin", "adminpw");
            FabricClient fabClient = new FabricClient(adminUser);
            ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
            Channel channel = channelClient.getChannel();
            //Properties propertiess = new Properties();
            /*propertiess.put("pemBytes", Files.readAllBytes(Paths.get("C:\\Users\\xxrib\\Desktop\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\ca.crt")));
            propertiess.setProperty("sslProvider", "openSSL");
            propertiess.setProperty("negotiationType", "TLS");
            propertiess.setProperty("trustServerCertificate", "true");
            propertiess.setProperty("hostnameOverride", Config.ORG1_PEER_0);*/
            /*File fs = new File ("C:\\Users\\xxrib\\Desktop\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\peer0.org1.example.com.crt");
            String ficate = new String (IOUtils.toByteArray(new FileInputStream(f)),"UTF-8");
            Properties propert = new Properties();
            propert.put("pemBytes", ficate.getBytes());
            propert.setProperty("pemFile", fs.getAbsolutePath());
            propert.setProperty("allowAllHostNames", "true");*/
            File fp = new File ("C:\\Users\\xxrib\\Desktop\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\server");
            String certficatep = new String (IOUtils.toByteArray(new FileInputStream(fp)),"UTF-8");
            Properties peer_properties = new Properties();
            peer_properties.put("pemBytes", certficatep.getBytes());
            peer_properties.setProperty("sslProvider", "openSSL");
            peer_properties.setProperty("negotiationType", "TLS");
            Peer peer = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL,peer_properties);

            // EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01",Config.grpc+ Config.baseUrl+":7053");
            Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
            channel.addOrderer(orderer);
            channel.addPeer(peer);
            channel.initialize();
            Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, "Querying   ...");
            Collection<ProposalResponse> responsesQuery = channelClient.queryByChainCode("fabcar", "queryCar", new String[] {"CAR9"});
            for (ProposalResponse pres : responsesQuery) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                return  stringResponse;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
