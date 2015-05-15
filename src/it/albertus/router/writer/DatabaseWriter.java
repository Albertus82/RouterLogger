package it.albertus.router.writer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter extends Writer {

	private interface Defaults {
		String TABLE_NAME = "ROUTER_LOG";
		String COLUMN_TYPE = "VARCHAR";
		String COLUMN_LENGTH = "250";
		int CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS = 2000;
	}

	private static final String CONFIGURATION_KEY_DATABASE_PASSWORD = "database.password";
	private static final String CONFIGURATION_KEY_DATABASE_USERNAME = "database.username";
	private static final String CONFIGURATION_KEY_DATABASE_URL = "database.url";
	private static final String CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME = "database.driver.class.name";
	
	private static final String TIMESTAMP_COLUMN_NAME = "log_timestamp";

	private Connection connection = null;
	private boolean showMessage = true;
	private final int connectionValidationTimeoutInMillis;

	public DatabaseWriter() {
		if (configuration.getProperty(CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME) == null || configuration.getProperty(CONFIGURATION_KEY_DATABASE_URL) == null || configuration.getProperty(CONFIGURATION_KEY_DATABASE_USERNAME) == null || configuration.getProperty(CONFIGURATION_KEY_DATABASE_PASSWORD) == null) {
			throw new RuntimeException("Database configuration error. Review your " + CONFIGURATION_FILE_NAME + " file.");
		}
		try {
			Class.forName(configuration.getProperty(CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME));
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException("Missing database driver library (JAR) or misspelled class name \"" + configuration.getProperty(CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME) + "\" in your " + CONFIGURATION_FILE_NAME + " file.", e);
		}
		connectionValidationTimeoutInMillis = Integer.parseInt(configuration.getProperty("database.connection.validation.timeout.ms", Integer.toString(Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS)));
	}

	@Override
	public void saveInfo(final Map<String, String> info) {
		// Connessione al database...
		try {
			if (connection == null || !connection.isValid(connectionValidationTimeoutInMillis)) {
				connection = DriverManager.getConnection(configuration.getProperty(CONFIGURATION_KEY_DATABASE_URL), configuration.getProperty(CONFIGURATION_KEY_DATABASE_USERNAME), configuration.getProperty(CONFIGURATION_KEY_DATABASE_PASSWORD));
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException se) {
			throw new RuntimeException(se);
		}

		// Verifica esistenza tabella ed eventuale creazione...
		final String tableName = configuration.getProperty("database.table.name", Defaults.TABLE_NAME).trim().replace(' ', '_');
		if (!tableExists(tableName)) {
			System.out.println("Creating database table: " + tableName + "...");
			createTable(tableName, info);
		}

		// Inserimento dati...
		if (showMessage) {
			System.out.println("Logging into database table: " + tableName + "...");
			showMessage = false;
		}
		try {
			Map<Integer, String> columns = new HashMap<Integer, String>();
			StringBuilder dml = new StringBuilder("INSERT INTO ").append(tableName).append(" (" + TIMESTAMP_COLUMN_NAME);
			int index = 2;
			for (String key : info.keySet()) {
				columns.put(index++, key);
				dml.append(", ").append(cleanColumnName(key));
			}
			dml.append(") VALUES (?");
			for (int i = 0; i < info.size(); i++) {
				dml.append(", ?");
			}
			dml.append(')');
			PreparedStatement insert = connection.prepareStatement(dml.toString());
			insert.setTimestamp(1, new Timestamp(new Date().getTime()));
			for (int parameterIndex : columns.keySet()) {
				insert.setString(parameterIndex, info.get(columns.get(parameterIndex)));
			}
			insert.executeUpdate();
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
	}

	private boolean tableExists(final String tableName) {
		try {
			// Verifica esistenza tabella...
			PreparedStatement st = connection.prepareStatement("SELECT 1 FROM " + tableName);
			st.setFetchSize(1);
			st.executeQuery();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}

	private void createTable(final String tableName, final Map<String, String> info) {
		// Creazione tabella...
		StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(tableName).append(" (").append(TIMESTAMP_COLUMN_NAME).append(" TIMESTAMP");
		for (String key : info.keySet()) {
			ddl.append(", ").append(cleanColumnName(key)).append(' ').append(configuration.getProperty("database.column.type", Defaults.COLUMN_TYPE)).append('(').append(configuration.getProperty("database.column.length", Defaults.COLUMN_LENGTH)).append(')');
		}
		ddl.append(", CONSTRAINT pk_routerlogger PRIMARY KEY (").append(TIMESTAMP_COLUMN_NAME).append("))");

		try {
			PreparedStatement createTable = connection.prepareStatement(ddl.toString());
			createTable.executeUpdate();
		}
		catch (SQLException se) {
			throw new RuntimeException(se);
		}
	}

	private String cleanColumnName(String key) {
		return key.replaceAll("[^A-Za-z0-9_]+", "");
	}

	@Override
	public void release() {
		closeDatabaseConnection();
	}

	private void closeDatabaseConnection() {
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					System.out.println("Closing database connection.");
					connection.close();
					connection = null;
				}
			}
			catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

}