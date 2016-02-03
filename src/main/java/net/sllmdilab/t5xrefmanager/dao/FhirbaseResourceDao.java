package net.sllmdilab.t5xrefmanager.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import net.sllmdilab.commons.exceptions.DatabaseException;

public class FhirbaseResourceDao<T extends IResource> {
	private static final Logger logger = LoggerFactory.getLogger(FhirbaseResourceDao.class);

	private static final String initQuery = "SET plv8.start_proc = 'plv8_init'";
	public static final String FHIRBASE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

	protected final String resourceName;
	protected final Class<T> clazz;
	protected IParser jsonParser;

	@Autowired
	protected FhirContext fhirContext;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	public FhirbaseResourceDao(Class<T> clazz) {
		this.clazz = clazz;
		try {
			resourceName = clazz.newInstance().getResourceName();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String escapeKeyword(String name) {
		if(StringUtils.equalsIgnoreCase(name, "order")) {
			return "\\\"" + name + "\\\"";
		} else {
			return name;
		}
	}

	/**
	 * Extensions in metadata is yet not supported by HAPI-FHIR. This is a temporary workaround.
	 * @param jsonString A json string containing a resource bundle.
	 * @return The same bundle with any extensions in the meta-field removed for each entry.
	 */
	private String removeMetadataExtensionsFromBundle(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root;
		try {
			root = (ObjectNode) mapper.readTree(jsonString);
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse Json", e);
		}
		
		ArrayNode entries = (ArrayNode) root.get("entry");
		for (int i = 0; i < entries.size(); ++i) {
			ObjectNode entry = (ObjectNode) entries.get(i);
			ObjectNode resource = (ObjectNode) entry.get("resource");

			ObjectNode meta = (ObjectNode) resource.get("meta");

			if (meta != null) {
				meta.remove("extension");
			}
		}

		try {
			return mapper.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Json serialization failed.", e);
		}
	}
	
	/**
	 * Extensions in metadata is yet not supported by HAPI-FHIR. This is a temporary workaround.
	 * @param jsonString A json string containing a single fhir resource.
	 * @return The same resource with any extensions in the meta-field removed.
	 */
	private String removeMetadataExtensionsFromResource(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root;
		try {
			root = (ObjectNode) mapper.readTree(jsonString);
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse Json", e);
		}

		ObjectNode meta = (ObjectNode) root.get("meta");

		if (meta != null) {
			meta.remove("extension");
		}

		try {
			return mapper.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Json serialization failed.", e);
		}
	}

	@PostConstruct
	public void postConstructInit() {
		jsonParser = fhirContext.newJsonParser();
	}

	public String getResourceName() {
		return resourceName;
	}

	public void insert(T resource) {
		ResourceMetadataMap metadata = resource.getResourceMetadata();

		String jsonString = "{\"allowId\":true, \"resource\":" + encode(resource) + "}";
		String insertQuery = "SET plv8.start_proc = 'plv8_init' ; SELECT fhir_create_resource( '" + jsonString + "' )";

		try {
			jdbcTemplate.execute(insertQuery);
		} catch (DataAccessException e) {
			throw new DatabaseException("Insert query failed.", e);
		}
	}

	public void update(T resource) {
		String jsonString = "{ \"resource\":" + encode(resource) + "}";
		String insertQuery = "SET plv8.start_proc = 'plv8_init' ; SELECT fhir_update_resource( '" + jsonString + "' )";

		try {
			jdbcTemplate.execute(insertQuery);
		} catch (DataAccessException e) {
			throw new DatabaseException("Update query failed.", e);
		}
	}

	@Transactional
	public T read(String id) {
		String initQuery = "SET plv8.start_proc = 'plv8_init'";
		String readQuery = "SELECT fhir_read_resource('{\"resourceType\": \"" + resourceName + "\", \"id\": \"" + id
				+ "\"}')";

		List<T> result;
		try {
			jdbcTemplate.execute(initQuery);
			result = jdbcTemplate.query(readQuery, new ResourceRowMapper());

		} catch (DataAccessException e) {
			throw new DatabaseException("Read query failed", e);
		}
		if (result.isEmpty()) {
			throw new ResourceNotFoundException(new IdDt(id));
		}

		return result.get(0);
	}

	@Transactional
	public void delete(String id) {
		String initQuery = "SET plv8.start_proc = 'plv8_init'";
		String deleteQuery = "SELECT fhir_delete_resource('{\"resourceType\": \"" + resourceName + "\", \"id\": \"" + id
				+ "\"}')";

		List<T> result;
		try {
			jdbcTemplate.execute(initQuery);
			result = jdbcTemplate.query(deleteQuery, new ResourceRowMapper());

		} catch (DataAccessException e) {
			throw new DatabaseException("Delete statement failed", e);
		}
		if (result.isEmpty()) {
			throw new ResourceNotFoundException(new IdDt(id));
		}
	}

	private class ResourceRowMapper implements RowMapper<T> {
		@Override
		public T mapRow(ResultSet rs, int index) throws SQLException {
			try {
				return jsonParser.parseResource(clazz, removeMetadataExtensionsFromResource(rs.getString(1)));
			} catch (DataFormatException e) {
				OperationOutcome outcome = jsonParser.parseResource(OperationOutcome.class, rs.getString(1));

				logger.warn("Got OperationOutcome from FHIRBase.");

				if ("not-found".equals(outcome.getIssueFirstRep().getCode())) {
					throw new ResourceNotFoundException("Resouce not found.", outcome);
				} else {
					throw e;
				}

			}
		}
	}

	private class BundleRowMapper implements RowMapper<Bundle> {
		@Override
		public Bundle mapRow(ResultSet rs, int index) throws SQLException {
			return jsonParser.parseResource(Bundle.class, removeMetadataExtensionsFromBundle(rs.getString(1)));
		}
	}

	@Transactional
	public List<IResource> search(Params parameters) {
		String readQuery = "SELECT fhir_search('{\"resourceType\": \"" + escapeKeyword(resourceName)
				+ "\", \"queryString\": \"" + parameters.buildParamString() + "\"}')";

		List<Bundle> results;
		try {
			jdbcTemplate.execute(initQuery);
			results = jdbcTemplate.query(readQuery, new BundleRowMapper());
		} catch (DataAccessException e) {
			throw new DatabaseException("Search query failed", e);
		}

		if (results.size() != 1) {
			throw new DatabaseException("Search query failed, " + results.size() + " rows returned.");
		}

		Bundle bundle = results.get(0);

		List<IResource> resources = bundle.getEntry().stream().map(entry -> entry.getResource())
				.collect(Collectors.toList());
		return resources;
	}

	public List<IResource> search(Params parameters, Set<Include> includes) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Include includeParam : includes) {
			stringBuilder.append(includeParam.getValue());
			stringBuilder.append(",");
		}
		if (stringBuilder.length() > 0) {
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		}
		parameters.add("_include", stringBuilder.toString());
		return search(parameters);
	}

