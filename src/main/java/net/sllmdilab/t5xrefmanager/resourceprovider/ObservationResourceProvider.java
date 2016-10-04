package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.cdshooks.CDSResponse;
import net.sllmdilab.t5xrefmanager.cdshooks.Card;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;
import net.sllmdilab.t5xrefmanager.service.ObservationService;

@Component
public class ObservationResourceProvider extends BaseResourceProvider<Observation> {
	public static final String MODIFIER_MISSING = ":missing";
	public static final String TRUE = "true";
	public static final String SP_COMMENTS = "-comments";
	public static final String SP_METHOD = "-method";

	@Autowired
	private ObservationService observationService;

	@Autowired
	private FhirbaseResourceDao<Observation> fhirbaseResourceDao;

	@Autowired
	private FhirContext fhirContext;

	public ObservationResourceProvider() {
		super(Observation.class);
	}

	@SuppressWarnings("unchecked")
	@Search()
	public List<Observation> searchAnnotations(@OptionalParam(name = Observation.SP_SUBJECT) ReferenceParam patientId,
			@OptionalParam(name = Observation.SP_DATE) DateRangeParam dateRange,
			@OptionalParam(name = Observation.SP_CODE) StringParam observationTypeCode,
			// Using StringParam because QuantityParam has a bug handling
			// :missing
			@OptionalParam(name = Observation.SP_VALUE_QUANTITY) StringParam valueQuantity,
			@RequiredParam(name = SP_COMMENTS) StringParam comments,
			@OptionalParam(name = Observation.SP_PERFORMER) ReferenceParam performer) {
		Params params = Params.empty();

		addMissing(params, Observation.SP_VALUE_QUANTITY, valueQuantity);
		addMissing(params, SP_COMMENTS, comments);

		if (patientId != null) {
			params.add(Observation.SP_SUBJECT, patientId.getIdPart());
		}

		if (observationTypeCode != null) {
			params.add(Observation.SP_CODE, observationTypeCode.getValue());
		}

		if (dateRange != null) {
			if (dateRange.getLowerBoundAsInstant() != null) {
				params.add(Observation.SP_DATE, "ge", dateRange.getLowerBoundAsInstant());
			}

			if (dateRange.getUpperBoundAsInstant() != null) {
				params.add(Observation.SP_DATE, "le", dateRange.getUpperBoundAsInstant());
			}
		}

		if (performer != null) {
			params.add(Observation.SP_PERFORMER, performer.getValue());
		}

		return (List<Observation>) (List<? extends IResource>) fhirbaseResourceDao.search(params);
	}

