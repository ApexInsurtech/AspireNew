package com.template.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.template.states.ChatState
import group.chat.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

class AddMessageFlowTests {
    companion object {
        val log = loggerFor<AddMessageFlowTests>()
    }

    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.poker.contracts"),
            TestCordapp.findCordapp("com.poker.flows")
    )))
    private val moderator = network.createNode()
    private val memberA = network.createNode()
    private val memberB = network.createNode()

    init {
        listOf(memberA, memberB, moderator).forEach {
            it.registerInitiatedFlow(AddPlayerAcceptor::class.java)
//            it.registerInitiatedFlow(PlayFlowResponder::class.java)
            it.registerInitiatedFlow(AcceptChatFlow::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Add a Betting amount should add the value in game state`() {
        val betAmount = ""
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = moderator.startFlow(StartChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()

        moderator.startFlow(AddMemberFlow(gameUID.id.toString(), memberA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        moderator.startFlow(AddMemberFlow(gameUID.id.toString(), memberB.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
//        moderator.startFlow(PlayFLow(gameUID.toString(), RoundEnum.Dealt.name)).toCompletableFuture()
//        network.runNetwork()
        memberA.startFlow(AddMessageFlow(gameUID.toString(), betAmount)).toCompletableFuture()
        network.runNetwork()

        val moderatorAVault = moderator.services.vaultService.queryBy<ChatState>()
        val moderatorStateAndRef = moderatorAVault.states.first()
        val gameState = moderatorStateAndRef.state.data
        assertThat(gameState.betAmount).isEqualTo(betAmount)
    }
}