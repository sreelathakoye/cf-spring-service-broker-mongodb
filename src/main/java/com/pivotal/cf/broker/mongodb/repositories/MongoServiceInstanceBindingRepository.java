/**
 * 
 */
package com.pivotal.cf.broker.mongodb.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.pivotal.cf.broker.model.ServiceInstanceBinding;

/**
 * 
 * @author Johannes Hiemer.
 *
 */
public abstract interface MongoServiceInstanceBindingRepository extends CrudRepository<ServiceInstanceBinding, String>,
	PagingAndSortingRepository<ServiceInstanceBinding, String> {

}