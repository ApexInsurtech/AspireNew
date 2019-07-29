APEX ASPIRE
Welcome to the Apex Git repo.  If you are reading this, you are part of a privileged few, whom I trust very much.

TO START:

Start all the nodes at the same time, cd to the Aspire main folder and execute the following:

LINUX:

```./gradlew clean deployNodes Deploys Nodes```
```cd build/nodes Navigate to nodes folder```
```./runnodes Runs the nodes```
```cd Aspire Navigate back to main folder```
```./gradlew runTemplateClient Deploys Webservers```

WINDOWS:

```gradlew.bat clean deployNodes Deploys Nodes```
```cd build/nodes Navigate to nodes folder```
```runnodes Runs the nodes```
```cd Aspire Navigate back to main folder```
```gradlew.bat runTemplateClient Deploys Webservers```

Starting node's individually:

Occassionally a node will fail to start. In this situation we can run a node individually by opening a terminal window within the node’s folder (cd build/nodes) and running:

java -jar corda.jar
---------------------------------------------------------------------------------------------------------------------------------------------------------
OUR DEMO:

For now we will execute all flows from the node shell. Once all flows are working will will integrate Spring etc.

In the demo below a Broker requests a quote from an Insurer (creating a Proposal), they are then able to negotiate between themselves and reach agreement on a the terms of the insurance policy.

In Aspire, any node can create a proposal to any other node. The node that is making the proposal is the proposer and the node that is receiving the proposal is the proposee. Please note that these role's change each time a proposal is sent back or forth.

The commands below must be executed from the relevant node shell. Change the amounts, parties as required, and be sure to add the correct linear ID (you can access this by doing a vault query, more below).


### CREATING A PROPOSAL:

ProposalFlow$Initiator

This flow creates a proposal to Insurer C, this proposal will have the data Amount = 10, and isBuyer = true.

In the Broker shell execute the following command:

cd..

### VIEWING A PROPOSAL:

Now go to InsurerC shell and execute the command below: run vaultQuery contractStateType: negotiation.contracts.ProposalState

This allow's us to view the proposal in Insurer C's vault. This flow can be executed from any shell to allow us to view the vault contents. Note down the linear ID (its near the top of the state data).

### MODIFYING A PROPOSAL:

Now assume that Insurer C wishes to modify the agreement and return it to the Broker, execute the following:

flow start ModificationFlow$Initiator proposalId: xx, newAmount: 8

Where xx is the linear ID taken from the vault, also notice that we have amended the amount to 8.

Again go back to Broker Shell and execute a vault query:

run vaultQuery contractStateType: negotiation.contracts.ProposalState

### ACCEPTING THE PROPOSAL

Now we can see that the state has changed. We can go back and forth as many times as we wish until we finally reach agreement. Once the broker and Insurer C agree on an amount we execute the acceptance flow:

flow start AcceptanceFlow$Initiator proposalId: xx

This converts the proposal state into a trade state. Next the insurer must convert the trade state into a policy state (TBD).

-----------------------------------------------------------------------------------------------------------------------------------------------------------
## NODE CHAT:

To start a chat (group or one to one), go to any Node shell:

### 1. START A GROUP CHAT

flow start group.chat.flows.StartChat notary: "O=Notary, L=London, C=GB"

This will start Start Chat flow, which in turn will create a ChatState.  All members and messages between members are stored in the ChatState.

### 2. ADD INSURER A:

First we need the linear ID or game ID: 2a77a365-512c-44d6-a937-352965d84281

run vaultQuery contractStateType: com.template.states.ChatState

Scroll up and copy the Linear ID field. Then:

flow start group.chat.flows.AddMemberFlow gameID: "2a77a365-512c-44d6-a937-352965d84281", member: "O=Broker, L=London, C=GB"

Here we have added the broker node, although you can add any node you wish at this point.  To add additional nodes simply execute the above flow again and change the member field to the relevant party that you would like to include in the group chat.

### 3. ADD A MESSAGE TO THE GROUP CHAT:

flow start group.chat.flows.AddMessageFlow gameID: "2a77a365-512c-44d6-a937-352965d84281", message: "APEX INSURTECH - WINNERS OF THE CORDACON 2019 INSURTECH CHALLENGE!"

### 4. CHECK GROUP CHAT MESSAGES:

Execute a vault query from relevant nodes to confirm that message has been added to the group chat:

run vaultQuery contractStateType: com.template.states.ChatState

Oracle:

To request the 5th prime number:

flow start CreatePrime index: 5

To check in vault:

run vaultQuery contractStateType: net.corda.examples.oracle.base.contract.PrimeState