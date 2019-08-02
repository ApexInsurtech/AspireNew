package com.template.webserver

import negotiation.contracts.ProposalState
import negotiation.workflows.ModificationFlow
import net.corda.core.contracts.LinearState
import org.slf4j.LoggerFactory
import negotiation.workflows.ProposalFlow.Initiator as PInitiator
import negotiation.workflows.ModificationFlow.Initiator as MInitiator
import negotiation.workflows.AcceptanceFlow.Initiator as AInitiator
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.internal.concurrent.thenMatch
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/dashboard")
class DashboardController (rpc: NodeRPCConnection){
  companion object {
    private val logger = LoggerFactory.getLogger(RestController::class.java)
  }
  private val proxy = rpc.proxy

  @PostMapping("")
  fun revenue(): Map<String, Any> {
    var revenue = 0;
    var deposits = "";
    var claims = "";
    proxy.vaultQueryBy<ProposalState>().states.map {
        revenue += it.state.data.billing_min_premium;
        deposits += it.state.data.billing_deposit;
    };
    /*proxy.vaultQueryBy<Claim>().states.map {
      revenue += it.state.data.billing_min_premium;
      deposits += it.state.data.billing_deposit;
    };*/
    return mapOf(
      "revenue" to revenue,
      "deposits" to deposits
    );
  }
}
