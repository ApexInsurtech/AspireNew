package com.template.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class CardSuitEnum {
    DIAMONDS,
    HEARTS,
    SPADES,
    CLUBS,
}