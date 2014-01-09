package app.create.rpg;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentFileList extends ListFragment {
	
	protected String mDirectoryPath, mFilters[];
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		Bundle args = getArguments();
		if ((mDirectoryPath = args.getString("path")) == null)
			mDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFilters = args.getStringArray("filters");
	}
	
	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);
		if (state != null)
			mDirectoryPath = state.getString("path");
		refreshDir (mDirectoryPath);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("path", mDirectoryPath);
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

			// setTitle(mDirectoryPath = newDir);

			final ListView listView = getListView();
			FileListAdapter adapter = (FileListAdapter) listView.getAdapter();
			if (adapter == null) {
				List<File> totalList = new ArrayList<File>(folderList);
				totalList.addAll(fileList);
				listView.setAdapter(new FileListAdapter(getActivity(), totalList));
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
			Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
		}
	}

}
