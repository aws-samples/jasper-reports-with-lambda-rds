package com.amazonaws.lambda.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.util.Base64;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class ReportGenerator {

	static final String outFile = "/tmp/Reports.pdf";
	static final String fileName = "/tmp/template.jrxml";

	private LambdaLogger logger;

	public ReportGenerator(LambdaLogger logger) {
		this.logger = logger;
	}

	public String generateBase64EncodedReport(List<EmployeeDataBean> beanList) throws JRException, IOException {
		try {
			File file = new File(outFile);
			OutputStream outputSteam = new FileOutputStream(file);
			generateReport(beanList, outputSteam);
			byte[] encoded = Base64.encode(FileUtils.readFileToByteArray(file));
			return new String(encoded, StandardCharsets.US_ASCII);
		} catch (FileNotFoundException e) {
			logger.log("It was not possible to access the output file: " + e.getMessage());
			throw e;
		} catch (IOException e) {
			logger.log("It was not possible to read and encode the report: " + e.getMessage());
			throw e;
		}
	}

	public void generateReport(List<EmployeeDataBean> beanList, OutputStream outputSteam) throws JRException {
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(beanList);
		Map<String, Object> parameter = new HashMap<String, Object>();

		parameter.put("title", new String("Report Example"));
		parameter.put("employeeDataSource", beanColDataSource);
		JasperReport jasperDesign = JasperCompileManager.compileReport(fileName);
		try {
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperDesign, parameter,
					new JREmptyDataSource());
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputSteam);
		} catch (JRException e) {
			logger.log("There was an error while generating the report: " + e.getMessage());
			throw e;
		}
	}
}
