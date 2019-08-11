package negotiation.contracts

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import java.util.*

class ProposalAndTradeContract : Contract {
    companion object {
        const val ID = "negotiation.contracts.ProposalAndTradeContract"
    }

    // A transaction is considered valid if the verify() function of the contract of each of the transaction's input
    // and output states does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val cmd = tx.commands.requireSingleCommand<Commands>()

        when (cmd.value) {
            is Commands.Propose -> requireThat {
                "There are no inputs" using (tx.inputStates.isEmpty())
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type ProposalState" using (tx.outputsOfType<ProposalState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<ProposalState>().single()
                "The buyer and seller are the proposer and the proposee" using (setOf(output.broker, output.lead_insurer) == setOf(output.proposer, output.proposee))

                "The proposer is a required signer" using (cmd.signers.contains(output.proposer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(output.proposee.owningKey))
            }

            is Commands.Accept -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type ProposalState" using (tx.inputsOfType<ProposalState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type TradeState" using (tx.outputsOfType<PolicyState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val input = tx.inputsOfType<ProposalState>().single()
                val output = tx.outputsOfType<PolicyState>().single()

               // "The amount is unmodified in the output" using (output.billing_min_premium == output.billing_min_premium)//we have change second argument from imput.ammount to output.ammount due to int string datatype error
                "The buyer is unmodified in the output" using (input.broker == output.broker)
                "The seller is unmodified in the output" using (input.lead_insurer == output.lead_insurer)

                "The proposer is a required signer" using (cmd.signers.contains(input.proposer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(input.proposee.owningKey))
            }

            is Commands.Modify -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type ProposalState" using (tx.inputsOfType<ProposalState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type ProposalState" using (tx.outputsOfType<ProposalState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<ProposalState>().single()
                val input = tx.inputsOfType<ProposalState>().single()

               // "The amount is modified in the output" using (output.billing_min_premium != input.billing_min_premium)
                "The buyer is unmodified in the output" using (input.broker == output.broker)
                "The seller is unmodified in the output" using (input.lead_insurer == output.lead_insurer)

                "The proposer is a required signer" using (cmd.signers.contains(output.proposer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(output.proposee.owningKey))
            }
        }
    }


    // Used to indicate the transaction's intent.
    sealed class Commands : TypeOnlyCommandData() {
        class Propose : Commands()
        class Accept : Commands()
        class Modify : Commands()
    }
}

@BelongsToContract(ProposalAndTradeContract::class)
data class ProposalState(
        //APPLICANT PERSONAL INFORMATION
        val policy_applicant_name: String,
        val policy_applicant_mailing_address: String,
        val policy_applicant_gl_code: String,
        val policy_applicant_sic: String,
        val policy_applicant_fein_or_soc_sec: String,
        val policy_applicant_buisness_phone: String,
        val policy_applicant_buisness_type: String,
        //BROKER INFORMATION
        val broker_company_name: String,
        val broker_contact_name: String,
        val broker_phone: String,
        val broker_email: String,
        //CARRIER INFORMATION
        val carrier_company_name: String,
        val carrier_contact_name: String,
        val carrier_phone: String,
        val carrier_email: String,
        //ADDITIONAL INSURED PARTIES
        val additional_insured_name: String,
        val additional_insured_mailing_address: String,
        val additional_insured_gl_code: String,
        val additional_insured_sic: String,
        val additional_insured_fein_or_soc_sec: String,
        val additional_insured_buisness_phone: String,
        val additional_insured_type_of_buisness: String,
        //LINES OF BUSINESS OR AREAS OF COVER - NEED TO BE ABLE TO SELECT MULTIPLE LINES
        val lines_of_business: String,
        //POLICY INFORMATION
        val policy_information_proposed_eff_date: Date,
        val policy_information_proposed_exp_date: Date,
        //POLICY BILLING INFORMATION
        val billing_plan: String,
        val billing_payment_plan: String,
        val billing_method_of_payment: String,
        val billing_audit: String,
        val billing_deposit: Int,
        val billing_min_premium: Int,
        //OTHER ATTACHMENTS
        val attachments_additional: String,
        //PREMISES INFORMATION
        val premises_address: String,
        val premises_within_city_limits: Boolean,
        val premises_interest: String,
        val premises_additional: Boolean,
        //New Variables
        val total_coverage: Int,
        val coverage_amount: Int,
       // val coverage_ratio: Float,
        //val amount: Int,

        val broker: Party,
        val lead_insurer: Party,
        val proposer: Party,
        val proposee: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants = listOf(proposer,proposee)
}

@BelongsToContract(ProposalAndTradeContract::class)
data class PolicyState(
        //APPLICANT PERSONAL INFORMATION
        val policy_applicant_name : String,
        val policy_applicant_mailing_address: String,
        val policy_applicant_gl_code: String,
        val policy_applicant_sic: String,
        val policy_applicant_fein_or_soc_sec: String,
        val policy_applicant_buisness_phone: String,
        val policy_applicant_buisness_type: String,
        //BROKER INFORMATION
        val broker_company_name: String,
        val broker_contact_name: String,
        val broker_phone: String,
        val broker_email: String,
        //CARRIER INFORMATION
        val carrier_company_name: String,
        val carrier_contact_name: String,
        val carrier_phone: String,
        val carrier_email: String,
        //ADDITIONAL INSURED PARTIES
        val additional_insured_name : String,
        val additional_insured_mailing_address: String,
        val additional_insured_gl_code: String,
        val additional_insured_sic: String,
        val additional_insured_fein_or_soc_sec: String,
        val additional_insured_buisness_phone: String,
        val additional_insured_type_of_buisness: String,
        //LINES OF BUSINESS OR AREAS OF COVER - NEED TO BE ABLE TO SELECT MULTIPLE LINES
        val lines_of_business: String,

        //POLICY INFORMATION
        val policy_information_proposed_eff_date: Date,
        val policy_information_proposed_exp_date: Date,
        //POLICY BILLING INFORMATION
        val billing_plan: String,
        val billing_payment_plan: String,
        val billing_method_of_payment: String,
        val billing_audit: String,
        val billing_deposit: Int,
        val billing_min_premium: Int,
        //OTHER ATTACHMENTS
        val attachments_additional: String,
        //PREMISES INFORMATION
        val premises_address: String,
        val premises_within_city_limits: Boolean,
        val premises_interest: String,
        val premises_additional: Boolean,
        //val amount: Int,
        //val buyer: Party,
        //val seller: Party,
       // val amount: Int,
        val total_coverage: Int,
        val coverage_amount: Int,
        val broker: Party,
        val lead_insurer: Party,
        val proposer: Party,
        val proposee: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants = listOf(proposer,proposee )//  the one intiating
}



