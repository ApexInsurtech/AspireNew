package com.template.schema

/*

//TODO: The mapping is WIP
@Mapper(nullValueMappingStrategy = RETURN_DEFAULT, unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface GameMapper     {

    @Mappings(Mapping(target = "linearId", source = "linearId", qualifiedByName = ["toUUID"]),
            Mapping(target = "playerA", source = "playerA"),
            Mapping(target = "playerB", source = "playerB"),
            Mapping(target = "tableCards", source = "tableCards", qualifiedByName = ["toPlayerB"]),
            Mapping(target = "playerA", source = "participants", qualifiedByName = ["toPlayerA"]),
            Mapping(target = "rankingEnum", source = "rankingEnum"),
            Mapping(target = "rankingList", source = "rankingList", qualifiedByName = ["toRankingList"]),
            Mapping(target = "highCard", source = "highCard", qualifiedByName = ["toCardSchemaV1"]))
    fun toPersistentGameSchemaV1(gameState: ChatState): GameSchemaV1.PersistentGameSchemaV1

    @Named("toCardSchemaV1Set")
    fun toCardSchemaV1Set(cards: Array<Card?>) : Set<CardSchemaV1>

    @Named("toCardSchemaV1")
    @InheritInverseConfiguration
    fun toCardSchemaV1(card: Card) : CardSchemaV1

    @Named("toRankingList")
    fun toRankingList(cards: List<Card>) : List<CardSchemaV1>

    @Named("toPlayerA")
    fun toPlayerA(participants: List<AbstractParty>): AbstractParty = participants.get(0)

    @Named("toPlayerB")
    fun toPlayerB(participants: List<AbstractParty>): AbstractParty = participants.get(1)

    @Named("toUUID")
    fun toUUID(uniqueID: UniqueIdentifier): UUID = uniqueID.id
}
*/
