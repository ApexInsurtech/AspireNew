package negotiation.workflows

import co.paralleluniverse.fibers.Suspendable
import negotiation.contracts.MakeaClaimState
import negotiation.contracts.MakeaClaimcontract
import negotiation.contracts.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object MakeClaimFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val proposalId: UniqueIdentifier) : FlowLogic<UniqueIdentifier>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call(): UniqueIdentifier {
            // Creating the output.

            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(proposalId))
            val inputStateAndRef = serviceHub.vaultService.queryBy<ProposalState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data


            val output = MakeaClaimState(input.policy_applicant_name, input.policy_applicant_mailing_address,
                    input.broker, input.lead_insurer, input.linearId)


            // Creating the command.
            val commandType = MakeaClaimcontract.Commands.Propose()
            val requiredSigners = listOf(input.broker.owningKey, input.lead_insurer.owningKey)
            //val requiredSigners = listOf(ourIdentity.owningKey, counterparty.owningKey)
            val command = Command(commandType, requiredSigners)

            // Building the transaction.
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addOutputState(output, MakeaClaimcontract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.

            val counterparty = if (ourIdentity == input.broker) input.lead_insurer else input.broker
            val counterpartySession = initiateFlow(counterparty)
            val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

            // Finalising the transaction.
            val finalisedTx = subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
            return finalisedTx.tx.outputsOfType<MakeaClaimState>().single().linearId
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    // No checking to be done.
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(counterpartySession, txId))
        }
    }
}
