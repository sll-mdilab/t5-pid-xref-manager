package net.sllmdilab.t5xrefmanager.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Device;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

@RunWith(MockitoJUnitRunner.class)
public class FhirbaseResourceDaoTest {
	
	private static final String MOCK_PARAM_VALUE = "mock_param_value";
	private static final String MOCK_PARAM_PREFIX = "mock_param_prefix";
	private static final String MOCK_PARAM = "mock_param";
	private static final String MOCK_ID= "MOCK_ID";
	private static final String MOCK_IDENTIFIER = "MOCK_IDENTIFIER";

	@Spy
	private FhirContext fhirContext = FhirContext.forDstu2();
	
	@Mock
	private JdbcTemplate jdbcTemplate;
	
	@InjectMocks
	private FhirbaseResourceDao<Device> dao = new FhirbaseResourceDao<>(Device.class);
	
	private Device device;
	
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		dao.postConstructInit();
		
		device = new Device();
		IdentifierDt identifier = new IdentifierDt();
		identifier.setValue(MOCK_IDENTIFIER);
		device.setIdentifier(Arrays.asList(identifier));
		device.setId(MOCK_ID);
	}

	@Test
	public void resourceIsInserted() {

		dao.insert(device);
		
		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
		verify(jdbcTemplate).execute(queryCaptor.capture());
		String query = queryCaptor.getValue();
		assertTrue(query.contains(MOCK_IDENTIFIER));
		assertTrue(query.contains(MOCK_ID));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void resourceIsRead() {
		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
		
		when(jdbcTemplate.query(anyString(), (RowMapper<Device>)any())).thenReturn(Arrays.asList(device));
		Device result = dao.read(MOCK_ID);
		assertEquals(device, result);
		
		verify(jdbcTemplate).query(queryCaptor.capture(), (RowMapper<Device>)any());
		String query = queryCaptor.getValue();
		assertTrue(query.contains(MOCK_ID));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void resourcesAreSearched() {
		Bundle bundle = new Bundle();
		Entry entry = new Entry();
		entry.setResource(device);
		bundle.addEntry(entry);
		when(jdbcTemplate.query(anyString(), (RowMapper<Bundle>)any())).thenReturn(Arrays.asList(bundle));
		
		Params params = Params.of(MOCK_PARAM, MOCK_PARAM_PREFIX, MOCK_PARAM_VALUE);
		
		List<IResource> result = dao.search(params);
		assertEquals(1, result.size());
		assertEquals(device, result.get(0));
		
		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
		verify(jdbcTemplate).query(queryCaptor.capture(), (RowMapper<Device>)any());
		String query = queryCaptor.getValue();
		assertTrue(query.contains(MOCK_PARAM + "=" + MOCK_PARAM_PREFIX + MOCK_PARAM_VALUE));
	}
}
