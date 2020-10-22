package org.jbpm.contrib.demoservices;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

public class ServiceListener {

    private Collection<Subscription> subscriptions = new HashSet<>();

    public void fire(EventType eventType, Object event) {
        subscriptions.stream()
                .filter(s -> eventType.equals(s.getEventType()))
                .forEach(s -> s.getConsumer().accept(event));
    }

    public Subscription subscribe(EventType eventType, Consumer<Object> consumer) {
        Subscription subscription = new Subscription(eventType, consumer);
        subscriptions.add(subscription);
        return subscription;
    }

    public void unsubscribe(Subscription subscription) {
        subscriptions.remove(subscription);
    }

    public class Subscription {
        private EventType eventType;
        private Consumer<Object> consumer;

        public Subscription(EventType eventType, Consumer<Object> consumer) {
            this.eventType = eventType;
            this.consumer = consumer;
        }

        public EventType getEventType() {
            return eventType;
        }

        public Consumer<Object> getConsumer() {
            return consumer;
        }
    }
}
