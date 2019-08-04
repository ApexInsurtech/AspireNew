package com.template.webserver

import negotiation.contracts.ProposalState
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

  @PostMapping("proposals")
  fun proposals(): Map<String, Any> {
    var revenue = 0;
    var deposits = 0;
    proxy.vaultQueryBy<ProposalState>().states.map {
      revenue += it.state.data.billing_min_premium;
      deposits += it.state.data.billing_deposit;
    };
    return mapOf(
      "revenue" to revenue,
      "deposits" to deposits
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
}
