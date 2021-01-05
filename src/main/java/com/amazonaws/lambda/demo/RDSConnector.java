package com.amazonaws.lambda.demo;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class RDSConnector
{
    private LambdaLogger logger;
    
    public RDSConnector(LambdaLogger logger) {
        this.logger = logger;
    }
    
    public Connection connectToRDS(DatabaseCredentials credentials) throws ClassNotFoundException, SQLException {
        try {
            Class.forName(System.getenv("RDS_DB_DRIVER"));
        }
        catch (ClassNotFoundException e) {
            this.logger.log("There was an error when loading the database driver:" + e.getMessage());
            throw e;
        }
        String jdbcUrl = this.buildJdbcUrl(credentials);
        Connection connection;
        try {
            connection = DriverManager.getConnection(jdbcUrl);
        }
        catch (SQLException e) {
            this.logger.log("There was an error when creating the connection: " + e.getMessage());
            throw e;
        }
        return connection;
    }
    
    public String buildJdbcUrl(DatabaseCredentials credentials) {
        String dbName = credentials.getDbname();
        String userName = credentials.getUsername();
        String password = credentials.getPassword();
        String hostname = credentials.getHost();
        String port = credentials.getPort();
        return "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
    }
    
    public List<EmployeeDataBean> getBeanList(Connection connection, JSONObject queryParameters) throws SQLException {
        List<EmployeeDataBean> beanList = new ArrayList<EmployeeDataBean>();
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("drop table if exists Employee");
            statement.executeUpdate("create table Employee (\r\n\tEmpID  int NOT NULL,\r\n\tName varchar(255) NOT NULL,\r\n\tCountry varchar(255),\r\n\tPRIMARY KEY (EmpID)\r\n);");
            statement.executeUpdate("insert into Employee (EmpID, Name, Country) values(1, \"Jo\u00e3o\", \"Brazil\");");
            statement.executeUpdate("insert into Employee (EmpID, Name, Country) values(2, \"Bob\", \"US\");");
            statement.executeUpdate("insert into Employee (EmpID, Name, Country) values(3, \"Mary\", \"Canada\");");
            statement.executeUpdate("insert into Employee (EmpID, Name, Country) values(4, \"Carlos\", \"Brazil\");");
            statement.executeUpdate("insert into Employee (EmpID, Name, Country) values(5, \"Ana\", \"Brazil\");");
            ResultSet resultSet = null;
            if (queryParameters == null || queryParameters.get((Object)"country") == null) {
                resultSet = this.getAll(statement);
            }
            else {
                String countryParameter = (String)queryParameters.get((Object)"country");
                resultSet = this.getFilteredByCountry(statement, countryParameter);
            }
            while (resultSet.next()) {
                EmployeeDataBean bean = new EmployeeDataBean(resultSet.getString("Name"), resultSet.getInt("EmpId"), resultSet.getString("Country"));
                beanList.add(bean);
            }
            statement.executeUpdate("drop table Employee");
        }
        catch (SQLException e) {
            this.logger.log("There was an error when executing the SQL: " + e.getMessage());
            throw e;
        }
        return beanList;
    }
    
    public ResultSet getAll(Statement statement) throws SQLException {
        return statement.executeQuery("select * from Employee");
    }
    
    public ResultSet getFilteredByCountry(Statement statement, String countryParameter) throws SQLException {
        return statement.executeQuery("select * from Employee where Country like '" + countryParameter + "';");
    }
}