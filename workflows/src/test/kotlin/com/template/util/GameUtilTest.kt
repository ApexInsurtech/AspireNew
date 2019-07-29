package com.template.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.template.model.Card
import com.template.model.CardRankEnum.*
import com.template.model.CardSuitEnum.*
import com.template.model.RankingEnum.*
import com.template.states.Deck
import com.template.states.MemberState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.mock

class GameUtilTest {
    val mockParty = mock(Party::class.java)

    var mockUniqueIdentifier = mock(UniqueIdentifier::class.java)

    @Test
    fun testDrawGameTwoPlayers() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()
        tableCards.add(Card(SPADES, CARD_10))
        tableCards.add(Card(SPADES, ACE))
        playerA.myCards = listOf(Card(DIAMONDS, CARD_2), Card(SPADES, CARD_2))
        playerB.myCards = listOf(Card(CLUBS, CARD_2), Card(HEARTS, CARD_2))
        val winnerList = game.getWinner(playerList, tableCards)
        assertThat(winnerList.size).isEqualTo(2)
        assertThat(playerA.rankingEnum).isEqualTo(ONE_PAIR)
        assertThat(playerB.rankingEnum).isEqualTo(ONE_PAIR)
    }

    @Ignore
    fun testPlayerAWinDrawGameBestRanking() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()
        tableCards.add(Card(SPADES, CARD_10))
        tableCards.add(Card(SPADES, ACE))
        playerA.myCards = listOf(Card(CLUBS, CARD_10), Card(HEARTS, CARD_2))
        playerB.myCards = listOf(Card(CLUBS, CARD_2), Card(HEARTS, ACE))
        val winnerList = game.getWinner(playerList, tableCards)
        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerA).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(ONE_PAIR)
        assertThat(playerB.rankingEnum).isEqualTo(ONE_PAIR)
    }

    @Ignore
    fun testPlayerBWinDrawGameHighCard() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()

        tableCards.add(Card(SPADES, CARD_10))
        playerA.myCards = listOf(Card(CLUBS, CARD_10), Card(HEARTS, ACE))
        playerB.myCards = listOf(Card(CLUBS, CARD_2), Card(HEARTS, CARD_10))

        val winnerList = game.getWinner(playerList, tableCards)
        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerB).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(ONE_PAIR)
        assertThat(playerB.rankingEnum).isEqualTo(ONE_PAIR)
    }

    @Test
    fun testPlayerAWinStraighFlush() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()

        tableCards.add(Card(SPADES, CARD_3))
        tableCards.add(Card(SPADES, CARD_4))
        tableCards.add(Card(SPADES, CARD_5))
        tableCards.add(Card(CLUBS, QUEEN))

        playerA.myCards = listOf(Card(SPADES, CARD_6), Card(SPADES, CARD_7))
        playerB.myCards = listOf(Card(SPADES, CARD_10), Card(DIAMONDS, CARD_10))

        val winnerList = game.getWinner(playerList, tableCards)
        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerA).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(STRAIGHT_FLUSH)
        assertThat(playerB.rankingEnum).isEqualTo(ONE_PAIR)
    }

    @Ignore
    fun testPlayerBWinFourOfAKind() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()

        tableCards.add(Card(SPADES, CARD_3))
        tableCards.add(Card(SPADES, CARD_4))
        tableCards.add(Card(HEARTS, CARD_10))
        tableCards.add(Card(CLUBS, CARD_10))

        playerA.myCards = listOf(Card(SPADES, ACE), Card(SPADES, CARD_2))
        playerB.myCards = listOf(Card(SPADES, CARD_10), Card(DIAMONDS, CARD_10))

        val winnerList = game.getWinner(playerList, tableCards)

        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerA).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(ONE_PAIR)
        assertThat(playerB.rankingEnum).isEqualTo(FOUR_OF_A_KIND)
    }

    @Test
    fun testPlayerAWinFullHouse() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()

        tableCards.add(Card(SPADES, CARD_3))
        tableCards.add(Card(SPADES, CARD_4))
        tableCards.add(Card(HEARTS, CARD_10))
        tableCards.add(Card(CLUBS, CARD_10))

        playerA.myCards = listOf(Card(HEARTS, CARD_3), Card(CLUBS, CARD_3))
        playerB.myCards = listOf(Card(SPADES, CARD_10), Card(SPADES, CARD_2))

        val winnerList = game.getWinner(playerList, tableCards)

        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerA).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(FULL_HOUSE)
        assertThat(playerB.rankingEnum).isEqualTo(THREE_OF_A_KIND)
    }

    @Ignore
    fun testPlayerBWinFlush() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()

        tableCards.add(Card(SPADES, CARD_3))
        tableCards.add(Card(SPADES, CARD_4))
        tableCards.add(Card(SPADES, CARD_7))

        playerA.myCards = listOf(Card(HEARTS, CARD_5), Card(CLUBS, CARD_6))
        playerB.myCards = listOf(Card(SPADES, CARD_10), Card(SPADES, CARD_2))

        val winnerList = game.getWinner(playerList, tableCards)

        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerA).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(STRAIGHT)
        assertThat(playerB.rankingEnum).isEqualTo(FLUSH)
    }

    @Test
    fun testPlayerAWinStraight() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()

        tableCards.add(Card(SPADES, CARD_3))
        tableCards.add(Card(SPADES, CARD_4))
        tableCards.add(Card(SPADES, CARD_7))

        playerA.myCards = listOf(Card(HEARTS, CARD_5), Card(CLUBS, CARD_6))
        playerB.myCards = listOf(Card(CLUBS, CARD_10), Card(HEARTS, CARD_10))

        val winnerList = game.getWinner(playerList, tableCards)

        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerA).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(STRAIGHT)
        assertThat(playerB.rankingEnum).isEqualTo(ONE_PAIR)
    }

    @Test
    fun testPlayerAWinThreeOfAKind() {
        //Basic tests
        val game = GameUtil
        val playerA = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerB = MemberState(mockUniqueIdentifier, mockParty, mockParty)
        val playerList = mutableListOf<MemberState>(playerA, playerB)
        val dealer = mockParty
        val tableCards = mutableListOf<Card>()
        val deck: Deck = Deck(dealer)
        deck.shuffle()

        game.deal(playerList, tableCards, deck)
        game.callFlop(playerList, tableCards, deck)
        game.betRiver(playerList, tableCards, deck)
        game.betTurn(playerList, tableCards, deck)
        tableCards.clear()

        tableCards.add(Card(SPADES, CARD_10))
        tableCards.add(Card(SPADES, ACE))

        playerA.myCards = listOf(Card(HEARTS, ACE), Card(CLUBS, ACE))
        playerB.myCards = listOf(Card(CLUBS, CARD_6), Card(HEARTS, QUEEN))

        val winnerList = game.getWinner(playerList, tableCards)

        assertThat(winnerList.size).isEqualTo(1)
        assertThat(playerA).isEqualTo(winnerList.get(0))
        assertThat(playerA.rankingEnum).isEqualTo(THREE_OF_A_KIND)
        assertThat(playerB.rankingEnum).isEqualTo(HIGH_CARD)
    }

}