/**
 * 
 */
package com.pivotal.cf.broker.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.mongodb.repositories.MongoServiceInstanceBindingRepository;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;
import com.pivotal.cf.broker.service.impl.mongodb.MongoDBAdminService;

/**
 * @author Johannes Hiemer.
 *
 */
@Service
public class MongoDBServiceInstanceBindingService implements ServiceInstanceBindingService {
	
	@Autowired
	private MongoDBAdminService mongoDBAdminService;
	
	@Autowired
	private MongoServiceInstanceBindingRepository mongoServiceInstanceBindingRepository;
	
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
			String serviceId, String planId, String appGuid)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return mongoServiceInstanceBindingRepository.findOne(id);
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(String id)
			throws ServiceBrokerException {
		ServiceInstanceBinding serviceInstanceBinding = mongoServiceInstanceBindingRepository.findOne(id);
		if (serviceInstanceBinding != null) {
			mongoDBAdminService.deleteUser(serviceInstanceBinding.getServiceInstanceId(), id);
			mongoServiceInstanceBindingRepository.delete(id);
		}
		return serviceInstanceBinding;
	}

}
