package app.create.rpg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import app.create.rpg.file.CachedFile;
import app.create.rpg.file.CachedRMData;
import app.create.rpg.file.CachedScriptFile;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.util.Log;

public class ActivityProject extends ActionBarActivity {
	private CustomPagerAdapter mAdapter;
	private String mDirPath;
	public Map<String, CachedFile> mCache = new HashMap<String, CachedFile>();

	@SuppressWarnings("unchecked")
	public <T extends CachedFile> T require(CachedFile.User user, String name) {
		try {
			String key = name.toLowerCase(Locale.US);
			CachedFile f = mCache.get(key);
			if (f == null) {
				File file = new File(mDirPath, "/Data/" + name + ".rxdata");
				if (key.equals("scripts"))
					f = new CachedScriptFile(file, this);
				else
					f = new CachedRMData(file, this, name);
				mCache.put(key, f);
			}
			f.registerUser(user);
			return (T) f;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Collection<CachedFile> col = mCache.values();
		for (CachedFile f : col)
			f.stopWatching();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Collection<CachedFile> col = mCache.values();
		for (CachedFile f : col)
			f.startWatching();
	}

	public CustomPagerAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.project);
		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		mAdapter = new CustomPagerAdapter(this);

		final Intent intent = getIntent();
		mDirPath = intent.getStringExtra(ActivityFileDialog.FILE_PATH);

		restoreState(state);
	}

	@Override
	public void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		restoreState(state);
	}

	protected void restoreState(Bundle state) {
		if (state == null) return;
		mDirPath = state.getString("FILE_PATH");
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putString("FILE_PATH", mDirPath);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.menu_project, menu);
		return true;
	}

	public String getProjectDir() {
		return mDirPath;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// ActionBar bar = getSupportActionBar();
		try {
			switch (item.getItemId()) {
				case R.id.menu_materialbase:
					mAdapter.addPage(new FragmentMaterialBase.MyCreator(), true);
					break;
				case R.id.menu_actors:
					mAdapter.addPage(new FragmentActors.MyCreator(), true);
					break;
				default:
					return super.onOptionsItemSelected(item);
			}
		} catch (Throwable e) {
			Log.e("CreateRPG", "Weird exception in ActivityProject.onOp~~~ed", e);
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT);
		} finally {
			Toast.makeText(this, "yo", Toast.LENGTH_SHORT);
		}
		return true;
	}


}
