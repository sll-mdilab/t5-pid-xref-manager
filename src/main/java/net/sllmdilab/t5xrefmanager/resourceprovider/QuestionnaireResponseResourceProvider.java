package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireResponse;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class QuestionnaireResponseResourceProvider extends BaseResourceProvider<QuestionnaireResponse> {
	public QuestionnaireResponseResourceProvider() {
		super(QuestionnaireResponse.class);
	}
	
	@Search
	public List<IResource> search(@OptionalParam(name = QuestionnaireResponse.SP_PATIENT) ReferenceParam patient) {
		Params params = Params.empty();
		if (patient != null) {
			params.add(QuestionnaireResponse.SP_PATIENT, patient.getValue());
		}
		return getResourceDao().search(params);
	}
}
