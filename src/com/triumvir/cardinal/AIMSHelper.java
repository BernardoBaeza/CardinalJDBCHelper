package com.triumvir.cardinal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
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
	

	public AIMSHelper(Connection conn) {
		this.connection = conn;
	}
	

	private void loadPropertiesFile()
	{
		InputStream input = null;
		try
		{
			input = this.getClass().getResourceAsStream("querys.properties");
			// load a properties file
			prop.load(input);

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
					//mandar a log
				}
			}
		}
	}

	public void createAccount(String queryKey,List<Object> params) throws SQLException, GeneralException 
	{	
		//should I receive the account request?
		
		PreparedStatement statement = connection.prepareStatement(getProperty(queryKey));
		ParameterMetaData parameterData = statement.getParameterMetaData(); 
		int parametersCount = parameterData.getParameterCount(); 
		
		if(parametersCount!= params.size())
		{
			//mandar a log
			System.err.println("params err msg");
		}
		else
		{
			for (int i = 0; i < parametersCount; i++) 
			{
				Object parameter = params.get(i);
				if(parameter instanceof String)
				{
					statement.setString(i, parameter.toString());
				}
				else if(parameter instanceof Integer)
				{
					statement.setInt(i, (Integer) parameter);
				}
				else if(parameter instanceof java.sql.Timestamp)
				{
					Timestamp timeParam = (Timestamp) parameter;
					statement.setTimestamp(i, timeParam);
				}
				else
				{
					throw new GeneralException("generic msg for unespected type");
				}				
			}
			statement.executeUpdate();
			
		}		
	}

	public void deleteAccount(String queryKey,/*AccountRequest rqst,*/ String id) throws GeneralException, SQLException, IOException
	{
		//String id = rqst.getNativeIdentity();

		if(Util.isAnyNullOrEmpty(id))
		{
		//	throw new GeneralException("Native identity was null for the account request: " + rqst.toXml());
		}

		//log.debug("Processing the following account request: " + rqst.toXml());
		PreparedStatement preparedStatement = connection.prepareStatement(getProperty(queryKey));
		preparedStatement.setString(1, id);
		preparedStatement.executeUpdate();
	}

	private String getProperty(String key) {
		if(prop.isEmpty())
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
				if(attributeList.get(index).getValue() instanceof String)
				{
					statement.setString(index + 1, (String) attributeList.get(index).getValue());
				}

				if(attributeList.get(index).getValue() instanceof Integer)
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
			if ( attrRqst.getValue() != null && !"null".equals(Util.otoa(attrRqst.getValue())))
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


}
