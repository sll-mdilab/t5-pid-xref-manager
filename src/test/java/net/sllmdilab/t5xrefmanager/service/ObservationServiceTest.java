package net.sllmdilab.t5xrefmanager.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import net.sllmdilab.commons.domain.SqlObservation;
import net.sllmdilab.commons.t5.validators.RosettaValidator;
import net.sllmdilab.t5xrefmanager.converter.SqlObservationToFhirConverter;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;
import net.sllmdilab.t5xrefmanager.dao.ObservationSqlDao;
import net.sllmdilab.t5xrefmanager.resource.T5DeviceUseStatement;

@RunWith(MockitoJUnitRunner.class)
public class ObservationServiceTest {
	
	private static final String MOCK_DEVICE_ID = "MOCK_DEVICE_ID";
	private static final String MOCK_CODE = "MOCK_CODE";
	private static final String MOCK_PATIENT_ID = "MOCK_PATIENT_ID";
	private static final Date MOCK_DATE_START = new Date(0);
	private static final String MOCK_PARAM_START = "gt1970-01-01T00:00:00.000Z";
	private static final Date MOCK_DATE_END = new Date(1000 * 60);
	private static final String MOCK_PARAM_END = "lt1970-01-01T00:01:00.000Z";
	private static final Date MOCK_DATE_START2 = new Date(1000);
	private static final Date MOCK_DATE_END2 = new Date(1000 * 30);
	
	@Spy
	private FhirContext fhirContext = FhirContext.forDstu2();
	
	@Spy
	private SqlObservationToFhirConverter mockObsConverter = new SqlObservationToFhirConverter(new RosettaValidator());
	
	@Mock
	private ObservationSqlDao mockObservationDao;
	
	@Mock
	private FhirbaseResourceDao<T5DeviceUseStatement> mockDeviceUseStatementDao = new FhirbaseResourceDao<>(T5DeviceUseStatement.class);
	
