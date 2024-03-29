package com.adaptris.interlok.nats;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

import io.nats.client.Dispatcher;

public class NatsConnectionTest {

  @Test
  public void testLifecycle() throws Exception {
    MockNatsConnection c = new MockNatsConnection().withConnectionRetries(5, new TimeInterval(100L, TimeUnit.MILLISECONDS));
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

  @Test
  public void testClientDispatcher_IllegalState() throws Exception {
    MockNatsConnection c = new MockNatsConnection();

    assertThrows(IllegalStateException.class, () -> {
      c.createDispatcher((msg) -> {
      });
    });
  }

  @Test
  public void testCloseDispatcher() throws Exception {
    MockNatsConnection c = new MockNatsConnection();
    Dispatcher d = Mockito.mock(Dispatcher.class);
    c.close(d);
  }

  @Test
  public void testRetryExceedsMaxAttempts() throws Exception {
    new MockNatsConnection().withConnectionRetries(5, null).exceedsMaxAttempts(1, new CustomException());
    new MockNatsConnection().withConnectionRetries(-1, null).exceedsMaxAttempts(1, new CustomException());
    try {
      new MockNatsConnection().withConnectionRetries(5, null).exceedsMaxAttempts(5, new CustomException());
      fail();
    } catch (CustomException expected) {
    }
  }

  private class CustomException extends Exception {
    private static final long serialVersionUID = -1409838507657865817L;
  }

}
