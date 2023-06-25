package com.dms.restcamel.service;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.dms.model.Usuarios;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RestService extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		onException(JsonProcessingException.class)
			.log("Error in Processor: ${exception}");
		onException(HttpOperationFailedException.class)
			.log("Error no HTTP: ${exception}");
	
		restConfiguration()
			.component("servlet")
			.host("localhost")
			.port(8082)
			.bindingMode(RestBindingMode.auto);

		rest()
			.get("/users")
			.consumes(MediaType.APPLICATION_JSON_VALUE)
			.produces(MediaType.APPLICATION_JSON_VALUE)
			.route()
			.to("direct:register");

		from("direct:register")
			.setHeader("Content-Type", constant("application/json"))
			.to("http://localhost:8080/users?bridgeEndpoint=true")
			.convertBodyTo(String.class)
			.process(xchg -> {
				String message = (String) xchg.getIn().getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				List<Usuarios> users = objectMapper.readValue(message, new TypeReference<List<Usuarios>>(){});
				xchg.getIn().setBody(users);
			});
	}
}
