package com.adaptris.interlok.nats;

import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.CoreException;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Basic Connection to a NATS server.
 * <p>
 * This connection assumes that {@code Nats#connect(String)} is sufficient configuration for connecting to your NATS server (i.e.
 * everything you need is configurable via the URL). Generally put your 'closest' NATS server as the URL, since it will discover
 * other servers after it initially connects and use those as required.
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
   * The Java client generally expects URLs of the form {@code nats://hostname:port} but has support for the additional forms (it
   * can be a list that is comma separated):
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
  @InputFieldDefault(value = Options.DEFAULT_URL)
  @AutoPopulated
  private String url = Options.DEFAULT_URL;

  @Override
  protected void prepareConnection() throws CoreException {
    super.prepareConnection();
    Args.notNull(url, "url");
  }

  @Override
  protected Connection connect() throws Exception {
    return Nats.connect(getUrl());
  }

  public <T extends BasicNatsConnection> T withUrl(String u) {
    setUrl(u);
    return (T) this;
  }

  @Override
  protected String connectionName() {
    return getUrl();
  }
}
