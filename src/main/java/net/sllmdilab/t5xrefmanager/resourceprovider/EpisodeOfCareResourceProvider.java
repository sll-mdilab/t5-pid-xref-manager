package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.EpisodeOfCare;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@Component
public class EpisodeOfCareResourceProvider extends BaseResourceProvider<EpisodeOfCare> {
	public EpisodeOfCareResourceProvider() {
		super(EpisodeOfCare.class);
	}

	@Search
	public List<IResource> search(@OptionalParam(name = EpisodeOfCare.SP_TEAM_MEMBER) ReferenceParam teamMember,
			@OptionalParam(name = EpisodeOfCare.SP_STATUS) StringParam status,
			@IncludeParam Set<Include> includeParams) {
		Params params = Params.empty();
		if (teamMember != null) {
			params.add(EpisodeOfCare.SP_TEAM_MEMBER, teamMember.getValue());
		}
		if (status != null) {
			params.add(EpisodeOfCare.SP_STATUS, status.getValue());
		}

		return getResourceDao().search(params, includeParams);
	}
}
