package com.drdisagree.colorblendr.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.drdisagree.colorblendr.R;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class RadioDialog extends Dialog {

    private final Context context;
    private int selectedIndex;
    private final int dialogId;
    private WeakReference<RadioDialogListener> listenerReference;

    public RadioDialog(Context context, int dialogId, int selectedIndex) {
        super(context);
        this.context = context;
        this.dialogId = dialogId;
        this.selectedIndex = selectedIndex;

        initializeDialog();
    }

    private void initializeDialog() {
        setContentView(R.layout.view_radio_dialog);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    public void setRadioDialogListener(RadioDialogListener listener) {
        this.listenerReference = new WeakReference<>(listener);
    }

    public void show(int title, int items, TextView output, boolean showSelectedPrefix) {
        if (context instanceof android.app.Activity && !((android.app.Activity) context).isFinishing()) {
            if (listenerReference != null) {
                RadioDialogListener listener = listenerReference.get();
                if (listener != null) {
                    if (isShowing()) {
                        dismiss();
                    }

                    String[] options = context.getResources().getStringArray(items);

                    setContentView(R.layout.view_radio_dialog);

                    TextView text = findViewById(R.id.title);
                    text.setText(context.getResources().getText(title));

                    RadioGroup radioGroup = findViewById(R.id.radio_group);

                    for (int i = 0; i < options.length; i++) {
                        LayoutInflater inflater = LayoutInflater.from(context);
                        RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.view_radio_button, radioGroup, false);

                        radioButton.setText(options[i]);
                        radioButton.setId(i);
                        radioGroup.addView(radioButton);
                    }

                    ((RadioButton) radioGroup.getChildAt(selectedIndex)).setChecked(true);

                    radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        selectedIndex = checkedId;
                        dismiss();
                        output.setText(
                                showSelectedPrefix ?
                                        context.getString(
                                                R.string.opt_selected1,
                                                ((RadioButton) radioGroup.getChildAt(checkedId)).getText()
                                        ) :
                                        ((RadioButton) radioGroup.getChildAt(checkedId)).getText()
                        );

                        listener.onItemSelected(dialogId, selectedIndex);
                    });

                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.copyFrom(Objects.requireNonNull(getWindow()).getAttributes());
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    getWindow().setAttributes(layoutParams);

                    show();
                }
            }
        }
    }

    public void hide() {
        if (isShowing()) {
            dismiss();
        }
    }

    public void dismiss() {
        super.dismiss();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public interface RadioDialogListener {
        void onItemSelected(int dialogId, int selectedIndex);
    }
}
