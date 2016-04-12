package it.albertus.router.writer;

import it.albertus.router.engine.ConfigurationException;
import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CsvWriter extends Writer {

	public static final String DESTINATION_KEY = "lbl.writer.destination.csv";

	private static final String LINE_SEPARATOR = NewLine.SYSTEM_LINE_SEPARATOR;

	public interface Defaults {
		NewLine NEW_LINE = LINE_SEPARATOR != null ? NewLine.getEnum(LINE_SEPARATOR) : NewLine.CRLF;
		String DIRECTORY = getDefaultDirectory();
		String FIELD_SEPARATOR = ";";
		String FIELD_SEPARATOR_REPLACEMENT = ",";
	}

	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");

	private FileWriter logFileWriter = null;

	@Override
	public synchronized void saveInfo(final RouterData info) {
		// Selezione del percorso e nome del file di destinazione...
		final String csvDestinationDir = configuration.getString("csv.destination.path");
		File csvFile;
		if (StringUtils.isNotBlank(csvDestinationDir)) {
			File logDestDir = new File(csvDestinationDir.trim());
			if (logDestDir.exists() && !logDestDir.isDirectory()) {
				throw new RuntimeException(Resources.get("err.invalid.path", logDestDir));
			}
			if (!logDestDir.exists()) {
				logDestDir.mkdirs();
			}
			csvFile = new File(csvDestinationDir.trim() + File.separator + DATE_FORMAT_FILE_NAME.format(new Date()) + ".csv");
		}
		else {
			csvFile = getDefaultFile();
		}

		try {
			// Scrittura header CSV (solo se il file non esiste gia')...
			String path;
			try {
				path = csvFile.getCanonicalPath();
			}
			catch (Exception e1) {
				try {
					path = csvFile.getAbsolutePath();
				}
				catch (Exception e2) {
					path = csvFile.getPath();
				}
			}

			if (!csvFile.exists()) {
				closeOutputFile();
				logFileWriter = new FileWriter(csvFile); // Crea nuovo file.
				out.println(Resources.get("msg.logging.to.file", path), true);
				logFileWriter.append(buildCsvHeader(info));
			}

			if (logFileWriter == null) {
				logFileWriter = new FileWriter(csvFile, true); // Apre file esistente.
				out.println(Resources.get("msg.logging.to.file", path), true);
			}
			logFileWriter.append(buildCsvRow(info));
			logFileWriter.flush();
		}
		catch (IOException ioe) {
			logger.log(ioe);
			closeOutputFile();
		}
	}

	@Override
	public void release() {
		closeOutputFile();
	}

	private String buildCsvHeader(final RouterData info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder header = new StringBuilder(Resources.get("lbl.column.timestamp.text")).append(fieldSeparator);
		header.append(Resources.get("lbl.column.response.time.text")).append(fieldSeparator); // Tempo di risposta.
		for (String field : info.getData().keySet()) {
			header.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
		}
		header.replace(header.length() - fieldSeparator.length(), header.length(), getRecordSeparator());
		return header.toString();
	}

	private String buildCsvRow(final RouterData info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder row = new StringBuilder(DATE_FORMAT_LOG.format(info.getTimestamp())).append(fieldSeparator);
		row.append(info.getResponseTime()).append(fieldSeparator); // Tempo di risposta.
		for (String field : info.getData().values()) {
			row.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
		}
		row.replace(row.length() - fieldSeparator.length(), row.length(), getRecordSeparator());
		return row.toString();
	}

	private String getFieldSeparator() {
		return configuration.getString("csv.field.separator", Defaults.FIELD_SEPARATOR);
	}

	private String getFieldSeparatorReplacement() {
		return configuration.getString("csv.field.separator.replacement", Defaults.FIELD_SEPARATOR_REPLACEMENT);
	}

	private String getRecordSeparator() {
		final String cfgKey = "csv.newline.characters";
		final String cfg = configuration.getString(cfgKey);
		if (cfg == null || cfg.length() == 0) {
			return Defaults.NEW_LINE.toString();
		}
		else {
			final NewLine newLine = NewLine.getEnum(cfg);
			if (newLine != null) {
				return newLine.toString();
			}
			else {
				throw new ConfigurationException(Resources.get("err.invalid.cfg", cfgKey) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), cfgKey);
			}
		}
	}

	private void closeOutputFile() {
		if (logFileWriter != null) {
			try {
				out.println(Resources.get("msg.closing.output.file"), true);
				logFileWriter.close();
				logFileWriter = null;
			}
			catch (IOException ioe) {
				logger.log(ioe);
			}
		}
	}

	private static File getDefaultFile() {
		File csvFile;
		try {
			csvFile = new File(new File(CsvWriter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParent() + File.separator + DATE_FORMAT_FILE_NAME.format(new Date()) + ".csv");
		}
		catch (final Exception e1) {
			try {
				/* In caso di problemi, scrive nella directory del profilo dell'utente */
				csvFile = new File(System.getProperty("user.home").toString() + File.separator + DATE_FORMAT_FILE_NAME.format(new Date()) + ".csv");
			}
			catch (final Exception e2) {
				/* Nella peggiore delle ipotesi, scrive nella directory corrente */
				csvFile = new File(DATE_FORMAT_FILE_NAME.format(new Date()) + ".csv");
			}
		}
		return csvFile;
	}

	private static String getDefaultDirectory() {
		String directory;
		try {
			directory = getDefaultFile().getParentFile().getCanonicalPath();
		}
		catch (Exception e1) {
			try {
				directory = getDefaultFile().getParentFile().getAbsolutePath();
			}
			catch (Exception e2) {
				directory = getDefaultFile().getParentFile().getPath();
			}
		}
		return directory;
	}

}
