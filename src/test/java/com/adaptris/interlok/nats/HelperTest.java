package com.adaptris.interlok.nats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;

public class HelperTest extends Helper {


  @Test
  public void testBuild() throws Exception {
    Message msg = Mockito.mock(Message.class);
    Mockito.when(msg.getData()).thenReturn("hello world".getBytes(StandardCharsets.UTF_8));
    Mockito.when(msg.getSID()).thenReturn("subscriptionID");
    Mockito.when(msg.getSubject()).thenReturn("theSubject");
    Mockito.when(msg.getReplyTo()).thenReturn("theReplyTo");
    AdaptrisMessage amsg = build(msg, null);
    assertEquals("hello world", amsg.getContent());
    assertTrue(amsg.headersContainsKey(NatsConstants.NATS_SUBSCRIPTION_ID));
    assertEquals("subscriptionID", amsg.getMetadataValue(NatsConstants.NATS_SUBSCRIPTION_ID));

    assertTrue(amsg.headersContainsKey(NatsConstants.NATS_SUBJECT));
    assertEquals("theSubject", amsg.getMetadataValue(NatsConstants.NATS_SUBJECT));

    assertTrue(amsg.headersContainsKey(NatsConstants.NATS_REPLY_TO));
    assertEquals("theReplyTo", amsg.getMetadataValue(NatsConstants.NATS_REPLY_TO));

  }

  @Test
  public void testBuild_NoMetadata() throws Exception {
    Message msg = Mockito.mock(Message.class);
    Mockito.when(msg.getData()).thenReturn("hello world".getBytes(StandardCharsets.UTF_8));
    AdaptrisMessage amsg = build(msg, null);
    assertEquals("hello world", amsg.getContent());
    assertFalse(amsg.headersContainsKey(NatsConstants.NATS_SUBSCRIPTION_ID));
  }

  @Test
  public void testCloseQuietly() throws Exception {
    closeQuietly(null);
    closeQuietly(() -> {
    });
  }

  @Test
  public void closeDispatcher() throws Exception {
    closeDispatcher(null, (Dispatcher) null);
    Dispatcher d = Mockito.mock(Dispatcher.class);
    Connection c = Mockito.mock(Connection.class);
    closeDispatcher(c, d);
  }


  @Test
  public void closeDispatchers() throws Exception {
    ArrayList<Dispatcher> list = new ArrayList<>();
    list.add(Mockito.mock(Dispatcher.class));
    list.add(Mockito.mock(Dispatcher.class));
    Connection c = Mockito.mock(Connection.class);
    closeDispatchers(c, list);
  }
}
