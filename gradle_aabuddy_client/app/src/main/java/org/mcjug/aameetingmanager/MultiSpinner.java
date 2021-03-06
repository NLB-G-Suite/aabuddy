package org.mcjug.aameetingmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

public class MultiSpinner extends Spinner implements OnMultiChoiceClickListener, OnCancelListener {

    private List<String> items;
    private boolean[] selected;

    private String allSelectedText;
    private String noneSelectedText;
    private MultiSpinnerListener listener;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MultiSpinner(Context context, AttributeSet attributeSet, int mode) {
        super(context, attributeSet, mode);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked)
            selected[which] = true;
        else
            selected[which] = false;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean someUnselected = false;
        for (int i = 0; i < items.size(); i++) {
            if (selected[i] == true) {
                setItemSpinnerText(spinnerBuffer, i);
                spinnerBuffer.append(", ");
            } else {
                someUnselected = true;
            }
        }

        String spinnerText;
        if (someUnselected || allSelectedText == null) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() == 0) {
                spinnerText = noneSelectedText;
            } else if (spinnerText.length() > 2) {
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
            }
        } else {
            spinnerText = allSelectedText;
        }

        if (spinnerText.equals(allSelectedText)) {
            for (int i = 0; i < items.size(); i++) {
                selected[i] = true;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{spinnerText});
        setAdapter(adapter);
        listener.onItemsSelected(selected);
    }

    protected void setItemSpinnerText(StringBuffer spinnerBuffer, int itemIdx) {
        spinnerBuffer.append(items.get(itemIdx));
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(items.toArray(new CharSequence[items.size()]), selected, this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setOnCancelListener(this);
        builder.show();
        return true;
    }

    /**
     * If you pass in a selected item text thats in the items list, it will get selected.
     * Otherwise all items will be selected and the selectedItemText will be displayed for the spinner.
     *
     * @param items            - the items to put in.
     * @param selectedItemText - the text of the selected item.
     * @param allSelectedText  - the text to show when all items are selected
     * @param listener         - the listener.
     */
    public void setItems(List<String> items, String selectedItemText, String noneSelectedText, String allSelectedText, MultiSpinnerListener listener) {
        this.items = items;
        this.noneSelectedText = noneSelectedText;
        this.allSelectedText = allSelectedText;
        this.listener = listener;

        // all selected by default
        selected = new boolean[items.size()];
        if (items.contains(selectedItemText)) {
            selected[items.indexOf(selectedItemText)] = true;
        } else if (allSelectedText != null && selectedItemText.equals(allSelectedText)) {
            for (int i = 0; i < selected.length; i++)
                selected[i] = true;
        }

        // all text on the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{selectedItemText});
        setAdapter(adapter);
    }

    public void setItems(List<String> items, String selectedItems, boolean selectedFlags[], String noneSelectedText, String allSelectedText, MultiSpinnerListener listener) {
        this.items = items;
        this.noneSelectedText = noneSelectedText;
        this.allSelectedText = allSelectedText;
        this.listener = listener;
        this.selected = selectedFlags;

        // all text on the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{selectedItems});
        setAdapter(adapter);
    }

    public void setItems(List<String> items, boolean preSelected[], MultiSpinnerListener listener) {
        this.items = items;
        this.noneSelectedText = null;
        this.allSelectedText = null;
        this.listener = listener;
        selected = preSelected;

        // all text on the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{""});
        setAdapter(adapter);
    }

    public String getAllSelectedText() {
        return allSelectedText;
    }

    public String getNoneSelectedText() {
        return noneSelectedText;
    }

    public int getNumSelected() {
        int numSelected = 0;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                numSelected++;
            }
        }
        return numSelected;
    }

    public boolean[] getSelected() {
        return selected;
    }

    public List<String> getItems() {
        return items;
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }
}
