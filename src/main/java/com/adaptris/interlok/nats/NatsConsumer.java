package com.adaptris.interlok.nats;

import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.nats.client.Dispatcher;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consumer implementation for NATS.io.
 * 
 * <p>
 * This uses the NATS core API, and as a result does not offer anything similar to JMS Topic durable susbscribers; NATS core offers
 * an at most once quality of service. If a subscriber is not listening on the subject (no subject match), or is not active when the
 * message is sent, the message is not received. This is the same level of guarantee that TCP/IP provides. By default, NATS is a
 * fire-and-forget messaging system.
 * </p>
 * <p>
 * <strong>When the consumer is stopped; then we automatically unsubscribe from the subscription</strong>
 * </p>
 * 
 * @config nats-standard-consumer
 *
 */
@XStreamAlias("nats-standard-consumer")
@ComponentProfile(summary = "Consumer implementation for NATS.io", tag = "nats.io, nats", since = "3.9.3")
@NoArgsConstructor
public class NatsConsumer extends AdaptrisMessageConsumerImp {

  /**
   * Specify the queue group for this consumer if it is required.
   * <p>
   * By specifying a queue group you implicitly enable built-in load balancing feature called distributed queues. this will balance
   * message delivery across a group of subscribers. All subscribers with the same queue name form the queue group. As messages on
   * the registered subject are published, one member of the group is chosen randomly to receive the message. Although queue groups
   * have multiple subscribers, each message is consumed by only one.
   * </p>
   */
  @Getter
  @Setter
  @AdvancedConfig
  private String queueGroup;

  private transient Dispatcher dispatcher = null;

  @Override
  public void prepare() throws CoreException {

  }

  @Override
  public void start() throws CoreException {
    try {
      dispatcher = retrieveConnection(NatsConnection.class).createDispatcher((msg) -> {
        String oldName = renameThread();
        AdaptrisMessage am = Helper.build(msg, getMessageFactory());
        retrieveAdaptrisMessageListener().onAdaptrisMessage(am);
        Thread.currentThread().setName(oldName);
      });
      String subject = getDestination().getDestination();
      if (!StringUtils.isBlank(getQueueGroup())) {
        dispatcher.subscribe(subject, getQueueGroup());
      } else {
        dispatcher.subscribe(subject);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void stop() {
    retrieveConnection(NatsConnection.class).close(dispatcher);
    dispatcher = null;
  }

  public NatsConsumer withQueueGroup(String s) {
    setQueueGroup(s);
    return this;
  }


  public NatsConsumer withDestination(ConsumeDestination d) {
    setDestination(d);
    return this;
  }

  @Override
  public String consumeLocationKey() {
    return NatsConstants.NATS_SUBJECT;
  }

}
