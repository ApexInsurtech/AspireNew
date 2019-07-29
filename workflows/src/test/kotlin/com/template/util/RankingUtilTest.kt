package com.template.util


import com.template.model.Card
import com.template.model.CardRankEnum.*
import com.template.model.CardSuitEnum.*
import com.template.model.RankingEnum.*
import com.template.states.MemberState
import junit.framework.TestCase
import net.corda.core.identity.Party
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito
import java.util.*

//TODO Replace TestCase with AssertK
class RankingUtilTest {
    /*
	 * 	01) ROYAL_FLUSH,
	 *	02) STRAIGHT_FLUSH,
	 *	03) FOUR_OF_A_KIND,
	 *	04) FULL_HOUSE,
	 *	05) FLUSH,
	 *	06) STRAIGHT,
	 *	07) THREE_OF_A_KIND,
	 *	08) TWO_PAIR,
	 *	09) ONE_PAIR,
	 *	10) HIGH_CARD
	 */

    val mockParty = Mockito.mock(Party::class.java)

    @Test
    fun testCheckRoyalFlush() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setRoyalFlush(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(ROYAL_FLUSH, player.rankingEnum)
        assertEquals(RankingUtil.getRoyalFlush(player, tableCards), player
                .highCardRankingList)
        assertEquals(ROYAL_FLUSH.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckStraightFlush() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setStraightFlush(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(STRAIGHT_FLUSH, player.rankingEnum)
        assertEquals(RankingUtil.getStraightFlush(player, tableCards), player
                .highCardRankingList)
        assertEquals(STRAIGHT_FLUSH.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckFourOfAKind() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setFourOfAKind(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(FOUR_OF_A_KIND, player.rankingEnum)
        assertEquals(RankingUtil.getFourOfAKind(player, tableCards), player
                .highCardRankingList)
        assertEquals(FOUR_OF_A_KIND.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckFullHouse() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setFullHouse(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(FULL_HOUSE, player.rankingEnum)
        assertEquals(RankingUtil.getFullHouse(player, tableCards), player
                .highCardRankingList)
        assertEquals(FULL_HOUSE.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckFlush() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setFlush(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(FLUSH, player.rankingEnum)
        assertEquals(RankingUtil.getFlush(player, tableCards), player
                .highCardRankingList)
        assertEquals(FLUSH.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckStraight() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setStraight(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(STRAIGHT, player.rankingEnum)
        assertEquals(RankingUtil.getStraight(player, tableCards), player
                .highCardRankingList)
        assertEquals(STRAIGHT.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckThreeOfAKind() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setThreeOfAKind(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(THREE_OF_A_KIND, player.rankingEnum)
        assertEquals(RankingUtil.getThreeOfAKind(player, tableCards), player
                .highCardRankingList)
        assertEquals(THREE_OF_A_KIND.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckTwoPair() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setTwoPair(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(TWO_PAIR, player.rankingEnum)
        assertEquals(RankingUtil.getTwoPair(player, tableCards), player
                .highCardRankingList)
        assertEquals(TWO_PAIR.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckOnePair() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setOnePair(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(ONE_PAIR, player.rankingEnum)
        assertEquals(RankingUtil.getOnePair(player, tableCards), player
                .highCardRankingList)
        assertEquals(ONE_PAIR.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testCheckHighCard() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setHighCard(player, tableCards)
        RankingUtil.checkRanking(player, tableCards)
        assertEquals(HIGH_CARD, player.rankingEnum)
        assertEquals(RankingUtil.getHighCard(player, tableCards), player
                .highCardRankingList.get(0))
        assertEquals(HIGH_CARD.ordinal, RankingUtil
                .getRankingToInt(player))
    }

    @Test
    fun testIsRoyalFlush() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setRoyalFlush(player, tableCards)

        val royalFlushList = ArrayList<Card>()
        royalFlushList.addAll(tableCards)
        royalFlushList.add(player.myCards[0])
        royalFlushList.add(player.myCards[1])

        val result = RankingUtil.getRoyalFlush(player, tableCards)
        TestCase.assertTrue(isSameCardList(royalFlushList, result!!)!!)
    }

    @Test
    fun testIsRoyalFlushNotSequence() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(CLUBS, JACK), Card(CLUBS, CARD_10))

        tableCards.add(Card(CLUBS, QUEEN))
        tableCards.add(Card(CLUBS, ACE))
        tableCards.add(Card(CLUBS, KING))

        val royalFlushList = ArrayList<Card>()
        royalFlushList.addAll(tableCards)
        royalFlushList.add(player.myCards[0])
        royalFlushList.add(player.myCards[1])

        val result = RankingUtil.getRoyalFlush(player, tableCards)
        TestCase.assertTrue(isSameCardList(royalFlushList, result!!)!!)
    }

    @Test
    fun testIsNotRoyalFlushNotSameSuit() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(CLUBS, ACE), Card(HEARTS, CARD_10))

        tableCards.add(Card(CLUBS, CARD_2))
        tableCards.add(Card(SPADES, CARD_3))
        tableCards.add(Card(SPADES, CARD_4))

        assertNull(RankingUtil.getRoyalFlush(player, tableCards))
    }

    @Test
    fun testIsStraightFlush() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setStraightFlush(player, tableCards)

        val straightFlushList = ArrayList<Card>()
        straightFlushList.addAll(tableCards)
        straightFlushList.add(player.myCards[0])
        straightFlushList.add(player.myCards[1])

        val result = RankingUtil.getStraightFlush(player, tableCards)
        TestCase.assertTrue(isSameCardList(straightFlushList, result!!)!!)
    }

    @Test
    fun testIsStraightFlushNotSequence() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(CLUBS, CARD_2), Card(CLUBS, CARD_3))

        tableCards.add(Card(CLUBS, CARD_4))
        tableCards.add(Card(CLUBS, CARD_8))
        tableCards.add(Card(CLUBS, CARD_6))

        assertNull(RankingUtil.getStraightFlush(player, tableCards))
    }

    @Test
    fun testIsNotStraightFlushNoSameSuit() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(CLUBS, CARD_2), Card(CLUBS, CARD_3))

        tableCards.add(Card(CLUBS, CARD_4))
        tableCards.add(Card(CLUBS, CARD_5))
        tableCards.add(Card(DIAMONDS, CARD_6))

        assertNull(RankingUtil.getStraightFlush(player, tableCards))
    }

    @Test
    fun testIsFourOfAKind() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setFourOfAKind(player, tableCards)

        val fourOfAKindList = ArrayList<Card>()
        fourOfAKindList.add(tableCards[0])
        fourOfAKindList.add(tableCards[2])
        fourOfAKindList.add(player.myCards[0])
        fourOfAKindList.add(player.myCards[1])

        val result = RankingUtil.getFourOfAKind(player, tableCards)
        TestCase.assertTrue(isSameCardList(fourOfAKindList, result!!)!!)
    }

    @Test
    fun testIsNotFourOfAKind() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(DIAMONDS, CARD_10), Card(CLUBS, CARD_10))

        tableCards.add(Card(CLUBS, CARD_10))
        tableCards.add(Card(HEARTS, KING))
        tableCards.add(Card(CLUBS, ACE))

        assertNull(RankingUtil.getFourOfAKind(player, tableCards))
    }

    @Test
    fun testIsFullHouse() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setFullHouse(player, tableCards)

        val fullHouseList = ArrayList<Card>()
        fullHouseList.add(player.myCards[0])
        fullHouseList.add(tableCards[1])
        fullHouseList.add(tableCards[2])
        fullHouseList.add(player.myCards[1])
        fullHouseList.add(tableCards[0])

        val result = RankingUtil.getFullHouse(player, tableCards)
        TestCase.assertTrue(isSameCardList(fullHouseList, result!!)!!)
    }

    @Test
    fun testIsNotFullHouse() {
        val cardThree1 = Card(CLUBS, CARD_10)
        val cardThree2 = Card(HEARTS, ACE)
        val cardThree3 = Card(CLUBS, CARD_10)

        val cardTwo1 = Card(CLUBS, JACK)
        val cardTwo2 = Card(HEARTS, JACK)

        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(cardThree3, cardTwo2)

        tableCards.add(cardTwo1)
        tableCards.add(cardThree2)
        tableCards.add(cardThree1)

        val result = RankingUtil.getFullHouse(player, tableCards)
        TestCase.assertNull(result)
    }

    @Test
    fun testIsFlush() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setFlush(player, tableCards)

        val flushList = ArrayList<Card>()
        flushList.addAll(tableCards)
        flushList.add(player.myCards[0])
        flushList.add(player.myCards[1])

        val result = RankingUtil.getFlush(player, tableCards)
        TestCase.assertTrue(isSameCardList(flushList, result!!)!!)
    }

    @Test
    fun testIsNotFlush() {
        val tableCards = ArrayList<Card>()
        val memberState: MemberState = MemberState(party = mockParty, moderator = mockParty)
        memberState.myCards = listOf(Card(CLUBS, CARD_10), Card(CLUBS, CARD_10))
        tableCards.add(Card(HEARTS, CARD_2))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(CLUBS, ACE))

        assertNull(RankingUtil.getFlush(memberState, tableCards))
    }

    @Test
    fun testIsStraight() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setStraight(player, tableCards)

        val straightList = ArrayList<Card>()
        straightList.addAll(tableCards)
        straightList.add(player.myCards[0])
        straightList.add(player.myCards[1])

        val result = RankingUtil.getStraight(player, tableCards)
        TestCase.assertTrue(isSameCardList(straightList, result!!)!!)
    }

