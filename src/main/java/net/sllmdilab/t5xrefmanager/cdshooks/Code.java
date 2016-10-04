package net.sllmdilab.t5xrefmanager.cdshooks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Code {

    private List<Coding> coding;

	public List<Coding> getCoding() {
		return coding;
	}

	public void setCoding(List<Coding> coding) {
		this.coding = coding;
	}

   
   
}