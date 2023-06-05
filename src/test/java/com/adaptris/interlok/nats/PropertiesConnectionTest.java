package com.adaptris.interlok.nats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;
import io.nats.client.Options;

public class PropertiesConnectionTest {

  @Test
  public void testLifecycle() throws Exception {
    NatsConnectionFromProperties c =
        new MockConnectionFromProperties().withConnectionProperties(new KeyValuePairSet()).withConnectionRetries(5,
            new TimeInterval(100L, TimeUnit.MILLISECONDS));
    try {
      LifecycleHelper.initAndStart(c);
      assertEquals(Options.DEFAULT_URL, c.connectionName());
    } finally {
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testConnectionName() throws Exception {
    assertEquals(Options.DEFAULT_URL, NatsConnectionFromProperties.findConnectionName(new KeyValuePairSet()));
    KeyValuePairSet keys = new KeyValuePairSet(Arrays.asList(new KeyValuePair(Options.PROP_CONNECTION_NAME, "MyConnectionName")));
    assertEquals("MyConnectionName", NatsConnectionFromProperties.findConnectionName(keys));
    KeyValuePairSet keys2 = new KeyValuePairSet(Arrays.asList(new KeyValuePair(Options.PROP_URL, "nats://localhost:1234")));
    assertEquals("nats://localhost:1234", NatsConnectionFromProperties.findConnectionName(keys2));
  }

}
