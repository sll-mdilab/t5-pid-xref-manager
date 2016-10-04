package net.sllmdilab.t5xrefmanager.cdshooks;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Needs refactoring. Complete once communication has been integrated.
 * 
 * */
public class ReadFromExampleFileTest {

	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		CDSRequest cdsRequest = mapper.readValue(new File("src/main/resources//exampleCDSRequest.json"),
				CDSRequest.class);
		String jsonString = mapper.writeValueAsString(cdsRequest);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);

		String cdsResponse = restTemplate.postForObject(
				"https://cds-hooks-poc.cambiocds.com/cds-services/modified-early-warning-score", entity, String.class);
		System.out.println("CDS Response: " + cdsResponse);

		CDSResponse responseObject = mapper.readValue(cdsResponse, CDSResponse.class);

		System.out.println("End Result:" + responseObject.getCards().get(0).getSummary());

	}

}
