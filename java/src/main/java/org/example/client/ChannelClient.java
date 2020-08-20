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

package org.example.client;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.example.chaincode.invocation.InvokeChaincode;
import org.example.user.UserContext;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

/**
 * Wrapper class for a channel client.
 * 
 * @author Balaji Kadambi
 *
 */

public class ChannelClient {

	String name;
	Channel channel;
	FabricClient fabClient;

	public String getName() {
		return name;
	}

	public Channel getChannel() {
		return channel;
	}

	public FabricClient getFabClient() {
		return fabClient;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param channel
	 * @param fabClient
	 */
	public ChannelClient(String name, Channel channel, FabricClient fabClient) {
		this.name = name;
		this.channel = channel;
		this.fabClient = fabClient;
	}

	/**
	 * Query by chaincode.
	 * 
	 * @param chaincodeName
	 * @param functionName
	 * @param args
	 * @return
	 * @throws InvalidArgumentException
	 * @throws ProposalException
	 */
	public Collection<ProposalResponse> queryByChainCode(String chaincodeName, String functionName, String[] args)
			throws InvalidArgumentException, ProposalException {
		Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
				"Querying " + functionName + " on channel " + channel.getName());
		QueryByChaincodeRequest request = fabClient.getInstance().newQueryProposalRequest();
		ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincodeName).build();
		request.setChaincodeID(ccid);
		request.setFcn(functionName);
		if (args != null)
			request.setArgs(args);

		Collection<ProposalResponse> response = channel.queryByChaincode(request);

