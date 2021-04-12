package com.adaptris.interlok.nats;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.util.LifecycleHelper;
import io.nats.client.Connection;

public class NatsProducerTest {

  @Test
  public void testToByteArray() throws Exception {
    NatsProducer p = new NatsProducer().withSubject("hello");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
    assertNotNull(p.toByteArray(msg, 1024L));
  }

  @Test(expected = ProduceException.class)
  public void testToByteArray_ExceedsMax() throws Exception {
    NatsProducer p = new NatsProducer().withSubject("hello");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
    assertNotNull(p.toByteArray(msg, 1L));
  }

  @Test
  public void testProduce() throws Exception {
    MockNatsConnection conn = new MockNatsConnection();
    NatsProducer p = new NatsProducer().withSubject("hello");
    StandaloneProducer producer = new StandaloneProducer(conn, p);
    try {
      LifecycleHelper.initAndStart(producer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
      producer.doService(msg);
      Connection c = conn.connect();
      Mockito.verify(c, Mockito.times(1)).publish(anyString(), any());
    } finally {
      LifecycleHelper.stopAndClose(producer);
    }
  }

  @Test(expected = ServiceException.class)
  public void testProduce_Exception() throws Exception {
    MockNatsConnection conn = new MockNatsConnection(false, true);
    NatsProducer p = new NatsProducer().withSubject("hello");
    StandaloneProducer producer = new StandaloneProducer(conn, p);
    try {
      LifecycleHelper.initAndStart(producer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
      producer.doService(msg);
    } finally {
      LifecycleHelper.stopAndClose(producer);
    }
  }

  @Test
  public void testRequest() throws Exception {
    MockNatsConnection conn = new MockNatsConnection();
    NatsProducer p = new NatsProducer().withSubject("hello");
    StandaloneRequestor requestor = new StandaloneRequestor(conn, p);
    try {
      LifecycleHelper.initAndStart(requestor);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
      requestor.doService(msg);
      Connection c = conn.connect();
      Mockito.verify(c, Mockito.times(1)).request(anyString(), any(), any(Duration.class));
      assertNotEquals("hello world".getBytes(StandardCharsets.UTF_8), msg.getPayload());
    } finally {
      LifecycleHelper.stopAndClose(requestor);
    }
  }

  @Test(expected = ServiceException.class)
  public void testRequest_Exception() throws Exception {
    MockNatsConnection conn = new MockNatsConnection(false, true);
    NatsProducer p = new NatsProducer().withSubject("hello");
    StandaloneRequestor requestor = new StandaloneRequestor(conn, p);
    try {
      LifecycleHelper.initAndStart(requestor);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
      requestor.doService(msg);
    } finally {
      LifecycleHelper.stopAndClose(requestor);
    }
  }

  @Test(expected = ServiceException.class)
  public void testRequest_Timeout() throws Exception {
    MockNatsConnection conn = new MockNatsConnection();
    NatsProducer p = new NatsProducer().withSubject("hello");
    StandaloneRequestor requestor = new StandaloneRequestor(conn, p);
    try {
      LifecycleHelper.initAndStart(requestor);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
      requestor.doService(msg);
      requestor.doService(msg); // 2nd time around will return null, thus causing a ProduceException...
    } finally {
      LifecycleHelper.stopAndClose(requestor);
    }
  }
}
