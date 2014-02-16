package com.pivotal.cf.broker.controller;


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
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.exception.ServiceInstanceDoesNotExistException;
import com.pivotal.cf.broker.model.ErrorMessage;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.model.ServiceInstanceBindingRequest;
import com.pivotal.cf.broker.model.ServiceInstanceBindingResponse;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;
import com.pivotal.cf.broker.service.ServiceInstanceService;

/**
 * See: Source: http://docs.cloudfoundry.com/docs/running/architecture/services/writing-service.html
 * 
 * @author sgreenberg@gopivotal.com
 * @author Johannes Hiemer.
 */
@Controller
@RequestMapping(value = "/v2/service_instances")
public class ServiceInstanceBindingController extends BaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingController.class);
	
	public static final String SERVICE_INSTANCE_BINDING_BASE_PATH = "/v2/service_instances/{instanceId}/service_bindings";
	
	@Autowired
	private ServiceInstanceBindingService serviceInstanceBindingService;
	
	@Autowired
	private ServiceInstanceService serviceInstanceService;
	
	@RequestMapping(value = "/{instanceId}/service_bindings/{bindingId}", method = RequestMethod.PUT)
	public ResponseEntity<ServiceInstanceBindingResponse> bindServiceInstance(@PathVariable("instanceId") String instanceId, 
			@PathVariable("bindingId") String bindingId, @Valid @RequestBody ServiceInstanceBindingRequest request) throws
			ServiceInstanceDoesNotExistException, ServiceInstanceBindingExistsException, ServiceBrokerException {
		
		logger.debug( "PUT: " + SERVICE_INSTANCE_BINDING_BASE_PATH + "/{bindingId}"
				+ ", bindServiceInstance(), serviceInstance.id = " + instanceId 
				+ ", bindingId = " + bindingId);
		
		ServiceInstance instance = serviceInstanceService.getServiceInstance(instanceId);
		if (instance == null) {
			throw new ServiceInstanceDoesNotExistException(instanceId);
		}
		
		ServiceInstanceBinding binding = serviceInstanceBindingService.createServiceInstanceBinding(
				bindingId, instance, request.getServiceDefinitionId(),
				request.getPlanId(), request.getAppGuid());
		
		logger.debug("ServiceInstanceBinding Created: " + binding.getId());
        
		return new ResponseEntity<ServiceInstanceBindingResponse>(new ServiceInstanceBindingResponse(binding), 
        		HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/{instanceId}/service_bindings/{bindingId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteServiceInstanceBinding(@PathVariable("instanceId") String instanceId, 
			@PathVariable("bindingId") String bindingId, @RequestParam("service_id") String serviceId,
			@RequestParam("plan_id") String planId) throws ServiceBrokerException {
		
		logger.debug( "DELETE: " + SERVICE_INSTANCE_BINDING_BASE_PATH + "/{bindingId}"
				+ ", deleteServiceInstanceBinding(),  serviceInstance.id = " + instanceId 
				+ ", bindingId = " + bindingId 
				+ ", serviceId = " + serviceId
				+ ", planId = " + planId);
		
		ServiceInstanceBinding binding = serviceInstanceBindingService.deleteServiceInstanceBinding(bindingId);
		
		if (binding == null) {
			return new ResponseEntity<String>("{}", HttpStatus.NOT_FOUND);
		}
		
		logger.debug("ServiceInstanceBinding Deleted: " + binding.getId());
        
		return new ResponseEntity<String>("{}", HttpStatus.OK);
	}
	
	@ExceptionHandler(ServiceInstanceDoesNotExistException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(ServiceInstanceDoesNotExistException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@ExceptionHandler(ServiceInstanceBindingExistsException.class)
	@ResponseBody public ResponseEntity<ErrorMessage> handleException(
			ServiceInstanceBindingExistsException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}
	
}
