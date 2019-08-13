package com.template.webserver

import co.paralleluniverse.fibers.Suspendable
import com.template.states.ChatState
import group.chat.flows.AddMemberFlow
import group.chat.flows.AddMessageFlow
import group.chat.flows.StartChat
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneOffset
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/chat")
class ChatController (rpc: NodeRPCConnection){
  companion object {
    private val logger = LoggerFactory.getLogger(RestController::class.java)
  }
  private val proxy = rpc.proxy

  @PostMapping("/make-group")
  fun makeGroup(): CompletableFuture<CompletableFuture<Unit>>? {
    val notary = proxy.notaryIdentities().first();
    val x = proxy.startTrackedFlowDynamic(
      StartChat::class.java,
      notary
    );
    val me = proxy.nodeInfo().legalIdentities.first().name.organisation;
    return x.returnValue.toCompletableFuture().thenApply { group_id ->
      proxy.startTrackedFlowDynamic(
        AddMessageFlow::class.java,
        group_id!!.toString(),
        "Group chat started by $me"
      ).returnValue.toCompletableFuture()
    }
  }

  @PostMapping("/available")
  fun available(): List<String> {
    val notary = proxy.notaryIdentities().first().name.organisation;
    val me = proxy.nodeInfo().legalIdentities.first().name.organisation;
    var parties = proxy.partiesFromName("", false).map { party ->
      party.name.organisation
    };
    return parties - notary - me;
  }

  @PostMapping("/list")
  fun listChats() : List<Map<String, Any>> {
    val me = proxy.nodeInfo().legalIdentities.first();
    return proxy.vaultQueryBy<ChatState>().states.map {
      mapOf(
        "last_message" to it.state.data.message,
        "by" to it.state.data.currentParticipants.name.organisation,
        "time" to it.state.data.lastChange.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
        "chat_id" to it.state.data.linearId.toString(),
        "allow_add" to (it.state.data.moderator == me)
      )
    }
  }

  @PostMapping("/add-member", consumes = ["application/json"])
  fun addMembers(
    @RequestBody body : Map<String, Any>
  ) : Unit {
    val groupId = body["group_id"] as String;
    val party = body["party"] as String;
    val p = proxy.partiesFromName(party, true).first();
    val me = proxy.nodeInfo().legalIdentities.first();
    proxy.startTrackedFlowDynamic(
      AddMemberFlow::class.java,
      groupId,
      p
    ).returnValue.toCompletableFuture();
    proxy.startTrackedFlowDynamic(
      AddMessageFlow::class.java,
      groupId,
      me.name.organisation + " added " + p.name.organisation+" to chat"
    ).returnValue.toCompletableFuture();
  }

  @PostMapping("/add-message", consumes = ["application/json"])
  fun addMessage(
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
      it.state.data.message != ""
    }.map {
      var sender = "";
      sender = if(it.state.data.currentParticipants == me){
        "Me"
      }else{
        it.state.data.currentParticipants.name.organisation;
      }
      mapOf(
        "message" to it.state.data.message,
        "party" to sender,
        "time" to it.state.data.lastChange.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
      )
    };
  }
}
