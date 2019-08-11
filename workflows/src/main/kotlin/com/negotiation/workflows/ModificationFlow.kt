package com.negotiation.workflows

import co.paralleluniverse.fibers.Suspendable
import negotiation.contracts.ProposalAndTradeContract
import negotiation.contracts.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

object ModificationFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val proposalId: UniqueIdentifier, val policy_applicant_name : String, val policy_applicant_mailing_address : String,
                    val policy_applicant_gl_code : String,
                    val policy_applicant_sic : String,
                    val policy_applicant_fein_or_soc_sec : String,
                    val policy_applicant_buisness_phone : String,
                    val policy_applicant_buisness_type : String, val broker_company_name    : String,
                    val broker_contact_name    : String,
                    val broker_phone    : String,
                    val broker_email    : String,
                    val carrier_company_name    : String,
                    val carrier_contact_name    : String,
                    val carrier_phone    : String,
                    val carrier_email    : String,
                    val additional_insured_name    : String,
                    val additional_insured_mailing_address    : String,
                    val additional_insured_gl_code    : String,
                    val additional_insured_sic    : String,
                    val additional_insured_fein_or_soc_sec    : String,
                    val additional_insured_buisness_phone    : String,
                    val additional_insured_type_of_buisness    : String,
                    val lines_of_business    : String,
                    val policy_information_proposed_eff_date    : Date,
                    val policy_information_proposed_exp_date    : Date,
                    val  billing_plan    : String,
                    val billing_payment_plan    : String,
                    val billing_method_of_payment    : String,
                    val billing_audit    : String,
                    val billing_deposit    : Int,
                    val billing_min_premium   : Int,
                    val attachments_additional   : String,
                    val premises_additional    : Boolean,
                    val premises_address    : String,
                    val premises_within_city_limits    : Boolean,
                    val premises_interest: String,
                    val total_coverage: Int,
                    val coverage_amount: Int) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            // Retrieving the input from the vault.
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(proposalId))
            val inputStateAndRef = serviceHub.vaultService.queryBy<ProposalState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data

            // Creating the output.
            val counterparty = if (ourIdentity == input.proposer) input.proposee else input.proposer
            val output = input.copy(policy_applicant_name = policy_applicant_name, policy_applicant_mailing_address = policy_applicant_mailing_address,
                    policy_applicant_gl_code= policy_applicant_gl_code,
                      policy_applicant_sic  =policy_applicant_sic,
              policy_applicant_fein_or_soc_sec  =policy_applicant_fein_or_soc_sec,
              policy_applicant_buisness_phone  =policy_applicant_buisness_phone,
              policy_applicant_buisness_type  =policy_applicant_buisness_type,   broker_company_name  =broker_company_name,
              broker_contact_name =broker_contact_name,
              broker_phone =broker_phone,
              broker_email =broker_email,
              carrier_company_name =carrier_company_name,
              carrier_contact_name =carrier_contact_name,
              carrier_phone =carrier_phone,
              carrier_email =carrier_email,
              additional_insured_name =additional_insured_name,
              additional_insured_mailing_address =additional_insured_mailing_address,
              additional_insured_gl_code =additional_insured_gl_code,
              additional_insured_sic =additional_insured_sic,
              additional_insured_fein_or_soc_sec =additional_insured_fein_or_soc_sec,
              additional_insured_buisness_phone =additional_insured_buisness_phone,
              additional_insured_type_of_buisness     =additional_insured_type_of_buisness,
              lines_of_business     =lines_of_business,
              policy_information_proposed_eff_date =policy_information_proposed_eff_date,
              policy_information_proposed_exp_date  =policy_information_proposed_exp_date,
               billing_plan     =billing_plan,
              billing_payment_plan     =billing_payment_plan,
              billing_method_of_payment     =billing_method_of_payment,
              billing_audit     =billing_audit,
              billing_deposit =billing_deposit,
              billing_min_premium =billing_min_premium,
              attachments_additional    =attachments_additional,
              premises_additional =premises_additional,
              premises_address     =premises_address,
              premises_within_city_limits =premises_within_city_limits,
              premises_interest =premises_interest,
              total_coverage =total_coverage,
              coverage_amount =coverage_amount,
            proposer = ourIdentity, proposee = counterparty)

            // Creating the command.
            val requiredSigners = listOf(input.proposer.owningKey, input.proposee.owningKey)
            val command = Command(ProposalAndTradeContract.Commands.Modify(), requiredSigners)

            // Building the transaction.
            val notary = inputStateAndRef.state.notary
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addInputState(inputStateAndRef)
            txBuilder.addOutputState(output, ProposalAndTradeContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
            val counterpartySession = initiateFlow(counterparty)
            val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

            // Finalising the transaction.
            subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
        }
    }

    @InitiatedBy(ModificationFlow.Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val ledgerTx = stx.toLedgerTransaction(serviceHub, false)
                    val proposee = ledgerTx.inputsOfType<ProposalState>().single().proposee
                    if (proposee != counterpartySession.counterparty) {
                        throw FlowException("Only the proposee can modify a proposal.")
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(counterpartySession, txId))
        }
    }
}
