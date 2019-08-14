package com.template.webserver

import bundle.claim.sub.flows.AddClaimMembersFlow
import com.template.states.RefClaimState
import group.chat.flows.AddLossAmmunttoClaimFlow
import group.chat.flows.AddPolicyIDtoClaimFlow
import group.chat.flows.GenerateParentPolicy
import negotiation.contracts.PolicyState
import negotiation.workflows.MakeClaimFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.FlowProgressHandle
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
    var criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED);
    var vpStates = proxy.vaultQueryBy<RefClaimState>(criteria);
    var i = 0;
    return mapOf(
      "data" to vpStates.states.map { claim ->
        mapOf(
          "id" to claim.state.data.linearId.toString(),
          "parties" to claim.state.data.members.map { p ->
            p.name.organisation
          },
          "policies" to claim.state.data.policyID.count(),
          "open" to (vpStates.statesMetadata[i++].status == Vault.StateStatus.UNCONSUMED),
          "coverage_amt" to claim.state.data.coverage_ammount_ref.sum(),
          "loss_amount" to claim.state.data.loss_amount,
          "moderator" to claim.state.data.moderator.name.organisation
        );
      }
    )
  }

  @PostMapping("/members")
  fun members(): List<String> {
    val notary = proxy.notaryIdentities().first().name.organisation;
    val me = proxy.nodeInfo().legalIdentities.first().name.organisation;
    var parties = proxy.partiesFromName("", false).map { party ->
      party.name.organisation
    };
    return parties - notary - me;
  }

  @PostMapping("/add-member", consumes = ["application/json"])
  fun addMembers(
    @RequestBody data : Map<String, Any>
  ): CompletableFuture<UniqueIdentifier> {
    val claimId = data["claim_id"] as String;
    val member = proxy.partiesFromName((data["member"] as String), true).first();
    return proxy.startTrackedFlowDynamic(
      AddClaimMembersFlow::class.java,
      claimId,
      member
    ).returnValue.toCompletableFuture();
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

  @PostMapping("/add-policy", consumes = ["application/json"])
  fun addPolicies(
    @RequestBody data : Map<String, Any>
  ) : CompletableFuture<Unit> {
    val claimId = data["claim_id"] as String;
    val policyId = data["policy_id"] as String;
    return proxy.startTrackedFlowDynamic(
      AddPolicyIDtoClaimFlow::class.java,
      claimId,
      UniqueIdentifier.fromString(policyId)
    ).returnValue.toCompletableFuture();
  }

  @PostMapping("/add")
  fun add(): CompletableFuture<UniqueIdentifier> {
    val notary = proxy.notaryIdentities().first();
    return proxy.startTrackedFlowDynamic(
      GenerateParentPolicy::class.java,
      notary
    ).returnValue.toCompletableFuture();
  }

  @PostMapping("/add-loss", consumes = ["application/json"])
  fun addLoss(
    @RequestBody data : Map<String, Any>
  ) : CompletableFuture<Unit> {
    val claimId = data["claim_id"] as String;
    val amount = data["amount"] as Int;
    return proxy.startTrackedFlowDynamic(
      AddLossAmmunttoClaimFlow::class.java,
      claimId,
      amount
    ).returnValue.toCompletableFuture();
  }

}
