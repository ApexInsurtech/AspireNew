POLICY PLACEMENT

Step 1:

Broker requests a quote for coverage on a commercial property.  By law the Broker must seek the most competitive quote, they must approach at least 3 insurers.  In this demo we will request a quote from 3 insurers.  It begins with the broker submitting a proposal form to an Insurer.

From Broker:

Quote Requested from InsurerA:

```flow start ProposalFlow$Initiator isBuyer: true, policy_applicant_name: “A”, policy_applicant_mailing_address: “A”, policy_applicant_gl_code: “A”, policy_applicant_sic: “A”, policy_applicant_fein_or_soc_sec: “A”, policy_applicant_buisness_phone: “A”, policy_applicant_buisness_type: “A”, broker_company_name : “A”, broker_contact_name: “A”, broker_phone: “A”, broker_email: “A”, carrier_company_name: “A”, carrier_contact_name: “A”, carrier_phone: “A”, carrier_email: “A”, additional_insured_name: “A”, additional_insured_mailing_address: “A”, additional_insured_gl_code: “A”, additional_insured_sic: “A”, additional_insured_fein_or_soc_sec: “A”, additional_insured_buisness_phone: “A”, additional_insured_type_of_buisness: “A”, lines_of_business: “A”, policy_information_proposed_eff_date: “A”, policy_information_proposed_exp_date: “A”, billing_plan: “A”, billing_payment_plan: “A”,   billing_method_of_payment: “A”, billing_audit: “A”, billing_deposit: “A”, billing_min_premium: 1, attachments_additional: “A”, premises_additional: “A”, premises_address: true, premises_within_city_limits: “A” ,premises_interest: true, counterparty: InsurerA, coverage_amount: 100, total_coverage: 500```


Quote Requested from InsurerB:


```flow start ProposalFlow$Initiator isBuyer: true, policy_applicant_name: “A”, policy_applicant_mailing_address: “A”, policy_applicant_gl_code: “A”, policy_applicant_sic: “A”, policy_applicant_fein_or_soc_sec: “A”, policy_applicant_buisness_phone: “A”, policy_applicant_buisness_type: “A”, broker_company_name : “A”, broker_contact_name: “A”, broker_phone: “A”, broker_email: “A”, carrier_company_name: “A”, carrier_contact_name: “A”, carrier_phone: “A”, carrier_email: “A”, additional_insured_name: “A”, additional_insured_mailing_address: “A”, additional_insured_gl_code: “A”, additional_insured_sic: “A”, additional_insured_fein_or_soc_sec: “A”, additional_insured_buisness_phone: “A”, additional_insured_type_of_buisness: “A”, lines_of_business: “A”, policy_information_proposed_eff_date: “A”, policy_information_proposed_exp_date: “A”, billing_plan: “A”, billing_payment_plan: “A”,   billing_method_of_payment: “A”, billing_audit: “A”, billing_deposit: “A”, billing_min_premium: 1, attachments_additional: “A”, premises_additional: “A”, premises_address: true, premises_within_city_limits: “A” ,premises_interest: true, counterparty: InsurerB, coverage_amount: 300, total_coverage: 500```


Quote Requested from InsurerC:


```flow start ProposalFlow$Initiator isBuyer: true, policy_applicant_name: “A”, policy_applicant_mailing_address: “A”, policy_applicant_gl_code: “A”, policy_applicant_sic: “A”, policy_applicant_fein_or_soc_sec: “A”, policy_applicant_buisness_phone: “A”, policy_applicant_buisness_type: “A”, broker_company_name : “A”, broker_contact_name: “A”, broker_phone: “A”, broker_email: “A”, carrier_company_name: “A”, carrier_contact_name: “A”, carrier_phone: “A”, carrier_email: “A”, additional_insured_name: “A”, additional_insured_mailing_address: “A”, additional_insured_gl_code: “A”, additional_insured_sic: “A”, additional_insured_fein_or_soc_sec: “A”, additional_insured_buisness_phone: “A”, additional_insured_type_of_buisness: “A”, lines_of_business: “A”, policy_information_proposed_eff_date: “A”, policy_information_proposed_exp_date: “A”, billing_plan: “A”, billing_payment_plan: “A”,   billing_method_of_payment: “A”, billing_audit: “A”, billing_deposit: “A”, billing_min_premium: 1, attachments_additional: “A”, premises_additional: “A”, premises_address: true, premises_within_city_limits: “A” ,premises_interest: true, counterparty: InsurerC, coverage_amount: 100, total_coverage: 500```


