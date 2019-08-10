package com.template.util

import com.template.model.Card
import com.template.states.Deck
import com.template.states.MemberState
import java.util.*

object GameUtil {

    fun deal(memberStates: MutableList<MemberState>, tableCards: MutableList<Card>, deck: Deck) {
        for (player in memberStates) {
            player.myCards = listOf(deck.pop(), deck.pop())
        }
        checkPlayersRanking(memberStates, tableCards)
    }

    fun betTurn(memberStates: MutableList<MemberState>, tableCards: MutableList<Card>, deck: Deck) {
        //deck.pop()
        tableCards.add(deck.pop())
        checkPlayersRanking(memberStates, tableCards)
    }

    fun betRiver(memberStates: MutableList<MemberState>, tableCards: MutableList<Card>, deck: Deck) {
        //deck.pop()
        tableCards.add(deck.pop())
        checkPlayersRanking(memberStates, tableCards)
    }

    /**
     * double initial bet
     */
    fun callFlop(memberStates: MutableList<MemberState>, tableCards: MutableList<Card>, deck: Deck) {
        //deck.pop()
        tableCards.add(deck.pop())
        tableCards.add(deck.pop())
        tableCards.add(deck.pop())
        checkPlayersRanking(memberStates, tableCards)
    }

    fun getWinner(memberStates: MutableList<MemberState>, tableCards: MutableList<Card>): List<MemberState> {
        checkPlayersRanking(memberStates, tableCards)
        val winnerList = ArrayList<MemberState>()
        var winner = memberStates.get(0)
        var winnerRank = RankingUtil.getRankingToInt(winner)
        winnerList.add(winner)
        for (i in 1 until memberStates.size) {
            val player = memberStates.get(i)
            val playerRank = RankingUtil.getRankingToInt(player)
            //Draw game
            if (winnerRank == playerRank) {
                var highHandPlayer = checkHighSequence(winner, player)
                //Draw checkHighSequence
                if (highHandPlayer == null) {
                    highHandPlayer = checkHighCardWinner(winner, player)
                }
                //Not draw in checkHighSequence or checkHighCardWinner
                if (highHandPlayer != null && !winner.equals(highHandPlayer)) {
                    winner = highHandPlayer
                    winnerList.clear()
                    winnerList.add(winner)
                } else if (highHandPlayer == null) {
                    //Draw in checkHighSequence and checkHighCardWinner
                    winnerList.add(winner)
                }
            } else if (winnerRank < playerRank) {
                winner = player
                winnerList.clear()
                winnerList.add(winner)
            }
            winnerRank = RankingUtil.getRankingToInt(winner)
        }

        return winnerList
    }

    private fun checkHighSequence(memberState1: MemberState, memberState2: MemberState): MemberState? {
        val player1Rank = sumRankingList(memberState1)
        val player2Rank = sumRankingList(memberState2)
        if (player1Rank > player2Rank) {
            return memberState1
        } else if (player1Rank < player2Rank) {
            return memberState2
        }
        return null
    }

    private fun checkHighCardWinner(memberState1: MemberState, memberState2: MemberState): MemberState? {
        var winner = compareHighCard(memberState1, memberState1.highCard!!,
                memberState2, memberState2.highCard!!)
        if (winner == null) {
            var player1Card = RankingUtil.getHighCard(memberState1,
                    emptyList<Card>())
            var player2Card = RankingUtil.getHighCard(memberState2,
                    emptyList<Card>())
            winner = compareHighCard(memberState1, player1Card, memberState2, player2Card)
            if (winner != null) {
                memberState1.highCard = player1Card
                memberState2.highCard = player2Card
            } else {
                player1Card = getSecondHighCard(memberState1, player1Card)
                player2Card = getSecondHighCard(memberState2, player2Card)
                winner = compareHighCard(memberState1, player1Card, memberState2,
                        player2Card)
                if (winner != null) {
                    memberState1.highCard = player1Card
                    memberState2.highCard = player2Card
                }
            }
        }
        return winner
    }

    private fun checkPlayersRanking(memberStates: MutableList<MemberState>, tableCards: MutableList<Card>) {
        for (player in memberStates) {
            RankingUtil.checkRanking(player, tableCards)
        }
    }

    /*
	 * TODO This method must be moved to RankingUtil
	 */
    private fun sumRankingList(memberState: MemberState): Int {
        var sum: Int = 0
        for (card in memberState.highCardRankingList) {
            sum += card.getRankToInt()
        }
        return sum
    }

    /*
	 * TODO This method must be moved to RankingUtil
	 */
    private fun getSecondHighCard(memberState: MemberState, card: Card): Card {
        return if (memberState.myCards[0].equals(card)) {
            memberState.myCards[1]
        } else memberState.myCards[0]
    }

    private fun compareHighCard(memberState1: MemberState, player1HighCard: Card,
                                memberState2: MemberState, player2HighCard: Card): MemberState? {
        if (player1HighCard.getRankToInt() > player2HighCard.getRankToInt()) {
            return memberState1
        } else if (player1HighCard.getRankToInt() < player2HighCard
                        .getRankToInt()) {
            return memberState2
        }
        return null
    }


}