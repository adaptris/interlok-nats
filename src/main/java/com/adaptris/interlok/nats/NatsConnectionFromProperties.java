package com.adaptris.interlok.nats;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.CoreException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Connection to a NATS server.
 * <p>
 * Uses {@code io.nats.client#Options#Builder()} to build the connection via the properties; no validation of properties is done, we
 * just convert the configuration into properties and pass it into the {@code Builder} object.
 * </p>
 * <p>
 * Configuring any kind of {@link ConnectionErrorHandler} has no effect on this type of connection, as we will be relying on the
 * automatic reconnection that happens behind the seens from the NATS core java client.
 * </p>
 * 
 * @config nats-server-properties-connection
 *
 */
@XStreamAlias("nats-server-properties-connection")
@ComponentProfile(summary = "connection to a NATS server", tag = "nats.io, nats", since = "3.9.3")
@NoArgsConstructor
public class NatsConnectionFromProperties extends NatsConnection {

  private static final List<String> KEYS = Arrays.asList(Options.PROP_CONNECTION_NAME, Options.PROP_URL);

  /**
   * The connection properties.
   * <p>
   * You will probably need to refer to the NATS.io documentation to figure out the available properties; they will be passed in
   * as-is to {@code new Options.Builder(Properties)} with no verification. Generally the configuration properties will start with
   * {@code io.nats.client.} but may change between releases.
   * </p>
   * <p>
   * For instance if you wanted to control the URL / Username + Password then you might have
   * <pre>
     {@code
     io.nats.client.url=nats://localhost:1234
     io.nats.client.username=MyUsername
     io.nats.client.password=MyPassword
     io.nats.client.verbose=true
     }
   * </pre>
   * </p>
   */
  @Getter
  @Setter
  private KeyValuePairSet connectionProperties = new KeyValuePairSet();

  private transient String connectionName = "No Configured Connection Name";

  @Override
  protected void prepareConnection() throws CoreException {
    super.prepareConnection();
    connectionName = findConnectionName(connectionProperties());
  }

  @Override
  protected Connection connect() throws Exception {
    return Nats.connect(new Options.Builder(KeyValuePairBag.asProperties(connectionProperties())).build());
  }

  public <T extends NatsConnectionFromProperties> T withConnectionProperties(KeyValuePairSet p) {
    setConnectionProperties(p);
    return (T) this;
  }

  @Override
  protected String connectionName() {
    return connectionName;
  }

  private KeyValuePairSet connectionProperties() {
    return ObjectUtils.defaultIfNull(getConnectionProperties(), new KeyValuePairSet());
  }

  protected static String findConnectionName(KeyValuePairSet p) {
    Optional<KeyValuePair> conName = p.stream().filter(k -> KEYS.contains(k.getKey())).findFirst();
    if (conName.isPresent()) {
      return conName.get().getValue();
    }
    // Nothing configured; probably using the default...
    return Options.DEFAULT_URL;
  }
}
