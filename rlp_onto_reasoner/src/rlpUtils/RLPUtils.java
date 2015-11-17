package rlpUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
		ArrayList<String> columnValues = new ArrayList<String>();
		String url = "jdbc:postgresql://localhost:5432/RLP?user=postgres&password=BobtheBuilder";
		Statement st = null;
		Connection db = null;
		try {
			db = DriverManager.getConnection(url);
			st = db.createStatement();
			String myQuery = String.format("SELECT DISTINCT( \"%s\") as param "
							 			 + "FROM \"%s\" "
							 			 + "WHERE\"%s\" != ''",
							 			 colName, tableName, colName); 
			System.out.println(myQuery);
			ResultSet rs = st.executeQuery(myQuery);
			while (rs.next()) {
				String quotedColName = "\""+colName + "\"";
				String parameter = rs.getString("param"); //quotedColName);
				if (parameter == null){ 
					System.out.println("parameter == NULL!");
					continue;
				}
				System.out.println(parameter);
				if (parameter.contains("/")){
					parameter = parameter.split("/")[1];
				} else if (parameter.contains(" ")){
					parameter = parameter.replace(" ", "_");
				}
				columnValues.add(parameter);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException f){
			f.printStackTrace();
		} finally {
			if (st != null) {
				st.close();
			}
		}
		return columnValues;
	}
}