	private String encode(T resource) {
		return sanitizePostgresJson(jsonParser.encodeResourceToString(resource));
	}

	public static String sanitizePostgresJson(String json) {
		return json.replaceAll("'", "''");
	}

	public static class Params {
		private Map<String, List<String>> paramMap = new HashMap<String, List<String>>();

		private Params() {
		}

		public static Params of(String name, Object value) {
			Params params = new Params();
			params.add(name, value);
			return params;
		}

		public static Params of(String name, String operator, Object value) {
			Params params = new Params();
			params.add(name, operator, value);
			return params;
		}

		public static Params empty() {
			return new Params();
		}

		public List<String> getValues(String name) {
			return paramMap.get(name);
		}

		public Params add(String name, Object value) {
			return add(name, "", value);
		}

		public Params add(String name, String operator, Object value) {
			String valueString;
			if (value instanceof Date) {
				valueString = convertToParamString((Date) value);
			} else {
				valueString = value.toString();
			}

			if (!paramMap.containsKey(name)) {
				paramMap.put(name, new ArrayList<String>(2));
			}

			paramMap.get(name).add(operator + valueString);

			return this;
		}

		private String buildParamString() {
			StringBuilder paramBilder = new StringBuilder();
			for (Map.Entry<String, List<String>> param : paramMap.entrySet()) {
				for (String value : param.getValue()) {
					paramBilder.append(sanitizePostgresJson(param.getKey()) + "=" + sanitizePostgresJson(value) + "&");
				}
			}

			if (paramBilder.length() > 0) {
				paramBilder.deleteCharAt(paramBilder.length() - 1);
			}
			return paramBilder.toString();
		}
	}

	public static String convertToParamString(Date date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FHIRBASE_DATE_FORMAT)
				.withZone(ZoneId.of("UTC"));

		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));

		return zonedDateTime.format(dateTimeFormatter);
	}
}
