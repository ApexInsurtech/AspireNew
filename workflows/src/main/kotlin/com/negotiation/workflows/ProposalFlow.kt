package negotiation.workflows

import co.paralleluniverse.fibers.Suspendable
import negotiation.contracts.ProposalAndTradeContract
import negotiation.contracts.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object ProposalFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val isBuyer: Boolean,
                    val policy_applicant_name : String ,
                    val policy_applicant_mailing_address : String ,
                    val policy_applicant_gl_code : String ,
                    val policy_applicant_sic : String ,
                    val policy_applicant_fein_or_soc_sec : String ,
                    val policy_applicant_buisness_phone : String ,
                    val policy_applicant_buisness_type : String ,
                    val broker_company_name    : String ,
                    val broker_contact_name    : String ,
                    val broker_phone    : String ,
                    val broker_email    : String ,
                    val carrier_company_name    : String ,
                    val carrier_contact_name    : String ,
                    val carrier_phone    : String ,
                    val carrier_email    : String ,
                    val additional_insured_name    : String ,
                    val additional_insured_mailing_address    : String ,
                    val additional_insured_gl_code    : String ,
                    val additional_insured_sic    : String ,
                    val additional_insured_fein_or_soc_sec    : String ,
                    val additional_insured_buisness_phone    : String ,
                    val additional_insured_type_of_buisness    : String ,
                    val lines_of_business    : String ,
                    val policy_information_proposed_eff_date    : String ,
                    val policy_information_proposed_exp_date    : String ,
                   val  billing_plan    : String ,
                    val billing_payment_plan    : String ,
                    val billing_method_of_payment    : String ,
                    val billing_audit    : String ,
                    val billing_deposit    : String ,
                    val billing_min_premium   : Int ,
                    val attachments_additional   : String ,
                    val premises_additional    : String ,
                    val premises_address    : Boolean ,
                    val premises_within_city_limits    : String ,
                    val premises_interest: Boolean,val total_coverage: Int
                    ,val coverage_amount: Int,val counterparty: Party  ) : FlowLogic<UniqueIdentifier>() {
        override val progressTracker = ProgressTracker()
//remeber to add two more parties to this initiator which may have nullvalues
        @Suspendable
        override fun call(): UniqueIdentifier {
            // Creating the output.
            val (broker, lead_insurer) = when {
                isBuyer -> ourIdentity to counterparty
                else -> counterparty to ourIdentity
            }
            //val output = ProposalState(amount, buyer, seller, ourIdentity, counterparty)
            val output = ProposalState(policy_applicant_name , policy_applicant_mailing_address,
                    policy_applicant_gl_code   ,policy_applicant_sic   ,policy_applicant_fein_or_soc_sec   ,
                    policy_applicant_buisness_phone   ,policy_applicant_buisness_type   ,
                    broker_company_name   , broker_contact_name   ,broker_phone   ,broker_email   ,
                    carrier_company_name   , carrier_contact_name   ,carrier_phone   ,carrier_email   ,
                    additional_insured_name   ,additional_insured_mailing_address   ,additional_insured_gl_code   ,
                    additional_insured_sic   ,additional_insured_fein_or_soc_sec   ,additional_insured_buisness_phone   ,
                    additional_insured_type_of_buisness   ,
                    lines_of_business   ,policy_information_proposed_eff_date   ,policy_information_proposed_exp_date   ,
                    billing_plan   ,billing_payment_plan   ,billing_method_of_payment   ,billing_audit   ,billing_deposit   ,
                    billing_min_premium  ,attachments_additional  ,premises_additional   ,premises_address   ,premises_within_city_limits   ,
                    premises_interest  ,total_coverage,coverage_amount,broker , lead_insurer, ourIdentity, counterparty)

            // Creating the command.
            val commandType = ProposalAndTradeContract.Commands.Propose()
            val requiredSigners = listOf(ourIdentity.owningKey, counterparty.owningKey)
            val command = Command(commandType, requiredSigners)

            // Building the transaction.
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addOutputState(output, ProposalAndTradeContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
            val counterpartySession = initiateFlow(counterparty)
            val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

            // Finalising the transaction.
            val finalisedTx = subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
            return finalisedTx.tx.outputsOfType<ProposalState>().single().linearId
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
