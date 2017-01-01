package it.albertus.router.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.resources.Messages;

public class ClearConsoleSelectionListener extends ClearSelectionListener {

	public ClearConsoleSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canClearConsole() && confirm(Messages.get("msg.confirm.clear.console.text"), Messages.get("msg.confirm.clear.console.message"))) {
			gui.getConsole().clear();
		}
	}

}
