package com.template.webserver

import org.slf4j.LoggerFactory
import negotiation.workflows.ProposalFlow.Initiator as PInitiator
import com.negotiation.workflows.ModificationFlow.Initiator as MInitiator
import negotiation.workflows.AcceptanceFlow.Initiator as AInitiator
import net.corda.core.contracts.UniqueIdentifier
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/proposal-flow")
class RunnerController (rpc: NodeRPCConnection){
  companion object {
    private val logger = LoggerFactory.getLogger(RestController::class.java)
  }
  private val proxy = rpc.proxy

  @PostMapping("/initiator", consumes = ["application/json"])
  fun initialize(
    @RequestBody data : Map<String, Any>
  ): CompletableFuture<UniqueIdentifier> {
    var counterparty = proxy.partiesFromName(data["counterparty"] as String, true).first();
    var isBuyer = data["is_buyer"] as Boolean;
    var applicant = data["applicant"] as HashMap<String, String>;
    var broker = data["broker"] as HashMap<String, String>;
    var carrier = data["carrier"] as HashMap<String, String>;
    var additional = data["additional"] as HashMap<String, String>;
    var lob = data["lob"] as String;
    var startDate = data["start_date"] as String;
    var expireDate = data["expire_date"] as String;
    var billing = data["billing"] as HashMap<String, String>;
    var attachments = data["attachments"] as String;
    var premises = data["premises"] as HashMap<String, String>;
    /*var flow = Initiator(
      isBuyer = isBuyer,
      policy_applicant_name = applicant["name"]!!,
      policy_applicant_mailing_address = applicant["mailing_address"]!!,
      policy_applicant_gl_code = applicant["gl_code"]!!,
      policy_applicant_sic = applicant["sic"]!!,
      policy_applicant_fein_or_soc_sec = applicant["ss"]!!,
      policy_applicant_buisness_phone = applicant["business_phone"]!!,
      policy_applicant_buisness_type = applicant["business_type"]!!,
      broker_company_name = broker["company_name"]!!,
      broker_contact_name = broker["contact_name"]!!,
      broker_phone = broker["phone"]!!,
      broker_email = broker["email"]!!,
      carrier_company_name = carrier["company"]!!,
      carrier_contact_name = carrier["contact"]!!,
      carrier_phone = carrier["phone"]!!,
      carrier_email = carrier["email"]!!,
      additional_insured_name = additional["name"]!!,
      additional_insured_mailing_address = additional["mailing_address"]!!,
      additional_insured_gl_code = additional["gl_code"]!!,
      additional_insured_sic = additional["sic"]!!,
      additional_insured_fein_or_soc_sec = additional["ss"]!!,
      additional_insured_buisness_phone = additional["business_phone"]!!,
      additional_insured_type_of_buisness = additional["business_type"]!!,
      lines_of_business = lob,
      policy_information_proposed_eff_date = startDate,
      policy_information_proposed_exp_date = expireDate,
      billing_plan = billing["plan"]!!,
      billing_payment_plan = billing["payment_plan"]!!,
      billing_method_of_payment = billing["mop"]!!,
      billing_audit = billing["audit"]!!,
      billing_deposit = billing["deposit"]!!,
      billing_min_premium = billing["min_premium"]!!.toInt(),
      attachments_additional = attachments,
      premises_additional = premises["additional"]!!,
      premises_address = premises["address"]!!.toBoolean(),
      premises_within_city_limits = premises["city_limits"]!!,
      premises_interest = premises["interest"]!!.toBoolean(),
      counterparty = counterparty
    );*/
    val flow = proxy.startTrackedFlowDynamic(
      PInitiator::class.java,
      isBuyer,
      applicant["name"]!!,
      applicant["mailing_address"]!!,
      applicant["gl_code"]!!,
      applicant["sic"]!!,
      applicant["ss"]!!,
      applicant["business_phone"]!!,
      applicant["business_type"]!!,
      broker["company_name"]!!,
      broker["contact_name"]!!,
      broker["phone"]!!,
      broker["email"]!!,
      carrier["company"]!!,
      carrier["contact"]!!,
      carrier["phone"]!!,
      carrier["email"]!!,
      additional["name"]!!,
      additional["mailing_address"]!!,
      additional["gl_code"]!!,
      additional["sic"]!!,
      additional["ss"]!!,
      additional["business_phone"]!!,
      additional["business_type"]!!,
      lob,
      startDate,
      expireDate,
      billing["plan"]!!,
      billing["payment_plan"]!!,
      billing["mop"]!!,
      billing["audit"]!!,
      billing["deposit"]!!,
      billing["min_premium"]!!.toInt(),
      attachments,
      premises["additional"]!!,
      premises["address"]!!.toBoolean(),
      premises["city_limits"]!!,
      premises["interest"]!!.toBoolean(),
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
}
