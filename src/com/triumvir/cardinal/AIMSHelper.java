package com.triumvir.cardinal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
		this.connection = conn;
	}

	private void loadPropertiesFile()
	{
		try
		{
			input = new FileInputStream("querys.properties");

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


	private String getProperty(String key) {
		if(input==null)
		{
			loadPropertiesFile();
		}

		return prop.getProperty(key);
	}
	
	public void deleteAccount(AccountRequest rqst, String queryToExecute) throws GeneralException, SQLException, IOException
	{
		String id = rqst.getNativeIdentity();

		if(Util.isAnyNullOrEmpty(id))
		{
			throw new GeneralException("Native identity was null for the account request: " + rqst.toXml());
		}

		log.debug("Processing the following account request: " + rqst.toXml());

		PreparedStatement preparedStatement = connection.prepareStatement(getProperty("queryToExecute"));
		preparedStatement.setString(1, id);
		preparedStatement.executeUpdate();
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

	public static void main(String[] args)
	{
		AIMSHelper helper = new AIMSHelper(null);

		helper.pruebaDeProperties();
	}

	public void pruebaDeProperties()
	{
		System.out.println(getProperty("MSCAIMS-11.insertAccount"));
	}
}
