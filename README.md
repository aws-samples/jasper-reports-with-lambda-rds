## Jasper Reports with Lambda, RDS and API Gateway

This repository hosts the code for a Java Lambda Function that generates JasperReports reports in PDF format, using data queried from an RDS MySQL database.
The Lambda function is meant to be invoked through a REST API created by in API Gateway. This code and repository is part of an AWS Blog Post. You can find an example
of usage of this code in the Blog Post.

Along with the function code, this project contains a template file for the JasperReports created in the Blog Post, "template.jrxml", and a CloudFormation script
responsible for launching the required resources, "jasper-lambda-architecture.json". Both files are located in the root of the repository.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.

