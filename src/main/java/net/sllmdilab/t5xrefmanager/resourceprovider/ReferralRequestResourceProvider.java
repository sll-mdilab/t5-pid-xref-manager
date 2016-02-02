package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.ReferralRequest;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class ReferralRequestResourceProvider extends BaseResourceProvider<ReferralRequest> {
	private static final String SP_SUPPORTING_INFORMATION = "-supporting-information";
	private static final String SP_ENCOUNTER = "-encounter";

	public ReferralRequestResourceProvider() {
		super(ReferralRequest.class);
	}

	@Search
	public List<IResource> search(@OptionalParam(name = ReferralRequest.SP_REQUESTER) ReferenceParam requester,
			@OptionalParam(name = ReferralRequest.SP_RECIPIENT) ReferenceParam recipient,
			@OptionalParam(name = ReferralRequest.SP_STATUS) StringParam status,
			@OptionalParam(name = ReferralRequest.SP_PATIENT) ReferenceParam patient, @IncludeParam Set<Include> includeParams) {
		Params params = Params.empty();
		if (requester != null) {
			params.add(ReferralRequest.SP_REQUESTER, requester.getValue());
		}
		if (status != null) {
			params.add(ReferralRequest.SP_STATUS, status.getValue());
		}
		if (recipient != null) {
			params.add(ReferralRequest.SP_RECIPIENT, recipient.getValue());
		}
		if (patient != null) {
			params.add(ReferralRequest.SP_PATIENT, patient.getValue());
		}

		return getResourceDao().search(params, includeParams);
	}
}
