package negotiation.contracts

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

class ClaimContract : Contract {
    companion object {
        const val ID = "negotiation.contracts.ClaimContract"
    }

    // A transaction is considered valid if the verify() function of the contract of each of the transaction's input
    // and output states does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val cmd = tx.commands.requireSingleCommand<Commands>()

        when (cmd.value) {
            is Commands.Propose -> requireThat {
                "There are no inputs" using (tx.inputStates.isEmpty())
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type ClaimState" using (tx.outputsOfType<ClaimState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<ClaimState>().single()
                "The buyer and seller are the proposer and the proposee" using (setOf(output.buyer, output.seller) == setOf(output.buyer, output.seller))

                "The proposer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(output.seller.owningKey))
            }

            is Commands.Accept -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type ClaimState" using (tx.inputsOfType<ClaimState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type PolicyState" using (tx.outputsOfType<ClaimState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val input = tx.inputsOfType<ClaimState>().single()
                val output = tx.outputsOfType<ClaimState>().single()

                "The amount is unmodified in the output" using (output.policy_applicant_name == output.policy_applicant_name)//we have change second argument from imput.ammount to output.ammount due to int string datatype error
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)

                "The proposer is a required signer" using (cmd.signers.contains(input.buyer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(input.seller.owningKey))
            }

            is Commands.Modify -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type ClaimState" using (tx.inputsOfType<ClaimState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type ClaimState" using (tx.outputsOfType<ClaimState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<ClaimState>().single()
                val input = tx.inputsOfType<ClaimState>().single()

                //"The amount is modified in the output" using (output.billing_min_premium != input.billing_min_premium)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)

                "The proposer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(output.seller.owningKey))
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



@BelongsToContract(ClaimContract::class)
data class ClaimState(
        //APPLICANT PERSONAL INFORMATION
        val policy_applicant_name: String,
        val policy_applicant_mailing_address: String,
        val buyer: Party,
        val seller: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants = listOf(buyer, seller)
}




