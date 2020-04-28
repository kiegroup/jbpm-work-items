package org.jbpm.contrib.demoservices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * @author Ryszard Kozmik
 *
 */
public class Param {

    private String name;
    private String value;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public NameValuePair toNameValuePair() {
        return new BasicNameValuePair(name, value);
    }
    
    @Override
    public String toString() {
        return "Param [" + (name != null ? "name=" + name + ", " : "")
                + (value != null ? "value=" + value : "") + "]";
    }
    
}
