package com.adaptris.interlok.nats;

import static com.adaptris.interlok.nats.NatsConstants.NATS_REPLY_TO;
import static com.adaptris.interlok.nats.NatsConstants.NATS_SUBJECT;
import static com.adaptris.interlok.nats.NatsConstants.NATS_SUBSCRIPTION_ID;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import java.util.Collection;
import java.util.Optional;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.util.Args;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;

/**
 * Helper for testability.
 * 
 */
public abstract class Helper {

  /**
   * Create an {@link AdaptrisMessage} from the incoming {@code Message}.
   * 
   * <p>if the corresponding {@code Message#getSID()}, {@code Message#getSubject()}, {@code Message#getReplyTo()} are not blank
   * then they will be added against their respective metadata keys (defined in {@link NatsConstants}).
   * </p>
   * 
   * @param nats the message from the nats server
   * @param mf the message factory (default factory is used if null)
   * @return the new AdaptrisMessage.
   */
  public static AdaptrisMessage build(Message nats, AdaptrisMessageFactory mf) {
    AdaptrisMessage msg = AdaptrisMessageFactory.defaultIfNull(mf).newMessage(nats.getData());
    // if value is not empty, then add it, otherwise, do nothing.
    // better code coverage; less readability?
    Optional.ofNullable(trimToNull(nats.getSID())).ifPresent((v) -> msg.addMetadata(NATS_SUBSCRIPTION_ID, v));
    Optional.ofNullable(trimToNull(nats.getSubject())).ifPresent((v) -> msg.addMetadata(NATS_SUBJECT, v));
    Optional.ofNullable(trimToNull(nats.getReplyTo())).ifPresent((v) -> msg.addMetadata(NATS_REPLY_TO, v));
    return msg;
  }

  public static void closeQuietly(AutoCloseable c) {
    try {
      Args.notNull(c, "closeable");
      c.close();
    } catch (Exception ignored) {

    }
  }

  public static void closeDispatchers(Connection conn, Collection<Dispatcher> dispatchers) {
    dispatchers.forEach((d) -> closeDispatcher(conn, d));
  }

  public static void closeDispatcher(Connection c, Dispatcher d) {
    try {
      Args.notNull(c, "connection");
      c.closeDispatcher(d);
    } catch (Exception ignored) {

    }
  }
}
