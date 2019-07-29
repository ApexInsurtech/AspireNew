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
        val moderator: Party,
        val members: List<Party>,
        val deckIdentifier: UniqueIdentifier,
        var tableCards: List<Card>,
        var rounds: RoundEnum,
        var betAmount: String,
        var winner: Party? = null,
        val lastChange: LocalDateTime = LocalDateTime.now()
) : LinearState {

    override val participants: List<Party> get() = listOf(moderator) + members

    fun addBetAmount(amount: String) = copy(
           // betAmount = betAmount + amount,
            betAmount = amount,
            lastChange = LocalDateTime.now()
    )

    fun addPlayer(member: Party) = copy(
            members = members + member,
            lastChange = LocalDateTime.now()
    )
    //TODO: Deck Signature to be included in ChatState . There should be a way for members to ensure that the deck is not tampered
    //TODO: lastchange to be updated on Rounds
}