package com.adaptris.interlok.nats;

import org.mockito.Mockito;
import io.nats.client.Connection;


public class MockConnectionFromProperties extends NatsConnectionFromProperties {

  private transient Connection mockConn;

  public MockConnectionFromProperties() throws Exception {
    mockConn = Mockito.mock(Connection.class);
  }

  @Override
  protected Connection connect() throws Exception {
    return mockConn;
  }
}
