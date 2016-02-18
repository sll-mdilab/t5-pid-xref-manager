package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.Appointment;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class AppointmentResourceProvider extends BaseResourceProvider<Appointment> {
	public AppointmentResourceProvider() {
		super(Appointment.class);
	}
	
	@Search
	public List<IResource> search(@OptionalParam(name = Appointment.SP_STATUS) StringParam status,
			@OptionalParam(name = Appointment.SP_DATE) DateRangeParam dateRange,
 @IncludeParam Set<Include> includeParams) {
		Params params = Params.empty();

		if (status != null) {
			params.add(Appointment.SP_STATUS, status.getValue());
		}
		
		if (dateRange != null) {
			if(dateRange.getLowerBoundAsInstant() != null) {
				params.add(Appointment.SP_DATE, "ge", dateRange.getLowerBoundAsInstant());
			}
			
			if(dateRange.getUpperBoundAsInstant() != null) {
				params.add(Appointment.SP_DATE, "le", dateRange.getUpperBoundAsInstant());
			}
		}

		return (List<IResource>) getResourceDao().search(params, includeParams);
	}
}
