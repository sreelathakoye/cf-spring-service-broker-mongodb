/**
 * 
 */
package com.pivotal.cf.broker.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.pivotal.cf.broker.exception.ServiceInstanceExistsException;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.fixture.ServiceFixture;
import com.pivotal.cf.broker.model.fixture.ServiceInstanceFixture;
import com.pivotal.cf.broker.service.CatalogService;
import com.pivotal.cf.broker.service.ServiceInstanceService;

/**
 * 
 * @author Johannes Hiemer.
 *
 */
public class ServiceInstanceControllerIntegrationTest {
		
	private MockMvc mockMvc;

	@InjectMocks
	private ServiceInstanceController controller;

	@Mock
	private ServiceInstanceService serviceInstanceService;
	
	@Mock
	private CatalogService catalogService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

	    this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
	            .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
	}
	
	@Test
	public void serviceInstancesAreRetrievedCorrectly() throws Exception {
	    when(serviceInstanceService.getAllServiceInstances()).thenReturn(ServiceInstanceFixture.getAllServiceInstances());
	
	    this.mockMvc.perform(get(ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH)
	        .accept(MediaType.APPLICATION_JSON))
	        .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[*].", hasSize(2)))
            .andExpect(jsonPath("$[*].service_instance_id", 
            		containsInAnyOrder(
            				ServiceInstanceFixture.getServiceInstance().getId(), 
            				ServiceInstanceFixture.getServiceInstanceTwo().getId()
            		)
         ));
	}
	
	@Test
	public void serviceInstanceIsCreatedCorrectly() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
		
		when(serviceInstanceService.createServiceInstance(any(ServiceDefinition.class), any(String.class), any(String.class), any(String.class), any(String.class)))
	    	.thenReturn(instance);
	    
		when(catalogService.getServiceDefinition(any(String.class)))
    	.thenReturn(ServiceFixture.getService());
		
	    String dashboardUrl = ServiceInstanceFixture.getCreateServiceInstanceResponse().getDashboardUrl();
	    
	    String url = ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH + "/" + instance.getId();
	    String body = ServiceInstanceFixture.getCreateServiceInstanceRequestJson();
	    
	    mockMvc.perform(
	    		put(url)
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dashboard_url", is(dashboardUrl)));
 	}
	
	@Test
	public void unknownServiceDefinitionInstanceCreationFails() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
	    
		when(catalogService.getServiceDefinition(any(String.class)))
    	.thenReturn(null);
	    
	    String url = ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH + "/" + instance.getId();
	    String body = ServiceInstanceFixture.getCreateServiceInstanceRequestJson();
	    
	    mockMvc.perform(
	    		put(url)
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isUnprocessableEntity())
	    	.andExpect(jsonPath("$.message", containsString(instance.getServiceDefinitionId())));
 	}
	
	@Test
	public void duplicateServiceInstanceCreationFails() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
	    
	    when(catalogService.getServiceDefinition(any(String.class)))
    	.thenReturn(ServiceFixture.getService());
	    
		when(serviceInstanceService.createServiceInstance(any(ServiceDefinition.class), any(String.class), any(String.class), any(String.class), any(String.class)))
	    	.thenThrow(new ServiceInstanceExistsException(instance));
	    
	    String url = ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH + "/" + instance.getId();
	    String body = ServiceInstanceFixture.getCreateServiceInstanceRequestJson();
	    
	    mockMvc.perform(
	    		put(url)
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isConflict())
	    	.andExpect(jsonPath("$.message", containsString(instance.getId())));
 	}
	
	@Test
	public void badJsonServiceInstanceCreationFails() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
		
		when(serviceInstanceService.createServiceInstance(any(ServiceDefinition.class), any(String.class), any(String.class), any(String.class), any(String.class)))
	    	.thenReturn(instance);
	    
		when(catalogService.getServiceDefinition(any(String.class)))
    	.thenReturn(ServiceFixture.getService());
	    
	    String url = ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH + "/" + instance.getId();
	    String body = ServiceInstanceFixture.getCreateServiceInstanceRequestJson();
	    body = body.replace("service_id", "foo");
	    
	    mockMvc.perform(
	    		put(url)
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isUnprocessableEntity())
	    	.andExpect(jsonPath("$.message", containsString("foo")));
 	}
	
	@Test
	public void badJsonServiceInstanceCreationFailsMissingFields() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
		
		when(serviceInstanceService.createServiceInstance(any(ServiceDefinition.class), any(String.class), any(String.class), any(String.class), any(String.class)))
	    	.thenReturn(instance);
	    
		when(catalogService.getServiceDefinition(any(String.class)))
    	.thenReturn(ServiceFixture.getService());
	    
	    String url = ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH + "/" + instance.getId();
	    String body = "{}";
	    
	    mockMvc.perform(
	    		put(url)
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isUnprocessableEntity())
	    	.andExpect(jsonPath("$.message", containsString("serviceDefinitionId")))
	    	.andExpect(jsonPath("$.message", containsString("planId")))
	    	.andExpect(jsonPath("$.message", containsString("organizationGuid")))
	    	.andExpect(jsonPath("$.message", containsString("spaceGuid")));
 	}
	
	@Test
	public void serviceInstanceIsDeletedSuccessfully() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
			    
		when(serviceInstanceService.deleteServiceInstance(any(String.class)))
    		.thenReturn(instance);
	    
	    String url = ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH + "/" + instance.getId() 
	    		+ "?service_id=" + instance.getServiceDefinitionId()
	    		+ "&plan_id=" + instance.getPlanId();
	    
	    mockMvc.perform(delete(url)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", is("{}"))
        );
 	}
	
	@Test
	public void deleteUnknownServiceInstanceFails() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
			    
		when(serviceInstanceService.deleteServiceInstance(any(String.class)))
    		.thenReturn(null);
	    
	    String url = ServiceInstanceController.SERVICE_INSTANCE_BASE_PATH + "/" + instance.getId() 
	    		+ "?service_id=" + instance.getServiceDefinitionId()
	    		+ "&plan_id=" + instance.getPlanId();
	    
	    mockMvc.perform(delete(url)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isNotFound())
	    	.andExpect(jsonPath("$", is("{}")));
 	}

}
