package it.albertus.router.writer;

import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter extends Writer {

	private interface Defaults {
		String TABLE_NAME = "router_log";
		String COLUMN_NAME_PREFIX = "rl_";
		String TIMESTAMP_COLUMN_TYPE = "TIMESTAMP";
		String INFO_COLUMN_TYPE = "VARCHAR(250)";
		int COLUMN_NAME_MAX_LENGTH = 30;
		int CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS = 2000;
	}

	private static final String CFG_KEY_DB_PASSWORD = "database.password";
	private static final String CFG_KEY_DB_USERNAME = "database.username";
	private static final String CFG_KEY_DB_URL = "database.url";
	private static final String CFG_KEY_DB_DRIVER_CLASS_NAME = "database.driver.class.name";

	private Connection connection = null;
	private boolean showMessage = true;
	private final int connectionValidationTimeoutInMillis;

	public DatabaseWriter() {
		if (!configuration.contains(CFG_KEY_DB_DRIVER_CLASS_NAME) || !configuration.contains(CFG_KEY_DB_URL) || !configuration.contains(CFG_KEY_DB_USERNAME) || !configuration.contains(CFG_KEY_DB_PASSWORD)) {
			throw new RuntimeException(Resources.get("err.database.cfg.error") + ' ' + Resources.get("err.review.cfg", configuration.getFileName()));
		}
		try {
			Class.forName(configuration.getString(CFG_KEY_DB_DRIVER_CLASS_NAME));
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(Resources.get("err.database.jar", configuration.getString(CFG_KEY_DB_DRIVER_CLASS_NAME), configuration.getFileName()), e);
		}
		connectionValidationTimeoutInMillis = configuration.getInt("database.connection.validation.timeout.ms", Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS);
	}

	@Override
	public synchronized void saveInfo(final RouterData data) {
		final Map<String, String> info = data.getData();

		// Connessione al database...
		try {
			if (connection == null || !connection.isValid(connectionValidationTimeoutInMillis)) {
				connection = DriverManager.getConnection(configuration.getString(CFG_KEY_DB_URL), configuration.getString(CFG_KEY_DB_USERNAME), configuration.getString(CFG_KEY_DB_PASSWORD));
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException se) {
			throw new RuntimeException(se);
		}

		// Verifica esistenza tabella ed eventuale creazione...
		final String tableName = getTableName();
		if (!tableExists(tableName)) {
			out.println(Resources.get("msg.creating.database.table", tableName), true);
			createTable(tableName, info);
		}

		// Inserimento dati...
		if (showMessage) {
			out.println(Resources.get("msg.logging.into.database", tableName), true);
			showMessage = false;
		}

		final String timestampColumnName = getColumnName("timestamp");
		Map<Integer, String> columns = new HashMap<Integer, String>();
		
		StringBuilder dml = new StringBuilder("INSERT INTO ").append(tableName).append(" (").append(timestampColumnName);
		int index = 2;
		for (String key : info.keySet()) {
			columns.put(index++, key);
			dml.append(", ").append(getColumnName(key));
		}
		dml.append(") VALUES (?");
		for (int i = 0; i < info.size(); i++) {
			dml.append(", ?");
		}
		dml.append(')');

		PreparedStatement insert = null;
		try {
			insert = connection.prepareStatement(dml.toString());
			insert.setTimestamp(1, new Timestamp(data.getTimestamp().getTime()));
			for (int parameterIndex : columns.keySet()) {
				insert.setString(parameterIndex, info.get(columns.get(parameterIndex)));
			}
			insert.executeUpdate();
		}
		catch (SQLException se) {
			logger.log(se);
		}
		finally {
			try {
				insert.close();
			}
			catch (Exception e) {}
		}
	}

	private boolean tableExists(final String tableName) {
		PreparedStatement statement = null;
		try {
			// Verifica esistenza tabella...
			statement = connection.prepareStatement("SELECT 1 FROM " + tableName);
			statement.setFetchSize(1);
			statement.executeQuery();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
		finally {
			try {
				statement.close();
			}
			catch (Exception e) {}
		}
	}

	private void createTable(final String tableName, final Map<String, String> info) {
		final String timestampColumnName = getColumnName("timestamp");
		final String timestampColumnType = configuration.getString("database.timestamp.column.type", Defaults.TIMESTAMP_COLUMN_TYPE);
		final String infoColumnType = configuration.getString("database.info.column.type", Defaults.INFO_COLUMN_TYPE);

		// Creazione tabella...
		StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(tableName).append(" (").append(timestampColumnName).append(' ').append(timestampColumnType);
		for (String key : info.keySet()) {
			ddl.append(", ").append(getColumnName(key)).append(' ').append(infoColumnType);
		}
		ddl.append(", CONSTRAINT pk_routerlogger PRIMARY KEY (").append(timestampColumnName).append("))");

		PreparedStatement createTable = null;
		try {
			createTable = connection.prepareStatement(ddl.toString());
			createTable.executeUpdate();
		}
		catch (SQLException se) {
			throw new RuntimeException(se);
		}
		finally {
			try {
				createTable.close();
			}
			catch (Exception e) {}
		}
	}
	
	private String getTableName() {
		return configuration.getString("database.table.name", Defaults.TABLE_NAME).replaceAll("[^A-Za-z0-9_]+", "");
	}

	private String getColumnName(String name) {
		name = configuration.getString("database.column.name.prefix", Defaults.COLUMN_NAME_PREFIX) + name; 
		name = name.replaceAll("[^A-Za-z0-9_]+", "");
		final int maxLength = configuration.getInt("database.column.name.max.length", Defaults.COLUMN_NAME_MAX_LENGTH);
		if (name.length() > maxLength) {
			name = name.substring(0, maxLength);
		}
		return name;
	}

	@Override
	public void release() {
		closeDatabaseConnection();
	}

	private void closeDatabaseConnection() {
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					out.println(Resources.get("msg.closing.database.connection"), true);
					connection.close();
					connection = null;
				}
			}
			catch (SQLException se) {
				logger.log(se);
			}
		}
	}

}
