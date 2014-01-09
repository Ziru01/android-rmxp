package app.create.rpg.file;

import java.io.File;
import java.io.IOException;

import app.create.rpg.ActivityProject;

public class CachedScriptFile extends CachedFile {
	
	public CachedScriptFile(File file, ActivityProject main) {
		super(file, main, "scripts");
	}

	@Override
	public void load() throws IOException {
		RubyScriptCollection rsc = new RubyScriptCollection(mFile);
		rsc.loadList();
		mData = rsc;
	}

	@Override
	public void save() throws IOException {
		if (mData instanceof RubyScriptCollection)
			((RubyScriptCollection) mData).saveList();
	}
	
}
