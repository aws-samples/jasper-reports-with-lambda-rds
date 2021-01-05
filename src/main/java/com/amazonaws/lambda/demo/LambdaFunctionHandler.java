package com.amazonaws.lambda.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class LambdaFunctionHandler implements RequestStreamHandler
{
	LambdaLogger logger;

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		this.logger = context.getLogger();
		JSONObject responseJson = new JSONObject();
		try {
			JSONObject queryParameters = extractQueryStringParameters(inputStream);
			
			AmazonS3Consumer s3Consumer = new AmazonS3Consumer(this.logger);
			s3Consumer.retrieveTemplateFromS3();
			
			DatabaseCredentials credentials = new DatabaseCredentials(this.logger);
			credentials.buildCredentials();
		
			RDSConnector connector = new RDSConnector(this.logger);
			Connection connection = connector.connectToRDS(credentials);
			List<EmployeeDataBean> beanList = connector.getBeanList(connection, queryParameters);
			
			ReportGenerator reportGenerator = new ReportGenerator(this.logger);
			String encodedReport = reportGenerator.generateBase64EncodedReport(beanList);
			
			buildSuccessfulResponse(encodedReport, responseJson);
		}
		catch (ParseException e) {
			this.buildErrorResponse(e.getMessage(), 400, responseJson);
		}
		catch (Exception e) {
			this.buildErrorResponse(e.getMessage(), 500, responseJson);
		}
		OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
		writer.write(responseJson.toString());
		writer.close();
	}

	public JSONObject extractQueryStringParameters(InputStream inputStream) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		JSONObject queryParameters = null;
		try {
			JSONObject event = (JSONObject) parser.parse((Reader) reader);
			if (event.get("queryStringParameters") != null) {
				queryParameters = (JSONObject)event.get("queryStringParameters");
			}
			return queryParameters;
		}
		catch (ParseException e) {
			logger.log("Error when parsing query string parameters.");
			throw e;
		} catch (IOException e) {
			logger.log("Error extracting query string parameters.");
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public void buildSuccessfulResponse(String encodedReport, JSONObject responseJson) {
		JSONObject headerJson = new JSONObject();
		headerJson.put("Content-Type", "application/pdf");
		headerJson.put("Accept", "application/pdf");
		headerJson.put("Content-disposition", "attachment; filename=file.pdf");
		responseJson.put("body", encodedReport);
		responseJson.put("statusCode", 200);
		responseJson.put("isBase64Encoded", true);
		responseJson.put("headers", headerJson);
	}

	@SuppressWarnings("unchecked")
	public void buildErrorResponse(String body, int statusCode, JSONObject responseJson) {
		responseJson.put("body", body);
		responseJson.put("statusCode", statusCode);
	}
}