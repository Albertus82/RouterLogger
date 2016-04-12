package it.albertus.router.gui.preference.field;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class LocalizedListEditor extends ListEditor {

	private boolean localized = false;

	public LocalizedListEditor() {
		super();
	}

	public LocalizedListEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	public Composite getButtonBoxControl(final Composite parent) {
		final Composite buttonBox = super.getButtonBoxControl(parent);
		if (!localized) {
			final Button addButton = getAddButton();
			addButton.setText(Resources.get("lbl.preferences.list.button.add"));

			final Button removeButton = getRemoveButton();
			removeButton.setText(Resources.get("lbl.preferences.list.button.remove"));

			final Button upButton = getUpButton();
			upButton.setText(Resources.get("lbl.preferences.list.button.up"));

			final Button downButton = getDownButton();
			downButton.setText(Resources.get("lbl.preferences.list.button.down"));
			localized = true;
		}
		return buttonBox;
	}

}