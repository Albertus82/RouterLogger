package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;

import it.albertus.routerlogger.gui.RouterLoggerGui;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class EditClearSubMenuListener implements ArmListener, MenuListener {

	private final RouterLoggerGui gui;

	public EditClearSubMenuListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(final ArmEvent ae) {
		execute();
	}

	@Override
	public void menuShown(final MenuEvent e) {
		execute();
	}

	@Override
	public void menuHidden(final MenuEvent e) {/* Ignore */}

	private void execute() {
		gui.getMenuBar().getEditClearDataTableMenuItem().setEnabled(gui.getDataTable().canClear());
		gui.getMenuBar().getEditClearConsoleMenuItem().setEnabled(gui.canClearConsole());
	}

}
