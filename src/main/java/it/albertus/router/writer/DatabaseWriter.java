package it.albertus.router.writer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.ConfigurationException;
import it.albertus.util.StringUtils;
import it.albertus.util.sql.SqlUtils;

public class DatabaseWriter extends Writer {

	public static final String DESTINATION_KEY = "lbl.writer.destination.database";

	public static class Defaults {
		public static final String TABLE_NAME = "router_log";
		public static final String COLUMN_NAME_PREFIX = "rl_";
		public static final String TIMESTAMP_COLUMN_TYPE = "TIMESTAMP";
		public static final String RESPONSE_TIME_COLUMN_TYPE = "INTEGER";
		public static final String INFO_COLUMN_TYPE = "VARCHAR(250)";
		public static final int COLUMN_NAME_MAX_LENGTH = 30;
		public static final int CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS = 2000;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_DB_PASSWORD = "database.password";
	protected static final String CFG_KEY_DB_USERNAME = "database.username";
	protected static final String CFG_KEY_DB_URL = "database.url";
	protected static final String CFG_KEY_DB_DRIVER_CLASS_NAME = "database.driver.class.name";

	protected static final String MSG_KEY_ERR_CONFIGURATION_REVIEW = "err.configuration.review";
	protected static final String MSG_KEY_ERR_DATABASE_CFG_ERROR = "err.database.cfg.error";

	protected Connection connection = null;

