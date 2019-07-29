package com.template.util

import com.template.model.Card
import com.template.model.CardRankEnum
import com.template.model.CardRankEnum.*
import com.template.model.RankingEnum
import com.template.model.RankingEnum.*
import com.template.states.MemberState
import java.util.*

/*
ROYAL_FLUSH,
STRAIGHT_FLUSH,
FOUR_OF_A_KIND,
FULL_HOUSE,
FLUSH,
STRAIGHT,
THREE_OF_A_KIND,
TWO_PAIR,
ONE_PAIR,
HIGH_CARD
*/
object RankingUtil {

    fun getRankingToInt(memberState: MemberState): Int {
        return memberState.rankingEnum.ordinal
    }

    fun checkRanking(memberState: MemberState, tableCards: List<Card>) {

        //HIGH_CARD
        val highCard = getHighCard(memberState, tableCards)
        memberState.highCard = highCard

        //ROYAL_FLUSH
        var rankingList = getRoyalFlush(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, ROYAL_FLUSH, rankingList)
            return
        }
        //STRAIGHT_FLUSH
        rankingList = getStraightFlush(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, STRAIGHT_FLUSH,
                    rankingList)
            return
        }
        //FOUR_OF_A_KIND
        rankingList = getFourOfAKind(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, FOUR_OF_A_KIND,
                    rankingList)
            return
        }
        //FULL_HOUSE
        rankingList = getFullHouse(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, FULL_HOUSE, rankingList)
            return
        }
        //FLUSH
        rankingList = getFlush(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, FLUSH, rankingList)
            return
        }
        //STRAIGHT
        rankingList = getStraight(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, STRAIGHT, rankingList)
            return
        }
        //THREE_OF_A_KIND
        rankingList = getThreeOfAKind(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, THREE_OF_A_KIND,
                    rankingList)
            return
        }
        //TWO_PAIR
        rankingList = getTwoPair(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, TWO_PAIR, rankingList)
            return
        }
        //ONE_PAIR
        rankingList = getOnePair(memberState, tableCards)
        if (rankingList != null) {
            setRankingEnumAndList(memberState, ONE_PAIR, rankingList)
            return
        }
        //HIGH_CARD
        memberState.rankingEnum = HIGH_CARD
        val highCardRankingList = ArrayList<Card>()
        highCardRankingList.add(highCard)
        memberState.highCardRankingList = highCardRankingList
        return
    }

    fun getRoyalFlush(memberState: MemberState, tableCards: List<Card>): List<Card>? {
        if (!isSameSuit(memberState, tableCards)) {
            return null
        }

        val rankEnumList = toRankEnumList(memberState, tableCards)

        return if (rankEnumList.contains(CARD_10)
                && rankEnumList.contains(JACK)
                && rankEnumList.contains(QUEEN)
                && rankEnumList.contains(KING)
                && rankEnumList.contains(ACE)) {

            getMergedCardList(memberState, tableCards)
        } else null

    }

    fun getStraightFlush(memberState: MemberState,
                         tableCards: List<Card>): List<Card>? {
        return getSequence(memberState, tableCards, 5, true)
    }

    fun getFourOfAKind(memberState: MemberState,
                       tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(memberState, tableCards)
        return checkPair(mergedList, 4)
    }

    fun getFullHouse(memberState: MemberState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(memberState, tableCards)
        val threeList = checkPair(mergedList, 3)
        if (threeList != null) {
            mergedList.removeAll(threeList)
            val twoList = checkPair(mergedList, 2)
            if (twoList != null) {
                threeList.addAll(twoList)
                return threeList
            }
        }
        return null
    }

    fun getFlush(memberState: MemberState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(memberState, tableCards)
        val flushList = ArrayList<Card>()

        for (card1 in mergedList) {
            for (card2 in mergedList) {
                if (card1.suit.equals(card2.suit)) {
                    if (!flushList.contains(card1)) {
                        flushList.add(card1)
                    }
                    if (!flushList.contains(card2)) {
                        flushList.add(card2)
                    }
                }
            }
            if (flushList.size == 5) {
                return flushList
            }
            flushList.clear()
        }
        return null
    }

    //S‹o 5 cartas seguidas de naipes diferentes, caso empate ganha aquele com a maior sequ?ncia.
    fun getStraight(memberState: MemberState, tableCards: List<Card>): List<Card>? {
        return getSequence(memberState, tableCards, 5, false)
    }

    fun getThreeOfAKind(memberState: MemberState,
                        tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(memberState, tableCards)
        return checkPair(mergedList, 3)
    }

    fun getTwoPair(memberState: MemberState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(memberState, tableCards)
        val twoPair1 = checkPair(mergedList, 2)
        if (twoPair1 != null) {
            mergedList.removeAll(twoPair1)
            val twoPair2 = checkPair(mergedList, 2)
            if (twoPair2 != null) {
                twoPair1.addAll(twoPair2)
                return twoPair1
            }
        }
        return null
    }

    fun getOnePair(memberState: MemberState, tableCards: List<Card>): List<Card>? {
        val mergedList = getMergedCardList(memberState, tableCards)
        return checkPair(mergedList, 2)
    }

    fun getHighCard(memberState: MemberState, tableCards: List<Card>): Card {
        val allCards = ArrayList<Card>()
        allCards.addAll(tableCards)
        allCards.add(memberState.myCards[0])
        allCards.add(memberState.myCards[1])

        var highCard = allCards[0]
        for (card in allCards) {
            if (card.getRankToInt() > highCard.getRankToInt()) {
                highCard = card
            }
        }
        return highCard
    }

    private fun getSequence(memberState: MemberState,
                            tableCards: List<Card>, sequenceSize: Int?, compareSuit: Boolean): List<Card>? {
        val orderedList = getOrderedCardList(memberState, tableCards)
        val sequenceList = ArrayList<Card>()

        var cardPrevious: Card? = null
        for (card in orderedList) {
            if (cardPrevious != null) {
                if (card.getRankToInt() - cardPrevious.getRankToInt() == 1) {
                    if ((!compareSuit) || cardPrevious.suit.equals(card.suit)) {
                        if (sequenceList.size == 0) {
                            sequenceList.add(cardPrevious)
                        }
                        sequenceList.add(card)
                    }
                } else {
                    if (sequenceList.size == sequenceSize) {
                        return sequenceList
                    }
                    sequenceList.clear()
                }
            }
            cardPrevious = card
        }

        return if (sequenceList.size == sequenceSize) sequenceList else null
    }

    private fun getMergedCardList(memberState: MemberState,
                                  tableCards: List<Card>): MutableList<Card> {
        val merged = ArrayList<Card>()
        merged.addAll(tableCards)
        merged.add(memberState.myCards[0])
        merged.add(memberState.myCards[1])
        return merged
    }

    private fun getOrderedCardList(memberState: MemberState,
                                   tableCards: List<Card>): List<Card> {
        val ordered: MutableList<Card> = getMergedCardList(memberState, tableCards)
        Collections.sort(ordered, Comparator<Card> { c1: Card, c2: Card -> if (c1.getRankToInt() < c2.getRankToInt()) -1 else 1 })
        return ordered
    }

    private fun checkPair(mergedList: List<Card>, pairSize: Int?): MutableList<Card>? {
        val checkedPair = ArrayList<Card>()
        for (card1 in mergedList) {
            checkedPair.add(card1)
            for (card2 in mergedList) {
                if (!card1.equals(card2) && card1.rank.equals(card2.rank)) {
                    checkedPair.add(card2)
                }
            }
            if (checkedPair.size == pairSize) {
                return checkedPair
            }
            checkedPair.clear()
        }
        return null
    }

    private fun isSameSuit(memberState: MemberState, tableCards: List<Card>): Boolean {
        val suit = memberState.myCards[0].suit

        if (!suit.equals(memberState.myCards[1].suit)) {
            return false
        }

        for (card in tableCards) {
            if (!card.suit.equals(suit)) {
                return false
            }
        }

        return true
    }

    private fun toRankEnumList(memberState: MemberState,
                               tableCards: List<Card>): List<CardRankEnum> {
        val rankEnumList = ArrayList<CardRankEnum>()

        for (card in tableCards) {
            rankEnumList.add(card.rank)
        }

        rankEnumList.add(memberState.myCards[0].rank)
        rankEnumList.add(memberState.myCards[1].rank)

        return rankEnumList
    }

    private fun setRankingEnumAndList(memberState: MemberState,
                                      rankingEnum: RankingEnum, rankingList: List<Card>) {
        memberState.rankingEnum = rankingEnum
        memberState.highCardRankingList = rankingList
    }
}
