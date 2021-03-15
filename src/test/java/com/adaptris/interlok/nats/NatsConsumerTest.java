package com.adaptris.interlok.nats;

import static org.junit.Assert.assertEquals;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import io.nats.client.Message;

public class NatsConsumerTest {

  @Test
  public void testLocationKey() throws Exception {
    NatsConsumer c = new NatsConsumer().withSubject("hello");
    assertEquals(NatsConstants.NATS_SUBJECT, c.consumeLocationKey());
  }

  @Test
  public void testConsumer() throws Exception {
    MockNatsConnection conn = new MockNatsConnection();
    MockMessageListener mockListener = new MockMessageListener();
    NatsConsumer c = new NatsConsumer().withSubject("hello");
    Message msg = Mockito.mock(Message.class);
    Mockito.when(msg.getData()).thenReturn("hello world".getBytes(StandardCharsets.UTF_8));
    Mockito.when(msg.getSID()).thenReturn("subscriptionID");
    Mockito.when(msg.getSubject()).thenReturn("theSubject");
    Mockito.when(msg.getReplyTo()).thenReturn("theReplyTo");

    StandaloneConsumer consumer = new StandaloneConsumer(conn, c);
    consumer.registerAdaptrisMessageListener(mockListener);
    try {
      LifecycleHelper.initAndStart(consumer);
      conn.submit(msg);
      BaseCase.waitForMessages(mockListener, 1);
      AdaptrisMessage consumed = mockListener.getMessages().get(0);
      assertEquals("hello world", consumed.getContent());
    } finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }

  @Test
  public void testConsumer_QueueGroup() throws Exception {
    MockNatsConnection conn = new MockNatsConnection();
    MockMessageListener mockListener = new MockMessageListener();
    NatsConsumer c = new NatsConsumer().withSubject("hello").withQueueGroup("group");
    Message msg = Mockito.mock(Message.class);
    Mockito.when(msg.getData()).thenReturn("hello world".getBytes(StandardCharsets.UTF_8));
    Mockito.when(msg.getSID()).thenReturn("subscriptionID");
    Mockito.when(msg.getSubject()).thenReturn("theSubject");
    Mockito.when(msg.getReplyTo()).thenReturn("theReplyTo");

    StandaloneConsumer consumer = new StandaloneConsumer(conn, c);
    consumer.registerAdaptrisMessageListener(mockListener);
    try {
      LifecycleHelper.initAndStart(consumer);
      conn.submit(msg);
      BaseCase.waitForMessages(mockListener, 1);
      AdaptrisMessage consumed = mockListener.getMessages().get(0);
      assertEquals("hello world", consumed.getContent());
    } finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }

  @Test(expected = CoreException.class)
  public void testConsumer_ConnectionException() throws Exception {
    MockNatsConnection conn = new MockNatsConnection(true, false);
    MockMessageListener mockListener = new MockMessageListener();
    NatsConsumer c = new NatsConsumer().withQueueGroup("group");
    c.setSubject("hello");
    StandaloneConsumer consumer = new StandaloneConsumer(conn, c);
    consumer.registerAdaptrisMessageListener(mockListener);
    try {
      LifecycleHelper.initAndStart(consumer);
    } finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }

  @Test(expected = CoreException.class)
  public void testConsumer_ConsumerException() throws Exception {
    MockNatsConnection conn = new MockNatsConnection(false, true);
    MockMessageListener mockListener = new MockMessageListener();
    NatsConsumer c = new NatsConsumer().withSubject("hello").withQueueGroup("group");
    StandaloneConsumer consumer = new StandaloneConsumer(conn, c);
    consumer.registerAdaptrisMessageListener(mockListener);
    try {
      LifecycleHelper.initAndStart(consumer);
    } finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }
}