    @Test
    fun testIsNotStraight() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(DIAMONDS, CARD_2), Card(CLUBS, CARD_3))

        tableCards.add(Card(CLUBS, CARD_8))
        tableCards.add(Card(HEARTS, CARD_2))
        tableCards.add(Card(SPADES, CARD_6))

        assertNull(RankingUtil.getStraight(player, tableCards))
    }

    @Test
    fun testIsThreeOfAKind() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setThreeOfAKind(player, tableCards)

        val listThreeOfAKind = ArrayList<Card>()

        listThreeOfAKind.add(tableCards[1])
        listThreeOfAKind.add(player.myCards[0])
        listThreeOfAKind.add(player.myCards[1])

        val result = RankingUtil.getThreeOfAKind(player, tableCards)
        TestCase.assertTrue(isSameCardList(listThreeOfAKind, result!!)!!)
    }

    @Test
    fun testIsNotThreeOfAKind() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(DIAMONDS, CARD_10), Card(CLUBS, CARD_10))

        tableCards.add(Card(CLUBS, CARD_2))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(CLUBS, ACE))

        assertNull(RankingUtil.getThreeOfAKind(player, tableCards))
    }

    @Test
    fun testIsTwoPair() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setTwoPair(player, tableCards)

        val fullHouseList = ArrayList<Card>()
        fullHouseList.add(player.myCards[0])
        fullHouseList.add(tableCards[0])
        fullHouseList.add(player.myCards[1])
        fullHouseList.add(tableCards[1])

        val result = RankingUtil.getTwoPair(player, tableCards)
        TestCase.assertTrue(isSameCardList(fullHouseList, result!!)!!)
    }

    @Test
    fun testIsNotTwoPair() {
        val cardThree1 = Card(CLUBS, CARD_10)
        val cardThree2 = Card(HEARTS, CARD_10)
        val cardThree3 = Card(SPADES, CARD_10)

        val cardTwo1 = Card(CLUBS, JACK)
        val cardTwo2 = Card(HEARTS, JACK)

        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(cardThree3, cardTwo2)

        tableCards.add(cardTwo1)
        tableCards.add(cardThree2)
        tableCards.add(cardThree1)

        val result = RankingUtil.getTwoPair(player, tableCards)
        TestCase.assertNull(result)
    }

    @Test
    fun testIsOnePair() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        setOnePair(player, tableCards)

        val listOnePair = ArrayList<Card>()
        listOnePair.add(player.myCards[0])
        listOnePair.add(player.myCards[1])

        val result = RankingUtil.getOnePair(player, tableCards)
        TestCase.assertTrue(isSameCardList(listOnePair, result!!)!!)
    }

    @Test
    fun testIsNotOnePair() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(DIAMONDS, CARD_2), Card(CLUBS, CARD_10))

        tableCards.add(Card(CLUBS, CARD_3))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(CLUBS, ACE))

        assertNull(RankingUtil.getOnePair(player, tableCards))
    }

    @Test
    fun testGetHighCardRepeatedCards() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        val fourCard = Card(CLUBS, CARD_4)
        player.myCards = listOf(fourCard, fourCard)

        tableCards.add(fourCard)
        tableCards.add(fourCard)
        tableCards.add(Card(CLUBS, CARD_2))

        assertEquals(fourCard, RankingUtil.getHighCard(player, tableCards))
    }

    @Test
    fun testGetHighCardAce() {
        val tableCards = ArrayList<Card>()
        val player = MemberState(party = mockParty, moderator = mockParty)
        player.myCards = listOf(Card(HEARTS, CARD_9), Card(SPADES, CARD_7))

        val aceCard = Card(CLUBS, ACE)
        tableCards.add(aceCard)
        assertEquals(aceCard, RankingUtil.getHighCard(player, tableCards))
    }

    fun isSameCardList(list1: List<Card>, list2: List<Card>): Boolean? {
        return list1.containsAll(list2) && list1.size == list2.size
    }

    private fun setRoyalFlush(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(CLUBS, CARD_10), Card(CLUBS, JACK))

        tableCards.add(Card(CLUBS, QUEEN))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(CLUBS, ACE))
    }

    private fun setStraightFlush(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(CLUBS, CARD_2), Card(CLUBS, CARD_3))

        tableCards.add(Card(CLUBS, CARD_4))
        tableCards.add(Card(CLUBS, CARD_5))
        tableCards.add(Card(CLUBS, CARD_6))
    }

    private fun setFourOfAKind(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(DIAMONDS, CARD_10), Card(CLUBS, CARD_10))

        tableCards.add(Card(CLUBS, CARD_10))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(HEARTS, CARD_10))
    }

    private fun setFullHouse(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(CLUBS, CARD_10), Card(HEARTS, JACK))

        tableCards.add(Card(CLUBS, JACK))
        tableCards.add(Card(HEARTS, CARD_10))
        tableCards.add(Card(CLUBS, CARD_10))
    }

    private fun setFlush(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(CLUBS, CARD_10), Card(CLUBS, CARD_3))

        tableCards.add(Card(CLUBS, CARD_2))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(CLUBS, ACE))
    }

    private fun setStraight(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(DIAMONDS, CARD_4), Card(CLUBS, CARD_5))

        tableCards.add(Card(CLUBS, CARD_2))
        tableCards.add(Card(HEARTS, CARD_3))
        tableCards.add(Card(SPADES, CARD_6))
    }

    private fun setThreeOfAKind(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(CLUBS, CARD_10), Card(SPADES, CARD_10))
        tableCards.add(Card(SPADES, ACE))
        tableCards.add(Card(HEARTS, CARD_10))
        tableCards.add(Card(HEARTS, CARD_2))
    }

    private fun setTwoPair(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(CLUBS, CARD_10), Card(CLUBS, JACK))

        tableCards.add(Card(SPADES, CARD_10))
        tableCards.add(Card(HEARTS, JACK))
    }

    private fun setOnePair(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(DIAMONDS, CARD_10), Card(CLUBS, CARD_10))

        tableCards.add(Card(CLUBS, CARD_2))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(CLUBS, ACE))
    }

    private fun setHighCard(memberState: MemberState, tableCards: MutableList<Card>) {
        memberState.myCards = listOf(Card(DIAMONDS, CARD_10), Card(CLUBS, CARD_9))

        tableCards.add(Card(CLUBS, CARD_2))
        tableCards.add(Card(CLUBS, KING))
        tableCards.add(Card(CLUBS, ACE))
    }
}