	@InjectMocks
	private ObservationService observationService = new ObservationService(null, null, new ArrayList<String>());

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		mockDeviceUseStatementDao.postConstructInit();
	}
	
	@Test
	public void searchByPatient() {
		T5DeviceUseStatement deviceUseStatement = new T5DeviceUseStatement();
		PeriodDt periodDt = new PeriodDt();
		periodDt.setStart(new DateTimeDt(MOCK_DATE_START2));
		periodDt.setEnd(new DateTimeDt(MOCK_DATE_END2));
		deviceUseStatement.setDevice(new ResourceReferenceDt(MOCK_DEVICE_ID));
		deviceUseStatement.setWhenUsed(periodDt);
		when(mockDeviceUseStatementDao.search(any())).thenReturn(Arrays.asList(deviceUseStatement));
	
		observationService.searchByPatient(MOCK_PATIENT_ID, MOCK_CODE, MOCK_DATE_START, MOCK_DATE_END, 0, null, null);
		
		ArgumentCaptor<Params> paramsCaptor = ArgumentCaptor.forClass(Params.class);
		verify(mockDeviceUseStatementDao).search(paramsCaptor.capture());
		Params params = paramsCaptor.getValue();
		assertThat(params.getValues(T5DeviceUseStatement.SP_PERIOD), is(Arrays.asList(MOCK_PARAM_START, MOCK_PARAM_END)));
		
		verify(mockObservationDao).searchByDevice(eq(MOCK_DEVICE_ID), eq(MOCK_CODE), eq(MOCK_DATE_START2), eq(MOCK_DATE_END2));
	}
	
	@Test
	public void searchByPatientRateLimited() {
		T5DeviceUseStatement deviceUseStatement = new T5DeviceUseStatement();
		PeriodDt periodDt = new PeriodDt();
		periodDt.setStart(new DateTimeDt(MOCK_DATE_START2));
		periodDt.setEnd(new DateTimeDt(MOCK_DATE_END2));
		deviceUseStatement.setDevice(new ResourceReferenceDt(MOCK_DEVICE_ID));
		deviceUseStatement.setWhenUsed(periodDt);
		when(mockDeviceUseStatementDao.search(any())).thenReturn(Arrays.asList(deviceUseStatement));
		
		SqlObservation[] sqlObs = new SqlObservation[3];
		sqlObs[0] = new SqlObservation();
		sqlObs[0].setCode(MOCK_CODE);
		sqlObs[0].setId(0);
		sqlObs[0].setValue("0");
		sqlObs[0].setStartTime(Date.from(Instant.ofEpochMilli(MOCK_DATE_START2.toInstant().toEpochMilli() + 1)));
		
		sqlObs[1] = new SqlObservation();
		sqlObs[1].setCode(MOCK_CODE);
		sqlObs[1].setId(0);
		sqlObs[1].setValue("0");
		sqlObs[1].setStartTime(Date.from(Instant.ofEpochMilli(MOCK_DATE_START2.toInstant().toEpochMilli() + 2)));
		
		sqlObs[2] = new SqlObservation();
		sqlObs[2].setCode(MOCK_CODE);
		sqlObs[2].setId(0);
		sqlObs[2].setValue("0");
		sqlObs[2].setStartTime(Date.from(Instant.ofEpochMilli(MOCK_DATE_START2.toInstant().toEpochMilli() + 52)));
		
		when(mockObservationDao.searchByDevice(any(), any(), any(), any())).thenReturn(Arrays.asList(sqlObs));
		
		List<Observation> result = observationService.searchByPatient(MOCK_PATIENT_ID, MOCK_CODE, MOCK_DATE_START, MOCK_DATE_END, 50, null, null);
		assertEquals(2, result.size());
		
		ArgumentCaptor<Params> paramsCaptor = ArgumentCaptor.forClass(Params.class);
		verify(mockDeviceUseStatementDao).search(paramsCaptor.capture());
		Params params = paramsCaptor.getValue();
		assertThat(params.getValues(T5DeviceUseStatement.SP_PERIOD), is(Arrays.asList(MOCK_PARAM_START, MOCK_PARAM_END)));
		
		verify(mockObservationDao).searchByDevice(eq(MOCK_DEVICE_ID), eq(MOCK_CODE), eq(MOCK_DATE_START2), eq(MOCK_DATE_END2));
	}
	
	@Test
	public void searchByDevice() {
		observationService.searchByDevice(MOCK_DEVICE_ID, MOCK_CODE, MOCK_DATE_START, MOCK_DATE_END, 0, null, null);
		
		verify(mockObservationDao).searchByDevice(eq(MOCK_DEVICE_ID), eq(MOCK_CODE), eq(MOCK_DATE_START), eq(MOCK_DATE_END));
	}
	
	@Test
	public void searchSummaryByPatient() {
		T5DeviceUseStatement deviceUseStatement = new T5DeviceUseStatement();
		PeriodDt periodDt = new PeriodDt();
		periodDt.setStart(new DateTimeDt(MOCK_DATE_START2));
		periodDt.setEnd(new DateTimeDt(MOCK_DATE_END2));
		deviceUseStatement.setDevice(new ResourceReferenceDt(MOCK_DEVICE_ID));
		deviceUseStatement.setWhenUsed(periodDt);
		when(mockDeviceUseStatementDao.search(any())).thenReturn(Arrays.asList(deviceUseStatement));
		
		observationService.searchSummaryByPatient(MOCK_PATIENT_ID, MOCK_DATE_START, MOCK_DATE_END);
		
		ArgumentCaptor<Params> paramsCaptor = ArgumentCaptor.forClass(Params.class);
		verify(mockDeviceUseStatementDao).search(paramsCaptor.capture());
		Params params = paramsCaptor.getValue();
		assertThat(params.getValues(T5DeviceUseStatement.SP_PERIOD), is(Arrays.asList(MOCK_PARAM_START, MOCK_PARAM_END)));
		
		verify(mockObservationDao).searchCodesByDevice(eq(MOCK_DEVICE_ID), eq(MOCK_DATE_START2), eq(MOCK_DATE_END2));
	}
	
	@Test
	public void searchSummaryByDevice() {
		observationService.searchSummaryByDevice(MOCK_DEVICE_ID, MOCK_DATE_START, MOCK_DATE_END);
		
		verify(mockObservationDao).searchCodesByDevice(eq(MOCK_DEVICE_ID), eq(MOCK_DATE_START), eq(MOCK_DATE_END));
	}
}
