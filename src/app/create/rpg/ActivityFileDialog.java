package app.create.rpg;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.view.*;
import android.widget.*;
import android.text.*;

public class ActivityFileDialog extends ListActivity implements
OnClickListener, OnItemClickListener, OnItemLongClickListener {

	/** Item key for SimpleAdapter */
	public static final String ITEM_KEY = "key";

	/** Item image for SimpleAdatper */
	public static final String ITEM_IMAGE = "image";

	/** Item key/image pair for SimpleAdapter */
	public static final String[] ITEMS = new String[] { ITEM_KEY, ITEM_IMAGE };

	/** Matching view Id for SimpleAdapter */
	public static final int[] ITEM_IDS = new int[] { R.id.row_text,
		R.id.row_image };

	/** Current directory path / result path intent key */
	public static final String FILE_PATH = "FILE_PATH";

	/** File name filter intent parameter key */
	public static final String FILENAME_FILTER = "FILENAME_FILTER";

	/** Selection mode flags */
	public static final String SELECTION_MODE_FLAGS = "SELECTION_MODE_FLAGS";

	private int mFlags;
	private String mDirectoryPath;
	private String[] mFilters;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		//setMarquee();
		setContentView(R.layout.dialog_file);
		setMarquee();

		findViewById(android.R.id.button1).setOnClickListener(this);
		findViewById(android.R.id.button2).setOnClickListener(this);
		findViewById(R.id.btn_new_dir).setOnClickListener(this);

		final ListView lv = getListView();
		lv.setOnItemClickListener(this);
		lv.setLongClickable(true);
		lv.setOnItemLongClickListener(this);

		Intent in = getIntent();
		mFlags = in.getIntExtra(SELECTION_MODE_FLAGS, SelectionFlags.FLAG_ONLY_AVAILABLE);
		if ((mDirectoryPath = in.getStringExtra(FILE_PATH)) == null)
			mDirectoryPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File f = new File(mDirectoryPath);
		if (!f.isAbsolute()) {
			f = new File(Environment.getExternalStorageDirectory(), f.getPath());
		}
		if (!f.isDirectory() && f.getParentFile().isDirectory()) {
			mDirectoryPath = f.getParent();
			((EditText) findViewById(R.id.textFilePath)).setText(f.getName());
		}
		mFilters = in.getStringArrayExtra(FILENAME_FILTER);

		restoreState(state);
		refreshDir(mDirectoryPath);
	}
	
	protected void setMarquee () {
		TextView tv = (TextView) findViewById(android.R.id.title);
		if (tv != null) {
			tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			tv.setMarqueeRepeatLimit(-1);
			tv.setSingleLine(true);
			tv.setHorizontallyScrolling(true);
			tv.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
			//* Seems like the marquee needs focus...
			tv.setFocusable(true);
			tv.setFocusableInTouchMode(true);
			//*/
			/* Test code
			 Toast.makeText(this, "lol", Toast.LENGTH_SHORT).show();
			 //*/
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// setMarquee ();
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		restoreState(state);
	}

	protected void restoreState(Bundle state) {
		if (state != null) {
			mFlags = state.getInt(SELECTION_MODE_FLAGS);
			mDirectoryPath = state.getString(FILE_PATH);
			mFilters = state.getStringArray(FILENAME_FILTER);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTION_MODE_FLAGS, mFlags);
		outState.putString(FILE_PATH, mDirectoryPath);
		outState.putStringArray(FILENAME_FILTER, mFilters);
	}

	protected void addItem(List<Map<String, Object>> map, File file, int icon) {
		HashMap<String, Object> item = new HashMap<String, Object>(2);
		item.put(ITEM_KEY, file.getName());
		item.put(ITEM_IMAGE, icon);
		map.add(item);
	}

	protected void refreshDir(String newDir) {
		File folder = new File(newDir);
		if (!folder.isDirectory())
			return;

		try {
			File[] files = folder.listFiles(new FileFilter() {

					@Override
					public boolean accept(File file) {
						if (file.isDirectory() || mFilters == null)
							return true;
						for (String filter : mFilters) {
							if (file.getName().endsWith("."
														+ filter.toLowerCase(Locale.ENGLISH))) {
								return true;
							}
						}
						return false;
					}
				});
			List<File> fileList = new ArrayList<File>(), folderList = new ArrayList<File>();

			for (File file : files) {
				if (file.isDirectory()) {
					folderList.add(file);
				} else {
					fileList.add(file);
				}
			}
			Collections.sort(fileList);
			Collections.sort(folderList);

			//setMarquee();
			setTitle(mDirectoryPath = newDir);

			final ListView listView = getListView();
			FileListAdapter adapter = (FileListAdapter) listView.getAdapter();
			if (adapter == null) {
				List<File> totalList = new ArrayList<File>(folderList);
				totalList.addAll(fileList);
				listView.setAdapter(new FileListAdapter(this, totalList));
			} else {
				adapter.setNotifyOnChange(false);
				adapter.clear();
				for (File f : folderList)
					adapter.add(f);
				for (File f : fileList)
					adapter.add(f);
				adapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
							int position, long id) {
		File file = (File) ((Adapter) adapterView.getAdapter()).getItem(position);
		if (file.isDirectory()) {
			refreshDir(file.getAbsolutePath());
		} else if ((mFlags & SelectionFlags.FLAG_DIR) != SelectionFlags.FLAG_DIR) {
			if (file.isFile()) {
				setResult(RESULT_OK,
						  getIntent().putExtra(FILE_PATH, file.getAbsolutePath()));
				finish();
			}
		}
	}

	protected void deleteRecursive(File file) {
	    if (file.isDirectory())
	        for (File child : file.listFiles())
	            deleteRecursive(child);
	    file.delete();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view,
								   int position, long id) {
		final File file = (File) ((Adapter) adapterView.getAdapter()).getItem(position);
		CharSequence[] items =
			file.isDirectory() == ((mFlags & SelectionFlags.FLAG_DIR) == SelectionFlags.FLAG_DIR)
			? new CharSequence[]{getText(R.string.str_delete), getText(R.string.str_rename), getText(R.string.str_select)}
			: new CharSequence[]{getText(R.string.str_delete), getText(R.string.str_rename)};
		new AlertDialog.Builder(this)
			.setTitle(R.string.str_menu)
			.setItems(items,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						try {
							if (!file.delete()) {
								if (file.list().length > 0) {
									new AlertDialog.Builder(ActivityFileDialog.this)
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.str_question_delete)
										.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) { }
										})
										.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												try { deleteRecursive(file); refreshDir(mDirectoryPath); } catch (Exception e) { Toast.makeText(ActivityFileDialog.this, "Error", Toast.LENGTH_SHORT).show(); }
											}
										}).show();
								} else throw new Exception();
							} else refreshDir(mDirectoryPath);
						} catch (Exception e) {
							Toast.makeText(ActivityFileDialog.this, "Error", Toast.LENGTH_SHORT).show();
						}
					} else if (which == 1) {
						final EditText et = ((EditText) findViewById(R.id.textFilePath));
						final String str = et.getText().toString();
						if (str.length() > 0) {
							String path = file.getParent();
							if (file.renameTo(new File((path == null ? "" : path) + "/" + str))) {
								et.setText("");
								refreshDir(mDirectoryPath);
							} else Toast.makeText(ActivityFileDialog.this, "Error", Toast.LENGTH_SHORT);
						} else {
							et.setText(file.getName());
							Toast.makeText(ActivityFileDialog.this, R.string.str_error_empty, Toast.LENGTH_LONG).show();
						}
					} else {
						setResult(RESULT_OK, getIntent().putExtra(FILE_PATH, file.getAbsolutePath()));
						finish();
					}
				}
			}).show();
		return false;
	}

	@Override
	public void onBackPressed() {
		File folder = new File(mDirectoryPath);
		if (folder.getParentFile() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		refreshDir(folder.getParent());
	}

	@Override
	public void onClick(View v) {
		final EditText et = ((EditText) findViewById(R.id.textFilePath));
		final String fileName = et.getText()
			.toString();
		File f;
		switch (v.getId()) {
			case R.id.btn_new_dir:
				if (fileName.length() < 1) break;
				f = new File(mDirectoryPath, fileName);
				f.mkdirs();
				refreshDir(f.getAbsolutePath());
				et.setText("");
				break;
			case android.R.id.button2:
				if (fileName.length() > 0) {
					f = new File(mDirectoryPath, fileName);
					if (((mFlags & SelectionFlags.FLAG_ONLY_AVAILABLE) != SelectionFlags.FLAG_ONLY_AVAILABLE) || f.exists() || (f = new File(mDirectoryPath)).isDirectory()) {
						if (f.isDirectory() == ((mFlags & SelectionFlags.FLAG_DIR) != 0)) {
							setResult(RESULT_OK, getIntent().putExtra(FILE_PATH, f.getAbsolutePath()));
							finish();
						} else if (f.isDirectory()) {
							refreshDir(f.getAbsolutePath());
						}
					} else {
						Toast.makeText(this, R.string.str_error_nonexist, Toast.LENGTH_SHORT).show();
					}
				} else {
					setResult(RESULT_OK, getIntent()
							  .putExtra(FILE_PATH, mDirectoryPath));
				}
				finish();
				break;
			case android.R.id.button1:
				setResult(RESULT_CANCELED);
				finish();
				break;
		}
	}

}
