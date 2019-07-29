package com.template.webserver

import com.template.webserver.NodeRPCConnection
import net.corda.core.contracts.ContractState
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/templateendpoint"], produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @GetMapping(value = ["/test_status"], produces = arrayOf("text/plain"))
    private fun test_status(): String {
        return "answwer is 1."
    }
    @GetMapping(value = ["/flow_details"], produces = arrayOf("text/plain"))
    private fun flow_details(): String {
        return proxy.registeredFlows().toString()
    }

    @GetMapping(value = ["/states_details"], produces = arrayOf("text/plain"))
    private fun states_details(): String {
        return proxy.vaultQueryBy<ContractState>().states.toString()
    }
}