package net.sllmdilab.t5xrefmanager.cdshooks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CDSResponse {
	List<Card> cards;
	
	public CDSResponse(){
		
	}
	
	public List<Card> getCards(){
		return this.cards;
	}
	
	public void setCards(List<Card> cards){
		this.cards = cards;
	}
	
}