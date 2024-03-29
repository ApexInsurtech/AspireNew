package com.template.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class CardRankEnum {
    CARD_2,
    CARD_3,
    CARD_4,
    CARD_5,
    CARD_6,
    CARD_7,
    CARD_8,
    CARD_9,
    CARD_10,
    JACK,
    QUEEN,
    KING,
    ACE
}