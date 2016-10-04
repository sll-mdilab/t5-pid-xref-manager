package net.sllmdilab.t5xrefmanager.cdshooks;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CDSRequest {
	
	private UUID hookInstance;
	private String fhirServer;
	private String hook;
	private String redirect;
	private String patient;
	private List<Context> context;

    public CDSRequest() {
    }

    public UUID getHookInstance() {
        return this.hookInstance;
    }

    public void setHookInstance(UUID hookInstance) {
        this.hookInstance = hookInstance;
    }

    public String getFhirServer(){
    	return this.fhirServer;
    }
    
    public void setFhirServer(String fhirServer) {
        this.fhirServer = fhirServer;
    }

    public String getHook(){
    	return this.hook;
    }
    
    public void setHook(String hook) {
        this.hook = hook;
    }
    
    public String getRedirect(){
    	return this.redirect;
    }
    
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
    
    public String getPatient(){
    	return this.patient;
    }
    
    public void setPatient(String patient) {
        this.patient = patient;
    }
    
    public List<Context> getContext(){
    	return this.context;
    }
    
    public void setContext(List<Context> context) {
        this.context = context;
    }
   
}
