/**
 * 
 */
package com.pivotal.cf.broker.service.impl;

import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;

/**
 * @author Johannes Hiemer.
 *
 */
@Service
public class BeanServiceInstanceBindingService implements ServiceInstanceBindingService {

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			String bindingId, ServiceInstance serviceInstance,
			String serviceId, String planId, String appGuid)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstanceBinding getServiceInstanceBinding(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(String id)
			throws ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

}
