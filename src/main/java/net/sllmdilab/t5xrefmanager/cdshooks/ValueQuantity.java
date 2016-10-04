package net.sllmdilab.t5xrefmanager.cdshooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueQuantity {

    private int value;
    private String unit;

    public ValueQuantity() {
    }

    public int getValue(){
    	return this.value;
    }
    
    public void setValue(int value){
    	this.value = value;
    }
    
    public String getUnit(){
    	return this.unit;
    }
    
    public void setUnit(String unit){
    	this.unit = unit;
    }

//    @Override
//    public String toString() {
//        return "Value{" +
//                "id=" + id +
//                ", quote='" + quote + '\'' +
//                '}';
//    }
}
