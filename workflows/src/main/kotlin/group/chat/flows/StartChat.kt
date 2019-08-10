package group.chat.flows

import co.paralleluniverse.fibers.Suspendable
import com.sun.media.jfxmedia.logging.Logger
import com.template.contracts.ChatContract
import com.template.model.RoundEnum
import com.template.states.Deck
import com.template.states.ChatState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

// *********
// * Flows *
// reference: https://github.com/corda/corda/blob/master/finance/workflows/src/main/kotlin/net/corda/finance/flows/CashIssueFlow.kt
// *********
@InitiatingFlow
@StartableByRPC
class StartChat(
  val members : List<Party>
) : FlowLogic<UniqueIdentifier>() {
    /**
     * Tracks progress throughout the flows call execution.
     */
    override val progressTracker: ProgressTracker
        get() {
            return ProgressTracker(
                    INITIALISING,
                    BUILDING,
                    DECKING,
                    SIGNING,
                    FINALISING
            )
        }

    companion object {
        object INITIALISING : Step("Performing initial steps - create game state.")
        object DECKING : Step("Build Deck and store in the Dealer's vault")
        object BUILDING : Step("Building and verifying transaction - Start Game tx with gamestate as output and no input")
        object SIGNING : Step("Dealer Signing transaction.")
        object FINALISING : Step("Finalising transaction. - Full Final signature on the vault") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(INITIALISING, DECKING, BUILDING, SIGNING, FINALISING)
    }

    @Suspendable
    override fun call(): UniqueIdentifier {
        // Step 1. Initialisation.
        progressTracker.currentStep = INITIALISING
        val initiator = serviceHub.myInfo.legalIdentities.first()
        // Step 3. Building.
        progressTracker.currentStep = BUILDING
        val chatState: ChatState = ChatState(
            linearId = UniqueIdentifier(),
            initiator = initiator,
            message = "Chat Initialized by "+initiator.name.commonName!!,
            by = initiator,
            members = this.members
        );
        val txCommand = Command(ChatContract.Commands.StartGame(), chatState.participants.map { it.owningKey })
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(chatState)
                .addCommand(txCommand)
        // .setTimeWindow(serviceHub.clock.instant(), 5.minutes)
        txBuilder.verify(serviceHub)


        // Step 4. Sign the transaction.
        progressTracker.currentStep = SIGNING
        val dealerSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Step 5. Finalise the transaction.
        progressTracker.currentStep = FINALISING
        subFlow(FinalityFlow(dealerSignedTx, emptyList()))
        return chatState.linearId
    }
}


