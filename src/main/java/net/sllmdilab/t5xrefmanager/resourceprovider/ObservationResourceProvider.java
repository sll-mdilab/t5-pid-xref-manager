package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.service.ObservationService;

@Component
public class ObservationResourceProvider implements IResourceProvider {
	private static final String TRUE = "true";

	@Autowired
	private ObservationService observationService;

	@Override
	public Class<Observation> getResourceType() {
		return Observation.class;
	}

	@Search()
	public List<Observation> searchForPatient(@RequiredParam(name = Observation.SP_SUBJECT) TokenParam patientId,
			@OptionalParam(name = Observation.SP_DATE) DateRangeParam dateRange,
			@OptionalParam(name = Observation.SP_CODE) StringParam observationCode,
			@OptionalParam(name = "-summary") StringParam summary,
			@OptionalParam(name = "-samplingPeriod") NumberParam sampleRate) throws Exception {
		Date start = T5FHIRUtils.getStartTimeFromNullableRange(dateRange);
		Date end = T5FHIRUtils.getEndTimeFromNullableRange(dateRange);

		boolean isSummary = TRUE.equalsIgnoreCase(T5FHIRUtils.getValueOrNull(summary));

		if (isSummary) {
			return observationService.searchSummaryByPatient(patientId.getValue(), start, end);
		} else if (observationCode == null) {
			throw new InvalidRequestException("Observation type code missing.");
		} else {
			return observationService.searchByPatient(patientId.getValue(), observationCode.getValue(), start, end,
					getSamplingPeriodFromParam(sampleRate));
		}
	}

	private int getSamplingPeriodFromParam(NumberParam sampleRate) {
		return sampleRate != null ? sampleRate.getValue().intValue() * 1000 : 0;
	}

	@Search
	public List<Observation> searchForDevice(@RequiredParam(name = Observation.SP_DEVICE) TokenParam deviceId,
			@OptionalParam(name = Observation.SP_DATE) DateRangeParam dateRange,
			@OptionalParam(name = Observation.SP_CODE) StringParam observationCode,
			@OptionalParam(name = "-summary") StringParam summary,
			@OptionalParam(name = "-samplingPeriod") NumberParam sampleRate) throws Exception {

		Date start = T5FHIRUtils.getStartTimeFromNullableRange(dateRange);
		Date end = T5FHIRUtils.getEndTimeFromNullableRange(dateRange);

		boolean isSummary = TRUE.equalsIgnoreCase(T5FHIRUtils.getValueOrNull(summary));
		if (isSummary) {
			return observationService.searchSummaryByDevice(deviceId.getValue(), start, end);
		} else {
			if (observationCode == null) {
				throw new InvalidRequestException("Observation type code missing.");
			}
			return observationService.searchByDevice(deviceId.getValue(), observationCode.getValue(), start, end,
					getSamplingPeriodFromParam(sampleRate));
		}
	}
}