	private void addMissing(Params params, String name, IQueryParameterType param) {
		if (param != null) {
			if (param.getMissing() != null) {
				params.add(name + MODIFIER_MISSING, param.getMissing().toString());
			} else {
				throw new InvalidRequestException(SP_COMMENTS + " only supported with :missing-modifier.");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Search()
	public List<Observation> searchManualObservations(
			@OptionalParam(name = Observation.SP_SUBJECT) ReferenceParam patientId,
			@OptionalParam(name = Observation.SP_DATE) DateRangeParam dateRange,
			@OptionalParam(name = Observation.SP_CODE) StringParam observationTypeCode,
			@RequiredParam(name = SP_METHOD) TokenParam method,
			@OptionalParam(name = Observation.SP_PERFORMER) ReferenceParam performer) {
		Params params = Params.empty();

		if (patientId != null) {
			params.add(Observation.SP_SUBJECT, patientId.getIdPart());
		}

		if (observationTypeCode != null) {
			params.add(Observation.SP_CODE, observationTypeCode.getValue());
		}

		if (dateRange != null) {
			if (dateRange.getLowerBoundAsInstant() != null) {
				params.add(Observation.SP_DATE, "ge", dateRange.getLowerBoundAsInstant());
			}

			if (dateRange.getUpperBoundAsInstant() != null) {
				params.add(Observation.SP_DATE, "le", dateRange.getUpperBoundAsInstant());
			}
		}

		if (method != null) {
			params.add(SP_METHOD, method.getValue());
		}

		if (performer != null) {
			params.add(Observation.SP_PERFORMER, performer.getValue());
		}

		return (List<Observation>) (List<? extends IResource>) fhirbaseResourceDao.search(params);
	}

	@Search()
	public List<Observation> searchForPatient(@RequiredParam(name = Observation.SP_SUBJECT) ReferenceParam patientId,
			@OptionalParam(name = Observation.SP_DATE) DateRangeParam dateRange,
			@OptionalParam(name = Observation.SP_CODE) StringParam observationCode,
			@OptionalParam(name = "-summary") StringParam summary,
			@OptionalParam(name = "-samplingPeriod") NumberParam sampleRate, @Sort SortSpec sortSpec,
			@Count Integer count) throws Exception {
		Date start = T5FHIRUtils.getStartTimeFromNullableRange(dateRange);
		Date end = T5FHIRUtils.getEndTimeFromNullableRange(dateRange);

		boolean isSummary = TRUE.equalsIgnoreCase(T5FHIRUtils.getValueOrNull(summary));

		if (isSummary) {
			return observationService.searchSummaryByPatient(patientId.getIdPart(), start, end);
		} else if (observationCode == null) {
			throw new InvalidRequestException("Observation type code missing.");
		} else if (observationCode.getValue().equals("MDCX_SCORE_MEWS")) {
			return calculateMEWS(patientId, sampleRate, sortSpec, count, start, end);
		} else {
			validateCountParameter(count);
			return observationService.searchByPatient(patientId.getIdPart(), observationCode.getValue(), start, end,
					getSamplingPeriodFromParam(sampleRate), sortSpec, count);
		}
	}

	private List<Observation> calculateMEWS(ReferenceParam patientId, NumberParam sampleRate, SortSpec sortSpec,
			Integer count, Date start, Date end) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		Observation respRate = (observationService.searchByPatient(patientId.getIdPart(), "MDC_RESP_RATE", start, end, getSamplingPeriodFromParam(sampleRate), sortSpec, count).get(0));
		Observation pulsRate = (observationService.searchByPatient(patientId.getIdPart(), "MDC_PULS_OXIM_PULS_RATE", start, end, getSamplingPeriodFromParam(sampleRate), sortSpec, count).get(0));
		Observation sysBloodPress = (observationService.searchByPatient(patientId.getIdPart(), "MDC_PRESS_BLD_NONINV_SYS", start, end, getSamplingPeriodFromParam(sampleRate), sortSpec, count).get(0));
		Observation temperature = (observationService.searchByPatient(patientId.getIdPart(), "MDC_TEMP", start, end, getSamplingPeriodFromParam(sampleRate), sortSpec, count).get(0));
		Observation glasComaScore = (observationService.searchByPatient(patientId.getIdPart(), "MDC_SCORE_GLAS_COMA", start, end, getSamplingPeriodFromParam(sampleRate), sortSpec, count).get(0));
		Observation urineVol = (observationService.searchByPatient(patientId.getIdPart(), "MDC_VOL_URINE_COL", start, end, getSamplingPeriodFromParam(sampleRate), sortSpec, count).get(0));

		ObjectNode cdsRequest = mapper.createObjectNode();
		cdsRequest.put("hookInstance", UUID.randomUUID().toString()).put("fhirServer", "https://sll-mdilab.net/fhir")
				.put("hook", "cambio-sepsis").put("redirect", "https://capacity.sll-mdilab.net")
				.put("patient", patientId.getIdPart()).putArray("context")
				.addPOJO(convertToCdsFriendlyObservation(respRate, mapper))
				.addPOJO(convertToCdsFriendlyObservation(pulsRate, mapper))
				.addPOJO(convertToCdsFriendlyObservation(sysBloodPress, mapper))
				.addPOJO(convertToCdsFriendlyObservation(temperature, mapper))
				.addPOJO(convertToCdsFriendlyObservation(glasComaScore, mapper))
				.addPOJO(convertToCdsFriendlyObservation(urineVol, mapper));

		CDSResponse cdsResponseObject = fetchResultFromCDS(cdsRequest, mapper);
		return convertResponseToObservation(cdsResponseObject, patientId);
	}

	private ObjectNode convertToCdsFriendlyObservation(Observation observation, ObjectMapper mapper)
			throws JsonParseException, JsonMappingException, IOException {
		String snomedCode = "";
		String unit = "";
		IParser observationParser = fhirContext.newJsonParser();

		String observationString = observationParser.encodeResourceToString(observation);

		ObjectNode node = mapper.readValue(observationString, ObjectNode.class);
		String mdcCode = node.get("code").get("coding").get(0).get("code").textValue();

		if (mdcCode.equals("MDC_RESP_RATE")) {
			snomedCode = "86290005";
			unit = "/min";
		} else if (mdcCode.equals("MDC_PULS_OXIM_PULS_RATE")) {
			snomedCode = "364075005";
			unit = "/min";
		} else if (mdcCode.equals("MDC_PRESS_BLD_NONINV_SYS")) {
			snomedCode = "271649006";
			unit = "mm[HG]";
		} else if (mdcCode.equals("MDC_TEMP")) {
			snomedCode = "703421000";
			unit = "Cel";
		} else if (mdcCode.equals("MDC_SCORE_GLAS_COMA")) {
			snomedCode = "248241002";
			unit = "";
		} else if (mdcCode.equals("MDC_VOL_URINE_COL")) {
			snomedCode = "364202003";
			unit = "mL";
		}

		node.remove("device");
		node.remove("text");
		node.putObject("code").putArray("coding").addObject().put("system", "http://www.ihtsdo.org/snomed-ct")
				.put("code", snomedCode);

		int value = node.get("valueQuantity").get("value").asInt();
		node.putObject("valueQuantity").put("value", value).put("unit", unit);

		return node;
	}

	private CDSResponse fetchResultFromCDS(ObjectNode cdsRequest, ObjectMapper mapper)
			throws JsonParseException, JsonMappingException, IOException {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String cdsRequestInJson = mapper.writeValueAsString(cdsRequest);
		System.out.println(cdsRequestInJson);
		HttpEntity<String> entity = new HttpEntity<String>(cdsRequestInJson, headers);

		String cdsResponse = restTemplate.postForObject(
				"https://cds-hooks-poc.cambiocds.com/cds-services/modified-early-warning-score", entity, String.class);

		return mapper.readValue(cdsResponse, CDSResponse.class);
	}

	private List<Observation> convertResponseToObservation(CDSResponse cdsResponse, ReferenceParam patientId) {
		Observation mewsObservation = new Observation();

		// Set a unique ID
		mewsObservation.setId(UUID.randomUUID().toString());

		// Put MEWS-details, indicator and links for detailed result in
		// Observation's text-field, then set coding parameters
		Card cdsResponseCard = cdsResponse.getCards().get(0);
		mewsObservation.getCode().setText(cdsResponseCard.getDetail() + ";" + cdsResponseCard.getIndicator() + ";"
				+ cdsResponseCard.getLinks().get(0).getLabel() + ";" + cdsResponseCard.getLinks().get(0).getUrl())
				.addCoding().setSystem("MDC").setCode("MDCX_SCORE_MEWS");

		// Set the MEWScore
		QuantityDt value = new QuantityDt();
		mewsObservation.setValue(value.setValue(Character.getNumericValue(cdsResponseCard.getSummary().charAt(12))));

		// Set the patient reference
		ResourceReferenceDt patientReference = new ResourceReferenceDt("Patient/" + patientId.getValue());
		mewsObservation.setSubject(patientReference);

		List<Observation> mewsResult = new ArrayList<Observation>();
		mewsResult.add(mewsObservation);
		return mewsResult;
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
