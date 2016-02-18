package net.sllmdilab.t5xrefmanager.dao;

import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import net.sllmdilab.commons.converter.ObservationToT5XmlConverter;
import net.sllmdilab.commons.converter.T5QueryToFHIRConverter;
import net.sllmdilab.commons.database.MLDBClient;

@Ignore
public class ObservationDaoTest {

	private static final String MOCK_PATIENT_ID = "19121212-1212";
	private static final String MOCK_DEVICE_ID = "4535h2hjg5235";
	private static final String MOCK_OBS_TYPE_CODE = "MDC_SOME_OBS";
	private static final String MOCK_XML_RESPONSE = "<trend/>";
	private static final Date MOCK_START_DATE = new Date(0);
	private static final Date MOCK_END_DATE = new Date(1000);

	@Mock
	private MLDBClient mldbClient;
	
	@Mock
	private JdbcTemplate mockJdbcTemplate;

	@Mock
	private ObservationToT5XmlConverter mockConverter;

	@Mock
	private T5QueryToFHIRConverter t5QueryToFhirConverter;

	@InjectMocks
	private ObservationSqlDao observationDao;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

//	@Test
//	public void searchObservationsByPatientQueryIsSent() throws Exception {
//		when(mldbClient.sendQueryParseResponse(any())).thenReturn(parseXml(MOCK_XML_RESPONSE));
//
//		observationDao.searchByPatient(MOCK_PATIENT_ID, MOCK_OBS_TYPE_CODE, MOCK_START_DATE, MOCK_END_DATE);
//
//		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
//		verify(mldbClient).sendQueryParseResponse(queryCaptor.capture());
//
//		assertTrue(queryCaptor.getValue().contains(MOCK_PATIENT_ID));
//		assertTrue(queryCaptor.getValue().contains(MOCK_OBS_TYPE_CODE));
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void searchObservationsByDeviceQueryIsSent() throws Exception {
//		when(mldbClient.sendQueryParseResponse(any())).thenReturn(parseXml(MOCK_XML_RESPONSE));
//
//		observationDao.searchByDevice(MOCK_DEVICE_ID, MOCK_OBS_TYPE_CODE, MOCK_START_DATE, MOCK_END_DATE);
//		verify(mockJdbcTemplate).query((PreparedStatementCreator)any(), (RowMapper<SqlObservation>)any());
//	}
//
//	@Test
//	public void searchSummaryByPatientQueryIsSent() throws Exception {
//		when(mldbClient.sendQueryParseResponse(any())).thenReturn(parseXml(MOCK_XML_RESPONSE));
//
//		observationDao.searchSummaryByPatient(MOCK_PATIENT_ID, MOCK_START_DATE, MOCK_END_DATE);
//
//		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
//		verify(mldbClient).sendQueryParseResponse(queryCaptor.capture());
//
//		assertTrue(queryCaptor.getValue().contains(MOCK_PATIENT_ID));
//	}
//
//	@Test
//	public void searchSummaryByDeviceQueryIsSent() throws Exception {
//		when(mldbClient.sendQueryParseResponse(any())).thenReturn(parseXml(MOCK_XML_RESPONSE));
//
//		observationDao.searchSummaryByDevice(MOCK_DEVICE_ID, MOCK_START_DATE, MOCK_END_DATE);
//		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
//		verify(mldbClient).sendQueryParseResponse(queryCaptor.capture());
//
//		assertTrue(queryCaptor.getValue().contains(MOCK_DEVICE_ID));
//	}
}
