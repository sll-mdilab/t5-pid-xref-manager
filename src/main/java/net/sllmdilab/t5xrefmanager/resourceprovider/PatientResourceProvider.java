package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class PatientResourceProvider extends BaseResourceProvider<Patient> {

	public PatientResourceProvider() {
		super(Patient.class);
	}
	
	@Create
	@Override
	public MethodOutcome create(@ResourceParam Patient resource) {
		if (resource.getId() == null || !resource.getId().hasIdPart()) {
			if(!resource.getIdentifierFirstRep().isEmpty()){
				resource.setId(new IdDt(resource.getIdentifierFirstRep().getValue()));
			} else {
				throw new UnprocessableEntityException("Patient resource missing identifier.");
			}
		}

		getResourceDao().insert(resource);
		return new MethodOutcome(resource.getId(), true);
	}

	@Search
	public List<IResource> search(@OptionalParam(name = Patient.SP_NAME) StringParam name,
			@OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier) {

		Params params = Params.empty();
		if (name != null) {
			params.add(Patient.SP_NAME, name.getValue());
		}
		if (identifier != null) {
			params.add(Patient.SP_IDENTIFIER, identifier.getValue());
		}
		return getResourceDao().search(params);
	}
}