		return response;
	}

	/**
	 * Send transaction proposal.
	 * 
	 * @param request
	 * @return
	 * @throws ProposalException
	 * @throws InvalidArgumentException
	 */
	public Collection<ProposalResponse> sendTransactionProposal(TransactionProposalRequest request)
			throws ProposalException, InvalidArgumentException {
		Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
				"Sending transaction proposal on channel " + channel.getName());

		Collection<ProposalResponse> response = channel.sendTransactionProposal(request, channel.getPeers());
		for (ProposalResponse pres : response) {
			String stringResponse = new String(pres.getChaincodeActionResponsePayload());
			Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
					"Transaction proposal on channel " + channel.getName() + " " + pres.getMessage() + " "
							+ pres.getStatus() + " with transaction iC:\\Users\\13202\\Desktop" + pres.getTransactionID());
			Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,stringResponse);
		}

		CompletableFuture<TransactionEvent> cf = channel.sendTransaction(response);
		Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,cf.toString());

		return response;
	}

	/**
	 * 
	 * Instantiate chaincode.
	 * 
	 * @param chaincodeName
	 * @param version
	 * @param chaincodePath
	 * @param language
	 * @param functionName
	 * @param functionArgs
	 * @param policyPath
	 * @return
	 * @throws InvalidArgumentException
	 * @throws ProposalException
	 * @throws ChaincodeEndorsementPolicyParseException
	 * @throws IOException
	 */
	public
	CompletableFuture<TransactionEvent>   instantiateChainCode(String chaincodeName, String version, String chaincodePath,
																					  String language, String functionName, String[] functionArgs, String policyPath,String org1ChaincodePackageID,Collection<Peer> peers)
			throws InvalidArgumentException, ProposalException, ChaincodeEndorsementPolicyParseException, IOException {
		/*Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
				"Instantiate proposal request " + chaincodeName + " on channel " + channel.getName()
						+ " with Fabric client " + fabClient.getInstance().getUserContext().getMspId() + " "
						+ fabClient.getInstance().getUserContext().getName());
		InstantiateProposalRequest instantiateProposalRequest = fabClient.getInstance()
				.newInstantiationProposalRequest();
		instantiateProposalRequest.setProposalWaitTime(180000);
		ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(version)
				.setPath(chaincodePath);
		ChaincodeID ccid = chaincodeIDBuilder.build();
		Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
				"Instantiating Chaincode ID " + chaincodeName + " on channel " + channel.getName());
		instantiateProposalRequest.setChaincodeID(ccid);
		if (language.equals(Type.GO_LANG.toString()))
			instantiateProposalRequest.setChaincodeLanguage(Type.GO_LANG);
		else
			instantiateProposalRequest.setChaincodeLanguage(Type.JAVA);

		instantiateProposalRequest.setFcn(functionName);
		instantiateProposalRequest.setArgs(functionArgs);
		Map<String, byte[]> tm = new HashMap<>();
		tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
		tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
		instantiateProposalRequest.setTransientMap(tm);

		if (policyPath != null) {
			ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
			chaincodeEndorsementPolicy.fromYamlFile(new File(policyPath));
			instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
		}

		Collection<ProposalResponse> responses = channel.sendLifecycleApproveChaincodeDefinitionForMyOrgProposal(instantiateProposalRequest,channel.getPeers());
		//CompletableFuture<TransactionEvent> cf = channel.sendTransaction(responses);

		Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
				"Chaincode " + chaincodeName + " on channel " + channel.getName());
		return responses;*/
		LifecycleApproveChaincodeDefinitionForMyOrgRequest lifecycleApproveChaincodeDefinitionForMyOrgRequest = fabClient.getInstance().newLifecycleApproveChaincodeDefinitionForMyOrgRequest();
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setSequence(1);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeName(chaincodeName);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeVersion("1");
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setInitRequired(true);

		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setPackageId(org1ChaincodePackageID);

		Collection<LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse> lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse = channel.sendLifecycleApproveChaincodeDefinitionForMyOrgProposal(lifecycleApproveChaincodeDefinitionForMyOrgRequest,
				peers);

		for (LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse response : lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse) {
			final Peer peer = response.getPeer();
		}

		return channel.sendTransaction(lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse);
	}

	/**
	 * Query a transaction by id.
	 * 
	 * @param txnId
	 * @return
	 * @throws ProposalException
	 * @throws InvalidArgumentException
	 */
	public TransactionInfo queryByTransactionId(String txnId) throws ProposalException, InvalidArgumentException {
		Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,
				"Querying by trasaction id " + txnId + " on channel " + channel.getName());
		Collection<Peer> peers = channel.getPeers();
		for (Peer peer : peers) {
			TransactionInfo info = channel.queryTransactionByID(peer, txnId);
			return info;
		}
		return null;
	}
	public void verifyByCheckCommitReadinessStatus(String chaincodeName, boolean initRequired, Collection<Peer> org1MyPeers,
												   Set<String> expectedApproved, Set<String> expectedUnApproved) throws InvalidArgumentException, ProposalException {
		LifecycleCheckCommitReadinessRequest lifecycleCheckCommitReadinessRequest = fabClient.getInstance().newLifecycleSimulateCommitChaincodeDefinitionRequest();
		lifecycleCheckCommitReadinessRequest.setSequence(1);
		lifecycleCheckCommitReadinessRequest.setChaincodeName(chaincodeName);
		lifecycleCheckCommitReadinessRequest.setChaincodeVersion("1");
		lifecycleCheckCommitReadinessRequest.setInitRequired(initRequired);

		Collection<LifecycleCheckCommitReadinessProposalResponse> lifecycleSimulateCommitChaincodeDefinitionProposalResponse = channel.sendLifecycleCheckCommitReadinessRequest(lifecycleCheckCommitReadinessRequest, org1MyPeers);
		for (LifecycleCheckCommitReadinessProposalResponse resp : lifecycleSimulateCommitChaincodeDefinitionProposalResponse) {
			final Peer peer = resp.getPeer();
		}
	}
	public CompletableFuture<TransactionEvent> commitChaincodeDefinitionRequest(String chaincodeName,
																				 boolean initRequired, Collection<Peer> endorsingPeers) throws ProposalException, InvalidArgumentException, InterruptedException, ExecutionException, TimeoutException {
		LifecycleCommitChaincodeDefinitionRequest lifecycleCommitChaincodeDefinitionRequest = fabClient.getInstance().newLifecycleCommitChaincodeDefinitionRequest();

		lifecycleCommitChaincodeDefinitionRequest.setSequence(1);
		lifecycleCommitChaincodeDefinitionRequest.setChaincodeName(chaincodeName);
		lifecycleCommitChaincodeDefinitionRequest.setChaincodeVersion("1");
		lifecycleCommitChaincodeDefinitionRequest.setInitRequired(initRequired);

		Collection<LifecycleCommitChaincodeDefinitionProposalResponse> lifecycleCommitChaincodeDefinitionProposalResponses = channel.sendLifecycleCommitChaincodeDefinitionProposal(lifecycleCommitChaincodeDefinitionRequest,
				endorsingPeers);

		for (LifecycleCommitChaincodeDefinitionProposalResponse resp : lifecycleCommitChaincodeDefinitionProposalResponses) {

			final Peer peer = resp.getPeer();
			Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,peer.toString());
		}

		return channel.sendTransaction(lifecycleCommitChaincodeDefinitionProposalResponses);

	}
	public void chainCodeInit(String fcn, UserContext context, Boolean doInit, String chaincodeName, Type chaincodeType, String... args) throws InvalidArgumentException, ProposalException {
		TransactionProposalRequest transactionProposalRequest = fabClient.getInstance().newTransactionProposalRequest();
		transactionProposalRequest.setChaincodeName(chaincodeName);
		transactionProposalRequest.setChaincodeLanguage(chaincodeType);
		transactionProposalRequest.setUserContext(context);

		transactionProposalRequest.setFcn(fcn);
		transactionProposalRequest.setProposalWaitTime(12000);
		transactionProposalRequest.setArgs(args);
		if (null != doInit) {
			transactionProposalRequest.setInit(doInit);
		}
		Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
		channel.sendTransaction(transactionPropResp);
	}
}
