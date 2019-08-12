package group.chat.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.RefClaimstateContract
import com.template.states.RefClaimState
import negotiation.contracts.ProposalState

import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
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
class AddCoverageAmmounttoClaimFlow(val groupID: String, val policyID: UniqueIdentifier) : FlowLogic<Unit>() {
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
        object VALIDATING : ProgressTracker.Step("Performing initial steps - get game state and check if they are valid")
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
    override fun call(): Unit {
        // Step 1. Validation.
        progressTracker.currentStep = VALIDATING
        val gameStateRef = this.serviceHub.vaultService.queryBy(RefClaimState::class.java, QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier(id = UUID.fromString(groupID))))).states.first()
        val gameState = gameStateRef.state.data
        val notary = this.serviceHub.networkMapCache.notaryIdentities.first()
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(policyID))
        val inputStateAndRef = serviceHub.vaultService.queryBy<ProposalState>(inputCriteria).states.single()
        val input = inputStateAndRef.state.data
        // Step 2. Building.
        progressTracker.currentStep = BUILDING
        val currentParticipants = gameState.participants.map { it.owningKey }
        val newGameState = gameState.addCoverageAmmount(input.coverage_amount)
        val txCommand = Command(RefClaimstateContract.Commands.POLICY(), currentParticipants)
        val txBuilder = TransactionBuilder(notary)
                .addInputState(gameStateRef)
                .addOutputState(newGameState)
                .addCommand(txCommand)
        txBuilder.verify(serviceHub)

        // Step 3. Sign the transaction.
        progressTracker.currentStep = SIGNING
        val playerSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Step 4. Get the counter-party (Players) signature.
        progressTracker.currentStep = COLLECTING
        val me = this.serviceHub.myInfo.legalIdentities.first()
        val participants = newGameState.participants - me
        val otherPartySessions = participants.map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(playerSignedTx, otherPartySessions.toSet()))

        // Step 6. Finalise the transaction.
        progressTracker.currentStep = FINALISING
        subFlow(FinalityFlow(fullySignedTx, otherPartySessions.toSet()))
    }

}

@InitiatedBy(AddCoverageAmmounttoClaimFlow::class)
class AddCoverageAmmounttoClaimFlowAcceptor(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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