package com.adaptris.interlok.nats;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.util.LifecycleHelper;
import io.nats.client.Dispatcher;

public class NatsConnectionTest {


  @Test
  public void testLifecycle() throws Exception {
    MockNatsConnection c = new MockNatsConnection();
    try {
      LifecycleHelper.initAndStart(c);
    } finally {
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testClientConnection() throws Exception {
    MockNatsConnection c = new MockNatsConnection();
    try {
      LifecycleHelper.initAndStart(c);
      assertNotNull(c.clientConnection());
    } finally {
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testClientDispatcher() throws Exception {
    MockNatsConnection c = new MockNatsConnection();
    try {
      LifecycleHelper.initAndStart(c);
      assertNotNull(c.createDispatcher((msg) -> {
      }));
    } finally {
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testClientDispatcher_IllegalState() throws Exception {
    MockNatsConnection c = new MockNatsConnection();
    c.createDispatcher((msg) -> {
    });
  }

  @Test
  public void testCloseDispatcher() throws Exception {
    MockNatsConnection c = new MockNatsConnection();
    Dispatcher d = Mockito.mock(Dispatcher.class);
    c.close(d);
  }
}
