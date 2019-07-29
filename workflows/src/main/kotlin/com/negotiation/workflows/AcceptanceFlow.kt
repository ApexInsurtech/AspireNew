package negotiation.workflows

import co.paralleluniverse.fibers.Suspendable
import negotiation.contracts.ProposalAndTradeContract
import negotiation.contracts.ProposalState
import negotiation.contracts.TradeState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object AcceptanceFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val proposalId: UniqueIdentifier) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            // Retrieving the input from the vault.
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(proposalId))
            val inputStateAndRef = serviceHub.vaultService.queryBy<ProposalState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data

            // Creating the output.
            //val output = TradeState(input.billing_min_premium, input.buyer, input.seller, input.linearId)
           val output = TradeState( input.policy_applicant_name , input.policy_applicant_mailing_address,
                   input.policy_applicant_gl_code   ,input.policy_applicant_sic   ,input.policy_applicant_fein_or_soc_sec   ,
                   input.policy_applicant_buisness_phone   ,input.policy_applicant_buisness_type   ,
                   input.broker_company_name   , input.broker_contact_name   ,input.broker_phone   ,input.broker_email   ,
                   input.carrier_company_name   , input.carrier_contact_name   ,input.carrier_phone   ,input.carrier_email   ,
                   input.additional_insured_name   ,input.additional_insured_mailing_address   ,input.additional_insured_gl_code   ,
                   input.additional_insured_sic   ,input.additional_insured_fein_or_soc_sec   ,input.additional_insured_buisness_phone   ,
                   input.additional_insured_type_of_buisness   , input.lines_of_business   ,input.policy_information_proposed_eff_date   ,input.policy_information_proposed_exp_date   ,
                   input.billing_plan   ,input.billing_payment_plan   ,input.billing_method_of_payment   ,input.billing_audit   ,input.billing_deposit   ,
                   input.billing_min_premium ,input.attachments_additional ,input.premises_address,input.premises_within_city_limits   ,
                   input.premises_interest,input.premises_additional, input.buyer, input.seller, input.linearId)



            // Creating the command.
            val requiredSigners = listOf(input.buyer.owningKey, input.seller.owningKey)
            val command = Command(ProposalAndTradeContract.Commands.Accept(), requiredSigners)

            // Building the transaction.
            val notary = inputStateAndRef.state.notary
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addInputState(inputStateAndRef)
            txBuilder.addOutputState(output, ProposalAndTradeContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
            val counterparty = if (ourIdentity == input.buyer) input.seller else input.buyer
            val counterpartySession = initiateFlow(counterparty)
            val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

            // Finalising the transaction.
            subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val ledgerTx = stx.toLedgerTransaction(serviceHub, false)
                    val proposee = ledgerTx.inputsOfType<ProposalState>().single().seller
                    if (proposee != counterpartySession.counterparty) {
                        throw FlowException("Only the proposee can accept a proposal.")
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(counterpartySession, txId))
        }
    }
}
