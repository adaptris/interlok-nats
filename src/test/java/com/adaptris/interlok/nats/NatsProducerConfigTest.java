package com.adaptris.interlok.nats;

import com.adaptris.core.StandaloneProducer;
import com.adaptris.interlok.junit.scaffolding.ExampleProducerCase;

public class NatsProducerConfigTest extends ExampleProducerCase {

  @Override
  protected StandaloneProducer retrieveObjectForSampleConfig() {
    BasicNatsConnection c = new BasicNatsConnection().withUrl("nats://localhost:4222");

    NatsProducer p = new NatsProducer().withSubject("MySubject");
    return new StandaloneProducer(c, p);
  }

}
