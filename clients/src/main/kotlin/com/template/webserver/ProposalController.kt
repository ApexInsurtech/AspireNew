package com.template.webserver

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap
//import negotiation.workflows.AcceptanceFlow.Initiator as AInitiator
//import negotiation.workflows.ModificationFlow.Initiator as MInitiator
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
    var applicant = data["applicant"] as HashMap<String, String>;
    var broker = data["broker"] as HashMap<String, String>;
    var carrier = data["carrier"] as HashMap<String, String>;
    var additional = data["additional"] as HashMap<String, String>;
    var lob = data["lob"] as String;
    var startDate = data["start_date"] as Date;
    var expireDate = data["expire_date"] as Date;
    var billing = data["billing"] as HashMap<String, Any>;
    var attachments = data["attachments"] as String;
    var premises = data["premises"] as HashMap<String, Any>;
    var coverage = data["coverage"] as HashMap<String, Int>;
    var brokerParty : Party = proxy.partiesFromName(data["broker_party"] as String, true).first();
    var leadInsurerParty : Party = proxy.partiesFromName(data["lead_insurer_party"] as String, true).first();
    var proposerParty : Party = proxy.partiesFromName(data["proposer_party"] as String, true).first();
    var proposeeParty : Party = proxy.partiesFromName(data["proposee_party"] as String, true).first();

    val flow = proxy.startTrackedFlowDynamic(
      PInitiator::class.java,
      applicant["name"],
      applicant["mailing_address"],
      applicant["gl_code"],
      applicant["sic"],
      applicant["ss"],
      applicant["business_phone"],
      applicant["business_type"],
      broker["company_name"],
      broker["contact_name"],
      broker["phone"],
      broker["email"],
      carrier["company"],
      carrier["contact"],
      carrier["phone"],
      carrier["email"],
      additional["name"],
      additional["mailing_address"],
      additional["gl_code"],
      additional["sic"],
      additional["ss"],
      additional["business_phone"],
      additional["business_type"],
      lob,
      startDate,
      expireDate,
      billing["plan"],
      billing["payment_plan"],
      billing["mop"],
      billing["audit"],
      billing["deposit"] as Int,
      billing["min_premium"] as Int,
      attachments,
      premises["address"],
      premises["city_limits"] as Boolean,
      premises["interest"] as Boolean,
      coverage["total"] as Int,
      coverage["amount"] as Int,
      brokerParty,
      leadInsurerParty,
      proposerParty,
      proposeeParty
    );
    return flow.returnValue.toCompletableFuture();
  }

  /*@PostMapping("/modify", consumes = ["application/json"])
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
  }*/
}
