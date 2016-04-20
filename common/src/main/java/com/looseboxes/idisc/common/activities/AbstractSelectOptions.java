package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
    private ButtonGroupHandler<CheckBox> _bh;

    private class AllButtonListener implements OnClickListener {
        private AllButtonListener() {
        }

        public void onClick(View v) {
            boolean check = ((CompoundButton) v).isChecked();
            List<CheckBox> ccs = AbstractSelectOptions.this.getButtonGroupHandler().getButtons();
            if (ccs != null) {
                for (CheckBox cb : ccs) {
                    cb.setChecked(check);
                }
            }
        }
    }

    private class CategoryButtonListener implements OnClickListener {
        private CategoryButtonListener() {
        }

        public void onClick(View v) {
            if (!((CompoundButton) v).isChecked()) {
                ((CheckBox) AbstractSelectOptions.this.getButtonGroupHandler().getButton(AbstractSelectOptions.CATEGORY_ALL)).setChecked(false);
            }
        }
    }

    private class OkButtonListener implements OnClickListener {
        private OkButtonListener() {
        }

        public void onClick(View v) {
            Set selectedCategories = new HashSet();
            ButtonGroupHandler<CheckBox> cbHandler = AbstractSelectOptions.this.getButtonGroupHandler();
            CheckBox all = (CheckBox) cbHandler.getButton(AbstractSelectOptions.CATEGORY_ALL);
            if (all == null || !all.isChecked()) {
                List<CheckBox> ccs = cbHandler.getButtons();
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
        private List<String> _c_accessViaGetter;

        private CategoriesCheckBoxHandler() {
        }

        public int getViewGroupId() {
            return R.id.selectcategories_categories;
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
            if (this._c_accessViaGetter == null) {
                Set<Object> set = AbstractSelectOptions.this.getOptions();
                this._c_accessViaGetter = new ArrayList(set.size() + 1);
                this._c_accessViaGetter.add(AbstractSelectOptions.CATEGORY_ALL);
                for (Object o : set) {
                    this._c_accessViaGetter.add(o.toString());
                }
            }
            return this._c_accessViaGetter;
        }

        public Class<CheckBox> getButtonClass() {
            return CheckBox.class;
        }
    }

    public abstract Set getOptions();

    public abstract String getPreferenceKeyForStoredUserSelection();

    public abstract void onSelection(View view, Set<String> set);

    public int getContentView() {
        return R.layout.selectcategories;
    }

    protected void doCreate(Bundle icicle) {
        super.doCreate(icicle);
        ((Button) findViewById(R.id.selectcategories_ok)).setOnClickListener(new OkButtonListener());
        List<CheckBox> btns = getButtonGroupHandler().initButtons();
        Set<String> selectedCategories = Pref.getStringSet(getApplicationContext(), getPreferenceKeyForStoredUserSelection(), null);
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            for (CheckBox btn : btns) {
                btn.setChecked(selectedCategories.contains(btn.getText()));
            }
        }
    }

    public ButtonGroupHandler<CheckBox> getButtonGroupHandler() {
        if (this._bh == null) {
            this._bh = new CategoriesCheckBoxHandler();
        }
        return this._bh;
    }
}
