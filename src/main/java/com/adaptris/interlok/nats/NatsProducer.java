package com.adaptris.interlok.nats;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import java.time.Duration;
import java.util.Optional;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.NoArgsConstructor;

/**
 * Produce the current message to a NATS server with the configured subject.
 * 
 * <p>
 * This effectively uses the {@code Connection#publish(String, byte[])} or
 * {@code Connection#request(String, byte[], java.time.Duration)} to send the message to NATS. The subject will be derived form the
 * configured destination.
 * </p>
 * Since the payload for NATS.io is always an opaque set of bytes; if you wish to include {@link AdaptrisMessage#getMetadata()} in
 * the message, then you will have to configure a {@link AdaptrisMessageEncoder} instance; otherwise all metadata is lost.
 * </p>
 * 
 * @config nats-standard-producer
 *
 */
@XStreamAlias("nats-standard-producer")
@ComponentProfile(summary = "Send a message to a NATS server", tag = "nats.io, nats",
    recommended = {NatsConnection.class}, since = "3.9.3")
@NoArgsConstructor
public class NatsProducer extends RequestReplyProducerImp {

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    try {
      Connection conn = retrieveConnection(NatsConnection.class).clientConnection();
      conn.publish(dest.getDestination(msg), toByteArray(msg, conn.getMaxPayload()));
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
  }


  /**
   * The default timeout if not specified is 60 seconds.
   * 
   */
  @Override
  protected long defaultTimeout() {
    return 60000L;
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination dest, long timeout) throws ProduceException {
    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    try {
      Connection conn = retrieveConnection(NatsConnection.class).clientConnection();
      byte[] bytes = toByteArray(msg, conn.getMaxPayload());
      Message natsReply = Optional.ofNullable(conn.request(dest.getDestination(msg), bytes, Duration.ofMillis(timeout)))
          .orElseThrow(() -> new ProduceException("No Reply in " + timeout + "ms"));
      reply = decode(natsReply.getData());
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    return reply;
  }

  public NatsProducer withDestination(ProduceDestination p) {
    setDestination(p);
    return this;
  }

  protected byte[] toByteArray(AdaptrisMessage msg, long maxSize) throws Exception {
    byte[] bytes = encode(msg);
    if (bytes.length > maxSize) {
      throw new ProduceException(
          "Message Size [" + bytes.length + "] exceeds nats server configuration [" + maxSize + "]");
    }
    return bytes;
  }
}
