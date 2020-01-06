package com.adaptris.interlok.nats;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.adaptris.util.TimeInterval;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;


public class MockNatsConnection extends BasicNatsConnection {

  private transient Connection mockConn;
  private transient Message mockReply;
  private transient Dispatcher mockDispatcher;
  // This feels dodgy...
  private transient ArgumentCaptor<MessageHandler> arg = ArgumentCaptor.forClass(MessageHandler.class);
  private boolean exceptionOnConnect = false;
  // private transient MessageHandler handler;

  public MockNatsConnection() throws Exception {
    this(false, false);
  }

  public MockNatsConnection(boolean exceptionOnConnect, boolean exceptionOnActivity) throws Exception {
    mockConn = Mockito.mock(Connection.class);
    mockReply = Mockito.mock(Message.class);
    mockDispatcher = Mockito.mock(Dispatcher.class);
    if (exceptionOnActivity) {
      Mockito.doThrow(new RuntimeException()).when(mockConn).publish(anyString(), any());
      Mockito.doThrow(new RuntimeException()).when(mockConn).request(anyString(), any(), any(Duration.class));
      Mockito.doThrow(new RuntimeException()).when(mockConn).createDispatcher(any());
    } else {
      Mockito.when(mockConn.createDispatcher(arg.capture())).thenReturn(mockDispatcher);
      Mockito.when(mockConn.request(anyString(), any(), any(Duration.class))).thenReturn(mockReply, (Message) null);
      // This is arguably better, but arg.capture() is much simpler!
      // Mockito.when(mockConn.createDispatcher(ArgumentMatchers.any())).thenAnswer(new Answer<Dispatcher>() {
      // @Override
      // public Dispatcher answer(InvocationOnMock invocation) throws Throwable {
      // handler = invocation.getArgument(0, MessageHandler.class);
      // return mockDispatcher;
      // }
      // });
    }
    this.exceptionOnConnect = exceptionOnConnect;
    Mockito.when(mockReply.getData()).thenReturn("goodbye cruel world".getBytes(StandardCharsets.UTF_8));
    Mockito.when(mockConn.getMaxPayload()).thenReturn(1024L);
    setConnectionAttempts(5);
    setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
  }

  @Override
  protected Connection connect() throws Exception {
    if (exceptionOnConnect) {
      throw new Exception();
    }
    return mockConn;
  }

  public MockNatsConnection withConnection(Connection c) {
    mockConn = c;
    return this;
  }

  public void submit(Message msg) throws Exception {
    arg.getValue().onMessage(msg);
  }

}
