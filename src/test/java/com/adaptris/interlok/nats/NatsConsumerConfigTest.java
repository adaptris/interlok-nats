package com.adaptris.interlok.nats;

import com.adaptris.core.ConsumerCase;
import com.adaptris.core.StandaloneConsumer;

public class NatsConsumerConfigTest extends ConsumerCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected StandaloneConsumer retrieveObjectForSampleConfig() {
    BasicNatsConnection c = new BasicNatsConnection().withUrl("nats://localhost:4222");
    NatsConsumer p =
        new NatsConsumer().withSubject("MySubject").withQueueGroup("MyQueueGroup");
    return new StandaloneConsumer(c, p);
  }

}
