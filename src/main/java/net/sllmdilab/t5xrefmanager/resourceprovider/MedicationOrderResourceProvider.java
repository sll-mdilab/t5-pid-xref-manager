package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class MedicationOrderResourceProvider extends BaseResourceProvider<MedicationOrder>{

	public MedicationOrderResourceProvider() {
		super(MedicationOrder.class);
	}
	
	@Search
	public List<IResource> search(@OptionalParam(name = MedicationOrder.SP_PATIENT) ReferenceParam patient,
			@OptionalParam(name = MedicationOrder.SP_ENCOUNTER) ReferenceParam encounter,
			@OptionalParam(name = MedicationOrder.SP_DATEWRITTEN) DateParam datewritten,
 @IncludeParam Set<Include> includeParams) {
		Params params = Params.empty();
		if (patient != null) {
			params.add(MedicationOrder.SP_PATIENT, patient.getValue());
		}
		if (encounter != null) {
			params.add(MedicationOrder.SP_ENCOUNTER, encounter.getValue());
		}
		if (datewritten != null) {
			params.add(MedicationOrder.SP_DATEWRITTEN, datewritten.getValue());
		}

		return getResourceDao().search(params, includeParams);
	}
}
