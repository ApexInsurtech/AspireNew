package com.template.webserver

import negotiation.contracts.ClaimState
import negotiation.contracts.MakeaClaimState
import negotiation.contracts.ProposalState
import negotiation.contracts.PolicyState
import negotiation.contracts.Reservestate
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dashboard")
class DashboardController (rpc: NodeRPCConnection){
  companion object {
    private val logger = LoggerFactory.getLogger(RestController::class.java)
  }
  private val proxy = rpc.proxy

  @PostMapping("stats")
  fun proposals(): Map<String, Any> {
    var revenue = 0;
    var deposits = 0;
    var claims = 0;
    var reserve = 0;
    proxy.vaultQueryBy<ProposalState>().states.map {
      revenue += it.state.data.billing_min_premium;
      deposits += it.state.data.billing_deposit;
    };
    proxy.vaultQueryBy<ClaimState>().states.map {
      claims += 1;
    };
    proxy.vaultQueryBy<Reservestate>().states.map {
      reserve += 1;
    };
    return mapOf(
      "revenue" to revenue,
      "deposits" to deposits,
      "claims" to claims,
      "reserve" to reserve
    );
  }

  @PostMapping("business-lines")
  fun businessLines(): MutableMap<String, Int> {
    var bl : MutableMap<String, Int> = mutableMapOf(
      "BOILER & MACHINERY" to 0,
      "BUSINESS AUTO" to 0,
      "BUSINESS OWNERS" to 0,
      "COMMERCIAL GENERAL LIABILITY" to 0,
      "COMMERCIAL INLAND MARINE" to 0,
      "COMMERCIAL PROPERTY" to 0,
      "CRIME" to 0,
      "CYBER AND PRIVACY" to 0,
      "FIDUCIARY LIABILITY" to 0,
      "GARAGE AND DEALERS" to 0,
      "LIQUOR LIABILITY" to 0,
      "MOTOR CARRIER" to 0,
      "TRUCKERS" to 0,
      "UMBRELLA" to 0,
      "YACHT" to 0
    );
    proxy.vaultQueryBy<ProposalState>().states.map { ps ->
      if(bl.containsKey(ps.state.data.lines_of_business)){
        bl[ps.state.data.lines_of_business] = bl[ps.state.data.lines_of_business]!!.plus(1);
      }
    };
    return bl;
  }

  @PostMapping("claims")
  fun claimsChart(): Map<String, Int> {
    var makeclaims = 0;
    var claims = 0;
    proxy.vaultQueryBy<MakeaClaimState>().states.map { ps ->
      makeclaims += 1;
    };
    proxy.vaultQueryBy<ClaimState>().states.map { ps ->
      claims += 1;
    };
    return mapOf(
      "make" to makeclaims,
      "claims" to claims
    );
  }

  @PostMapping("reserves")
  fun reserves(): Map<String, Int> {
    var premium = 0;
    var claims = 0;
    proxy.vaultQueryBy<ProposalState>().states.map { ps ->
      premium += ps.state.data.billing_min_premium;
    };
    proxy.vaultQueryBy<ClaimState>().states.map { ps ->
      claims += 1;
    };
    return mapOf(
      "premium" to premium,
      "claims" to claims
    );
  }

  @PostMapping("policies")
  fun policies(): List<Map<String, Any>> {
    return proxy.vaultQueryBy<PolicyState>().states.map { ps ->
      mapOf(
        "applicant_name" to ps.state.data.policy_applicant_name,
        "broker_company" to ps.state.data.broker_company_name,
        "broker_contact" to ps.state.data.broker_contact_name,
        "billing_plan" to ps.state.data.billing_plan,
        "billing_premium" to ps.state.data.billing_min_premium,
        "billing_deposit" to ps.state.data.billing_deposit
      )
    };
  }
}
