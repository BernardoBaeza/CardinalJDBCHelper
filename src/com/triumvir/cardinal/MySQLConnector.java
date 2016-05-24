package pruebas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.triumvir.cardinal.AIMSHelper;

public class MySQLConnector 
{

	public static void main(String[] args)
	{
		Connection conexion = null;
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			conexion = DriverManager.getConnection("jdbc:mysql://localhost/aims", "bbaeza", "12345");
			conexion.setAutoCommit(false);
			AIMSHelper helper = new AIMSHelper(conexion);
			
			ArrayList<Object> accountParams = new ArrayList<Object>();
			accountParams.add(11);
			accountParams.add("we");
			accountParams.add(1);
			accountParams.add("baeza client");
			accountParams.add("baeza client");
			accountParams.add(1);
			accountParams.add(new Timestamp(new Long("1463004872998")));
			accountParams.add(new Timestamp(new Long("1463004872998")));
			accountParams.add(new Timestamp(new Long("1463004872998")));
			accountParams.add("");
			accountParams.add(new Timestamp(new Long("1463004872998")));
			accountParams.add(new Timestamp(new Long("1463004872998")));
			accountParams.add(12);
			accountParams.add(12); 
			
			
			ArrayList<Object> relationParams = new ArrayList<Object>();
			relationParams.add(11);
			relationParams.add(1); //ALL RIGHTS
			relationParams.add(-1);
			
			
			helper.createAccount("MSCAIMS-11.INSERT.AMS_USE_TEST", accountParams);
			helper.createAccount("MSCAIMS-11.INSERT.USER_USERGROUP_TEST", relationParams);
			conexion.commit();
		
		} 
		catch (SQLException e) 
		{
			if (conexion != null) 
			{
				try
				{
					System.out.println("rollout");
					conexion.rollback();
				} 
				catch(SQLException excep) 
				{
					excep.printStackTrace();
				}
			}
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
