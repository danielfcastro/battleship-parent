package com.odigeo.interview.coding.battleshipcomputerservice.controller;

import com.odigeo.interview.coding.battleshipcomputerservice.service.BattleshipService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

public class KafkaMDBTest {

    @Mock
    private ConsumerRecord consumerRecord;
    @Mock
    private BattleshipService battleshipService;
    @InjectMocks
    private KafkaMDB kafkaMDB;

    @BeforeMethod
    private void init() {
        initMocks(this);
        when(consumerRecord.value()).thenReturn("{\"gameId\":\"3238eef4-5e2c-4add-accb-4ca9514d5aa2\"}");
    }

    @Test
    public void testOnGameNew() {
        kafkaMDB.onGameNew(consumerRecord);
        verify(battleshipService, times(1)).joinGame("3238eef4-5e2c-4add-accb-4ca9514d5aa2");
        verify(battleshipService, times(1)).deployShips("3238eef4-5e2c-4add-accb-4ca9514d5aa2");
    }

    @Test
    public void testOnGameFieldFire() {
        kafkaMDB.onGameFieldFire(consumerRecord);
        verify(battleshipService, times(1)).fire("3238eef4-5e2c-4add-accb-4ca9514d5aa2");
    }

}