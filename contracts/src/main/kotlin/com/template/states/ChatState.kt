package com.template.states

import com.template.contracts.ChatContract
import com.template.model.Card
import com.template.model.RoundEnum
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@BelongsToContract(ChatContract::class)
@CordaSerializable
data class ChatState(
        override val linearId: UniqueIdentifier,
        val initiator: Party,
        val members: List<Party>,
        var message: String,
        var by: Party,
        val added_on: LocalDateTime = LocalDateTime.now(),
        val type : Int = 0  // 0 = Message, 1 = Info
) : LinearState {

    override val participants: List<Party> get() = listOf(initiator) + members

    fun addMessage(message: String, by: Party) = copy(
        message = message,
        by = by,
        added_on = LocalDateTime.now()
    )

    fun addMember(member: Party) = copy(
      message = "Added Party Member: "+member.name.commonName!!,
      members = this.members + member,
      by = this.initiator,
      added_on = LocalDateTime.now()
    )
}