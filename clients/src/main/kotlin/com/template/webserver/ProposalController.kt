package com.template.webserver

import negotiation.contracts.ProposalState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap
import negotiation.workflows.AcceptanceFlow.Initiator as AInitiator
import com.negotiation.workflows.ModificationFlow.Initiator as MInitiator
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
    val flow = proxy.startTrackedFlowDynamic(
      MInitiator::class.java,
      uuid,
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
      coverage["amount"] as Int
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
      mapOf(
        "proposal_id" to ps.state.data.linearId.toString(),
        "applicant_name" to ps.state.data.policy_applicant_name,
        "broker_company" to ps.state.data.broker_company_name,
        "broker_contact" to ps.state.data.broker_contact_name,
        "billing_plan" to ps.state.data.billing_plan,
        "billing_premium" to ps.state.data.billing_min_premium,
        "billing_deposit" to ps.state.data.billing_deposit,
        "allow_accept" to (ps.state.data.proposee == me),
        "allow_reject" to (ps.state.data.proposee == me),
        "allow_edit" to (ps.state.data.proposee == me)
      )
    };
  }

  @PostMapping("/counter-parties")
  fun available(): List<String> {
    val notary = proxy.notaryIdentities().first().name.organisation;
    val me = proxy.nodeInfo().legalIdentities.first().name.organisation;
    var parties = proxy.partiesFromName("", false).map { party ->
      party.name.organisation
    };
    return parties - notary - me;
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

  @PostMapping("/view", consumes = ["application/json"])
  fun view(
    @RequestBody data : Map<String, Any>
  ): Map<String, Any> {
    var proposalId = data["proposal_id"] as String;
    var uuid = UniqueIdentifier.fromString(proposalId);
    var parser = SimpleDateFormat("yyyy-MM-dd");
    val state = proxy.vaultQueryByCriteria(
      criteria = QueryCriteria.LinearStateQueryCriteria(
        linearId = listOf(uuid)
      ),
      contractStateType = ProposalState::class.java
    ).states.first().state.data;
    return mapOf(
      "applicant" to mapOf(
        "name" to state.policy_applicant_name,
        "mailing_address" to state.policy_applicant_mailing_address,
        "gl_code" to state.policy_applicant_gl_code,
        "sic" to state.policy_applicant_sic,
        "ss" to state.policy_applicant_fein_or_soc_sec,
        "business_phone" to state.policy_applicant_buisness_phone,
        "business_type" to state.policy_applicant_buisness_type
      ),
      "broker" to mapOf(
        "company_name" to state.broker_company_name,
        "contact_name" to state.broker_contact_name,
        "phone" to state.broker_phone,
        "email" to state.broker_email
      ),
      "carrier" to mapOf(
        "company_name" to state.carrier_company_name,
        "contact_name" to state.carrier_contact_name,
        "phone" to state.carrier_phone,
        "email" to state.carrier_email
      ),
      "additional" to mapOf(
        "name" to state.additional_insured_name,
        "mailing_address" to state.additional_insured_mailing_address,
        "gl_code" to state.additional_insured_gl_code,
        "sic" to state.additional_insured_sic,
        "ss" to state.additional_insured_fein_or_soc_sec,
        "business_phone" to state.additional_insured_buisness_phone,
        "business_type" to state.additional_insured_type_of_buisness
      ),
      "lob" to state.lines_of_business,
      "start_date" to parser.format(state.policy_information_proposed_eff_date),
      "expire_date" to parser.format(state.policy_information_proposed_exp_date),
      "premises" to mapOf(
        "additional" to (if(state.premises_additional) { "YES" } else { "NO" }),
        "address" to state.premises_address,
        "city_limits" to (if(state.premises_within_city_limits) { "YES" } else { "NO" }),
        "interest" to state.premises_interest
      ),
      "billing" to mapOf(
        "plan" to state.billing_plan,
        "payment_plan" to state.billing_payment_plan,
        "mop" to state.billing_method_of_payment,
        "audit" to state.billing_audit,
        "deposit" to state.billing_deposit,
        "min_premium" to state.billing_min_premium
      ),
      "coverage" to mapOf(
        "total" to state.total_coverage,
        "amount" to state.coverage_amount
      ),
      "counterparty" to state.proposee.name.organisation
    );
  }
}
