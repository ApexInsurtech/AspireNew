package com.template.states

import com.template.contracts.ChatContract
import com.template.contracts.ParentPolicyContract
import com.template.model.Card
import com.template.model.RoundEnum
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@BelongsToContract(ParentPolicyContract::class)
@CordaSerializable
data class ParentPolicyState(
        override val linearId: UniqueIdentifier,
        val moderator: Party,
        val members: List<Party>,
        val deckIdentifier: UniqueIdentifier,
        var tableCards: List<Card>,
        var rounds: RoundEnum,
        var policyID: UniqueIdentifier,
        var winner: Party? = null,
        val lastChange: LocalDateTime = LocalDateTime.now()
) : LinearState {

    override val participants: List<Party> get() = listOf(moderator) + members

    fun addChildPolicyID(policyID: UniqueIdentifier) = copy(
           // betAmount = betAmount + amount,
            policyID = policyID,
            lastChange = LocalDateTime.now()
    )

    fun addChildPolicyMember(member: Party, policyID: UniqueIdentifier) = copy(
            members = members + member,
            policyID = policyID ,
            lastChange = LocalDateTime.now()
    )

    //TODO: Deck Signature to be included in ChatState . There should be a way for members to ensure that the deck is not tampered
    //TODO: lastchange to be updated on Rounds
}