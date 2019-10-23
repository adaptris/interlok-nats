package com.adaptris.interlok.nats;

public interface NatsConstants {

  /**
   * Metadata key that stores {@code Message#getSID()}; value is {@value #NATS_SUBSCRIPTION_ID}.
   * 
   */
  static String NATS_SUBSCRIPTION_ID = "natsSubscriptionID";
  /**
   * Metadata key that stores {@code Message#getSubject()}; value is {@value #NATS_SUBJECT}.
   * 
   */
  static String NATS_SUBJECT = "natsSubject";
  /**
   * Metadata key that stores {@code Message#getReplyTo()}; value is {@value #NATS_REPLY_TO}.
   * 
   */
  static String NATS_REPLY_TO = "natsReplyTo";
}
