package com.adaptris.interlok.nats;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import java.time.Duration;
import java.util.Optional;
import javax.validation.Valid;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  /**
   * The destination is the NATS Subject.
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @Removal(version = "4.0.0", message = "Use 'subject' instead")
  private ProduceDestination destination;

  /**
   * The NATS Subject
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String subject;

  private transient boolean destWarning;


  @Override
  public void prepare() throws CoreException {
    DestinationHelper.logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, use 'subject' instead", LoggingHelper.friendlyName(this));
    DestinationHelper.mustHaveEither(getSubject(), getDestination());
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    try {
      Connection conn = retrieveConnection(NatsConnection.class).clientConnection();
      conn.publish(endpoint, toByteArray(msg, conn.getMaxPayload()));
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
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, String endpoint, long timeout)
      throws ProduceException {
    AdaptrisMessage reply = defaultIfNull(getMessageFactory()).newMessage();
    try {
      Connection conn = retrieveConnection(NatsConnection.class).clientConnection();
      byte[] bytes = toByteArray(msg, conn.getMaxPayload());
      Message natsReply =
          Optional.ofNullable(conn.request(endpoint, bytes, Duration.ofMillis(timeout)))
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

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return DestinationHelper.resolveProduceDestination(getSubject(), getDestination(), msg);
  }
}
