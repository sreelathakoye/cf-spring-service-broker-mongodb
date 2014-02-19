package com.pivotal.cf.broker.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceDefinitionDoesNotExistException;
import com.pivotal.cf.broker.exception.ServiceInstanceExistsException;
import com.pivotal.cf.broker.model.CreateServiceInstanceRequest;
import com.pivotal.cf.broker.model.CreateServiceInstanceResponse;
import com.pivotal.cf.broker.model.ErrorMessage;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.service.CatalogService;
import com.pivotal.cf.broker.service.ServiceInstanceService;

/**
 * See: http://docs.cloudfoundry.com/docs/running/architecture/services/writing-service.html
 * 
 * @author sgreenberg@gopivotal.com
 * @author Johannes Hiemer.
 */
@Controller
@RequestMapping(value = "/v2")
public class ServiceInstanceController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceController.class);
	
	public static final String SERVICE_INSTANCE_BASE_PATH = "/v2/service_instances";
	
	@Autowired
	private ServiceInstanceService service;
	
	@Autowired
	private CatalogService catalogService;
	
	@RequestMapping(value = "/service_instances", method = RequestMethod.GET)
	public @ResponseBody List<ServiceInstance> getServiceInstances() {
		logger.debug("GET: " + SERVICE_INSTANCE_BASE_PATH + ", getServiceInstances()");
		return service.getAllServiceInstances();
	}
		
	@RequestMapping(value = "/service_instances/{instanceId}", method = RequestMethod.PUT)
	public ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(@PathVariable("instanceId") String serviceInstanceId,  
			@Valid @RequestBody CreateServiceInstanceRequest request) throws
			ServiceDefinitionDoesNotExistException, ServiceInstanceExistsException, ServiceBrokerException {
		
		logger.debug("PUT: " + SERVICE_INSTANCE_BASE_PATH + "/{instanceId}" 
				+ ", createServiceInstance(), serviceInstanceId = " + serviceInstanceId);
		
		ServiceDefinition svc = catalogService.getServiceDefinition(request.getServiceDefinitionId());
		
		if (svc == null) {
			throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
		}
		
		ServiceInstance instance = service.createServiceInstance(
				svc, serviceInstanceId, 
				request.getPlanId(), request.getOrganizationGuid(), 
				request.getSpaceGuid());
		
		logger.debug("ServiceInstance Created: " + instance.getId());
        
		return new ResponseEntity<CreateServiceInstanceResponse>(new CreateServiceInstanceResponse(instance), 
        		HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/service_instances/{instanceId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteServiceInstance(@PathVariable("instanceId") String instanceId, 
			@RequestParam("service_id") String serviceId, @RequestParam("plan_id") String planId) throws ServiceBrokerException {
		
		logger.debug( "DELETE: " + SERVICE_INSTANCE_BASE_PATH + "/{instanceId}" 
				+ ", deleteServiceInstanceBinding(), serviceInstanceId = " + instanceId 
				+ ", serviceId = " + serviceId
				+ ", planId = " + planId);
		
		ServiceInstance instance = service.deleteServiceInstance(instanceId);
		
		if (instance == null) {
			return new ResponseEntity<String>("{}", HttpStatus.NOT_FOUND);
		}
		logger.debug("ServiceInstance Deleted: " + instance.getId());
        
		return new ResponseEntity<String>("{}", HttpStatus.OK);
	}
	
	@ExceptionHandler(ServiceDefinitionDoesNotExistException.class)
	@ResponseBody public ResponseEntity<ErrorMessage> handleException(ServiceDefinitionDoesNotExistException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@ExceptionHandler(ServiceInstanceExistsException.class)
	@ResponseBody public ResponseEntity<ErrorMessage> handleException(ServiceInstanceExistsException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}
	
}
	