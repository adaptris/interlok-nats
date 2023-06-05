package com.adaptris.interlok.nats;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.TimeInterval;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import lombok.NoArgsConstructor;
import lombok.Synchronized;

@NoArgsConstructor
public abstract class NatsConnection extends AllowsRetriesConnection {

  private transient Connection currentConnection;
  // otherwise it gets emitted by XSTream
  private transient final Object $lock = new Object[0];
  private transient List<Dispatcher> dispatchers = new ArrayList<>();

  @Override
  protected void prepareConnection() throws CoreException {
  }

  @Override
  protected void initConnection() throws CoreException {
    try {
      Connection c = clientConnection();
      log.trace("Connected to [{}]", c.getConnectedUrl());
      log.trace("Discovered Servers [{}]", c.getServers());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void startConnection() throws CoreException {
  }

  @Override
  protected void stopConnection() {
  }

  @Override
  protected void closeConnection() {
    Helper.closeDispatchers(currentConnection, dispatchers);
    dispatchers.clear();
    Helper.closeQuietly(currentConnection);
    currentConnection = null;
  }

  @Synchronized
  public Connection clientConnection() throws Exception {
    currentConnection = ObjectUtils.defaultIfNull(currentConnection, tryConnect());
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

  protected abstract String connectionName();

  private Connection tryConnect() throws Exception {
    int attemptCount = 0;
    Connection c = null;
    while (c == null) {
      try {
        attemptCount++;
        c = connect();
      } catch (Exception e) {
        if (logWarning(attemptCount)) {
          log.warn("Connection attempt [{}] failed for {}", attemptCount, connectionName(), e);
        }
        exceedsMaxAttempts(attemptCount, e);
        log.trace(createLoggingStatement(attemptCount));
        Thread.sleep(connectionRetryInterval());
        continue;
      }
    }
    return c;
  }

  // Rethrows the exception if the max attempts are exceeded; primary use is for
  // testability.
  protected <T extends Exception> void exceedsMaxAttempts(int attemptCount, T e) throws T {
    if (connectionAttempts() != -1 && attemptCount >= connectionAttempts()) {
      log.error("Failed to make a onnection");
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends NatsConnection> T withConnectionRetries(int i, TimeInterval t) {
    setConnectionAttempts(i);
    setConnectionRetryInterval(t);
    return (T) this;
  }

}
