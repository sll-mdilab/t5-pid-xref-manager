package net.sllmdilab.t5xrefmanager.cdshooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Source {

	private String label;
	private String url;
	
	public Source(){
		
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	public String getUrl(){
		return this.url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
}