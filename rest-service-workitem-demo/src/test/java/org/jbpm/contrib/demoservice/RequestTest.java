package org.jbpm.contrib.demoservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RequestTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void serialize() throws JsonProcessingException {
        RequestA requestA = new RequestA();
        requestA.setCallbackUrl("${handler.callback.url}");
        requestA.setName("Matej");
        System.out.println(objectMapper.writeValueAsString(requestA));

        RequestB requestB = new RequestB();
        requestB.setCallbackUrl("${handler.callback.url}");
        requestB.setNameFromA("${task.A.name}");
        requestB.setSurname("Lazar");

        System.out.println(objectMapper.writeValueAsString(requestB));
    }

}