package net.sllmdilab.t5xrefmanager.converter;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.SampledDataDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import net.sllmdilab.commons.domain.SqlObservation;
import net.sllmdilab.commons.exceptions.XmlParsingException;
import net.sllmdilab.commons.t5.validators.RosettaValidator;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.dao.ObservationSqlDao.Code;

public class SqlObservationToFhirConverter {
	private static Logger logger = LoggerFactory.getLogger(SqlObservationToFhirConverter.class);

	private final RosettaValidator rosettaValidator;

	public SqlObservationToFhirConverter(RosettaValidator rosettaValidator) {
		this.rosettaValidator = rosettaValidator;
	}

	public List<Observation> convert(String patientId, String deviceId, List<SqlObservation> sqlObservations) {

		if (sqlObservations.isEmpty()) {
			return new ArrayList<>(0);
		}

		ArrayList<Observation> result = new ArrayList<>();

		String observationTypeCode = sqlObservations.get(0).getCode();
		String observationTypeCodeSystem = sqlObservations.get(0).getCodeSystem();

		// Code is the observation code
		CodeableConceptDt codeableConcept = new CodeableConceptDt(observationTypeCodeSystem, observationTypeCode);
		try {
			codeableConcept.setText(rosettaValidator.getHarmonizedDescription(observationTypeCode));
		} catch (Exception e) {
			codeableConcept.setText("Description of the code not available");
		}

		// Cache the unit
		String unitUCUM = "";
		try {
			unitUCUM = rosettaValidator.getHarmonizedUCUMUnits(observationTypeCode);
		} catch (Exception e) {
			logger.warn("Unit not available for " + observationTypeCode);
		}

		for (SqlObservation sqlObs : sqlObservations) {
			Observation obs = new Observation();
			result.add(obs);
			obs.setCode(codeableConcept);

			if (patientId != null) {
				obs.getSubject().setReference("Patient/" + patientId);
			}

			if (deviceId != null) {
				obs.addPerformer().setReference("Device/" + deviceId);
			}

			obs.setId(sqlObs.getUid());

			obs.setValue(parseValue(sqlObs, unitUCUM));
			obs.setEffective(convertTime(sqlObs));
		}

		return result;
	}

	private DateTimeDt convertTime(SqlObservation sqlObs) {
		DateTimeDt obsTimeStamp = new DateTimeDt(sqlObs.getStartTime());
		obsTimeStamp.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")));
		return obsTimeStamp;
	}

	private IDatatype parseValue(SqlObservation sqlObs, String unitUCUM) {
		String value = sqlObs.getValue();
		String unit = sqlObs.getUnit();
		String sampleRate = sqlObs.getValue();
		String dataRange = sqlObs.getDataRange();

		if (isNumeric(value)) {
			return parseScalarValue(value, unit, unitUCUM);
		} else if (value.startsWith("NA")) {
			return parseNumericArrayValue(value, sampleRate, unit, dataRange);
		} else {
			return parseStringValue(value);
		}
	}

	private QuantityDt parseScalarValue(String value, String unit, String unitUCUM) {
		QuantityDt quantity = new QuantityDt();

		quantity.setValue(Double.parseDouble(value));

		if (StringUtils.isBlank(unitUCUM)) {
			quantity.setUnit(unit);
		} else {
			quantity.setUnit(unitUCUM);
		}

		return quantity;
	}

	private StringDt parseStringValue(String value) {
		return new StringDt(value);
	}

	private SampledDataDt parseNumericArrayValue(String samples, String sampleRate, String unitCode, String dataRange) {
		SampledDataDt sampledData = new SampledDataDt();

		setSampleData(samples, sampledData);
		setPeriod(sampleRate, sampledData);
		setOrigin(unitCode, sampledData);
		setRange(dataRange, sampledData);

		return sampledData;
	}

	private void setRange(String dataRange, SampledDataDt sampledData) {
		Pattern pattern = Pattern.compile("NR\\[(.*)\\]");
		Matcher matcher = pattern.matcher(dataRange);
		if (!matcher.matches()) {
			throw new XmlParsingException("Unable to parse numeric array.");
		}
		String[] range = matcher.group(1).split("\\^");
		if (range.length != 2) {
			throw new XmlParsingException("Data range contains " + range.length + " values, should contain 2");
		}
		sampledData.setLowerLimit(Double.parseDouble(range[0]));
		sampledData.setUpperLimit(Double.parseDouble(range[1]));
	}

	private void setOrigin(String unitCode, SampledDataDt sampledData) {
		SimpleQuantityDt origin = new SimpleQuantityDt(0.0);
		origin.setUnit(unitCode);
		sampledData.setOrigin(origin);
	}

	private void setPeriod(String sampleRate, SampledDataDt sampledData) {
		if (!StringUtils.isBlank(sampleRate) && isNumeric(sampleRate)) {
			double dblSampleRate = Double.parseDouble(sampleRate);
			if (dblSampleRate > 0) {
				sampledData.setPeriod(sampleRateToPeriod(Double.parseDouble(sampleRate)));
			}
		}
	}

	private double sampleRateToPeriod(double sampleRate) {
		return 1000.0 / sampleRate;
	}

	private void setSampleData(String valueString, SampledDataDt sampledData) {
		Pattern pattern = Pattern.compile("NA\\[(.*)\\]");
		Matcher matcher = pattern.matcher(valueString);
		if (!matcher.matches()) {
			throw new XmlParsingException("Unable to parse numeric array.");
		}
		sampledData.setData(matcher.group(1).replaceAll("\\^", " "));
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public List<Observation> convertToObservationSummary(Collection<Code> codes) {
		return codes.stream().map(code -> {
			CodeableConceptDt codeConcept = new CodeableConceptDt(code.codeSystem, code.code);
			try {
				codeConcept.setText(rosettaValidator.getHarmonizedDescription(code.code));
			} catch (Exception e) {
				codeConcept.setText("Description of the name not available");
			}

			Observation obs = new Observation();
			obs.setCode(codeConcept);
			obs.setId(T5FHIRUtils.generateUniqueId());
			return obs;
		}).collect(Collectors.toList());
	}
}
