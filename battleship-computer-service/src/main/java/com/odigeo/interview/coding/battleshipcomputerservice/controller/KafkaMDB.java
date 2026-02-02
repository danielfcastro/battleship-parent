package com.odigeo.interview.coding.battleshipcomputerservice.controller;

import com.google.gson.Gson;
import com.odigeo.interview.coding.battleshipapi.event.GameCreatedEvent;
import com.odigeo.interview.coding.battleshipapi.event.GameFireEvent;
import com.odigeo.interview.coding.battleshipcomputerservice.service.BattleshipService;
import fish.payara.cloud.connectors.kafka.api.KafkaListener;
import fish.payara.cloud.connectors.kafka.api.OnRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "battleship-computer-service"),
        @ActivationConfigProperty(propertyName = "groupIdConfig", propertyValue = "battleship.computer"),
        @ActivationConfigProperty(propertyName = "topics", propertyValue = "battleship.game.new,battleship.game.field.fire"),
        @ActivationConfigProperty(propertyName = "bootstrapServersConfig", propertyValue = "kafka:29092"),
        @ActivationConfigProperty(propertyName = "retryBackoff", propertyValue = "1000"),
        @ActivationConfigProperty(propertyName = "autoCommitInterval", propertyValue = "100"),
        @ActivationConfigProperty(propertyName = "keyDeserializer", propertyValue = "org.apache.kafka.common.serialization.StringDeserializer"),
        @ActivationConfigProperty(propertyName = "valueDeserializer", propertyValue = "org.apache.kafka.common.serialization.StringDeserializer"),
        @ActivationConfigProperty(propertyName = "pollInterval", propertyValue = "3000"),
        @ActivationConfigProperty(propertyName = "commitEachPoll", propertyValue = "true"),
        @ActivationConfigProperty(propertyName = "useSynchMode", propertyValue = "true")
})
@ResourceAdapter(value="kafka")
public class KafkaMDB implements KafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMDB.class);

    @Inject
    private BattleshipService battleshipService;

    public KafkaMDB() { }

    @OnRecord( topics={"battleship.game.new"})
    public void onGameNew(ConsumerRecord record) {
        logger.debug("Handled message on topic battleship.game.new: {}", record);
        GameCreatedEvent gameCreated = new Gson().fromJson(record.value().toString(), GameCreatedEvent.class);
        battleshipService.joinGame(gameCreated.getGameId());
        battleshipService.deployShips(gameCreated.getGameId());
    }

    @OnRecord( topics={"battleship.game.field.fire"})
    public void onGameFieldFire(ConsumerRecord record) {
        logger.debug("Handled message on topic battleship.game.field.fire: {}", record);
        GameFireEvent gameFire = new Gson().fromJson(record.value().toString(), GameFireEvent.class);
        battleshipService.fire(gameFire.getGameId());
    }

}