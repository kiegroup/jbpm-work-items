package org.jbpm.contrib.demoservices;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Ryszard Kozmik
 */
public class RequestA {
    
    //protected static ObjectMapper objectMapper = new ObjectMapper();

    private String callbackUrl;

    private String name;
    
    private String callbackMethod;
    
    private Param[] callbackParams;

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCallbackMethod() {
        return callbackMethod;
    }

    public void setCallbackMethod(String callbackMethod) {
        this.callbackMethod = callbackMethod;
    }
    
    public Param[] getCallbackParams() {
        return callbackParams;
    }

    public void setCallbackParams(Param[] callbackParams) {
        this.callbackParams = callbackParams;
    }

    public List<NameValuePair> getCallbackParamsAsNameValuePairList() {
        
        if( callbackParams==null ) {
            return null;
        }
        
        return Arrays.stream(callbackParams)
                .map(param -> param.toNameValuePair())
                .collect(Collectors.toList());
        
        /*try {
            return Arrays.stream(objectMapper.readValue(callbackParams, Param[].class))
                    .map(param -> param.toNameValuePair())
                    .collect(Collectors.toList());
        } catch (Exception e ) {
            e.printStackTrace();
            return new LinkedList<>();
        }*/
    }

}
