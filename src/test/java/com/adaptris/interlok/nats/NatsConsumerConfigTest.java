package com.adaptris.interlok.nats;

import com.adaptris.core.StandaloneConsumer;
import com.adaptris.interlok.junit.scaffolding.ExampleConsumerCase;

public class NatsConsumerConfigTest extends ExampleConsumerCase {

  @Override
  protected StandaloneConsumer retrieveObjectForSampleConfig() {
    BasicNatsConnection c = new BasicNatsConnection().withUrl("nats://localhost:4222");
    NatsConsumer p =
        new NatsConsumer().withSubject("MySubject").withQueueGroup("MyQueueGroup");
    return new StandaloneConsumer(c, p);
  }

}
