package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class EncounterResourceProvider extends BaseResourceProvider<Encounter> {

	private static final String SF_SERVICE_PROVIDER = "-service-provider";

	public EncounterResourceProvider() {
		super(Encounter.class);
	}

	@Search
	public List<IResource> search(
			@OptionalParam(name = Encounter.SP_PATIENT + "." + Patient.SP_IDENTIFIER) TokenParam patientId,
			@OptionalParam(name = Encounter.SP_STATUS) StringParam status,
			@OptionalParam(name = Encounter.SP_TYPE) StringParam type,
			@OptionalParam(name = Encounter.SP_EPISODEOFCARE) StringParam episodeOfCare,
			@OptionalParam(name = Encounter.SP_PART_OF) StringParam partOf,
			@IncludeParam Set<Include> includeParams) {
		Params params = Params.empty();
		if (patientId != null) {
			params.add(Encounter.SP_PATIENT, patientId.getValue());
		}
		if (status != null) {
			params.add(Encounter.SP_STATUS, status.getValue());
		}
		if (type != null) {
			params.add(Encounter.SP_TYPE, type.getValue());
		}
		if (episodeOfCare != null) {
			params.add(Encounter.SP_EPISODEOFCARE, episodeOfCare.getValue());
		}
		if (partOf != null) {
			params.add(Encounter.SP_PART_OF, partOf.getValue());
		}
		return getResourceDao().search(params, includeParams);
	}
}
