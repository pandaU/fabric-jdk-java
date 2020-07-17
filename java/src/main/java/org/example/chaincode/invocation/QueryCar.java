package org.example.chaincode.invocation;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.apache.commons.compress.utils.IOUtils;
import org.bouncycastle.openssl.PEMWriter;
import org.example.client.CAClient;
import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.user.UserContext;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QueryCar {
    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";


    static String getPEMStringFromPrivateKey(PrivateKey privateKey) throws IOException {
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);

        pemWriter.writeObject(privateKey);

        pemWriter.close();

        return pemStrWriter.toString();
    }


    public static HFCAClient enableTLS(HFCAClient hfcaclient , String uesrName , String userpasswd , String orgName , String caHsotIp) {
        final EnrollmentRequest enrollmentRequestTLS  = new EnrollmentRequest();
        enrollmentRequestTLS.addHost(caHsotIp);
        enrollmentRequestTLS.setProfile("tls");
        try {
            final Enrollment enroll =hfcaclient.enroll(uesrName, userpasswd,enrollmentRequestTLS);
            final String tlsCertPEM = enroll.getCert();
            final String tlsKeyPEM = getPEMStringFromPrivateKey(enroll.getKey());
            final Properties tlsProperties = new Properties ();
            tlsProperties.put("clientKeyBytes", tlsKeyPEM.getBytes(UTF_8));
            tlsProperties.put("clientCertBytes", tlsCertPEM.getBytes(UTF_8));

        } catch (EnrollmentException | InvalidArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return hfcaclient;

    }




    public static void main(String args[]) {
        try {
            Util.cleanUp();
            UserContext adminUser = new UserContext();
            adminUser.setName(Config.ADMIN);
            adminUser.setAffiliation(Config.ORG1);
            adminUser.setMspId(Config.ORG1_MSP);
            File f = new File ("C:\\Users\\xxrib\\Desktop\\linux-fabric\\blockchain-application-using-fabric-java-sdk\\java\\src\\main\\resources\\ca.crt");
            String certficate = new String (IOUtils.toByteArray(new FileInputStream(f)),"UTF-8");
            Properties properties = new Properties();
            properties.put("pemBytes", certficate.getBytes());
            properties.setProperty("pemFile", f.getAbsolutePath());
            properties.setProperty("allowAllHostNames", "true");
            CAClient caclient=new  CAClient(Config.CA_ORG1_URL, properties);
            caclient.setAdminUserContext(adminUser);
            adminUser =  caclient.enrollAdminUserTLS("admin", "adminpw");
            FabricClient fabClient = new FabricClient(adminUser);
            ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
            Channel channel = channelClient.getChannel();
            //Properties propertiess = new Properties();

            // 其实只需要一个TLS根证书就可以了，比如TLS相关的秘钥等都是可选的
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
            Peer peer = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);

           // EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01",Config.grpc+ Config.baseUrl+":7053");
            Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
            channel.addOrderer(orderer);
            channel.addPeer(peer);
            channel.initialize();
            Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, "Querying   ...");
            Collection<ProposalResponse> responsesQuery = channelClient.queryByChainCode("fabcar", "query", new String[] {"b"});
            for (ProposalResponse pres : responsesQuery) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, stringResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
