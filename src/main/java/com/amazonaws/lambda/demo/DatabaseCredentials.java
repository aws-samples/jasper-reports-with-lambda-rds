package com.amazonaws.lambda.demo;

import java.io.IOException;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseCredentials {

	private String host;
	private String port;
	private String dbname;
	private String username;
	private String password;

	private LambdaLogger logger;

	public DatabaseCredentials(LambdaLogger logger) {
		this.logger = logger;
	}

	private void build(String host, String port, 
			String dbname, String username, String password) {
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}
	public String getPort() {
		return port;
	}
	public String getDbname() {
		return dbname;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}

	public void buildCredentials() throws Exception {

		String secretName = System.getenv("SECRET_NAME");
		String AWSRegion = System.getenv("SECRET_REGION");
		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(AWSRegion).build();  
		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode secretsJson = null;

		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName); 

		GetSecretValueResult getSecretValueResponse = null;

		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
		} catch (ResourceNotFoundException e) {
			logger.log("The requested secret " + secretName + " was not found");
			throw e;
		} catch (InvalidRequestException e) {  
			logger.log("The request was invalid due to: " + e.getMessage());
			throw e;
		} catch (InvalidParameterException e) {  
			logger.log("The request had invalid params: " + e.getMessage());
			throw e;
		}
		if(getSecretValueResponse == null) {  
			logger.log("Secret response is null");
			throw new Exception("Secret response is null");
		}   

		String secret = getSecretValueResponse.getSecretString(); 

		if(secret != null) {
			try {    
				secretsJson = objectMapper.readTree(secret);  
			} catch (IOException e) {    
				logger.log("Exception while retrieving secret values: " + e.getMessage());
				throw e;
			}
		} else {  
			logger.log("The Secret String returned is null");
			throw new Exception("The Secret String returned is null");
		}
		this.build(secretsJson.get("host").textValue(), secretsJson.get("port").asText(), 
				secretsJson.get("dbname").textValue(), secretsJson.get("username").textValue(), 
				secretsJson.get("password").textValue());
	}
}