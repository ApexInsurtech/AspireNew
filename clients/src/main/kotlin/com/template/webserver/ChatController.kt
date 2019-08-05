package com.template.webserver

import com.template.states.ChatState
import group.chat.flows.AcceptChatFlow
import group.chat.flows.AddMemberFlow
import group.chat.flows.AddMessageFlow
import group.chat.flows.StartChat
import negotiation.contracts.ProposalState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap
import negotiation.workflows.AcceptanceFlow.Initiator as AInitiator
import negotiation.workflows.ModificationFlow.Initiator as MInitiator
import negotiation.workflows.ProposalFlow.Initiator as PInitiator

@RestController
@RequestMapping("/chat")
class ChatController (rpc: NodeRPCConnection){
  companion object {
    private val logger = LoggerFactory.getLogger(RestController::class.java)
  }
  private val proxy = rpc.proxy

  @PostMapping("/make-group")
  fun makeGroup() : CompletableFuture<UniqueIdentifier> {
    val party = proxy.notaryIdentities().first();
    val x = proxy.startTrackedFlowDynamic(
      StartChat::class.java,
      party
    );
    return x.returnValue.toCompletableFuture();
  }

  @PostMapping("/list")
  fun listChats() : List<Map<String, Any>> {
    return proxy.vaultQueryBy<ChatState>().states.map {
      mapOf(
        "last_message" to it.state.data.betAmount,
        "by" to it.state.data.moderator.name.organisation,
        "time" to it.state.data.lastChange.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
        "chat_id" to it.state.data.linearId.toString()
      )
    }
  }

  @PostMapping("/add-member", consumes = ["application/json"])
  fun addMembers(
    @RequestBody body : Map<String, Any>
  ) : CompletableFuture<UniqueIdentifier> {
    val groupId = body["group_id"] as String;
    val party = body["party"] as String;
    val p = proxy.partiesFromName(party, true).first();
    return proxy.startTrackedFlowDynamic(
      AddMemberFlow::class.java,
      groupId,
      p
    ).returnValue.toCompletableFuture();
  }

  @PostMapping("/add-message", consumes = ["application/json"])
  fun addMesage(
    @RequestBody body : Map<String, Any>
  ) : CompletableFuture<Unit> {
    val groupId = body["group_id"] as String;
    val message = body["message"] as String;
    return proxy.startTrackedFlowDynamic(
      AddMessageFlow::class.java,
      groupId,
      message
    ).returnValue.toCompletableFuture();
  }

  @PostMapping("/list-messages", consumes = ["application/json"])
  fun listMessages(
    @RequestBody body : Map<String, Any>
  ) : List<Map<String, Any>> {
    val groupId = body["group_id"] as String;
    var query = LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(groupId)), status = Vault.StateStatus.ALL);
    var me = proxy.nodeInfo().legalIdentities.first();
    return proxy.vaultQueryByCriteria(query, ChatState::class.java).states.filter {
      it.state.data.betAmount != ""
    }.map {
      var sender = "";
      sender = if(it.state.data.moderator == me){
        "Me"
      }else{
        it.state.data.moderator.name.organisation;
      }
      mapOf(
        "message" to it.state.data.betAmount,
        "party" to sender,
        "time" to it.state.data.lastChange.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
      )
    };
  }
}
