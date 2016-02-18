package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;
import net.sllmdilab.t5xrefmanager.service.ObservationService;

@Component
public class ObservationResourceProvider extends BaseResourceProvider<Observation> {
	private static final String TRUE = "true";
	private static final String SP_COMMENTS = "-comments";

	@Autowired
	private ObservationService observationService;

	@Autowired
	private FhirbaseResourceDao<Observation> fhirbaseResourceDao;

	public ObservationResourceProvider() {
		super(Observation.class);
	}

	@SuppressWarnings("unchecked")
	@Search()
	public List<Observation> searchAnnotation(@OptionalParam(name = Observation.SP_SUBJECT) TokenParam patientId,
			@OptionalParam(name = Observation.SP_DATE) DateRangeParam dateRange,
			@OptionalParam(name = Observation.SP_CODE) StringParam observationCode,
			@RequiredParam(name = Observation.SP_VALUE_QUANTITY) StringParam valueQuantity, // Using StringParam because QuantityParam has a bug handling :missing
			@RequiredParam(name = SP_COMMENTS) StringParam comments) {
		Params params = Params.empty();

		if (valueQuantity.getMissing() != null) {
			params.add(Observation.SP_VALUE_QUANTITY + ":missing", valueQuantity.getMissing().toString());
		} else {
			throw new InvalidRequestException(Observation.SP_VALUE_QUANTITY + " only supported with :missing-modifier.");
		}
		if (comments.getMissing() != null) {
			params.add(SP_COMMENTS + ":missing", comments.getMissing().toString());
		} else {
			throw new InvalidRequestException(SP_COMMENTS + " only supported with :missing-modifier.");
		}

		return (List<Observation>) (List<? extends IResource>) fhirbaseResourceDao.search(params);
	}

	@Search()
	public List<Observation> searchForPatient(@RequiredParam(name = Observation.SP_SUBJECT) TokenParam patientId,
			@OptionalParam(name = Observation.SP_DATE) DateRangeParam dateRange,
			@OptionalParam(name = Observation.SP_CODE) StringParam observationCode,
			@OptionalParam(name = "-summary") StringParam summary,
			@OptionalParam(name = "-samplingPeriod") NumberParam sampleRate, @Sort SortSpec sortSpec,
			@Count Integer count) throws Exception {
		Date start = T5FHIRUtils.getStartTimeFromNullableRange(dateRange);
		Date end = T5FHIRUtils.getEndTimeFromNullableRange(dateRange);

		boolean isSummary = TRUE.equalsIgnoreCase(T5FHIRUtils.getValueOrNull(summary));

		if (isSummary) {
			return observationService.searchSummaryByPatient(patientId.getValue(), start, end);
		} else if (observationCode == null) {
			throw new InvalidRequestException("Observation type code missing.");
		} else {
			validateCountParameter(count);

			return observationService.searchByPatient(patientId.getValue(), observationCode.getValue(), start, end,
					getSamplingPeriodFromParam(sampleRate), sortSpec, count);
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
			@OptionalParam(name = "-samplingPeriod") NumberParam sampleRate, @Sort SortSpec sortSpec,
			@Count Integer count) throws Exception {

		Date start = T5FHIRUtils.getStartTimeFromNullableRange(dateRange);
		Date end = T5FHIRUtils.getEndTimeFromNullableRange(dateRange);

		boolean isSummary = TRUE.equalsIgnoreCase(T5FHIRUtils.getValueOrNull(summary));
		if (isSummary) {
			return observationService.searchSummaryByDevice(deviceId.getValue(), start, end);
		} else {
			if (observationCode == null) {
				throw new InvalidRequestException("Observation type code missing.");
			}

			validateCountParameter(count);

			return observationService.searchByDevice(deviceId.getValue(), observationCode.getValue(), start, end,
					getSamplingPeriodFromParam(sampleRate), sortSpec, count);
		}
	}

	private void validateCountParameter(Integer count) {
		if (count != null && count < 0) {
			throw new InvalidRequestException("_count parameter cannot be negative.");
		}
	}
}
