package com.template.webserver

import com.template.states.RefClaimState
import group.chat.flows.AddPolicyIDtoClaimFlow
import negotiation.contracts.MakeaClaimState
import negotiation.contracts.PolicyState
import negotiation.workflows.MakeClaimFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/claims")
class ClaimsController (rpc: NodeRPCConnection){
  companion object {
    private val logger = LoggerFactory.getLogger(RestController::class.java)
  }
  private val proxy = rpc.proxy

  @PostMapping("/stats")
  fun stats(): Map<String, Int> {
    var claims = 0;
    var coverageAmount = 0;
    var minBillingPremium = 0;
    var criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL);
    proxy.vaultQueryBy<RefClaimState>(criteria).states.map {
      claims += 1;
      coverageAmount += it.state.data.coverage_ammount_ref.sum();
    }
    proxy.vaultQueryBy<PolicyState>().states.map {
      minBillingPremium += it.state.data.billing_min_premium;
    }
    return mapOf(
      "total" to claims,
      "coverage" to coverageAmount,
      "reserve" to coverageAmount,
      "ratio" to (coverageAmount/( if(minBillingPremium == 0){ 1 } else { minBillingPremium }))
    );
  }

  @PostMapping("/list")
  fun list(): Map<String, List<Map<String, Any>>> {
    var criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL);
    var vpStates = proxy.vaultQueryBy<MakeaClaimState>(criteria);
    var i = 0;
    return mapOf(
      "data" to vpStates.states.map { claim ->
        mapOf(
          "id" to claim.state.data.linearId.toString(),
          "proposee" to claim.state.data.proposee.name.organisation,
          "proposer" to claim.state.data.proposer.name.organisation,
          "applicant_name" to claim.state.data.policy_applicant_name,
          "applicant_address" to claim.state.data.policy_applicant_mailing_address,
          "open" to (vpStates.statesMetadata[i++].status == Vault.StateStatus.UNCONSUMED),
          "description" to claim.state.data.description_details
        );
      }
    )
  }

  @PostMapping("/policies")
  fun policies() : List<Map<String, Any>> {
    val me = proxy.nodeInfo().legalIdentities.first();
    return proxy.vaultQueryBy<PolicyState>().states.map {
      mapOf(
        "id" to it.state.data.linearId.toString(),
        "applicant_name" to it.state.data.policy_applicant_name
      )
    }
  }

  @PostMapping("/add")
  fun add(
    @RequestBody data : Map<String, Any>
  ): CompletableFuture<UniqueIdentifier> {
    val info = data["info"] as Map<String, Any>;
    val details = data["details"] as String;
    val policyIds = data["policy_ids"] as List<String>;
    return proxy.startTrackedFlowDynamic(
      MakeClaimFlow.Initiator::class.java,
      UniqueIdentifier.fromString(policyIds.first()),
      details
    ).returnValue.toCompletableFuture()/*.thenApply { uid ->
      if(policyIds.count() > 1){
        for( policy_id in policyIds){
          proxy.startTrackedFlowDynamic(
            AddPolicyIDtoClaimFlow::class.java,
            uid!!,
            policy_id
          ).returnValue.toCompletableFuture()
        }
      }
    };*/
  }

}
