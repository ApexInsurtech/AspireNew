package com.template.contracts

import com.template.model.RoundEnum
import com.template.states.ChildPolicyState
import com.template.states.ParentPolicyState

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class ParentPolicyContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        @JvmStatic
        val ID = ParentPolicyContract::class.qualifiedName!!
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        tx.commands.filter { it is Commands }.map { it as Commands }.forEach { it.verify(tx) }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        fun verify(tx: LedgerTransaction)
        class Start_GAME : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                val command = tx.commands.requireSingleCommand<Commands>()
                requireThat {
                    "There are no inputs" using (tx.inputStates.isEmpty())
                    "There is exactly one output" using (tx.outputStates.size == 1)
                    "The single output is of type Game state" using (tx.outputsOfType<ParentPolicyState>().size == 1)
                    "There is exactly one command" using (tx.commands.size == 1)
                    val output = tx.outputsOfType<ParentPolicyState>().single()
                    "The starter is a required signer/dealer" using (command.signers.contains(output.moderator.owningKey))
//                    "The table cards are empty" using (output.tableCards.isEmpty())
                  //  "Message cannot be empty " using (output.betAmount.length < 1)
                    "Players are empty" using (output.members.isEmpty())
                   // "Round is started" using (output.rounds.equals(RoundEnum.Started))
                   //// "Winner is not there yet" using (output.winner == null)
                }

            }
        }
        class ADD_PLAYER : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                requireThat {
                    "There should be exactly one input" using (tx.inputStates.size ==1)
                    "The input should be a ChatState" using (tx.inputStates.first() is ParentPolicyState)
                    //"Input Round is started" using ((tx.inputStates.first() as ParentPolicyState).rounds.equals(RoundEnum.Started))
                    "There should be two outputs" using (tx.outputStates.size ==2)
                   // "The two outputs are a Game State and a Player State" using (tx.groupStates(ParentPolicyState::deckIdentifier).size == 1 && tx.groupStates(ChildPolicyState::party).size == 1)
                    val outputGameState = tx.outputsOfType<ParentPolicyState>().single()
                    val outputPlayerState = tx.outputsOfType<ChildPolicyState>().single()
                    //"Output game state Round is started" using (outputGameState.rounds.equals(RoundEnum.Started))
                    //"the player cards are empty" using (outputPlayerState.myCards.isEmpty())
                }
            }
        }

       /* class DEALT : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        class FLOPPED : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        class RIVERED : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        class TURNED : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        class WINNER : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        class BET : TypeOnlyCommandData(), Commands {
            override fun verify(tx: LedgerTransaction) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }*/
    }
}