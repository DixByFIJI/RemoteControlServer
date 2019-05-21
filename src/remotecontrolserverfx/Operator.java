/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotecontrolserverfx;

import com.sun.deploy.config.Config;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.PrintRequestAttributeSet;
import remotecontrolserverfx.connections.SQLiteConnector;
import remotecontrolserverfx.requests.Requestable;
import remotecontrolserverfx.connections.ExecutingTask;

/**
 *
 * @author Username
 */
public abstract class Operator {
	
	public static boolean createTable(String table){
		boolean isExecuted = false;
		String query = "CREATE TABLE " + table + " (name VARCHAR(50) NOT NULL PRIMARY KEY, path VARCHAR(100) NOT NULL);";
		isExecuted = Requestable.execute(query) == null;
		return isExecuted;
	}
	
	public static boolean deleteTable(String table){
		String query = "DROP TABLE " + table;
		ExecutingTask<Boolean> task = new ExecutingTask<Boolean>() {
			@Override
			public Boolean onPostExecuted(ResultSet resultSet) {
				System.out.println("OnPostExecuted");
				return resultSet != null;
			}
		};
		
		return task.execute(query);
	}
	
	public static List<String> getTables(){
		final String query = "SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY rootpage DESC;";
		
		ExecutingTask<List<String>> task = new ExecutingTask<List<String>>() {
			@Override
			public List<String> onPostExecuted(ResultSet resultSet) {
				List<String> list = null;
				try {
					list = new LinkedList<String>(){{
							while (resultSet.next()) {
								add(resultSet.getString(1));
							}
					}};
				} catch (SQLException ex) {
						Logger.getLogger(Operator.class.getName()).log(Level.SEVERE, null, ex);
				}
				return list;
			}
		};
		
		return task.execute(query);
	}
	
	public static List<String> getColumnNames(String table){
		String query = "SELECT * FROM " + table;
		ExecutingTask<List<String>> task = new ExecutingTask<List<String>>() {
			@Override
			public List<String> onPostExecuted(ResultSet resultSet) {
				List<String> list = null;
				try {
					resultSet.next();
					list = new LinkedList<String>(){{
						for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
							add(resultSet.getMetaData().getColumnLabel(i));
						}
					}};
				} catch (SQLException ex) {
					Logger.getLogger(Operator.class.getName()).log(Level.SEVERE, null, ex);
				}
				return list;
			}
		};
		return task.execute(query);
	}
	
	public static int getColumnCount(String table){
		String query = "SELECT * FROM " + table;
		ExecutingTask<Integer> task = new ExecutingTask<Integer>() {
			@Override
			public Integer onPostExecuted(ResultSet resultSet) {
				int columnCount = 0;
				try {
					resultSet.next();
					columnCount = resultSet.getMetaData().getColumnCount();
				} catch (SQLException ex) {
					Logger.getLogger(Operator.class.getName()).log(Level.SEVERE, null, ex);
				}
				return columnCount;
			}
		};
		
		return task.execute(query);
	}
	
	public static void insert(String table, List<ArrayList<String>> data){
		String query = "INSERT INTO " + table + "(`"
			+ String.join("`, `", getColumnNames(table))
			+ "`) VALUES("
			+ String.join(", ", getColumnNames(table).stream().map(x -> "?").toArray(String[]::new)) + ");";
		
		for (ArrayList<String> rowData : data) {
			Requestable.execute(query, rowData.stream().toArray(String[]::new));
		}
	}
	
		public static List<ArrayList<String>> selectAll(String table){
		String query = "SELECT * FROM " + table + ";";
		final List<String> columnNames = getColumnNames(table);
		ExecutingTask<List<ArrayList<String>>> task = new ExecutingTask<List<ArrayList<String>>>() {
			@Override
			public List<ArrayList<String>> onPostExecuted(ResultSet resultSet) {
				List<ArrayList<String>> list = null;
				try {
					list = new ArrayList<ArrayList<String>>(){{
						while(resultSet.next()){
							add(new ArrayList<String>(){{
								for (String column : columnNames) {
									add(resultSet.getString(column));
								}
							}});
						}
					}};
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				return list;
			}
		};
		
		return task.execute(query);
	}
	
	public static void deleteAll(String table){
		String query = "DELETE FROM " + table + ";";
		Requestable.execute(query);
	}
}
