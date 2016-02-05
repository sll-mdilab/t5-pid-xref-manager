package net.sllmdilab.t5xrefmanager.resourceprovider;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.QuestionnaireResponse;

@Component
public class QuestionnaireResponseResourceProvider extends BaseResourceProvider<QuestionnaireResponse> {
	public QuestionnaireResponseResourceProvider() {
		super(QuestionnaireResponse.class);
	}
}
