package net.sllmdilab.t5xrefmanager.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class LogEntry {
	
	private String timeStamp;
	private String requestUrl;
	private String exception;
	private int exceptionCode;

	@JacksonXmlElementWrapper(localName = "queryParameters", useWrapping = true)
	private List<ValueList> queryParameter;
	@JacksonXmlElementWrapper(localName = "requestHeaders", useWrapping = true)
	private List<ValueList> requestHeader;
	@JacksonXmlElementWrapper(localName = "responseHeaders", useWrapping = true)
	private List<ValueList> responseHeader;
	private String responseBody;

	public List<ValueList> getQueryParameter() {
		return queryParameter;
	}

	public void setQueryParameter(List<ValueList> queryParameter) {
		this.queryParameter = queryParameter;
	}

	public List<ValueList> getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(List<ValueList> requestHeader) {
		this.requestHeader = requestHeader;
	}

	public List<ValueList> getResponseHeader() {
		return responseHeader;
	}

	public void setResponseHeader(List<ValueList> responseHeader) {
		this.responseHeader = responseHeader;
	}

	public void setExceptionCode(int exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public int getExceptionCode() {
		return exceptionCode;
	}

	public void setExceptionStatusCode(int exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public static class ValueList {
		@JacksonXmlProperty(isAttribute=true)
		private String name;
		@JacksonXmlElementWrapper(useWrapping=false)
		private String[] value;

		public ValueList(String name, String[] values) {
			this.name = name;
			this.setValue(values);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public static List<ValueList> ofMap(Map<String, String[]> map) {
			List<ValueList> lists = new ArrayList<ValueList>();
			for (Map.Entry<String, String[]> entry : map.entrySet()) {
				lists.add(new ValueList(entry.getKey(), entry.getValue()));
			}

			return lists;
		}

		public String[] getValue() {
			return value;
		}

		public void setValue(String[] value) {
			this.value = value;
		}
	}
}
