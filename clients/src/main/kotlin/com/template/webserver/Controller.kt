package com.template.webserver

import com.template.webserver.NodeRPCConnection
import negotiation.contracts.ProposalState
import net.corda.core.contracts.*
import net.corda.core.internal.requiredContractClassName
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/info") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/templateendpoint"], produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @GetMapping(value = ["/test_status"], produces = arrayOf("text/plain"))
    private fun test_status(): String {
        return "answwer is 1."
    }
    @GetMapping("/flow_details", produces = ["application/json"])
    private fun flowDetails(): String {
        return proxy.registeredFlows().toString()
    }

    @GetMapping("/state-details", produces = ["application/json"])
    private fun statesDetails(): List<Map<String, Any>> {
        return proxy.vaultQueryBy<LinearState>().states.map {
            var x = it.state.data.participants.map {
                val nameOrNull = it.nameOrNull()!!
                nameOrNull
            };
            mapOf(
              "name" to it.state.contract,
              "id" to it.state.data.linearId.toString(),
              "participants" to x
            );
        };
    }

    @PostMapping("/contract-details", produces = ["application/json"], consumes = ["application/json"])
    private fun statesDetails(
      @RequestBody data : Map<String, Any>
    ): Map<String, Any> {
        var propId = data["uuid"] as String;
        var uuid = UniqueIdentifier.fromString(propId);
        var queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(uuid));
        val data = proxy.vaultQueryByCriteria(queryCriteria, ProposalState::class.java).states.first().state.data;
        return mapOf(
            "seller" to data.lead_insurer.name,
            "buyer" to data.broker.name,
            "policy_applicant_name" to data.policy_applicant_name,
            "policy_applicant_mailing_address" to data.policy_applicant_mailing_address,
            "policy_applicant_gl_code" to data.policy_applicant_gl_code,
            "policy_applicant_sic" to data.policy_applicant_sic,
            "policy_applicant_fein_or_soc_sec" to data.policy_applicant_fein_or_soc_sec,
            "policy_applicant_buisness_phone" to data.policy_applicant_buisness_phone,
            "policy_applicant_buisness_type" to data.policy_applicant_buisness_type,
            "broker_company_name" to data.broker_company_name,
            "broker_contact_name" to data.broker_contact_name,
            "broker_phone" to data.broker_phone,
            "broker_email" to data.broker_email,
            "carrier_company_name" to data.carrier_company_name,
            "carrier_contact_name" to data.carrier_contact_name,
            "carrier_phone" to data.carrier_phone,
            "carrier_email" to data.carrier_email,
            "additional_insured_name" to data.additional_insured_name,
            "additional_insured_mailing_address" to data.additional_insured_mailing_address,
            "additional_insured_gl_code" to data.additional_insured_gl_code,
            "additional_insured_sic" to data.additional_insured_sic,
            "additional_insured_fein_or_soc_sec" to data.additional_insured_fein_or_soc_sec,
            "additional_insured_buisness_phone" to data.additional_insured_buisness_phone,
            "additional_insured_type_of_buisness" to data.additional_insured_type_of_buisness,
            "lines_of_business" to data.lines_of_business,
            "policy_information_proposed_eff_date" to data.policy_information_proposed_eff_date,
            "policy_information_proposed_exp_date" to data.policy_information_proposed_exp_date,
            "billing_plan" to data.billing_plan,
            "billing_payment_plan" to data.billing_payment_plan,
            "billing_method_of_payment" to data.billing_method_of_payment,
            "billing_audit" to data.billing_audit,
            "billing_deposit" to data.billing_deposit,
            "billing_min_premium" to data.billing_min_premium,
            "attachments_additional" to data.attachments_additional,
            "premises_additional" to data.premises_additional,
            "premises_address" to data.premises_address,
            "premises_within_city_limits" to data.premises_within_city_limits,
            "premises_interest" to data.premises_interest
        );
    }
}