package com.triumvir.cardinal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class AIMSHelper
{
	private static Log log = LogFactory.getLog(AIMSHelper.class);
	private Connection connection = null;
	Properties prop = new Properties();
	InputStream input = null;

	public AIMSHelper(Connection conn) {
		this.connection = conectar();
	}

	 public Connection conectar()
	    {
	        try
	        {
	            //"jdbc:mysql://localhost:3306/pagos?user=root&password=root&zeroDateTimeBehavior=convertToNull";
	            Class.forName("com.mysql.jdbc.Driver");
	            Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbo", "root", "123456");
	            return conexion;
	        }// fin de try
	        catch (SQLException | ClassNotFoundException exception)
	        {
	            log.error("Error: " + exception.getMessage());
	            return null;
	        }// fin de catch
	    }

	private void loadPropertiesFile()
	{
		try
		{
			input = this.getClass().getResourceAsStream("querys.properties");
			// load a properties file
			prop.load(input);

			// get the property value and print it out
			System.out.println(prop.getProperty("MSCAIMS-11.insertAccount"));

		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void createAccount(AccountRequest rqst) throws GeneralException, SQLException
	{
		String studentId = rqst.getNativeIdentity();
		if ( Util.isNullOrEmpty(studentId) ) {
			throw new GeneralException("native identity was null for account request: " + rqst.toXml());
		}

//		String orgId = getStringAttribute(rqst, "");
//		String lastName = getStringAttribute(rqst, ATT_STUDENT_LAST_NAME);
//		String firstName = getStringAttribute(rqst, ATT_STUDENT_FIRST_NAME);
//		String midInitial = getStringAttribute(rqst, ATT_STUDENT_MIDDLE_INITIAL);
//		String password = getStringAttribute(rqst, ATT_STUDENT_PASSWORD);
//		String emailId = getStringAttribute(rqst, ATT_STUDENT_EMAIL_ID);
//		Boolean isActive = getBooleanAttribute(rqst, ATT_STUDENT_IS_ACTIVE_FLAG);
//		String subOrg0 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_0);
//		String subOrg1 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_1);
//		String subOrg2 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_2);
//		String subOrg3 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_3);
//		String subOrg4 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_4);
//		String subOrg5 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_5);
//		String subOrg6 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_6);
//		String subOrg8 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_8);
//		String subOrg9 = getStringAttribute(rqst, ATT_STUDENT_SUB_ORG_9);
//
//		PreparedStatement statement = connection.prepareStatement(getProperty("MSCAIMS-11.insertAccount"));
//		statement.setString(1, orgId);
//		statement.setString(2, studentId);
//		statement.setString(3, lastName);
//		statement.setString(4, firstName);
//		statement.setString(5, midInitial);
//		statement.setString(6, password);
//		statement.setString(7, emailId);
//		statement.setBoolean(8, isActive);
//		statement.setString(9, subOrg0);
//		statement.setString(10, subOrg1);
//		statement.setString(11, subOrg2);
//		statement.setString(12, subOrg3);
//		statement.setString(13, subOrg4);
//		statement.setString(14, subOrg5);
//		statement.setString(15, subOrg6);
//		statement.setString(16, subOrg8);
//		statement.setString(17, subOrg9);
//
//		statement.executeUpdate();
	}

	public void deleteAccount(/*AccountRequest rqst,*/ String id) throws GeneralException, SQLException, IOException
	{
		//String id = rqst.getNativeIdentity();

		if(Util.isAnyNullOrEmpty(id))
		{
		//	throw new GeneralException("Native identity was null for the account request: " + rqst.toXml());
		}

		//log.debug("Processing the following account request: " + rqst.toXml());
		PreparedStatement preparedStatement = connection.prepareStatement(getProperty("MSCAIMS-11.deleteAccount"));
		preparedStatement.setString(1, id);
		preparedStatement.executeUpdate();
	}

	private String getProperty(String key) {
		if(input==null)
		{
			loadPropertiesFile();
		}

		return prop.getProperty(key);
	}

	public void updateAccount(List<AccountRequest> accountRequestList) throws GeneralException, SQLException
	{
		for(AccountRequest accountRequest : accountRequestList)
		{
			List <AttributeRequest> attributeList = accountRequest.getAttributeRequests();

			if(Util.isAnyNullOrEmpty(accountRequest.getNativeIdentity()))
			{
				throw new GeneralException("Native identity was null for the account request" + accountRequest.toXml());
			}

			PreparedStatement statement = connection.prepareStatement(buildDynamicUpdateQuery(attributeList));

			for(int index = 0; index < attributeList.size(); index ++)
			{
				//TODO Add more parameters
				if(attributeList.get(index).getValue().getClass().toString().equals("class java.lang.String"))
				{
					statement.setString(index + 1, (String) attributeList.get(index).getValue());
				}

				if(attributeList.get(index).getValue().getClass().toString().equals("class java.lang.Integer"))
				{
					statement.setInt(index + 1, (int) attributeList.get(index).getValue());
				}

			}

			statement.setString(attributeList.size() + 1, accountRequest.getNativeIdentity());
			statement.executeUpdate();
		}
	}

	private String buildDynamicUpdateQuery(List<AttributeRequest> attributeRequest)
	{
		//TODO strings query blocks come from .properties
		StringBuilder stringBuilder = new StringBuilder(getProperty("MSCAIMS-11.updatedUpdatedSt"));
		Iterator<AttributeRequest> iterator = attributeRequest.iterator();

		while (iterator.hasNext())
		{
			switch(iterator.next().getName())
			{
				//TODO Add more parameter to match from the Provisioning to the DataBase.
				case "LAN_Id":
					stringBuilder.append(" ");
					stringBuilder.append(getProperty("MSCAIMS-11.updateLAN_Id"));
					break;
				case "USR_Name":
					stringBuilder.append(getProperty("MSCAIMS-11.updateUSR_Name"));
					break;
			}
		}
		stringBuilder.append(" ");
		stringBuilder.append(getProperty("MSCAIMS-11.updatedWhereSt"));
		return stringBuilder.toString();
	}

	private String getStringAttribute(AccountRequest acctRqst, String attrName)
	{
		log.debug("getStringAttribute: " + attrName);
		String value = "";
		AttributeRequest attrRqst = acctRqst.getAttributeRequest(attrName);
		if ( attrRqst != null )
		{
			if ( attrRqst.getValue() != null )
			{
				value = Util.otoa(attrRqst.getValue());
			}
			else
			{
				log.debug("Attribute request value was null");
			}
		}
		else
		{
			log.debug("No attribute request found");
		}
		log.debug("Value: " + value);
		return value;
	}

	private Boolean getBooleanAttribute(AccountRequest acctRqst, String attrName)
	{
		log.debug("getBooleanAttribute: " + attrName);
		Boolean value = false;
		AttributeRequest attrRqst = acctRqst.getAttributeRequest(attrName);

		if ( attrRqst != null )
		{
			if ( attrRqst.getValue() != null )
			{
				value = Util.otob(attrRqst.getValue());
			}
			else
			{
				log.debug("Attribute request value was null");
			}
		}
		else
		{
			log.debug("No attribute request found");
		}
		log.debug("Value: " + value);
		return value;
	}

	public static void main(String[] args) throws GeneralException, SQLException, IOException
	{

	}

	public void pruebaDeProperties()
	{
		System.out.println(getProperty("MSCAIMS-11.insertAccount"));
	}
}
