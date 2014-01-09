package app.create.rpg;
import java.io.File;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import app.create.rpg.task.Task;
import app.create.rpg.task.TaskCopyFile;
import app.create.rpg.task.TaskExtractRgssad;
import app.create.rpg.task.TaskIntent;
import app.create.rpg.task.*;


public class ActivityMain extends ListActivity implements OnItemClickListener {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.main);

		getListView().setOnItemClickListener(this);

		final File f = new File(Environment.getExternalStorageDirectory(), "RTP_list/Standard");
		if (!f.isDirectory()) {
			DialogInterface.OnClickListener cl = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {
						startService(new Intent(ServiceFileTask.ACTION_PUSH_TASK)
									 .putExtra("queueid", -1)
									 .putExtra("task", new TaskDecompressZip(f, R.raw.standard)));
					}
				}
			};
			new AlertDialog.Builder(this)
				.setMessage("!stub! extract rtp confirm")
				.setNegativeButton(android.R.string.no, cl)
				.setPositiveButton(android.R.string.yes, cl)
				.show(); 
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			try {
				if (Class.forName(intent.getComponent().getClassName()).equals(ActivityFileDialog.class)) {
					File f;
					String path = intent.getStringExtra(ActivityFileDialog.FILE_PATH);
					if ((f = new File(path, "Game.rgssad")).isFile()) {
					} else if ((f = new File(path, "Game.rgss2a")).isFile()) {
					} else if ((f = new File(path, "Game.rgss3a")).isFile()) {
					} else {
						startActivity(new Intent(this, ActivityProject.class).putExtras(intent));
						return;
					}
					final Task[] tasks = new Task[] {
						new TaskExtractRgssad(f.getAbsolutePath()),
						new TaskCopyFile(f.getAbsolutePath(), f.getAbsolutePath() + ".bak", true),
						new TaskIntent(PendingIntent.getActivity(this, 0, new Intent(this, ActivityProject.class).putExtras(intent), 0))
					};
					DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE)
								startService(new Intent(ServiceFileTask.ACTION_PUSH_TASK)
											 .putExtra("tasks", tasks));
						}
					};
					new AlertDialog.Builder(this)
						.setTitle(R.string.str_ask_extract_rgssad_title)
						.setMessage(R.string.str_ask_extract_rgssad)
						.setNegativeButton(android.R.string.no, listener)
						.setPositiveButton(android.R.string.yes, listener).show();
				}
			} catch (ClassNotFoundException e) {}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View adapterView, int position, long id) {
		switch (position) {
			case 0:
				startActivity(new Intent(this, ActivityNewProject.class));
				break;
			case 1:
				startActivityForResult(new Intent(this, ActivityFileDialog.class)
									   .putExtra(ActivityFileDialog.SELECTION_MODE_FLAGS, SelectionFlags.FLAG_DIR), 0);
				break;
		}
	}

}
