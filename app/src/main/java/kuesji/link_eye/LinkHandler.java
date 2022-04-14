package kuesji.link_eye;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LinkHandler extends Activity {

	private EditText urlArea;
	private Button actionCopy,actionOpen,actionShare;
	private LinearLayout contentArea;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setStatusBarColor(Theme.background_primary);
		getWindow().setNavigationBarColor(Theme.background_primary);

		setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name),R.drawable.ic_link_eye,getColor(R.color.background_primary)));


		setContentView(R.layout.link_handler);
		urlArea = findViewById(R.id.link_handler_url);
		actionCopy = findViewById(R.id.link_handler_action_copy);
		actionCopy.setOnClickListener((v)->{
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setPrimaryClip(ClipData.newPlainText("link eye", urlArea.getText().toString()));
			Toast.makeText(this,getString(R.string.link_handler_copied_to_clipboard), Toast.LENGTH_SHORT).show();
		});
		actionOpen = findViewById(R.id.link_handler_action_open);
		actionOpen.setOnClickListener((v)->{
			actionOpen.setBackgroundColor(getColor(R.color.background_secondary));
			actionShare.setBackgroundColor(getColor(R.color.background_seconday_not_selected));

			listHandlers("open");
		});
		actionShare = findViewById(R.id.link_handler_action_share);
		actionShare.setOnClickListener((v)->{
			actionShare.setBackgroundColor(getColor(R.color.background_secondary));
			actionOpen.setBackgroundColor(getColor(R.color.background_seconday_not_selected));

			listHandlers("share");
		});
		contentArea = findViewById(R.id.link_handler_content);

		Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_VIEW)){
			urlArea.setText(intent.getData().toString());
		} else if (intent.getAction().equals(Intent.ACTION_SEND)){
			String text = "";
			if(intent.hasExtra(Intent.EXTRA_SUBJECT)){
				text += intent.getStringExtra(Intent.EXTRA_SUBJECT) +"\n";
			}
			if(intent.hasExtra(Intent.EXTRA_TEXT)){
				text += intent.getStringExtra(Intent.EXTRA_TEXT);
			}
			urlArea.setText(text);
		}

		actionOpen.performClick();

		HistoryHelper historyHelper = new HistoryHelper(this);
		historyHelper.insert(urlArea.getText().toString());
		historyHelper.close();
	}

	private void listHandlers(String target) {
		PackageManager pm = getPackageManager();

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (target.equals("open")) {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(urlArea.getText().toString()));
		} else if (target.equals("share")) {
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, urlArea.getText().toString());
		}

		List<ResolveInfo> resolves = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL | PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS);

		final Collator collator = Collator.getInstance(Locale.getDefault());
		Collections.sort(resolves, (o1, o2) -> {
			return collator.compare(
			 o1.activityInfo.loadLabel(pm).toString().toLowerCase(Locale.getDefault()),
			 o2.activityInfo.loadLabel(pm).toString().toLowerCase(Locale.getDefault())
			);
		});

		contentArea.removeAllViews();
		for (ResolveInfo resolve_info : resolves) {
			if (resolve_info.activityInfo.packageName.equals(getPackageName())) {
				continue;
			}

			HandlerEntry entry = new HandlerEntry(this)
			 .setIcon(resolve_info.loadIcon(pm))
			 .setLabel(resolve_info.loadLabel(pm).toString())
			 .setComponent(ComponentName.createRelative(resolve_info.activityInfo.packageName, resolve_info.activityInfo.name).flattenToShortString())
			 .setIntent(intent);


			contentArea.addView(entry);
		}
	}

	class HandlerEntry extends LinearLayout {

		private String component;
		private Intent intent;
		private ImageView icon;
		private TextView label;

		public HandlerEntry(Context context) {
			super(context);

			setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 112));
			setOrientation(LinearLayout.HORIZONTAL);
			setOnClickListener((v) -> {
				if (intent != null && component != null) {
					intent.setComponent(ComponentName.unflattenFromString(component));
					try {
						startActivity(intent);
						finishAndRemoveTask();
					} catch (Exception e) {
						Toast.makeText(getContext(), getString(R.string.link_handler_error_launch_failed), Toast.LENGTH_LONG).show();
					}
				}
			});

			icon = new ImageView(context);
			icon.setLayoutParams(new LayoutParams(144, 112));
			icon.setPadding(16, 16, 16, 16);
			icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			addView(icon);

			label = new TextView(context);
			label.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			label.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
			label.setText("");
			label.setTypeface(Typeface.MONOSPACE);
			label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			label.setTextColor(getColor(R.color.foreground_primary));
			addView(label);
		}

		public HandlerEntry setIcon(Drawable icon) {
			this.icon.setImageDrawable(icon);
			return HandlerEntry.this;
		}

		public HandlerEntry setLabel(String label) {
			this.label.setText(label);
			return HandlerEntry.this;
		}

		public HandlerEntry setComponent(String component) {
			this.component = component;
			return HandlerEntry.this;
		}

		public HandlerEntry setIntent(Intent intent) {
			this.intent = intent;
			return HandlerEntry.this;
		}
	}
}
