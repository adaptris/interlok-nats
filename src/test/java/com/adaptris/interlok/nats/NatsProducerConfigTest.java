package com.adaptris.interlok.nats;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;

public class NatsProducerConfigTest extends ProducerCase {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Override
  protected StandaloneProducer retrieveObjectForSampleConfig() {
    BasicNatsConnection c = new BasicNatsConnection().withUrl("nats://localhost:4222");
    NatsProducer p = new NatsProducer().withDestination(new ConfiguredProduceDestination("MySubject"));
    return new StandaloneProducer(c, p);
  }

}
