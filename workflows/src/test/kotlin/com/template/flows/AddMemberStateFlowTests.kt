package com.template.flows

import com.template.states.MemberState
import group.chat.flows.AddMemberFlow
import group.chat.flows.AddPlayerAcceptor
import group.chat.flows.StartChat
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AddMemberStateFlowTests {
    companion object {
        val log = loggerFor<AddMemberStateFlowTests>()
    }

    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.poker.contracts"),
            TestCordapp.findCordapp("com.poker.flows")
    )))
    private val moderator = network.createNode()
    private val memberA = network.createNode()

    init {
        listOf(memberA, moderator).forEach {
            it.registerInitiatedFlow(AddPlayerAcceptor::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Add member should return a UID and all its state values are initialized`() {
        val notaryNode = network.defaultNotaryNode.info.legalIdentities.first()
        val startGameFlow = moderator.startFlow(StartChat(notaryNode)).toCompletableFuture()
        network.runNetwork()
        val gameUID = startGameFlow.getOrThrow()
        log.info("game id: $gameUID")
        assertNotNull(gameUID.id)

        val addPlayerFlow = moderator.startFlow(AddMemberFlow(gameUID.id.toString(), memberA.info.legalIdentities.first())).toCompletableFuture()
        network.runNetwork()
        val memberUID = addPlayerFlow.getOrThrow()
        assertNotNull(memberUID.id)
        val memberVault = memberA.services.vaultService.queryBy<MemberState>()
        assertTrue(memberVault.states.size == 1)
        val stateAndRef = memberVault.states.first()
        assertTrue(stateAndRef.state.notary == notaryNode)
        val memberState = stateAndRef.state.data
        assertTrue(memberState.myCards.isEmpty())
        assertTrue(memberState.participants.size == 2)
    }
}