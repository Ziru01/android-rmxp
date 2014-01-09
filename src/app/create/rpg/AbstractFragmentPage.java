package app.create.rpg;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import app.create.rpg.file.CachedFile;
import app.create.rpg.file.CachedFile.User;

public abstract class AbstractFragmentPage extends Fragment implements User {
	
	private String mName;
	protected Helper mHelper;
	private boolean mFlag;
	private List<CachedFile> files = new ArrayList<CachedFile> ();
	
	@Override
	public void onOpen(CachedFile file) { }

	@Override
	public void onUpdate(CachedFile file) { }

	@Override
	public void onClosed(CachedFile file) { }

	protected ActivityProject getProject () {
		return (ActivityProject) getActivity();
	}
	
	protected CachedFile require (String name) {
		CachedFile file = getProject().require(this, name);
		files.add(file);
		return file;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		for (CachedFile f : files)
			f.terminate();
	}
	
	@Override
	public void onAttach(Activity act) {
		mHelper = new Helper(act);
		super.onAttach(act);
	}

	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);
		setName ((state != null && state.containsKey("page_name")) ? state.getString("page_name") : (mName == null ? ".." : mName));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mName != null)
			outState.putString("page_name", mName);
	}

	protected void setName (String name) {
		mName = name;
		if (mFlag)
			name += " *";
		ActionBar bar = ((ActionBarActivity) getActivity()).getSupportActionBar();
		for (int i = 0; i < bar.getTabCount(); ++i) {
			ActionBar.Tab tab = bar.getTabAt(i);
			if (((CustomPagerAdapter.FragmentCreator)tab.getTag()).isFromThis(this)) {
				tab.setText(name);
			}
		}
	}
	
	public void flag (boolean bEdit) {
		mFlag = bEdit;
		setName(mName);
	}
	
	public boolean hasChanged () {
		return mFlag;
	}
	
	public String getName () {
		return mName;
	}

}
