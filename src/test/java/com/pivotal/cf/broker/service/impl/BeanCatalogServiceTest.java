/**
 * 
 */
package com.pivotal.cf.broker.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pivotal.cf.broker.model.Catalog;
import com.pivotal.cf.broker.model.ServiceDefinition;

/**
 * 
 * @author Johannes Hiemer.
 *
 */
public class BeanCatalogServiceTest {

	private BeanCatalogService service;
	
	private Catalog catalog;
	private ServiceDefinition serviceDefinition;
	private static final String SVC_DEF_ID = "svc-def-id";
	
	@Before
	public void setup() {
		serviceDefinition = new ServiceDefinition(SVC_DEF_ID, "Name", "Description", true, null);
		List<ServiceDefinition> defs = new ArrayList<ServiceDefinition>();
		defs.add(serviceDefinition);
		catalog = new Catalog(defs);	
		service = new BeanCatalogService(catalog);
	}
	
	@Test
	public void catalogIsReturnedSuccessfully() {
		assertEquals(catalog, service.getCatalog());
	}
	
	@Test 
	public void itFindsServiceDefinition() {
		assertEquals(serviceDefinition, service.getServiceDefinition(SVC_DEF_ID));
	}
	
	
	@Test 
	public void itDoesNotFindServiceDefinition() {
		assertNull(service.getServiceDefinition("NOT_THERE"));
	}
	
}
