package net.sllmdilab.t5xrefmanager.cdshooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {

    private String reference;

    public Subject() {
    }

    public String getReference(){
    	return this.reference;
    }
    
    public void setReference(String reference){
    	this.reference = reference;
    }

//    @Override
//    public String toString() {
//        return "Value{" +
//                "id=" + id +
//                ", quote='" + quote + '\'' +
//                '}';
//    }
}
