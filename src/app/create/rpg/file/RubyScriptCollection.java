package app.create.rpg.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import android.os.Parcel;
import android.os.Parcelable;

import com.jinoh.ruby.marshal.Marshaler;
import com.jinoh.ruby.marshal.Unmarshaler;

/**
 * Ruby Script Collection (RSC) by jinoh67
 * 
 * @author jinoh67
 *
 */
public class RubyScriptCollection implements Parcelable, Cloneable {
	
	protected List<ScriptEntry> mList;
	protected List<Integer> mIDs; // index is oldest first, value is the right order in mKeys and mDatas
	protected File mFile;
	protected int mNextId;
	
	public static class ScriptEntry implements Parcelable, Cloneable {
		public String name;
		public byte[] scriptData;
		
		public ScriptEntry () { }
		public ScriptEntry (Parcel source) {
			this.name = source.readString();
			this.scriptData = source.createByteArray();
		}
		public ScriptEntry (ScriptEntry source) { this(source.name, source.scriptData); }
		public ScriptEntry (String name) { this.name = name; }
		public ScriptEntry (byte[] data) { this.scriptData = data; }
		public ScriptEntry (String name, byte[] data) { this.name = name; this.scriptData = data; }
		
		public void fillIn (ScriptEntry other) {
			this.name = other.name;
			this.scriptData = other.scriptData;
		}
		
		public ScriptEntry clone () {
			return new ScriptEntry (this);
		}
		
		public String toString () {
			return name;
		}
		
		public synchronized String inflate () {
			if (scriptData == null) return null;
			InputStreamReader reader = new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(scriptData)));
			char[] buf = new char[256];
			StringBuffer sb = new StringBuffer(32);
			int read;
			try {
				while ((read = reader.read(buf)) > 0) {
					sb.append(buf, 0, read);
				}
				return sb.toString();
			} catch (IOException e) {
			} finally {
				try {
					System.gc();
					reader.close();
				} catch (IOException e) { }
			}
			return null;
		}
		
		public synchronized void deflate (String script) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(new DeflaterOutputStream(baos));
			pw.write(script);
			pw.close();
			this.scriptData = baos.toByteArray();
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(name);
			dest.writeByteArray(scriptData);
		}
		
		public static final ScriptEntry EMPTY = new ScriptEntry();
		public static final Creator<ScriptEntry> CREATOR = new Creator<ScriptEntry>() {
			public ScriptEntry createFromParcel(Parcel source) { return new ScriptEntry(source); }
			public ScriptEntry[] newArray(int size) { return new ScriptEntry[size]; }
		};
	}

	public RubyScriptCollection(File file) {
		mIDs = null;
		mList = null;
		mFile = file;
		mNextId = 0;
	}
	
	public RubyScriptCollection(Parcel source, ClassLoader loader) {
		source.readList(mIDs = new ArrayList<Integer>(), loader);
		source.readList(mList = new ArrayList<ScriptEntry>(), loader);
		mFile = new File(source.readString());
		mNextId = source.readInt();
	}
	
	public synchronized List<ScriptEntry> getKeys () {
		return mList;
	}
	
	public synchronized void loadList () throws IOException {
		boolean success = false;
		InputStream is = new FileInputStream(mFile);
		try {
			// Use custom parser
			Unmarshaler um = new Unmarshaler(is);
			int major, minor, sz, t, sz2;
			major = is.read(); minor = is.read(); // Skip 2 bytes
			final String s_eof = "End of stream or array";
			if (major != 4 || minor > 8 || minor < 0)
				throw new IOException (minor == -1 ? s_eof : "Incompatible version");
			if (is.read() != '[') {
				throw new IOException ("Not an array");
			}
			sz = um.readInt();
			mNextId = 0;
			mIDs = new ArrayList<Integer>();
			mList = new ArrayList<ScriptEntry>();
			for (int i = 0; i < sz; i++) {
				t = is.read();
				if (t == -1) throw new IOException (s_eof);
				if (t != '0') {
					if (t != '[')
						throw new IOException ("An item found that is not array");
					sz2 = um.readInt();
					if (sz2 < 3) throw new IOException (s_eof);
					while ((t = is.read()) == '0');
					if (t == -1) throw new IOException (s_eof);
					mIDs.add(mNextId++);
					ScriptEntry se = new ScriptEntry();
					if ((t = is.read()) != '"') throw new IOException ("Not a string");
					se.name = new String(um.readBytesAsString());
					if ((t = is.read()) != '"') throw new IOException ("Not a string");
					se.scriptData = um.readBytesAsString();
				}
			}
			success = true;
		} finally {
			if (!success) {
				mNextId = 0;
				mIDs = null;
				mList = null;
			}
			System.gc();
			is.close();
		}
	}

	public synchronized void saveList () throws IOException {
		mFile.renameTo(new File(mFile.getAbsolutePath() + ".bak"));
		OutputStream os = new FileOutputStream(mFile);
		Marshaler m = new Marshaler(os);
		os.write(4); os.write(8);
		os.write('[');
		m.writeInt(mList.size() + 1);
		m.marshal();
		for (ScriptEntry se : mList) {
			os.write('[');
			m.writeInt(3);
			m.marshal();
			os.write('"');
			m.writeBytesAsString(se.name.getBytes());
			os.write('"');
			m.writeBytesAsString(se.scriptData);
		}
		System.gc();
		os.close();
	}
	
	public synchronized ScriptEntry findScriptById (int id) {
		int idx = mIDs.indexOf(id);
		if (idx < 0 || idx >= mList.size()) return null;
		return mList.get(idx);
	}
	
	public synchronized ScriptEntry get (int index) {
		return mList.get(index);
	}
	
	public synchronized int insertScript (int index, ScriptEntry se) {
		mIDs.add(index, mNextId);
		mList.add(index, se);
		return mNextId++;
	}
	
	public synchronized boolean deleteScript (ScriptEntry se) {
		int idx = mList.indexOf(se);
		if (idx == -1) return false;
		mIDs.remove(idx);
		mList.remove(idx);
		return true;
	}
	
	public synchronized boolean deleteScript (int id) {
		int idx = mIDs.indexOf(id);
		if (idx == -1) return false;
		mIDs.remove(idx);
		mList.remove(idx);
		return true;
	}
	
	public synchronized void deleteScriptAt (int idx) throws IndexOutOfBoundsException {
		mIDs.remove(idx);
		mList.remove(idx);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(mIDs);
		dest.writeList(mList);
		dest.writeString(mFile.getAbsolutePath());
		dest.writeInt(mNextId);
	}
	
	public static final Creator<RubyScriptCollection> CREATOR = new Creator<RubyScriptCollection>() {
		public RubyScriptCollection[] newArray(int size) {
			return new RubyScriptCollection[size];
		}
		public RubyScriptCollection createFromParcel(Parcel source) {
			return new RubyScriptCollection(source, source.getClass().getClassLoader());
		}
	};
	
}
