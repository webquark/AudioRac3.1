package com.conpo.audiorac.util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.conpo.audiorac.application.Common;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;


public class CPDRMUtil {

	private static CPDRMUtil _instance;
	
	static {
		_instance = new CPDRMUtil();
	}
	
	public static CPDRMUtil getInstance() {
		return _instance;
	}
	
	@SuppressLint("DefaultLocale")
	public static String DurationToTimeString(int duration) {
		String strTime = "00:00";
		if (duration <= 0) {
			return strTime;
		}

		int msc = duration / 1000;
		int min = msc / 60;
		int sec = msc % 60;

		strTime = String.format("%02d:%02d", min, sec);
		return strTime;
	}
	
	public static void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}
	
	public static String getTimestamp() {
		Long ts = System.currentTimeMillis() / 1000;
		return ts.toString();
	}
	
	public static String setPadding(String src, String pad, int len) {
		String result = src;
		int src_len = src.length();
		for(int i = src_len; i < len; i++) {
			result += pad;
		}
		
		return result;
	}
	
	public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefault) {

        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            
            return null;
        }


        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                        in = null;
                    }
                } catch (IOException ex) {
                }
            }
        }
        
        return null;
    }
    
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    //private static final String sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;

        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }

        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (IllegalStateException ex) {
        } catch (FileNotFoundException ex) {
        }
 
        return bm;
    }
    
    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.ic_launcher), null, opts);
        return null;
    }

	
	@SuppressWarnings("deprecation")
	public static String makeUTF8(String str) throws UnsupportedEncodingException {
		if(str == null || str.equals("")) 
			return str;
		
		return URLEncoder.encode(new String(str.getBytes("UTF-8")));
	}
	
	/**
     * Base64 ���ڵ�
     */
    public static String getBase64encode(String content){
    	String result = Base64.encodeToString(content.getBytes(), 0);
    	result = result.replaceAll("\n", "");
        return result;
    }
     
    /**
     * Base64 ���ڵ�
     */
    public static String getBase64decode(String content){
    	String result = new String(Base64.decode(content, 0));
    	result = result.replaceAll("\n", "");
        return result;
    }

    public static String getOriginBase64(String content) {
    	String result = "";
    	if(content.length() <= Common.DRM_PASTE_LENGTH)
    		return "";
    	
    	result  = content;
    	result = result.replace(Common.DRM_DUMMY, "");
    	String postfix = result.substring(0, result.length() - Common.DRM_PASTE_LENGTH);
    	String prefix  = result.substring(result.length() - Common.DRM_PASTE_LENGTH,  result.length());
    	
    	result = prefix + postfix;
    	result = result.replaceAll("\n", "");
    	return result;
    }
    
    public static String getCPDRMBase64(String content) {
    	String result = "";
    	if(content.length() <= Common.DRM_PASTE_LENGTH)
    		return "";
    	
    	result = content;
    	String postfix = result.substring(0, Common.DRM_PASTE_LENGTH);
    	String prefix = result.substring(Common.DRM_PASTE_LENGTH, result.length());
    	String dummy = Common.DRM_DUMMY;
    	
    	result =  prefix + dummy + postfix;
    	result = result.replaceAll("\n", "");
    	return result;
    }
	
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;

		final Paint paint = new Paint();

		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		final RectF rectF = new RectF(rect);

		final float roundPx = 12;

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);

		paint.setColor(color);

		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;

	}
    
    
    
    /*
    public static String strCut(String szText, String szKey, int nLength, int nPrev, boolean isNotag, boolean isAdddot){  // ���ڿ� �ڸ���
        
        String r_val = szText;
        int oF = 0, oL = 0, rF = 0, rL = 0;
        int nLengthPrev = 0;
        Pattern p = Pattern.compile("<(/?)([^<>]*)?>", Pattern.CASE_INSENSITIVE);  // �±����� ����
        
        if(isNotag) {r_val = p.matcher(r_val).replaceAll("");}  // �±� ����
        r_val = r_val.replaceAll("&", "&");
        r_val = r_val.replaceAll("(!/|\r|\n| )", "");  // �������
      
        try {
          byte[] bytes = r_val.getBytes("UTF-8");     // ����Ʈ�� ����
     
          if(szKey != null && !szKey.equals("")) {
            nLengthPrev = (r_val.indexOf(szKey) == -1)? 0: r_val.indexOf(szKey);  // �ϴ� ��ġã��
            nLengthPrev = r_val.substring(0, nLengthPrev).getBytes("MS949").length;  // ��ġ�������̸� byte�� �ٽ� ���Ѵ�
            nLengthPrev = (nLengthPrev-nPrev >= 0)? nLengthPrev-nPrev:0;    // �� �պκк��� �����������Ѵ�.
          }
        
          // x���� y���̸�ŭ �߶󳽴�. �ѱ۾ȱ�����.
          int j = 0;
     
          if(nLengthPrev > 0) while(j < bytes.length) {
            if((bytes[j] & 0x80) != 0) {
              oF+=2; rF+=3; if(oF+2 > nLengthPrev) {break;} j+=3;
            } else {if(oF+1 > nLengthPrev) {break;} ++oF; ++rF; ++j;}
          }
          
          j = rF;
     
          while(j < bytes.length) {
            if((bytes[j] & 0x80) != 0) {
              if(oL+2 > nLength) {break;} oL+=2; rL+=3; j+=3;
            } else {if(oL+1 > nLength) {break;} ++oL; ++rL; ++j;}
          }
     
          r_val = new String(bytes, rF, rL, "UTF-8");  // charset �ɼ�
     
          if(isAdddot && rF+rL+3 <= bytes.length) {r_val+="...";}  // ...�� ���������� �ɼ�
        } catch(UnsupportedEncodingException e){ e.printStackTrace(); }  
        
        return r_val;
      }    
    
    */

}
