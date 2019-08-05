package com.template.webserver

import negotiation.contracts.ProposalState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap
import negotiation.workflows.AcceptanceFlow.Initiator as AInitiator
import negotiation.workflows.ModificationFlow.Initiator as MInitiator
import negotiation.workflows.ProposalFlow.Initiator as PInitiator

@RestController
@RequestMapping("/proposal")
class ProposalController (rpc: NodeRPCConnection){
  companion object {
    private val logger = LoggerFactory.getLogger(RestController::class.java)
  }
  private val proxy = rpc.proxy

  @PostMapping("/initiate", consumes = ["application/json"])
  fun initialize(
    @RequestBody data : Map<String, Any>
  ): CompletableFuture<UniqueIdentifier> {
    var parser = SimpleDateFormat("yyyy-MM-dd");
    var applicant = data["applicant"] as HashMap<String, String>;
    var broker = data["broker"] as HashMap<String, String>;
    var carrier = data["carrier"] as HashMap<String, String>;
    var additional = data["additional"] as HashMap<String, String>;
    var lob = data["lob"] as String;
    var startDate = parser.parse(data["start_date"] as String)!!;
    var expireDate = parser.parse(data["expire_date"] as String)!!;
    var billing = data["billing"] as HashMap<String, Any>;
    var attachments = data["attachments"] as String;
    var premises = data["premises"] as HashMap<String, Any>;
    var coverage = data["coverage"] as HashMap<String, Int>;
    var counterparty : Party = proxy.partiesFromName("InsurerA", true).first();
    var isBuyer : Boolean = true;
    val flow = proxy.startTrackedFlowDynamic(
      PInitiator::class.java,
      isBuyer,
      applicant["name"] as String,
      applicant["mailing_address"] as String,
      applicant["gl_code"] as String,
      applicant["sic"] as String,
      applicant["ss"] as String,
      applicant["business_phone"] as String,
      applicant["business_type"] as String,
      broker["company_name"] as String,
      broker["contact_name"] as String,
      broker["phone"] as String,
      broker["email"] as String,
      carrier["company"] as String,
      carrier["contact"] as String,
      carrier["phone"] as String,
      carrier["email"] as String,
      additional["name"] as String,
      additional["mailing_address"] as String,
      additional["gl_code"] as String,
      additional["sic"] as String,
      additional["ss"] as String,
      additional["business_phone"] as String,
      additional["business_type"] as String,
      lob,
      startDate,
      expireDate,
      billing["plan"] as String,
      billing["payment_plan"] as String,
      billing["mop"] as String,
      billing["audit"] as String,
      billing["deposit"] as Int,
      billing["min_premium"] as Int,
      attachments,
      premises["additional"] as Boolean,
      premises["address"] as String,
      premises["city_limits"] as Boolean,
      premises["interest"] as String,
      coverage["total"] as Int,
      coverage["amount"] as Int,
      counterparty
    );
    return flow.returnValue.toCompletableFuture();
  }

  @PostMapping("/modify", consumes = ["application/json"])
  fun modify(
    @RequestBody data : Map<String, Any>
  ): CompletableFuture<Unit> {
    var proposalId = data["proposal_id"] as String;
    var uuid = UniqueIdentifier.fromString(proposalId);
    var newAmount = data["new_amount"] as Int;
    val flow = proxy.startTrackedFlowDynamic(
      MInitiator::class.java,
      uuid,
      newAmount
    );
    return flow.returnValue.toCompletableFuture();
  }

  @PostMapping("/accept", consumes = ["application/json"])
  fun accept(
    @RequestBody data : Map<String, Any>
  ): CompletableFuture<Unit> {
    var proposalId = data["proposal_id"] as String;
    var uuid = UniqueIdentifier.fromString(proposalId);
    val flow = proxy.startTrackedFlowDynamic(
      AInitiator::class.java,
      uuid
    );
    return flow.returnValue.toCompletableFuture();
  }

  @PostMapping("/list")
  fun list(): List<Map<String, Any>> {
    var me = proxy.nodeInfo().legalIdentities.first();
    return proxy.vaultQueryBy<ProposalState>().states.map { ps ->
      var allow = ps.state.data.proposee == me;
      mapOf(
        "proposal_id" to ps.state.data.linearId.toString(),
        "applicant_name" to ps.state.data.policy_applicant_name,
        "broker_company" to ps.state.data.broker_company_name,
        "broker_contact" to ps.state.data.broker_contact_name,
        "billing_plan" to ps.state.data.billing_plan,
        "billing_premium" to ps.state.data.billing_min_premium,
        "billing_deposit" to ps.state.data.billing_deposit,
        "allow_accept" to allow
      )
    };
  }

  @PostMapping("/stats")
  fun stats() : Map<String, Int> {
    var revenue = 0;
    var proposals = 0;
    proxy.vaultQueryBy<ProposalState>().states.map {
      revenue += it.state.data.billing_min_premium;
      proposals += 1;
    };
    return mapOf(
      "revenue" to revenue,
      "numbers" to proposals
    );
  }
}
