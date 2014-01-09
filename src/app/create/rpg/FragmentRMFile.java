package app.create.rpg;

import app.create.rpg.file.CachedFile;

public class FragmentRMFile extends AbstractFragmentPage {

	protected String mRequiredMain;
	
	public CachedFile requireMain (String name) {
		return require (mRequiredMain = name);
	}

}
