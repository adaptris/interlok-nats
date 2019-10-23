package com.adaptris.interlok.nats;

import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ConnectionErrorHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.nats.client.Connection;
import io.nats.client.Nats;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Basic Connection to a NATS server.
 * <p>
 * This connection assumes that {@code Nats#connect(String)} is sufficient configuration for connecting to your NATS server (i.e.
 * everything you need is configurable via the URL).
 * </p>
 * <p>
 * Configuring any kind of {@link ConnectionErrorHandler} has no effect on this type of connection, as we will be relying on the
 * automatic reconnection that happens behind the seens from the NATS core java client. By default outgoing messages will be
 * buffered in memory; pending reconnection and subsequent redelivery.
 * </p>
 * 
 * @config nats-server-basic-connection
 *
 */
@XStreamAlias("nats-server-basic-connection")
@ComponentProfile(summary = "Basic connection to a NATS server", tag = "nats.io, nats", since = "3.9.3")
@NoArgsConstructor
public class BasicNatsConnection extends NatsConnection {

  /**
   * The URL to the nats server assuming that {@code Nats#connect(String)} is sufficient configuration for connecting to your NATS
   * server.
   * 
   * <p>
   * The Java client generally expects URLs of the form {@code nats://hostname:port} but has support for the additional forms:
   * <ul>
   * <li>{@code nats://user:pass@hostname:port}</li>
   * <li>{@code nats://token@hostname:port}</li>
   * <li>{@code opentls://hostname:port} which will use TLS but accept "any certificates"</li>
   * <li>{@code tls://hostname:port} which will use TLS using the default SSL Context and may require client certificates</li>
   * </p>
   */
  @Getter
  @Setter
  @NotBlank
  @NonNull
  private String url;

  @Override
  protected Connection connect() throws Exception {
    return Nats.connect(getUrl());
  }

  public <T extends BasicNatsConnection> T withUrl(String u) {
    setUrl(u);
    return (T) this;
  }
}
