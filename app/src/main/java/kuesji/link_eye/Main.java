package kuesji.link_eye;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

public class Main extends Activity {

	private LinearLayout contentArea;
	private Button tabStatusButton, tabHistoryButton, tabAboutButton;

	private View tabStatus;
	private Button tabStatusChange, tabStatusTest;
	private TextView tabStatusStatus;

	private View tabHistory;
	private EditText tabHistorySearch;
	private Button tabHistoryDeleteAll;
	private LinearLayout tabHistoryContent;
	private HistoryHelper historyHelper;

	private View tabAbout;

	private class HistoryEntry extends TextView {
		public int historyId = 0;
		public long historyEpoch = 0;

		public HistoryEntry(Context context) {
			super(context);
			setTypeface(Typeface.MONOSPACE);
		}
	}

	private View.OnClickListener tabButtonClick = (v) -> {
		Button button = (Button) v;
		tabStatusButton.setBackgroundColor(getColor(R.color.background_seconday_not_selected));
		tabHistoryButton.setBackgroundColor(getColor(R.color.background_seconday_not_selected));
		tabAboutButton.setBackgroundColor(getColor(R.color.background_seconday_not_selected));
		button.setBackgroundColor(getColor(R.color.background_secondary));

		contentArea.removeAllViews();
		if (button == tabStatusButton) {
			contentArea.addView(tabStatus);
		} else if (button == tabHistoryButton) {
			contentArea.addView(tabHistory);
		} else if (button == tabAboutButton) {
			contentArea.addView(tabAbout);
		}

	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setStatusBarColor(getColor(R.color.background_primary));
		getWindow().setNavigationBarColor(getColor(R.color.background_primary));

		setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), R.drawable.ic_link_eye, getColor(R.color.background_primary)));

		setContentView(R.layout.main);
		contentArea = findViewById(R.id.main_content);
		tabStatusButton = findViewById(R.id.main_tab_status);
		tabStatusButton.setOnClickListener(tabButtonClick);
		tabHistoryButton = findViewById(R.id.main_tab_history);
		tabHistoryButton.setOnClickListener(tabButtonClick);
		tabAboutButton = findViewById(R.id.main_tab_about);
		tabAboutButton.setOnClickListener(tabButtonClick);

		setup_tab_status();
		setup_tab_history();
		setup_tab_about();
	}

	private void setup_tab_status() {
		tabStatus = getLayoutInflater().inflate(R.layout.main_status, null);
		tabStatus.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

			public void onViewAttachedToWindow(View v) {
				Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://kuesji.koesnu.com"));
				ResolveInfo resolveInfo = getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);

				if (resolveInfo.activityInfo.packageName != null && resolveInfo.activityInfo.packageName.equals(getPackageName())) {
					tabStatusStatus.setText(getString(R.string.main_status_enabled));
				} else {
					tabStatusStatus.setText(getString(R.string.main_status_disabled));
				}
			}

			public void onViewDetachedFromWindow(View v) {
			}
		});
		tabStatusChange = tabStatus.findViewById(R.id.main_status_change);
		tabStatusChange.setOnClickListener((vx) -> {
			Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
			try {
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(this, getString(R.string.main_status_error_launch_settings), Toast.LENGTH_LONG).show();
			}
		});
		tabStatusTest = tabStatus.findViewById(R.id.main_status_test);
		tabStatusTest.setOnClickListener((vx) -> {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(getString(R.string.main_status_test_url)));
			try {
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(this, getString(R.string.main_status_error_launch_test), Toast.LENGTH_LONG).show();
			}
		});
		tabStatusStatus = tabStatus.findViewById(R.id.main_status_status);
	}

	private void setup_tab_history() {
		tabHistory = getLayoutInflater().inflate(R.layout.main_history, null);
		tabHistory.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		tabHistory.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
			public void onViewAttachedToWindow(View v) {
				historyHelper = new HistoryHelper(Main.this);
				listHistoryEntries(null);
			}

			public void onViewDetachedFromWindow(View v) {
				historyHelper.close();
			}
		});
		tabHistorySearch = tabHistory.findViewById(R.id.main_history_search);
		tabHistorySearch.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				listHistoryEntries(tabHistorySearch.getText().toString());
			}

			public void afterTextChanged(Editable s) {
			}
		});
		tabHistoryDeleteAll = tabHistory.findViewById(R.id.main_history_delete_all);
		tabHistoryDeleteAll.setOnClickListener((v) -> {
			new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom))
			 .setCancelable(true)
			 .setTitle(getString(R.string.main_history_header_delete_all_title))
			 .setMessage(getString(R.string.main_history_header_delete_all_description))
			 .setNegativeButton(getString(R.string.main_history_header_delete_all_no), (dlg, which) -> {
				 dlg.dismiss();
			 })
			 .setPositiveButton(getString(R.string.main_history_header_delete_all_yes), (dlg, which) -> {
				 historyHelper.clear();
				 listHistoryEntries(null);
				 dlg.dismiss();
			 }).show();
		});
		tabHistoryContent = tabHistory.findViewById(R.id.main_history_content);
	}

	private void setup_tab_about(){
		tabAbout = getLayoutInflater().inflate(R.layout.main_about,null);

	}

	private HistoryEntry generateHistoryEntry() {
		HistoryEntry entry = new HistoryEntry(this);
		entry.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		entry.setPadding(16, 16, 16, 16);
		entry.setBackgroundColor(getColor(R.color.background_seconday_not_selected));
		entry.setTextColor(getColor(R.color.foreground_primary));
		entry.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		entry.setText("https://kuesji.koesnu.com");

		entry.setOnClickListener((v) -> {
			new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom))
			 .setTitle(getString(R.string.main_history_url_clicked_title))
			 .setMessage(entry.getText().toString())
			 .setPositiveButton(getString(R.string.main_history_url_clicked_open), (dlg, which) -> {
				 Intent intent = new Intent(Intent.ACTION_SEND);
				 intent.putExtra(Intent.EXTRA_TEXT, entry.getText().toString());
				 intent.setComponent(ComponentName.createRelative(getPackageName(), LinkHandler.class.getName()));
				 startActivity(intent);
			 })
			 .setNegativeButton(getString(R.string.main_history_url_clicked_delete), (dlg, which) -> {
				 historyHelper.delete(entry.historyId);
				 tabHistoryContent.removeView(entry);
			 })
			 .setNeutralButton(getString(R.string.main_history_url_clicked_cancel), (dlg, which) -> {
				 /* ¯\_(ツ)_/¯ */
			 }).show();
		});

		return entry;
	}

	private void listHistoryEntries(String query) {
		List<HistoryHelper.HistoryModel> historyEntries;
		if (query == null) {
			historyEntries = historyHelper.list();
		} else {
			historyEntries = historyHelper.search(query);
		}

		tabHistoryContent.removeAllViews();
		if (historyEntries.size() < 0) {

		} else {
			for (HistoryHelper.HistoryModel model : historyEntries) {
				HistoryEntry view = generateHistoryEntry();
				view.setText(model.content);
				view.historyId = model.id;
				view.historyEpoch = model.epoch;
				tabHistoryContent.addView(view);
				View divider = new View(this);
				divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
				tabHistoryContent.addView(divider);
			}
		}
	}

	protected void onStart() {
		super.onStart();

		if (contentArea.getChildCount() < 1) {
			tabStatusButton.performClick();
		}

		if (tabStatus.getParent() != null) {
			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://kuesji.koesnu.com"));
			ResolveInfo resolveInfo = getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);

			if (resolveInfo.activityInfo.packageName != null && resolveInfo.activityInfo.packageName.equals(getPackageName())) {
				tabStatusStatus.setText(getString(R.string.main_status_enabled));
			} else {
				tabStatusStatus.setText(getString(R.string.main_status_disabled));
			}
		}

		if (tabHistory.getParent() != null) {
			if( tabHistorySearch.getText().toString().length() < 1 ){
				listHistoryEntries(null);
			}
		}

	}
}