	public DatabaseWriter() {
		if (StringUtils.isBlank(configuration.getString(CFG_KEY_DB_DRIVER_CLASS_NAME))) {
			throw new ConfigurationException(Messages.get(MSG_KEY_ERR_DATABASE_CFG_ERROR) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), CFG_KEY_DB_DRIVER_CLASS_NAME);
		}
		if (StringUtils.isBlank(configuration.getString(CFG_KEY_DB_URL))) {
			throw new ConfigurationException(Messages.get(MSG_KEY_ERR_DATABASE_CFG_ERROR) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), CFG_KEY_DB_URL);
		}
		if (!configuration.contains(CFG_KEY_DB_USERNAME)) {
			throw new ConfigurationException(Messages.get(MSG_KEY_ERR_DATABASE_CFG_ERROR) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), CFG_KEY_DB_USERNAME);
		}
		if (!configuration.contains(CFG_KEY_DB_PASSWORD)) {
			throw new ConfigurationException(Messages.get(MSG_KEY_ERR_DATABASE_CFG_ERROR) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), CFG_KEY_DB_PASSWORD);
		}
		try {
			Class.forName(configuration.getString(CFG_KEY_DB_DRIVER_CLASS_NAME));
		}
		catch (final Throwable t) {
			throw new ConfigurationException(Messages.get("err.database.jar", configuration.getString(CFG_KEY_DB_DRIVER_CLASS_NAME), configuration.getFileName()), t, CFG_KEY_DB_DRIVER_CLASS_NAME);
		}
	}

	@Override
	public synchronized void saveInfo(final RouterData data) {
		final Map<String, String> info = data.getData();

		try {
			final boolean showMessage;
			// Connessione al database...
			if (connection == null || !connection.isValid(configuration.getInt("database.connection.validation.timeout.ms", Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS))) {
				connection = DriverManager.getConnection(configuration.getString(CFG_KEY_DB_URL), configuration.getString(CFG_KEY_DB_USERNAME), configuration.getString(CFG_KEY_DB_PASSWORD));
				connection.setAutoCommit(true);
				showMessage = true;
			}
			else {
				showMessage = false;
			}

			// Verifica esistenza tabella ed eventuale creazione...
			final String tableName = getTableName();
			if (!tableExists(tableName)) {
				out.println(Messages.get("msg.creating.database.table", tableName), true);
				createTable(tableName, info);
			}

			// Inserimento dati...
			if (showMessage) {
				out.println(Messages.get("msg.logging.into.database", tableName), true);
			}

			final Map<Integer, String> columns = new HashMap<Integer, String>();

			final StringBuilder dml = new StringBuilder("INSERT INTO ").append(tableName).append(" (").append(getTimestampColumnName());
			dml.append(", ").append(getResponseTimeColumnName());
			int index = 3;
			for (final String key : info.keySet()) {
				columns.put(index++, key);
				dml.append(", ").append(getColumnName(key));
			}
			dml.append(") VALUES (?, ?");
			for (int i = 0; i < info.size(); i++) {
				dml.append(", ?");
			}
			dml.append(')');

			PreparedStatement insert = null;
			try {
				insert = connection.prepareStatement(dml.toString());
				insert.setTimestamp(1, new Timestamp(data.getTimestamp().getTime()));
				insert.setInt(2, data.getResponseTime());
				for (final Entry<Integer, String> entry : columns.entrySet()) {
					insert.setString(entry.getKey(), info.get(entry.getValue()));
				}
				insert.executeUpdate();
			}
			finally {
				SqlUtils.closeQuietly(insert);
			}
		}
		catch (final Exception e) { // In caso di errore, chiudere la connessione al database.
			closeDatabaseConnection();
			throw new DatabaseException(e);
		}
	}

	protected String getResponseTimeColumnName() {
		return getColumnName("response_time_ms");
	}

	protected String getTimestampColumnName() {
		return getColumnName("timestamp");
	}

	protected boolean tableExists(final String tableName) {
		PreparedStatement statement = null;
		try {
			// Verifica esistenza tabella...
			statement = connection.prepareStatement("SELECT 1 FROM " + tableName.replace("'", "''"));
			statement.setFetchSize(1);
			statement.executeQuery();
			return true;
		}
		catch (final SQLException se) {
			return false;
		}
		finally {
			SqlUtils.closeQuietly(statement);
		}
	}

	protected void createTable(final String tableName, final Map<String, String> info) throws SQLException {
		final String timestampColumnType = configuration.getString("database.timestamp.column.type", Defaults.TIMESTAMP_COLUMN_TYPE);
		final String responseTimeColumnType = configuration.getString("database.response.column.type", Defaults.RESPONSE_TIME_COLUMN_TYPE);
		final String infoColumnType = configuration.getString("database.info.column.type", Defaults.INFO_COLUMN_TYPE);

		// Creazione tabella...
		StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(tableName).append(" (").append(getTimestampColumnName()).append(' ').append(timestampColumnType);
		ddl.append(", ").append(getResponseTimeColumnName()).append(' ').append(responseTimeColumnType); // Response time
		for (String key : info.keySet()) {
			ddl.append(", ").append(getColumnName(key)).append(' ').append(infoColumnType);
		}
		ddl.append(", CONSTRAINT pk_routerlogger PRIMARY KEY (").append(getTimestampColumnName()).append("))");

		PreparedStatement createTable = null;
		try {
			createTable = connection.prepareStatement(ddl.toString());
			createTable.executeUpdate();
		}
		finally {
			SqlUtils.closeQuietly(createTable);
		}
	}

	protected String getTableName() {
		return configuration.getString("database.table.name", Defaults.TABLE_NAME).replaceAll("[^A-Za-z0-9_]+", "");
	}

	protected String getColumnName(final String name) {
		String completeName = configuration.getString("database.column.name.prefix", Defaults.COLUMN_NAME_PREFIX) + name;
		completeName = completeName.replaceAll("[^A-Za-z0-9_]+", "");
		final int maxLength = configuration.getInt("database.column.name.max.length", Defaults.COLUMN_NAME_MAX_LENGTH);
		if (completeName.length() > maxLength) {
			completeName = completeName.substring(0, maxLength);
		}
		return completeName;
	}

	@Override
	public void release() {
		closeDatabaseConnection();
	}

	protected void closeDatabaseConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				logger.log(Messages.get("msg.closing.database.connection"), Destination.CONSOLE);
				connection.close();
				connection = null;
			}
		}
		catch (final SQLException se) {
			logger.log(se);
		}
	}

}
