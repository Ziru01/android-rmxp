package app.create.rpg.file;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.FileObserver;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;
import app.create.rpg.ActivityProject;
import app.create.rpg.BuildConfig;
import app.create.rpg.R;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import android.os.Parcel;
import java.util.Locale;

public abstract class CachedFile extends FileObserver implements Parcelable {
	protected Object mData;
	protected File mFile;
	protected Set<User> mUsers;
	protected ActivityProject mMain;
	protected String mName;
	protected boolean mHasChanged, mDiscarded;

	public interface User {
		public void onOpen(CachedFile file);
		public void onUpdate(CachedFile file);
		public void onClosed(CachedFile file);
	}
	
	protected void updateSelf () {
		
	}
	
	protected void notifyAllUsers () {
		for (User u : mUsers)
			u.onUpdate(this);
	}
	
	public void notifyChanged () {
		mHasChanged = true;
		notifyAllUsers();
	}
	
	public void onDiscard (boolean byUser) {
		mHasChanged = false;
		if (!byUser) {
			if (mMain == null) mDiscarded = true;
			else Toast.makeText(mMain, "!stub! err data discarded", Toast.LENGTH_SHORT).show();
		}
		notifyAllUsers();
	}
	
	public boolean isModified () {
		return mHasChanged;
	}

	public CachedFile(File file, ActivityProject main, String name) {
		super(file.getAbsolutePath(), MODIFY | DELETE_SELF | MOVE_SELF);
		mData = null;
		mFile = file;
		mUsers = new HashSet<User>();
		mMain = main;
		mName = name;
	}
	
	public CachedFile(Parcel source) {
		this(new File(source.readString()), null, null);
		mName = source.readString();
	}
	
	public void recover (ActivityProject project) {
		mMain = project;
		if (mDiscarded) {
			Toast.makeText (project, "!stub! data discarded 2", Toast.LENGTH_SHORT).show();
			mDiscarded = false;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mFile.getPath());
		dest.writeString(mName);
	}

	public Object getData() { return mData; }
	public File getFile() { return mFile; }
	public Set<User> getUsers() { return mUsers; }
	public ActivityProject getMain() { return mMain; }
	public String getName() { return mName; }

	public void registerUser(User user) {
		if (!mUsers.add(user))
			return;
		startWatching();
		if (mData == null) {
			try {
				load();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(mMain, "Error loading " + mFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		user.onOpen(this);
	}

	public void unregisterUser(User user) {
		mUsers.remove(user);
		if (mUsers.isEmpty()) {
			terminate();
		}
	}

	public void terminate() {
		stopWatching();
		for (User u : mUsers)
			u.onClosed(CachedFile.this);
		mData = null;
		System.gc();
		mMain.mCache.remove(mName);
	}

	public abstract void load() throws IOException;

	public abstract void save() throws IOException;

	@Override
	public void onEvent(final int event, String path) {
		try {
			final AlertDialog.Builder builder = new AlertDialog.Builder(mMain);
			DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {
						if ((event & MODIFY) == MODIFY) {
							try {
								load();
								for (User u : mUsers)
									u.onUpdate(CachedFile.this);
							} catch (IOException e) {
								e.printStackTrace();
								Toast.makeText(mMain, "Error", Toast.LENGTH_SHORT).show();
							}
						} else if ((event & (DELETE_SELF | MOVE_SELF)) != 0) {
							terminate();
						}
					}
				}
			};
			if ((event & MODIFY) == MODIFY) {
				builder.setMessage(R.string.str_notify_edited);
			} else if ((event & (DELETE_SELF | MOVE_SELF)) != 0) {
				builder.setMessage(R.string.str_ask_close_page);
			} else return;
			builder.setPositiveButton(android.R.string.yes, ocl)
				.setNegativeButton(android.R.string.no, ocl);
			mMain.runOnUiThread(new Runnable(){
					public void run() {
						builder.show();
					}
				});
		} catch (Throwable e) {
			Log.e("CreateRPG", "Weird that" + event + " of " + path + " caused", e);
		} finally {
			if (BuildConfig.DEBUG)
				Log.d("CreateRPG", String.format(Locale.US, "On %s at %s, event %u with flag 0x%08X occurred.", mName, mFile.getPath(), event & ALL_EVENTS, event & ~ALL_EVENTS));
		}
	}

}
