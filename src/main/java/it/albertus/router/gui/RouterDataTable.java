package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class RouterDataTable {

	private interface Defaults {
		int GUI_TABLE_MAX_ITEMS = 2000;
		boolean GUI_TABLE_COLUMNS_PACK = false;
	}

	private static class Singleton {
		private static final RouterDataTable TABLE = new RouterDataTable();
	}

	public static RouterDataTable getInstance() {
		return Singleton.TABLE;
	}

	private RouterDataTable() {}

	public void init(final Composite container, final Object layoutData) {
		if (this.table == null) {
			this.table = createTable(container, layoutData);
			createContextMenu();
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	private Table createTable(final Composite container, final Object layoutData) {
		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Supporto CTRL+C per "Copia"...
				if (e.stateMask == SWT.MOD1 && e.keyCode == GuiUtils.KEY_COPY && table.getSelection() != null && table.getSelection().length != 0) {
					copySelection();
				}

				// Supporto CTRL+A per "Seleziona tutto"...
				if (e.stateMask == SWT.MOD1 && e.keyCode == GuiUtils.KEY_SELECT_ALL) {
					table.selectAll();
				}
			}
		});

		return table;
	}

	private Menu createContextMenu() {
		final Menu menu = new Menu(table);

		// Copia...
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.copy") + '\t' + GuiUtils.getMod1KeyLabel() + '+' + Character.toUpperCase(GuiUtils.KEY_COPY));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				copySelection();
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		// Seleziona tutto...
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.select.all") + '\t' + GuiUtils.getMod1KeyLabel() + '+' + Character.toUpperCase(GuiUtils.KEY_SELECT_ALL));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				table.selectAll();
			}
		});

		table.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});

		return menu;
	}

	private void copySelection() {
		if (table.getColumns() != null && table.getColumns().length != 0) {
			if (table.getSelectionCount() > 1) {
				System.gc(); // La copia negli appunti puo' richiedere molta memoria!
			}
			final StringBuilder data = new StringBuilder();

			// Testata...
			for (final TableColumn column : table.getColumns()) {
				data.append(column.getText()).append(FIELD_SEPARATOR);
			}
			data.replace(data.length() - 1, data.length(), NewLine.SYSTEM_LINE_SEPARATOR);

			// Dati selezionati (ogni TableItem rappresenta una riga)...
			for (final TableItem item : table.getSelection()) {
				for (int i = 0; i < table.getColumnCount(); i++) {
					data.append(item.getText(i)).append(FIELD_SEPARATOR);
				}
				data.replace(data.length() - 1, data.length(), NewLine.SYSTEM_LINE_SEPARATOR);
			}

			// Inserimento dati negli appunti...
			final Clipboard clipboard = new Clipboard(table.getDisplay());
			clipboard.setContents(new String[] { data.toString() }, new TextTransfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
		}
	}

	private static final char FIELD_SEPARATOR = '\t';
	private static final DateFormat DATE_FORMAT_TABLE_GUI = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	private final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	private Table table = null;
	private boolean tableInitialized = false;
	private final boolean packColumns = configuration.getBoolean("gui.table.columns.pack", Defaults.GUI_TABLE_COLUMNS_PACK);

	public void addRow(final RouterData data, final Map<Threshold, String> thresholdsReached, final int iteration) {
		if (data != null && data.getData() != null && !data.getData().isEmpty()) {
			final Map<String, String> info = data.getData();
			final String timestamp = DATE_FORMAT_TABLE_GUI.format(data.getTimestamp());
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						if (table != null && !table.isDisposed()) {
							// Header (una tantum)...
							if (!tableInitialized) {
								// Iterazione...
								TableColumn column = new TableColumn(table, SWT.NONE);
								column.setText(Resources.get("lbl.column.iteration.text"));
								column.setToolTipText(Resources.get("lbl.column.iteration.tooltip"));

								// Timestamp...
								column = new TableColumn(table, SWT.NONE);
								column.setText(Resources.get("lbl.column.timestamp.text"));
								column.setToolTipText(Resources.get("lbl.column.timestamp.tooltip"));

								// Tempo di risposta...
								column = new TableColumn(table, SWT.NONE);
								column.setText(Resources.get("lbl.column.response.time.text"));
								column.setToolTipText(Resources.get("lbl.column.response.time.tooltip"));

								// Tutte le altre colonne...
								for (String key : info.keySet()) {
									column = new TableColumn(table, SWT.NONE);
									column.setText(packColumns ? " " : key);
									column.setToolTipText(key);
								}
							}

							// Dati...
							int i = 0;
							final TableItem item = new TableItem(table, SWT.NONE, 0);
							item.setText(i++, Integer.toString(iteration));
							item.setText(i++, timestamp);
							item.setText(i++, Integer.toString(data.getResponseTime()));

							for (String key : info.keySet()) {
								// Grassetto...
								if (key != null && configuration.getGuiImportantKeys().contains(key.trim())) {
									FontRegistry fontRegistry = JFaceResources.getFontRegistry();
									if (!fontRegistry.hasValueFor("tableBold")) {
										final Font tableFont = item.getFont();
										final FontData oldFontData = tableFont.getFontData()[0];
										fontRegistry.put("tableBold", new FontData[] { new FontData(oldFontData.getName(), oldFontData.getHeight(), SWT.BOLD) });
									}
									item.setFont(i, fontRegistry.get("tableBold"));

									// Evidenzia cella...
									item.setBackground(i, item.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
								}

								// Colore per i valori oltre soglia...
								for (final Threshold threshold : thresholdsReached.keySet()) {
									if (key.equals(threshold.getKey())) {
										item.setForeground(i, item.getDisplay().getSystemColor(SWT.COLOR_RED));
										break;
									}
								}

								item.setText(i++, info.get(key));
							}

							// Dimesionamento delle colonne (una tantum)...
							if (!tableInitialized) {
								for (int j = 0; j < table.getColumns().length; j++) {
									table.getColumn(j).pack();
								}
								table.getColumn(0).setWidth((int) (table.getColumn(0).getWidth() * 1.3));

								if (packColumns) {
									table.getColumn(2).setWidth((int) (table.getColumn(0).getWidth()));
									final String[] stringArray = new String[info.keySet().size()];
									final TableColumn[] columns = table.getColumns();
									final int startIndex = 3;
									for (int k = startIndex; k < columns.length; k++) {
										final TableColumn column = columns[k];
										column.setText(info.keySet().toArray(stringArray)[k - startIndex]);
									}
								}
								tableInitialized = true;
							}

							// Limitatore righe in tabella...
							final int maxItems = configuration.getInt("gui.table.items.max", Defaults.GUI_TABLE_MAX_ITEMS);
							if (table.getItemCount() > maxItems) {
								table.remove(maxItems);
							}
						}
					}
					catch (IllegalArgumentException iae) {}
					catch (SWTException se) {}
				}
			});
		}
	}

}