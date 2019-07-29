package com.template.schema

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

object GameSchema

object GameSchemaV1 : MappedSchema(
        schemaFamily = GameSchema.javaClass,
        version = 1,
        mappedTypes = listOf(GameSchemaV1.PersistentGameSchemaV1::class.java,
                CardSchemaV1::class.java,
                String::class.java
        )) {


    @Entity
    @Table(name = "game_state")
    class PersistentGameSchemaV1(
            @Column(name = "linear_id")
            val linearId: UUID,

            @Column(name = "moderator")
            val moderator: AbstractParty,

            //Note: A temproary workaround could be that we support only two members
            //TODO: net.corda.nodeapi.internal.persistence.HibernateConfigException: Could not create Hibernate configuration: Use of @OneToMany or @ManyToMany targeting an unmapped class: com.poker.schema.GameSchemaV1$PersistentGameSchemaV1.members[java.lang.String]
//        @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
//        val members: Set<String>,

            @Column(name = "memberA")
            val memberA: AbstractParty,

            @Column(name = "memberB")
            val memberB: AbstractParty,

            @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
            val tableCards: Set<CardSchemaV1>,

            @Column(name = "lastChange")
            val lastChange: LocalDateTime?
    ) : PersistentState()
}