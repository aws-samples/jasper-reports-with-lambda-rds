package com.amazonaws.lambda.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class AmazonS3Consumer {
    
	static final String key_name = "template.jrxml";
	static final String fileName = "/tmp/template.jrxml";
	
	private LambdaLogger logger;

	public AmazonS3Consumer(LambdaLogger logger) {
		this.logger = logger;
	}
	
	public void retrieveTemplateFromS3() throws IOException {
		String bucket_name = System.getenv("BUCKET_NAME");
    	logger.log("Downloading file " + key_name + " from bucket " + bucket_name + "...\n");
    	AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
    	S3Object object = s3Client.getObject(bucket_name, key_name);
		S3ObjectInputStream s3is = object.getObjectContent();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File(fileName));
			byte[] read_buf = new byte[1024];
			int read_len = 0;
			while ((read_len = s3is.read(read_buf)) > 0) {
				fos.write(read_buf, 0, read_len);
			}
			s3is.close();
			fos.close();
		} catch (FileNotFoundException e) {
			logger.log("There was an error when creating the output template file: " + e.getMessage());
			throw e;
		} catch (IOException e) {
			logger.log("There was an error when reading template file: " + e.getMessage());
			throw e;
		}
    }
}
