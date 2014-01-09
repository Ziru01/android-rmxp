package app.create.rpg;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import app.create.rpg.task.*;

import java.io.*;
import java.util.*;

public class ActivityNewProject extends Activity implements OnClickListener, TextWatcher {

	public static final String invalidChars = "/\\:*?\"<>|.%";
	public static final String[] invalidFileNames = new String[] {"CON", "PRN", "AUX", "CLOCK$", "NUL"};
	public static final boolean DEBUG = false;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.dialog_project);
		getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		final EditText et = ((EditText) findViewById(R.id.editGameTitle));
		et.addTextChangedListener(this);
		findViewById(android.R.id.button1).setOnClickListener(this);
		findViewById(android.R.id.button2).setOnClickListener(this);
		findViewById(R.id.btnChooseDir).setOnClickListener(this);
		
		setHint(et.getText().toString());
	}
	
	@Override
	public void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		setHint (((EditText) findViewById(R.id.editGameTitle)).getText().toString());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			try {
				if (Class.forName(intent.getComponent().getClassName()).equals(ActivityFileDialog.class)) {
					((EditText) findViewById(R.id.editDirPath)).setText(intent.getStringExtra(ActivityFileDialog.FILE_PATH));
				}
			} catch (ClassNotFoundException e) {}
		}
	}

	@Override
	public void onClick(View view) {
		String dirPath, projName;
		switch (view.getId()) {
			case R.id.btnChooseDir:
				projName = ((EditText) findViewById(R.id.editGameTitle)).getText().toString();
				dirPath = ((EditText) findViewById(R.id.editDirPath)).getText().toString();
				if (dirPath.equals("")) dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				startActivityForResult(new Intent(this, ActivityFileDialog.class)
									   .putExtra(ActivityFileDialog.FILE_PATH, dirPath)
									   .putExtra(ActivityFileDialog.SELECTION_MODE_FLAGS, SelectionFlags.FLAG_DIR), 0);
				break;
			case android.R.id.button1: finish(); break;
			case android.R.id.button2: {
					long time = 0;
					if (DEBUG) {
						Log.d("CreateRPG", "Starting...");
						time = System.currentTimeMillis();
					}
					String rtp1 = ((EditText) findViewById(R.id.editRTP1)).getText().toString(),
						rtp2 = ((EditText) findViewById(R.id.editRTP2)).getText().toString(),
						rtp3 = ((EditText) findViewById(R.id.editRTP3)).getText().toString();
					projName = ((EditText) findViewById(R.id.editGameTitle)).getText().toString();
					dirPath = ((EditText) findViewById(R.id.editDirPath)).getText().toString();
					if (dirPath.length() < 1) dirPath = getProjectDir(projName).getAbsolutePath();
					Task[] tasks = new Task[] {
							new TaskProject(true, projName, new String[]{rtp1, rtp2, rtp3}, dirPath),
							new TaskIntent(PendingIntent.getActivity(this, 0, new Intent(this, ActivityProject.class).putExtra(ActivityFileDialog.FILE_PATH, dirPath), 0))
						};
					if (DEBUG) {
						Log.d("CreateRPG", "Finished init. Took " + (System.currentTimeMillis() - time) / 1000.0f + " sec");
						Log.d("CreateRPG", "Start service");
						time = System.currentTimeMillis();
					}
					startService(new Intent(ServiceFileTask.ACTION_PUSH_TASK).putExtra("queueid", -1).putExtra("tasks", tasks));
					if (DEBUG) {
						Log.d("CreateRPG", "Finished start service. Took " + (System.currentTimeMillis() - time) / 1000.0f + " sec");
						Log.d("CreateRPG", "Finishing Activity..");
					}
					finish();
					if (DEBUG)
						Log.d("CreateRPG", "Finished activity. Took " + (System.currentTimeMillis() - time) / 1000.0f + " sec");
				}
		}
	}

	/**
	 * Check DOS devices plus invalid chars and convert it to be valid.
	 * @param name
	 * @return
	 */
	public File getProjectDir(String name) {
		StringBuilder sb = new StringBuilder();
		String upcase = name.toUpperCase(Locale.ENGLISH);
		char c; int i;
		if (upcase.length() == 4 && (upcase.startsWith("COM") || upcase.startsWith("LPT"))) {
			c = name.charAt(3);
			if ('0' <= c && c <= '9')
				sb.append('p');
		} else {
			for (i = 0; i < invalidFileNames.length; ++i) {
				if (invalidFileNames[i].equals(upcase)) {
					sb.append('p');
					break;
				}
			}
		}
		for (i = 0; i < name.length(); ++i) {
			c = name.charAt(i);
			sb.append((invalidChars.indexOf(c) == -1) ? c : '_');
		}
		return new File(Environment.getExternalStorageDirectory(), sb.toString());
	}
	
	public void setHint(String s) {
		((EditText) findViewById(R.id.editDirPath)).setHint(getProjectDir(s).getAbsolutePath());
	}

	@Override
	public void afterTextChanged(Editable s) {
		setHint(s.toString());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) { }

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }
}
