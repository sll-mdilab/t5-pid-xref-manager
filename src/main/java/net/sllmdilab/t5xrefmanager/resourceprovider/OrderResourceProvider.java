package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Order;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class OrderResourceProvider extends BaseResourceProvider<Order> {

	public OrderResourceProvider() {
		super(Order.class);
	}
	
	@Search
	public List<IResource> search(@OptionalParam(name = Order.SP_PATIENT) ReferenceParam patient) {
		Params params = Params.empty();
		if (patient != null) {
			params.add(Order.SP_PATIENT, patient.getValue());
		}
		return getResourceDao().search(params);
	}

}
