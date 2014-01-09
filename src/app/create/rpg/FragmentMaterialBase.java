package app.create.rpg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import app.create.rpg.task.Task;
import app.create.rpg.task.TaskCopyFile;
import app.create.rpg.task.TaskResult;

public class FragmentMaterialBase extends AbstractFragmentPage implements OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener {

	public static class MyCreator extends CustomPagerAdapter.FragmentCreator {
		public MyCreator() { super(); }
		public MyCreator(Parcel source) { super(source); }
		@Override public Fragment newInstance(Context context) { return new FragmentMaterialBase(); }
		@Override public String getTitle(Context context) { return context.getString(R.string.menu_materialbase); }
		@Override public Class<?> getFragmentClass() { return FragmentMaterialBase.class; }
		@Override public boolean equals(Object object) { return object instanceof MyCreator; }
		public static final MyCreator EMPTY = new MyCreator();
		public static final Creator<MyCreator> CREATOR = new Creator<MyCreator>() {
			public MyCreator[] newArray(int size) { return new MyCreator[size]; }
			public MyCreator createFromParcel(Parcel source) { return new MyCreator(source); }
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		setName(getString(R.string.menu_materialbase));
		View view = inflater.inflate(R.layout.page_material, null);
		Spinner spinner = (Spinner) view.findViewById(R.id.spinMaterialTypes);
		spinner.setOnItemSelectedListener(this);
		final ListView listView = (ListView) view.findViewById(R.id.listMaterials);
		listView.setOnItemClickListener(this);
		listView.setLongClickable(true);
		listView.setOnItemLongClickListener(this);
		return view;
	}

	public void refresh() {
		Spinner spinner = (Spinner) getView().findViewById(R.id.spinMaterialTypes);
		onItemSelected(spinner, spinner.getSelectedView(), spinner.getSelectedItemPosition(), spinner.getSelectedItemId());
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position,
							   long id) {
		final ListView listView = (ListView) getView().findViewById(R.id.listMaterials);
		final String cat = ((Adapter) adapterView.getAdapter()).getItem(position).toString();
		final File dirFile = mHelper.dirToProject(cat),
			dirFileRTP = mHelper.dirToRTP(cat);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> item;
		File[] files;
		item = new HashMap<String, Object>();
		item.put("key", getString(R.string.str_import));
		item.put("image", R.drawable.ic_btn_import);
		list.add(item);
		TreeSet<File> names;

		if (dirFileRTP.isDirectory()) {
			names = new TreeSet<File>();
			files = dirFileRTP.listFiles();
			for (File f : files) {
				if (f.isFile()) {
					names.add(f);
				}
			}
			for (File f : names) {
				item = new HashMap<String, Object>();
				String name = f.getName();
				int off = name.lastIndexOf('.');
				if (off == -1) off = name.length();
				item.put("key", name.substring(0, off));
				item.put("image", R.drawable.ic_btn_file);
				item.put("file", f);
				list.add(item);
			}
		}
		if (dirFile.isDirectory()) {
			names = new TreeSet<File>();
			files = dirFile.listFiles();
			for (File f : files) {
				if (f.isFile()) {
					names.add(f);
				}
			}
			for (File f : names) {
				item = new HashMap<String, Object>();
				String name = f.getName();
				int off = name.lastIndexOf('.');
				if (off == -1) off = name.length();
				item.put("key", name.substring(0, off));
				item.put("image", R.drawable.ic_btn_file);
				item.put("file", f);
				list.add(item);
			}
		}
		listView.setAdapter(new SimpleAdapter(getActivity(),
											  list, R.layout.dialog_file_row,
											  new String[]{"image", "key"}, new int[]{R.id.row_image, R.id.row_text}));
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) { }

	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
		File f = (File) ((Map<?, ?>) ((Adapter) p1.getAdapter()).getItem(p3)).get("file");
		if (f == null) {
			startActivityForResult(new Intent(getActivity(), ActivityFileDialog.class)
								   .putExtra(ActivityFileDialog.SELECTION_MODE_FLAGS, SelectionFlags.FLAG_ONLY_AVAILABLE), 9);
			return;
		}
		MimeTypeMap myMime = MimeTypeMap.getSingleton();
		Intent newIntent = new Intent(Intent.ACTION_VIEW);

		String name = f.getName(), mimeType = "*/*";
		int off = name.lastIndexOf('.') + 1;
		if (off > 0) mimeType = myMime.getMimeTypeFromExtension(name.substring(off).toLowerCase(Locale.ENGLISH));
		newIntent.setDataAndType(Uri.fromFile(f), mimeType);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			getActivity().startActivity(newIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getActivity(), "No handler for this type of file.", Toast.LENGTH_SHORT).show();
			newIntent.setType("*/*");
			try { getActivity().startActivity(newIntent);
			} catch (Throwable e1) { }
		}
	}

	public class MyResultReceiver extends ResultReceiver {

		public MyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			refresh();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		File from, to;
		if (resultCode == Activity.RESULT_CANCELED || data == null) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}
		if (requestCode == 9) { // Import
			Spinner spinner = (Spinner) getView().findViewById(R.id.spinMaterialTypes);
			from = new File(data.getStringExtra(ActivityFileDialog.FILE_PATH)).getAbsoluteFile();
			to = new File(new File(((ActivityProject) getActivity()).getProjectDir(), spinner.getSelectedItem().toString()), from.getName());
			to.getParentFile().mkdirs();
		} else if (requestCode == 10) { // Export
			to = new File(data.getStringExtra(ActivityFileDialog.FILE_PATH)).getAbsoluteFile();
			from = new File(data.getStringExtra("myFile"));
		} else { super.onActivityResult(requestCode, resultCode, data); return; }
		if (to.equals(from)) {
			Toast.makeText(getActivity(), "Error! Source and destination paths are equal.", Toast.LENGTH_SHORT).show();
			return;
		}
		Task[] list = new Task[] {
			new TaskCopyFile(from.getAbsolutePath(), to.getAbsolutePath()),
			new TaskResult(new MyResultReceiver(new Handler(getActivity().getMainLooper())))
		};
		Intent svc = new Intent(ServiceFileTask.ACTION_PUSH_TASK);
		if (requestCode == 9)
			svc.putExtra("tasks", list);
		else svc.putExtra("task", list[0]);
		getActivity().startService(svc);
	}

	public boolean onItemLongClick(AdapterView<?> list, View p2, final int pos, long p4) {
		final ListView lv = (ListView) list;
		final Map<?, ?> item = (Map<?, ?>) lv.getItemAtPosition(pos);
		final File file = (File) item.get("file");
		if (file == null) { onItemClick(list, p2, pos, p4); return true; }
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.str_menu)
			.setItems(R.array.material_menu,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface p1, int which) {
					if (which == 0) {
						startActivityForResult(new Intent(getActivity(), ActivityFileDialog.class)
											   .putExtra(ActivityFileDialog.SELECTION_MODE_FLAGS, 0)
											   .putExtra("myFile", file.getAbsolutePath())
											   .putExtra(ActivityFileDialog.FILE_PATH, file.getName()), 10);
					} else {
						file.delete();
						refresh();
					}
				}
			}).show();
		return true;
	}


}