Notice how we send exactly the same data to all 3 insurers when asking for a quote.  From UI after proposal form is filled out, we need to be able to send to all 3 insurers at same time with a single click (use check boxes or drop down).

Should have a drop down which will list all nodes on network.  Network Map function connected to a drop down box.

Step 2:

Modification of Quote:

On many occasions the Insurers will request additional information beforing issuing a quote.  In this case Insuer A and C will both ask for a Survey Report.  B will ask for some other document (not important).  We will attach the required documents and return to the 3 insurers.  This will be done one insurers at a time.


Command:

From Insurer A:

```flow start ModificationFlow$Initiator proposalId: linearID, newAmount: 0```

```flow start group.chat.flows.StartChat notary: "O=Notary, L=London, C=GB"```

```run vaultQuery contractStateType: com.template.states.ChatState```

```flow start group.chat.flows.AddMemberFlow gameID: "linearID", member: "O=Broker, L=London, C=GB"```

```flow start group.chat.flows.AddMessageFlow gameID: "linearID", message: "Hey please attach a Survey report for this quote request"```


Note: Can we connect this chat to the actual proposal state via linearID

From Insurer B:

```flow start ModificationFlow$Initiator proposalId: linearID, newAmount: 0```

```flow start group.chat.flows.StartChat notary: "O=Notary, L=London, C=GB"```

```run vaultQuery contractStateType: com.template.states.ChatState```

```flow start group.chat.flows.AddMemberFlow gameID: "linearID", member: "O=Broker, L=London, C=GB"```

```flow start group.chat.flows.AddMessageFlow gameID: "linearID", message: "Hey please attach a Survey report for this quote request"```


From Insurer C:

```flow start ModificationFlow$Initiator proposalId: linearID, newAmount: 0```

```flow start group.chat.flows.StartChat notary: "O=Notary, L=London, C=GB"```

```run vaultQuery contractStateType: com.template.states.ChatState```

```flow start group.chat.flows.AddMemberFlow gameID: "linearID", member: "O=Broker, L=London, C=GB"```

<<<<<<< HEAD
flow start group.chat.flows.AddMemberFlow groupID: "2a77a365-512c-44d6-a937-352965d84281", member: "O=Broker, L=London, C=GB"
=======
```flow start group.chat.flows.AddMessageFlow gameID: "linearID", message: "Hey please attach a Survey report for this quote request"```
>>>>>>> 803e33ed5b855048f768e395bc2556d821d8f7cd


From Broker Node:

<<<<<<< HEAD
flow start group.chat.flows.AddMessageFlow groupID: "2a77a365-512c-44d6-a937-352965d84281", message: "APEX INSURTECH - WINNERS OF THE CORDACON 2019 INSURTECH CHALLENGE!"
=======
```run vaultQuery contractStateType: com.template.states.ChatState```
can we add reference to the proposalID here that allows us to pull only those chat state that correspond to the relevant proposal state?
>>>>>>> 803e33ed5b855048f768e395bc2556d821d8f7cd

STEP 3:

Broker will issue a modification flow to attach requested docs:

```flow start ModificationFlow$Initiator proposalId: {linearID for quote from A}, newAmount: 0 + ATTACHMENT```

```flow start ModificationFlow$Initiator proposalId: {linearID for quote from B}, newAmount: 0 + ATTACHMENT```

```flow start ModificationFlow$Initiator proposalId: {linearID for quote from C}, newAmount: 0 + ATTACHMENT```

STEP 4:

From Insurer A:

```flow start ModificationFlow$Initiator proposalId: {linearID for quote from A}, newAmount: 10000```

From Insurer B:

```flow start ModificationFlow$Initiator proposalId: {linearID for quote from B}, newAmount: 9500```


From Insurer C:

```flow start ModificationFlow$Initiator proposalId: {linearID for quote from C}, newAmount: 9800```
