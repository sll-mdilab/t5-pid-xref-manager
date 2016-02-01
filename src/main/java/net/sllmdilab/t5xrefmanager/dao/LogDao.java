package net.sllmdilab.t5xrefmanager.dao;

import net.sllmdilab.commons.database.MLDBClient;
import net.sllmdilab.commons.exceptions.XmlParsingException;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.domain.LogEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;


public class LogDao {
	private Logger logger = LoggerFactory.getLogger(LogDao.class);
	private static final String DOCUMENT_PREFIX = "http://sll-mdilab.net/log/";
	
	@Autowired
	private MLDBClient mldbClient;

	public void insertAccessLog(LogEntry logEntry) {
		ObjectMapper xmlMapper = new XmlMapper();
		
		String xmlLogEntry;
		try {
			xmlLogEntry = xmlMapper.writeValueAsString(logEntry);
		} catch (JsonProcessingException e) {
			throw new XmlParsingException("Exception when parsing log entry.", e);
		}
		
		String uri = DOCUMENT_PREFIX + T5FHIRUtils.generateUniqueId() + ".xml";
		logger.info("Writing log entry to " + uri);
		
		mldbClient.insertDocument(uri, xmlLogEntry);
	}
	
}
