package app.create.rpg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Helper {
	
	public Context context;
	public final String[] GRAPHIC_EXTS = {".jpg", ".JPG", ".png", ".PNG"};

	public Helper(Context context) {
		this.context = context;
	}
	
	public File getProjectDir () {
		return new File(((ActivityProject) context).getProjectDir());
	}
	
	public String validatePath (String path, boolean bAbsoluteAllowed, boolean bEndSlashAllowed, boolean bToLowerCase) {
		int i, n = path.length() - 1, s = 0;
		char c;
		StringBuilder sb = new StringBuilder();
		if (!bAbsoluteAllowed && ((c = path.charAt(0)) == '/' || c == '\\'))
			for (; s < n && path.charAt(s) == '/'; s++);
		if (!bEndSlashAllowed && ((c = path.charAt(n)) == '/' || c == '\\'))
			for (; n >= 0 && path.charAt(n) == '/'; n--);
		for (i = s; i <= n; i++) {
			c = path.charAt(i);
			if (bToLowerCase && 'A' <= c && c <= 'Z')
				c = (char) (c - 'A' + 'a');
			else if (c == '/' || c == '\\')
				for (; i < n && !(path.charAt(i) == '/' || path.charAt(i) == '\\'); i++);
			sb.append(c);
		}
		return sb.toString();
	}
	
	public File dirToRTP (String basedir) {
		return new File(Environment.getExternalStorageDirectory(), "RTP_list/Standard/" + basedir);
	}
	
	public String woExt (String name) {
		int i = name.lastIndexOf('.');
		if (i == -1) return name;
		return name.substring(0, i);
	}
	
	public File dirToProject (String basedir) {
		return new File(getProjectDir(), basedir);
	}
	
	public InputStream openMaterial (String basepath, boolean bRTP) throws FileNotFoundException, IOException {
		File f = bRTP ? new File(Environment.getExternalStorageDirectory(), "RTP_list/Standard/" + basepath)
		: new File(getProjectDir(), basepath);
		return new FileInputStream (f);
	}
	
	public Drawable getGraphicsMaterial (String category, String name, int hue) {
		if (name == null || name.length() == 0)
			return null;
		InputStream is = null;
		String path;
		name = "Graphics/" + category + "/" + name;
		for (String ext : GRAPHIC_EXTS) {
			path = name + ext;
			try {
				is = openMaterial(path, false);
			} catch (IOException e) {
				try {
					is = openMaterial(path, true);
				} catch (IOException e1) {
					continue;
				}
			}
			TypedValue typed = new TypedValue();
			typed.density = DisplayMetrics.DENSITY_MEDIUM;
			Drawable draw = Drawable.createFromResourceStream(context.getResources(), typed, is, name);
			try {
				is.close();
			} catch (IOException e) {}
			if (hue > 0) {
				draw.setColorFilter(ColorFilterGenerator.adjustHue(hue));
			}
			return draw;
		}
		return null;
	}

}
