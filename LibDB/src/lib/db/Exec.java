package lib.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Exec
{
	public static void main(String[] args) throws Exception
	{
		Class.forName("org.mariadb.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost", "root", "123456");
		if (null == conn) {
			System.err.println("error");
			System.exit(0);
		}
		System.err.println(conn.isClosed());
		Statement statement = conn.createStatement();
		statement.executeUpdate("use test");
		ResultSet rs = statement.executeQuery("show tables");
		System.err.println("--------------------------------");
		while (rs.next()) {
			System.err.println(rs.getString(1));
		}
		System.err.println("--------------------------------");
		rs.close();
		
		int cnt = statement.executeUpdate("use mysql");
		System.err.println(cnt);
		
		rs = statement.executeQuery("show tables");
		System.err.println("--------------------------------");
		while (rs.next()) {
			System.err.println(rs.getString(1));
		}
		System.err.println("--------------------------------");
		rs.close();
		
		statement.close();
		conn.close();
		System.err.println(conn.isClosed());
		System.err.println("successful");
	}
}
