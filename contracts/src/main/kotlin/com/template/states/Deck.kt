package com.template.states

import com.template.contracts.DeckContract
import com.template.model.Card
import com.template.model.CardRankEnum
import com.template.model.CardSuitEnum
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@BelongsToContract(DeckContract::class)
@CordaSerializable
data class Deck(val owner: AbstractParty, var index: Int = 0,
                var cards: List<Card> = emptyList(), override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {


    override val participants: List<AbstractParty> = listOf(owner)


    val signature get() = cards.joinToString(",")

    fun pop(): Card {
        return if (index < cards.size) {
            cards.get(index++)
        } else {
            throw NoSuchElementException("Deck ran out of cards")
        }
    }

    fun shuffle()  {
        val allCards: HashSet<Card> = HashSet<Card>()
        for (suit in CardSuitEnum.values()) {
            for (rank in CardRankEnum.values()) {
                allCards.add(Card(suit, rank))
            }
        }
        // Created a new Deck of Shuffled Cards
        //cards = ArrayList(52)
        cards = cards+ allCards
        Collections.shuffle(cards, Random())
    }
}
