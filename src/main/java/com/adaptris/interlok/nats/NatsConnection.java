package com.adaptris.interlok.nats;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import lombok.NoArgsConstructor;
import lombok.Synchronized;

@NoArgsConstructor
public abstract class NatsConnection extends AdaptrisConnectionImp {

  private transient Connection currentConnection;
  // otherwise it gets emitted by XSTream
  private transient final Object $lock = new Object[0];
  private transient List<Dispatcher> dispatchers = new ArrayList<>();

  @Override
  protected void prepareConnection() throws CoreException {}

  @Override
  protected void initConnection() throws CoreException {
    try {
      clientConnection();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void startConnection() throws CoreException {}

  @Override
  protected void stopConnection() {}

  @Override
  protected void closeConnection() {
    Helper.closeDispatchers(currentConnection, dispatchers);
    dispatchers.clear();
    Helper.closeQuietly(currentConnection);
    currentConnection = null;
  }

  @Synchronized
  public Connection clientConnection() throws Exception {
    currentConnection = ObjectUtils.defaultIfNull(currentConnection, connect());
    return currentConnection;
  }

  public Dispatcher createDispatcher(MessageHandler handler) throws Exception {
    if (currentConnection == null) {
      throw new IllegalStateException("No Connection available to create a dispatcher (connection not started?)");
    }
    Dispatcher d = currentConnection.createDispatcher(handler);
    dispatchers.add(d);
    return d;
  }

  public void close(Dispatcher dispatcher) {
    Helper.closeDispatcher(currentConnection, dispatcher);
    dispatchers.remove(dispatcher);
  }

  protected abstract Connection connect() throws Exception;

}
