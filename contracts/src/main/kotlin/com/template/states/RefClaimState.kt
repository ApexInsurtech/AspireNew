package com.template.states


import com.template.contracts.RefClaimstateContract
import com.template.model.Card
import com.template.model.RoundEnum
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

@BelongsToContract(RefClaimstateContract::class)
@CordaSerializable
data class RefClaimState(
        override val linearId: UniqueIdentifier,
        val moderator: Party,
        val members: List<Party>,
        val deckIdentifier: UniqueIdentifier,
        var tableCards: List<Card>,
        var rounds: RoundEnum,
        var policyID: List<UniqueIdentifier>,
        var coverage_ammount_ref: List<Int>,
        var loss_amount: Int,
        var winner: Party? = null,
        val lastChange: LocalDateTime = LocalDateTime.now()
) : LinearState {

    override val participants: List<Party> get() = listOf(moderator) + members

    fun addPolicyIDtoClaim(policyIDs: UniqueIdentifier) = copy(
           // betAmount = betAmount + amount,
            policyID = policyID + policyIDs,
            lastChange = LocalDateTime.now()
    )
    fun addCoverageAmmount(coverage_ammount_refrence: Int) = copy(
            // betAmount = betAmount + amount,
            coverage_ammount_ref = coverage_ammount_ref + coverage_ammount_refrence,
            lastChange = LocalDateTime.now()
    )
    fun addRefClaimMember(member: Party) = copy(
            members = members + member,

            lastChange = LocalDateTime.now()
    )
    fun addLossAmount(loss_amounts: Int) = copy(
            loss_amount = loss_amount + loss_amounts,

            lastChange = LocalDateTime.now()
    )

    //TODO: Deck Signature to be included in ChatState . There should be a way for members to ensure that the deck is not tampered
    //TODO: lastchange to be updated on Rounds
}