package com.adaptris.interlok.nats;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.StandaloneConsumer;

public class NatsConsumerConfigTest extends ConsumerCase {

  public NatsConsumerConfigTest(String s) {
    super(s);
  }

  @Override
  protected StandaloneConsumer retrieveObjectForSampleConfig() {
    BasicNatsConnection c = new BasicNatsConnection().withUrl("nats://localhost:4222");
    NatsConsumer p =
        new NatsConsumer().withDestination(new ConfiguredConsumeDestination("MySubject")).withQueueGroup("MyQueueGroup");
    return new StandaloneConsumer(c, p);
  }

}
