package net.sllmdilab.t5xrefmanager.resourceprovider;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import net.sllmdilab.commons.util.T5FHIRUtils;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao;
import net.sllmdilab.t5xrefmanager.dao.FhirbaseResourceDao.Params;

public abstract class BaseResourceProvider<T extends IResource> implements IResourceProvider {

	@Autowired
	private FhirbaseResourceDao<T> resourceDao;

	private final Class<T> clazz;

	public BaseResourceProvider(Class<T> clazz) {
		this.clazz = clazz;
	}

	protected FhirbaseResourceDao<T> getResourceDao() {
		return resourceDao;
	}

	@Override
	public Class<T> getResourceType() {
		return clazz;
	}

	@Read
	public T read(@IdParam IdDt id) {
		return resourceDao.read(id.getIdPart());
	}

	@Search
	public List<IResource> searchAll() {
		return resourceDao.search(Params.empty());
	}

	@Create
	public MethodOutcome create(@ResourceParam T resource) {
		if (resource.getId() == null || !resource.getId().hasIdPart()) {
			resource.setId(new IdDt(resourceDao.getResourceName(), T5FHIRUtils.generateUniqueId()));
		}

		resourceDao.insert(resource);
		return new MethodOutcome(resource.getId(), true);
	}

	@Update
	public MethodOutcome update(@ResourceParam T resource, @IdParam IdDt id) {

		if (id == null || StringUtils.isBlank(id.getValueAsString())) {
			throw new InvalidRequestException("Invalid/missing resource ID.");
		}

		MethodOutcome methodOutcome = new MethodOutcome(id, false);

		try {
			resourceDao.read(id.getIdPart());
		} catch (ResourceNotFoundException e) {
			methodOutcome.setCreated(true);
		}

		resource.setId(id);
		resourceDao.update(resource);

		return methodOutcome;
	}
	
	@Delete
	public void delete( @IdParam IdDt id) {

		if (id == null || StringUtils.isBlank(id.getValueAsString())) {
			throw new InvalidRequestException("Invalid/missing resource ID.");
		}
		
		resourceDao.delete(id.getIdPart());
	}
}
