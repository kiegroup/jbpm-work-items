package org.jbpm.process.longrest.demoservices;

import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HeadersListener {
    private List<Consumer<HttpHeaders>> consumers = new ArrayList<>();

    public void accept(HttpHeaders httpHeaders) {
        consumers.forEach(c -> c.accept(httpHeaders));
    }

    public void addConsumer(Consumer<HttpHeaders> headersConsumer) {
        consumers.add(headersConsumer);
    }

    public void removeAllConsumers() {
        consumers.clear();
    }
}
