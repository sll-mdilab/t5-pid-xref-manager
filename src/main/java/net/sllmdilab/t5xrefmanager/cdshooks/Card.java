package net.sllmdilab.t5xrefmanager.cdshooks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

	private String summary;
	private String indicator;
	private String detail;
	private Source source;
	private List<Link> links;
	
	public Card(){
		
	}
	
	public String getSummary(){
		return this.summary;
	}
	
	public void setSummary(String summary){
		this.summary = summary;
	}
	
	public String getIndicator(){
		return this.indicator;
	}
	
	public void setIndicator(String indicator){
		this.indicator = indicator;
	}
	public String getDetail(){
		return this.detail;
	}
	
	public void setDetail(String detail){
		this.detail = detail;
	}
	
	public Source getSource(){
		return this.source;
	}
	
	public void setSource(Source source){
		this.source = source;
	}
	
	public List<Link> getLinks(){
		return this.links;
	}
	
	public void setLinks(List<Link> links){
		this.links = links;
	}
}