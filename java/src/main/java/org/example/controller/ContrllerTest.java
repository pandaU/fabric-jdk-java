package org.example.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.example.chaincode.invocation.InvokeChaincode;
import org.example.chaincode.invocation.QueryCar;
import org.example.chaincode.invocation.QueryChaincode;
import org.example.client.CAClient;
import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.user.UserContext;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
public class ContrllerTest {
    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";
    @RequestMapping("/lists")
    public Object list(){
        try {
            Util.cleanUp();
            UserContext adminUser = new UserContext();
            adminUser.setName(Config.ADMIN);
            adminUser.setAffiliation(Config.ORG2);
            adminUser.setMspId(Config.ORG2_MSP);
            File f = new File (this.getClass().getResource("/ca.crt").getPath());
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
            File fp = new File (this.getClass().getResource("/server-ogr2p0.crt").getPath());
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
    @RequestMapping("/createCar")
    public Object createCar(String key,String make,String model,String colour,String owner){
        try {
            Util.cleanUp();
            String caUrl = Config.CA_ORG1_URL;
            CAClient caClient = new CAClient(caUrl, null);
            // Enroll Admin to Org1MSP
            UserContext adminUserContext = new UserContext();
            adminUserContext.setName(Config.ADMIN);
            adminUserContext.setAffiliation(Config.ORG2);
            adminUserContext.setMspId(Config.ORG2_MSP);
            caClient.setAdminUserContext(adminUserContext);
            File f = new File (this.getClass().getResource("/ca.crt").getPath());
            String certficate = new String (IOUtils.toByteArray(new FileInputStream(f)),"UTF-8");
            Properties properties = new Properties();
            properties.put("pemBytes", certficate.getBytes());
            properties.setProperty("pemFile", f.getAbsolutePath());
            properties.setProperty("allowAllHostNames", "true");
            CAClient caclient=new  CAClient(Config.CA_ORG2_URL, properties);
            caclient.setAdminUserContext(adminUserContext);
            adminUserContext =  caclient.enrollAdminUserTLS("admin", "adminpw");
            FabricClient fabClient = new FabricClient(adminUserContext);
            ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
            Channel channel = channelClient.getChannel();
            File fp = new File (this.getClass().getResource("/server-ogr2p0.crt").getPath());
            String certficatep = new String (IOUtils.toByteArray(new FileInputStream(fp)),"UTF-8");
            Properties peer_properties = new Properties();
            peer_properties.put("pemBytes", certficatep.getBytes());
            peer_properties.setProperty("sslProvider", "openSSL");
            peer_properties.setProperty("negotiationType", "TLS");
            Peer peer = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL,peer_properties);
            //EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
            File fpOder = new File (this.getClass().getResource("/order.crt").getPath());
            String certficatepOder = new String (IOUtils.toByteArray(new FileInputStream(fpOder)),"UTF-8");
            Properties peer_propertiesOder = new Properties();
            peer_propertiesOder.put("pemBytes", certficatepOder.getBytes());
            peer_propertiesOder.setProperty("sslProvider", "openSSL");
            peer_propertiesOder.setProperty("negotiationType", "TLS");
            Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL,peer_propertiesOder);

            File fp2 = new File (this.getClass().getResource("/server-ogr1p0.crt").getPath());
            String certficatep2 = new String (IOUtils.toByteArray(new FileInputStream(fp2)),"UTF-8");
            Properties peer_properties2= new Properties();
            peer_properties2.put("pemBytes", certficatep2.getBytes());
            peer_properties2.setProperty("sslProvider", "openSSL");
            peer_properties2.setProperty("negotiationType", "TLS");
            Peer peer2 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL,peer_properties2);
            channel.addPeer(peer);
            channel.addPeer(peer2);
            //channel.addEventHub(eventHub);
            channel.addOrderer(orderer);
            channel.initialize();

            TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
            ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
            request.setChaincodeID(ccid);
            request.setFcn("createCar");
            String[] arguments = { key, make, model, colour, owner };
            request.setArgs(arguments);
            request.setProposalWaitTime(1000);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
            tm2.put("result", ":)".getBytes(UTF_8));
            tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
            request.setTransientMap(tm2);
            Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
            for (ProposalResponse res: responses) {
                ChaincodeResponse.Status status = res.getStatus();
                if (status.getStatus()==200){
                    return "操作成功";
                }
                Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked createCar on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "操作失败";
    }
    @RequestMapping("/changOwner")
    public Object changOwner(String key,String newOwner){
        try {
            Util.cleanUp();
            String caUrl = Config.CA_ORG1_URL;
            CAClient caClient = new CAClient(caUrl, null);
            // Enroll Admin to Org1MSP
            UserContext adminUserContext = new UserContext();
            adminUserContext.setName(Config.ADMIN);
            adminUserContext.setAffiliation(Config.ORG2);
            adminUserContext.setMspId(Config.ORG2_MSP);
            caClient.setAdminUserContext(adminUserContext);
            File f = new File (this.getClass().getResource("/ca.crt").getPath());
            String certficate = new String (IOUtils.toByteArray(new FileInputStream(f)),"UTF-8");
            Properties properties = new Properties();
            properties.put("pemBytes", certficate.getBytes());
            properties.setProperty("pemFile", f.getAbsolutePath());
            properties.setProperty("allowAllHostNames", "true");
            CAClient caclient=new  CAClient(Config.CA_ORG2_URL, properties);
            caclient.setAdminUserContext(adminUserContext);
            adminUserContext =  caclient.enrollAdminUserTLS("admin", "adminpw");
            FabricClient fabClient = new FabricClient(adminUserContext);
            ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
            Channel channel = channelClient.getChannel();
            File fp = new File (this.getClass().getResource("/server-ogr2p0.crt").getPath());
            String certficatep = new String (IOUtils.toByteArray(new FileInputStream(fp)),"UTF-8");
            Properties peer_properties = new Properties();
            peer_properties.put("pemBytes", certficatep.getBytes());
            peer_properties.setProperty("sslProvider", "openSSL");
            peer_properties.setProperty("negotiationType", "TLS");
            Peer peer = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL,peer_properties);
            //EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
            File fpOder = new File (this.getClass().getResource("/order.crt").getPath());
            String certficatepOder = new String (IOUtils.toByteArray(new FileInputStream(fpOder)),"UTF-8");
            Properties peer_propertiesOder = new Properties();
            peer_propertiesOder.put("pemBytes", certficatepOder.getBytes());
            peer_propertiesOder.setProperty("sslProvider", "openSSL");
            peer_propertiesOder.setProperty("negotiationType", "TLS");
            Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL,peer_propertiesOder);

            File fp2 = new File (this.getClass().getResource("/server-ogr1p0.crt").getPath());
            String certficatep2 = new String (IOUtils.toByteArray(new FileInputStream(fp2)),"UTF-8");
            Properties peer_properties2= new Properties();
            peer_properties2.put("pemBytes", certficatep2.getBytes());
            peer_properties2.setProperty("sslProvider", "openSSL");
            peer_properties2.setProperty("negotiationType", "TLS");
            Peer peer2 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL,peer_properties2);
            channel.addPeer(peer);
            channel.addPeer(peer2);
            //channel.addEventHub(eventHub);
            channel.addOrderer(orderer);
            channel.initialize();

            TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
            ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
            request.setChaincodeID(ccid);
            request.setFcn("changeCarOwner");
            String[] arguments = { key, newOwner };
            request.setArgs(arguments);
            request.setProposalWaitTime(1000);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
            tm2.put("result", ":)".getBytes(UTF_8));
            tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
            request.setTransientMap(tm2);
            Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
            for (ProposalResponse res: responses) {
                ChaincodeResponse.Status status = res.getStatus();
                if (status.getStatus()==200){
                    return "操作成功";
                }
                Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked createCar on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "操作失败";
    }
}
