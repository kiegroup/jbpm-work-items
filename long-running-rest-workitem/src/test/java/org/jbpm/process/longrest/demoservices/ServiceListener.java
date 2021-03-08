/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.longrest.demoservices;

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
