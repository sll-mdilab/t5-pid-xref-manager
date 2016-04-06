package net.sllmdilab.t5xrefmanager.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import net.sllmdilab.commons.domain.SqlObservation;
import net.sllmdilab.commons.t5.validators.RosettaValidator;
import net.sllmdilab.t5xrefmanager.dao.ObservationSqlDao.Code;

@RunWith(MockitoJUnitRunner.class)
public class SqlObservationToFhirConverterTest {

	private static final String MOCK_CODE_SYSTEM = "MOCK_CODE_SYSTEM";

	private static final String MOCK_ROSETTA_UNIT = "MOCK_ROSETTA_UNIT";

	private static final String MOCK_ROSETTA_HARMONIZED_UNIT = "MOCK_ROSETTA_HARMONIZED_UNIT";

	private static final String MOCK_ROSETTA_SYNONYM = "MOCK_ROSETTA_SYNONYM";

	private static final String MOCK_CODE = "MOCK_CODE";

	private static final String MOCK_PERFORMER_ID = "MOCK_PERFORMER_ID";

	private static final String MOCK_PATIENT_ID = "MOCK_PATIENT_ID";

	private static final String MOCK_ROSETTA_DESC = "MOCK_ROSETTA_DESC";

	@Mock
	private RosettaValidator mockRosettaValidator;

	@InjectMocks
	private SqlObservationToFhirConverter converter;

	@Before
	public void setUp() throws Exception {

		when(mockRosettaValidator.getHarmonizedDescription(any())).thenReturn(MOCK_ROSETTA_DESC);
		when(mockRosettaValidator.getHarmonizedSynonym(any())).thenReturn(MOCK_ROSETTA_SYNONYM);
		when(mockRosettaValidator.getHarmonizedUCUMUnits(any())).thenReturn(MOCK_ROSETTA_HARMONIZED_UNIT);
		when(mockRosettaValidator.getUCUMUnit(any())).thenReturn(MOCK_ROSETTA_UNIT);
	}

	@Test
	public void observationIsConverted() {
		SqlObservation sqlObs = new SqlObservation();
		sqlObs.setValueType("NN");
		sqlObs.setValue("42.0");
		sqlObs.setCode(MOCK_CODE);

		List<Observation> observations = converter.convert(MOCK_PATIENT_ID, MOCK_PERFORMER_ID, Arrays.asList(sqlObs));
	
		assertEquals(1, observations.size());
		
		Observation obs = observations.get(0);
		assertEquals(MOCK_PERFORMER_ID, obs.getDevice().getReference().getIdPart());
		assertEquals(42.0, ((QuantityDt)obs.getValue()).getValue().doubleValue(), 0.000001);
		assertEquals(MOCK_ROSETTA_DESC, obs.getCode().getText());
		assertEquals(MOCK_CODE, obs.getCode().getCoding().get(0).getCode());
		
	}
	
	@Test
	public void summaryIsConverted() {
		Code code = new Code(MOCK_CODE, MOCK_CODE_SYSTEM);

		List<Observation> observations = converter.convertToObservationSummary(Arrays.asList(code));
	
		assertEquals(1, observations.size());
		
		Observation obs = observations.get(0);
		assertEquals(MOCK_CODE, obs.getCode().getCoding().get(0).getCode());
		assertEquals(MOCK_CODE_SYSTEM, obs.getCode().getCoding().get(0).getSystem());
	}
}
