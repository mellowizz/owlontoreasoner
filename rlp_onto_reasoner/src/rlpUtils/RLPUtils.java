package rlpUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RLPUtils {
	   private static String OS = null;
	   public static String getOsName()
	   {
	      if(OS == null) { OS = System.getProperty("os.name").toLowerCase(); }
	      return OS;
	   }
	   public static boolean isWindows()
	   {
	      return getOsName().startsWith("win");
	   }

	   public static boolean isMac()
	   {
		   return getOsName().startsWith("mac");
	   }
	   public static boolean isLinux()
	   {
		   return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	   }

	public static ArrayList<String> getDistinctValuesFromTbl(String tableName, String colName) throws SQLException{
		ArrayList<String> columnValues = null; 
		String url = "jdbc:postgresql://localhost:5432/rlp_saarburg?user=postgres&password=BobtheBuilder";
		Statement st = null;
		Connection db = null;
		ResultSetMetaData rsmd = null;
		if (colName == "" || colName == null) return null; 
		try {
			db = DriverManager.getConnection(url);
			st = db.createStatement();
			String myQuery = "SELECT DISTINCT( " + colName + ")"
							 			 + " FROM " + tableName
							 			 + " WHERE " + colName + " IS NOT NULL";
			System.out.println(myQuery);
			ResultSet rs = st.executeQuery(myQuery);
			rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			columnValues = new ArrayList<String>(colCount);
			while (rs.next()) {
				String parameter = rs.getString(colName); //quotedColName);
				System.out.println(parameter);
				if (parameter.contains(" ")){
					parameter = parameter.replace(" ", "_");
				}
				/*
				if (parameter.startsWith("0")){
					parameter = "false";
				} else if (parameter.startsWith("1")){
					parameter = "true";
				}
				*/
				columnValues.add(parameter);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException f){
			f.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
				st = null;
			}
			if (db !=null){
				db.close();
				db = null;
			}
		}
		return columnValues;
	}
}
