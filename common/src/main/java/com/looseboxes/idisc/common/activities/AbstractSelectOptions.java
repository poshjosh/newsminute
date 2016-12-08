package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.handlers.ButtonGroupHandler;
import com.looseboxes.idisc.common.util.Pref;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractSelectOptions extends AbstractSingleTopActivity {

    public static final String CATEGORY_ALL = "All";

    private ButtonGroupHandler<CheckBox> buttonGroupHandler;

    private class AllButtonListener implements OnClickListener {
        public void onClick(View v) {
            boolean check = ((CompoundButton) v).isChecked();
            List<CheckBox> ccs = buttonGroupHandler.getButtons();
            if (ccs != null) {
                for (CheckBox cb : ccs) {
                    cb.setChecked(check);
                }
            }
        }
    }

    private class CategoryButtonListener implements OnClickListener {
        public void onClick(View v) {
            if (!((CompoundButton) v).isChecked()) {
                (buttonGroupHandler.getButton(AbstractSelectOptions.CATEGORY_ALL)).setChecked(false);
            }
        }
    }

    private class OkButtonListener implements OnClickListener {
        public void onClick(View v) {
            Set selectedCategories = new HashSet();
            CheckBox all = buttonGroupHandler.getButton(AbstractSelectOptions.CATEGORY_ALL);
            if (all == null || !all.isChecked()) {
                List<CheckBox> ccs = buttonGroupHandler.getButtons();
                if (ccs != null) {
                    for (CheckBox cb : ccs) {
                        if (!cb.getText().equals(AbstractSelectOptions.CATEGORY_ALL) && cb.isChecked()) {
                            selectedCategories.add(cb.getText().toString());
                        }
                    }
                }
            } else {
                selectedCategories.addAll(AbstractSelectOptions.this.getOptions());
            }
            Pref.setStringSet(AbstractSelectOptions.this.getApplicationContext(), AbstractSelectOptions.this.getPreferenceKeyForStoredUserSelection(), selectedCategories, true);
            AbstractSelectOptions.this.onSelection(v, selectedCategories);
        }
    }

    private class CategoriesCheckBoxHandler extends ButtonGroupHandler<CheckBox> {

        private final List<String> buttonTexts;

        public CategoriesCheckBoxHandler() {
            Set<Object> set = AbstractSelectOptions.this.getOptions();
            this.buttonTexts = new ArrayList(set.size() + 1);
            this.buttonTexts.add(AbstractSelectOptions.CATEGORY_ALL);
            for (Object o : set) {
                this.buttonTexts.add(o.toString());
            }
        }

        public int getViewGroupId() {
            return R.id.selectcategories_categories;
        }

        @Override
        public CheckBox createNew() {
            return new CheckBox(this.getActivity());
        }

        public OnClickListener getOnClickListener(CheckBox button) {
            if (AbstractSelectOptions.CATEGORY_ALL.equals(button.getText())) {
                return new AllButtonListener();
            }
            return new CategoryButtonListener();
        }

        public Activity getActivity() {
            return AbstractSelectOptions.this;
        }

        public List getButtonTexts() {
            return this.buttonTexts;
        }
    }

    public abstract Set getOptions();

    public abstract String getPreferenceKeyForStoredUserSelection();

    public abstract void onSelection(View view, Set<String> set);

    public int getContentViewId() {
        return R.layout.selectcategories;
    }

    protected void doCreate(Bundle icicle) {

        super.doCreate(icicle);

        this.buttonGroupHandler = new CategoriesCheckBoxHandler();

        (findViewById(R.id.selectcategories_ok)).setOnClickListener(new OkButtonListener());

        List<CheckBox> btns = this.buttonGroupHandler.initButtons();

        Set<String> storedUserSelectedCategories = Pref.getStringSet(getApplicationContext(), getPreferenceKeyForStoredUserSelection(), null);

        if (storedUserSelectedCategories != null && !storedUserSelectedCategories.isEmpty()) {

            for (CheckBox btn : btns) {

                btn.setChecked(storedUserSelectedCategories.contains(btn.getText()));
            }
        }
    }
}
