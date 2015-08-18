package rlpUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RLPUtils {

	public static ArrayList<String> getDistinctValuesFromTbl(String tableName, String colName) throws SQLException{
		ArrayList<String> columnValues = new ArrayList<String>();
		String url = "jdbc:postgresql://localhost/RLP?user=postgres&password=BobtheBuilder";
		Statement st = null;
		Connection db = null;
		try {
			db = DriverManager.getConnection(url);
			st = db.createStatement();
			String myQuery = String.format("SELECT DISTINCT( \"%s\") as parameter FROM \"%s\" where \"%s\" != ''", colName, tableName, colName); 
			System.out.println(myQuery);
			ResultSet rs = st.executeQuery(myQuery);
			while (rs.next()) {
				String quotedColName = "\""+colName + "\"";
				String parameter = rs.getString("parameter"); //quotedColName);
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
