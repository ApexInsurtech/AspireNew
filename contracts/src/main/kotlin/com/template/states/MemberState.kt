package com.template.states

import com.template.contracts.ChatContract
import com.template.model.Card
import com.template.model.RankingEnum
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(ChatContract::class)
@CordaSerializable
data class MemberState(
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        val party: Party,
        val moderator: Party,
        var myCards: List<Card> = emptyList<Card>(),
        var rankingEnum: RankingEnum = RankingEnum.HIGH_CARD,
        var highCard: Card? = null,
        var highCardRankingList: List<Card> = emptyList<Card>()
) : LinearState {
    override val participants: List<Party>
        get() = listOf(party, moderator)
}