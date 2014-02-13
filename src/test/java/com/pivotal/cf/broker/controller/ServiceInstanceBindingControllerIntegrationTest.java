/**
 * 
 */
package com.pivotal.cf.broker.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.model.fixture.ServiceInstanceBindingFixture;
import com.pivotal.cf.broker.model.fixture.ServiceInstanceFixture;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;
import com.pivotal.cf.broker.service.ServiceInstanceService;

/**
 * 
 * @author Johannes Hiemer.
 *
 */
public class ServiceInstanceBindingControllerIntegrationTest {

	private static final String BASE_PATH = "/v2/service_instances/" 
			+ ServiceInstanceFixture.getServiceInstance().getId()
			+ "/service_bindings";
	
	private MockMvc mockMvc;

	@InjectMocks
	private ServiceInstanceBindingController controller;

	@Mock
	private ServiceInstanceBindingService serviceInstanceBindingService;
	
	@Mock
	private ServiceInstanceService serviceInstanceService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

	    this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
	            .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
	}
	
	@Test
	public void serviceInstanceBindingIsCreatedCorrectly() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
	    ServiceInstanceBinding binding = ServiceInstanceBindingFixture.getServiceInstanceBinding();
		
		when(serviceInstanceService.getServiceInstance(any(String.class)))
	    	.thenReturn(instance);
	    
		when(serviceInstanceBindingService.createServiceInstanceBinding(any(String.class), any(ServiceInstance.class), any(String.class), any(String.class), any(String.class)))
    		.thenReturn(binding);
	    
	    String url = BASE_PATH + "/{bindingId}";
	    String body = ServiceInstanceBindingFixture.getServiceInstanceBindingRequestJson();
	    
	    mockMvc.perform(
	    		put(url, binding.getId())
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    	)
	    	.andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.credentials.uri", is("uri")))
            .andExpect(jsonPath("$.credentials.username", is("username")))
            .andExpect(jsonPath("$.credentials.password", is("password")));
 	}
	
	@Test
	public void unknownServiceInstanceFailsBinding() throws Exception {
	    ServiceInstanceBinding binding = ServiceInstanceBindingFixture.getServiceInstanceBinding();
		
		when(serviceInstanceService.getServiceInstance(any(String.class)))
	    	.thenReturn(null);
	    
	    String url = BASE_PATH + "/{bindingId}";
	    String body = ServiceInstanceBindingFixture.getServiceInstanceBindingRequestJson();
	    
	    mockMvc.perform(
	    		put(url, binding.getId())
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    	)
	    	.andDo(print())
	    	.andExpect(status().isUnprocessableEntity())
	    	.andExpect(jsonPath("$.message", containsString(binding.getServiceInstanceId())));
 	}
	
	@Test
	public void duplicateBindingRequestFailsBinding() throws Exception {
		ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
		ServiceInstanceBinding binding = ServiceInstanceBindingFixture.getServiceInstanceBinding();
		
		when(serviceInstanceService.getServiceInstance(any(String.class)))
	    	.thenReturn(instance);
	    
		when(serviceInstanceBindingService.createServiceInstanceBinding(any(String.class), any(ServiceInstance.class), any(String.class), any(String.class), any(String.class)))
			.thenThrow(new ServiceInstanceBindingExistsException(binding));
		
	    String url = BASE_PATH + "/{bindingId}";
	    String body = ServiceInstanceBindingFixture.getServiceInstanceBindingRequestJson();
	    
	    mockMvc.perform(
	    		put(url, binding.getId())
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    	)
	    	.andExpect(status().isConflict())
	    	.andExpect(jsonPath("$.message", containsString(binding.getId())));
 	}	
	
	@Test
	public void invalidBindingRequestJson() throws Exception {
		ServiceInstanceBinding binding = ServiceInstanceBindingFixture.getServiceInstanceBinding();
		
	    String url = BASE_PATH + "/{bindingId}";
	    String body = ServiceInstanceBindingFixture.getServiceInstanceBindingRequestJson();
	    body = body.replace("service_id", "foo");
	    mockMvc.perform(
	    		put(url, binding.getId())
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    	)
	    	.andExpect(status().isUnprocessableEntity())
	    	.andExpect(jsonPath("$.message", containsString("foo")));
 	}	
	
	@Test
	public void invalidBindingRequestMissingFields() throws Exception {
		ServiceInstanceBinding binding = ServiceInstanceBindingFixture.getServiceInstanceBinding();
		
	    String url = BASE_PATH + "/{bindingId}";
	    String body = "{}";
	    
	    mockMvc.perform(
	    		put(url, binding.getId())
	    		.contentType(MediaType.APPLICATION_JSON)
	    		.content(body)
	    	)
	    	.andExpect(status().isUnprocessableEntity())
	    	.andExpect(jsonPath("$.message", containsString("serviceDefinitionId")))
	    	.andExpect(jsonPath("$.message", containsString("planId")));
 	}
	
	@Test
	public void serviceInstanceBindingIsDeletedSuccessfully() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
	    ServiceInstanceBinding binding = ServiceInstanceBindingFixture.getServiceInstanceBinding();
		
	    when(serviceInstanceService.getServiceInstance(any(String.class)))
	    	.thenReturn(instance);
	    
		when(serviceInstanceBindingService.deleteServiceInstanceBinding(any(String.class)))
    		.thenReturn(binding);
	    
	    String url = BASE_PATH + "/" + binding.getId() 
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
	public void unknownServiceInstanceBindingNotDeleted() throws Exception {
	    ServiceInstance instance = ServiceInstanceFixture.getServiceInstance();
	    ServiceInstanceBinding binding = ServiceInstanceBindingFixture.getServiceInstanceBinding();
		
	    when(serviceInstanceService.getServiceInstance(any(String.class)))
	    	.thenReturn(instance);
	    
		when(serviceInstanceBindingService.deleteServiceInstanceBinding(any(String.class)))
    		.thenReturn(null);
	    
	    String url = BASE_PATH + "/" + binding.getId() 
	    		+ "?service_id=" + instance.getServiceDefinitionId()
	    		+ "&plan_id=" + instance.getPlanId();
	    
	    mockMvc.perform(delete(url)
	    		.accept(MediaType.APPLICATION_JSON)
	    	)
	    	.andExpect(status().isNotFound())
	    	.andExpect(jsonPath("$", is("{}")));
 	}

}
