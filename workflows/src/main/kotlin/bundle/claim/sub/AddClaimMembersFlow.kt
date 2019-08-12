package bundle.claim.sub.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.RefClaimstateContract


import com.template.states.RefClaimState
import com.template.states.RefClaimStateMember


import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class AddClaimMembersFlow(val groupID: String, val member: Party) : FlowLogic<UniqueIdentifier>() {
    /**
     * Tracks progress throughout the flows call execution.
     */
    override val progressTracker: ProgressTracker
        get() {
            return ProgressTracker(
                    VALIDATING,
                    BUILDING,
                    SIGNING,
                    COLLECTING,
                    FINALISING
            )
        }

    companion object {
        object VALIDATING : ProgressTracker.Step("Performing initial steps - get game state, player and check if they are valid")
        object BUILDING : ProgressTracker.Step("Building and verifying transaction")
        object SIGNING : ProgressTracker.Step("Dealer Signing transaction.")
        object COLLECTING : ProgressTracker.Step("Collecting signatures from the dealer and other players.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING : ProgressTracker.Step("Finalising transaction. - Full Final signature on the vault") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(VALIDATING, BUILDING, SIGNING, COLLECTING, FINALISING)
    }

    @Suspendable
    override fun call(): UniqueIdentifier {
        // Step 1. Validation.
        progressTracker.currentStep = VALIDATING
        val gameStateRef = this.serviceHub.vaultService.queryBy(RefClaimState::class.java, QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier(id = UUID.fromString(groupID))))).states.first()
        val gameState = gameStateRef.state.data
        val memberStateState: RefClaimStateMember = RefClaimStateMember(party = member, moderator = gameState.moderator)
        val notary = this.serviceHub.networkMapCache.notaryIdentities.first()

        // Step 2. Building.
        progressTracker.currentStep = BUILDING
        val newGameState = gameState.addRefClaimMember(member)
       // val newGameState_policy = gameState.addChildPolicyID(policyID)
        val currentParticipants = gameState.participants.map { it.owningKey } + member.owningKey
        val txCommand = Command(RefClaimstateContract.Commands.ADD_PLAYER(), currentParticipants)
        val txBuilder = TransactionBuilder(notary)
                .addInputState(gameStateRef)
                .addOutputState(newGameState)
                .addOutputState(memberStateState)
                .addCommand(txCommand)
        //  .setTimeWindow(serviceHub.clock.instant(), 5.minutes)
        txBuilder.verify(serviceHub)

        /*val txBuilder_policy = TransactionBuilder(notary)
                .addInputState(gameStateRef)
                .addOutputState(newGameState_policy)
                .addOutputState(memberStateState)
                .addCommand(txCommand)
        //  .setTimeWindow(serviceHub.clock.instant(), 5.minutes)
        txBuilder_policy.verify(serviceHub)*/

        // Step 3. Sign the transaction.
        progressTracker.currentStep = SIGNING
        val dealerSignedTx = serviceHub.signInitialTransaction(txBuilder)
       // val dealerSignedTx_policy = serviceHub.signInitialTransaction(txBuilder_policy)

        // Step 4. Get the counter-party (Players) signature.
        progressTracker.currentStep = COLLECTING
        val otherPartySessions = newGameState.members.map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(dealerSignedTx, otherPartySessions.toSet()))
        //val fullySignedTx_policy = subFlow(CollectSignaturesFlow(dealerSignedTx_policy, otherPartySessions.toSet()))

        // Step 6. Finalise the transaction.
        progressTracker.currentStep = FINALISING
        subFlow(FinalityFlow(fullySignedTx, otherPartySessions.toSet()))
       // subFlow(FinalityFlow(fullySignedTx_policy, otherPartySessions.toSet()))
        return memberStateState.linearId
    }

}

@InitiatedBy(AddClaimMembersFlow::class)
class AddClaimMembersFlowAcceptor(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //TODO
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
    }
}