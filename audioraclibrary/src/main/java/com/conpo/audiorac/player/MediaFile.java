package com.conpo.audiorac.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.Toast;

import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.util.CPDRMUtil;

/**
 * 미디어 파일 및 Conpo DRM Wrapper
 * @author hansolo
 *
 */
public class MediaFile {
	private Context mContext;
	
	private String mPath;
	private String mFilename;
	
	private String mDrmPrefix;
	private boolean mIsDrmFile = false;
	private long mFileLength = 0;
	
	public MediaFile(Context context) {
		mContext = context;
	}
	
	/**
	 * 미디어 파일을 로드하고, Conpo DRM 파일인 경우 관련 DRM 정보를 보관함
	 * @param path
	 * @param filename
	 */
	public void loadFile(String path, String filename) {
		mPath = path;
		mFilename = filename;
		
		mFileLength = getFileBytesLength(mContext, path, filename);
		
		if (mFileLength > 0) {
			mDrmPrefix = extractCPDRMPrefix(path, filename);

			if (mDrmPrefix.equals("")) {
				mIsDrmFile = false;

			} else {
				mIsDrmFile = true;
				
				String[] arrEncPrefix = mDrmPrefix.split("[|]");
				if(arrEncPrefix.length != 2) {
					Log.i(Common.TAG, "올바르지 않은 형식의 파일입니다.");
					Toast.makeText(mContext, "올바르지 않은 형식의 파일입니다." + mDrmPrefix, Toast.LENGTH_LONG).show();
					
					return;
				}
				
				String drmEncPostfix = arrEncPrefix[1].trim();
				String originEncPostfix = CPDRMUtil.getOriginBase64(drmEncPostfix);
				String drmDecPostfix = CPDRMUtil.getBase64decode(originEncPostfix);
			
				mDrmPrefix = arrEncPrefix[0] + "| " + drmDecPostfix;
			}
		}
	}
	
	/**
	 * 파일의 byte 길이를 구함
	 * @param context
	 * @param path
	 * @param filename
	 * @return
	 */
	private long getFileBytesLength(Context context, String path, String filename) {

		long fileBitesLength = 0;
		
		if (path != null && path.length() > 0) {
			File oDatabFolder = new File(path);
			if (oDatabFolder != null && oDatabFolder.exists() == true && oDatabFolder.isDirectory() == true) {
				String sFile = path + filename;

				FileInputStream oInputStream = null;
				try {
					oInputStream = new FileInputStream(sFile);
					fileBitesLength = (long)oInputStream.available();

					oInputStream.close();
					oInputStream = null;
				} catch (Exception e) {
					Log.i(Common.TAG, e.getMessage());
				} finally {
					if (oInputStream != null) {
						try {
							oInputStream.close();
							oInputStream = null;
						} catch (IOException e) {
							Log.i(Common.TAG, e.getMessage());
						}
					}
				}
			}
		}

		return fileBitesLength;
	}
	
	/**
	 * CONPO DRM 파일인지 여부
	 * @return
	 */
	public boolean isCPDRM() {
		return this.mIsDrmFile;
	}

	/**
	 * get MP3 duration
	 * @return
	 */
	public long getDuration() {
		String sFile = mPath + mFilename;
		MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
		
		try {
			metaRetriever.setDataSource(sFile);

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		
		if (duration != null)
			return Long.valueOf(duration);
		else
			return 0;
	}
	
	/**
	 * CONPO DRM 파일의 재생 가능한 남은 시간을 구함 (millisecond)
	 * @return -2: 무제한, -1:최초재생 전, 0:만료, 0보다 크면 플레이 가능
	 */
	public long getRemainTime() {
		if (!this.isCPDRM())
			return -2;

		long remain = 0;
		long limit = 0;
		long first = 0;
		long now  = 0;

		try {
			limit = Long.valueOf(getDRMPrefixAt(4));
			first = Long.valueOf(getDRMPrefixAt(7));
			now = Long.valueOf(CPDRMUtil.getTimestamp());

			if (limit > 0) {	// 시간 제한이 있고
				if (first > 0) {	// 플레이된 적이 있으면
					remain = first + limit - now;
					remain *= 1000;
					remain = (remain < 0) ? 0 : remain;

				} else {	// 플레이된 적 없음
					remain = -1;
				}

			} else {
				if (first == 0) {
					remain = -1;
				} else {    // 시간 제한 없음
					remain = -2;
				}
			}

		} catch(NumberFormatException e) {
			Log.i(Common.TAG, "올바르지 않은 형식의 파일입니다.");
			
			return -2;
		}
		
		return remain;
	}
	
	public String getPath() {
		return this.mPath;
	}
	
	public String getFilename() {
		return this.mFilename;
	}

	public String getFullPath() {
		return this.mPath + "/" + mFilename;
	}
	
	/**
	 * 파일의 byte 길이를 구함
	 * @return
	 */
	public long getFileLength() {
		return this.mFileLength;
	}
	
	public String getDRMPrefix() {
		return mDrmPrefix;
	}
	
	public String getDRMPrefixAt(int index) {
		String result = "";

		if (mDrmPrefix == null) {
			return result;
		}

		//CPDRM_[SAFIU1231-WEMNFSD665-ILSDFI2121-EWNDSF43123] | :CS202202150002:123146:conpo:2592000:999999:99999:1683959319:1684896808:0:0:0
		String[] arrDrmPrefix = mDrmPrefix.split("[:]");
		if(arrDrmPrefix.length == 12 && arrDrmPrefix.length >= index+1) {
			result = arrDrmPrefix[index];
		}
		
		return result;
	}
	
	public boolean isFirstDrmPlay() {
		if(isCPDRM()) {
			// 아이디가 없으면 최초
			if(getDRMPrefixAt(3).equals("")) {  // || getDRMPrefixAtIndex(4).equals("0")) {
				return true;
			}
		}
		
		// 일반 MP3이므로 false 반환
		return false;
	}
	
	/**
	 * 파일에서 Conpo DRM frefix를 추출
	 * CPDRM_[SAFIU1231-WEMNFSD665-ILSDFI2121-EWNDSF43123] | :12345:67890::0:999999:99999:0:0:0:0:0
	 * CPDRM_[SAFIU1231-WEMNFSD665-ILSDFI2121-EWNDSF43123] | :cs2312312:21323:ycw7702:3600:999999:99999:1406135072:1406135097:0:2:0 
	 *		
	 * @param path
	 * @param filename
	 * @return
	 */
	private String extractCPDRMPrefix(String path, String filename) {
		/*
		출판코드     - :
	 	강좌코드     -12345:
		강의코드     -67890:
		아이디        -:
		제한시간     -0:
		제한횟수     -999999:
		복사제한횟수 -99999:
		최초재생시간 -0:
		최종재생시간 -0:
		재생시간     -0:
		재생횟수     -0:
		복사횟수     -0 
		
		출판코드     - :
		강좌코드     -cs2312312:
		강의코드     -21323:
		아이디       -ycw7702:
		제한시간     -3600:
		제한횟수     -999999:
		복사제한횟수 -99999:
		최초재생시간 -1406135072:
		최종재생시간 -1406135097:
		재생시간     -0:
		재생횟수     -2:
		복사횟수     -0
		*/
		byte[] bArData = null;
		byte[] bDRMPrefix = null;
		String drmPrefix = "";

		if (path != null && path.length() > 0) {
			File oDatabFolder = new File(path);
			if (oDatabFolder.exists()) {
				Log.i(Common.TAG, "exist2");
			} else {
				Log.i(Common.TAG, "not exist2");
			}

			if (oDatabFolder.isDirectory()) {
				Log.i(Common.TAG, "folder2");
			} else {
				Log.i(Common.TAG, "not folder2");
			}

			if (oDatabFolder != null && oDatabFolder.exists() == true
					&& oDatabFolder.isDirectory() == true) {
				String sFile = path + filename;
		
				FileInputStream oInputStream = null;
				try {
					oInputStream = new FileInputStream(sFile);
					int nCount = oInputStream.available();
					Log.i(Common.TAG, "avail2:" + String.valueOf(nCount));

					if (nCount > 0) {
						bArData 	= new byte[Common.DRM_PREFIX_LENGTH];
						bDRMPrefix 	= new byte[Common.DRM_PREFIX_LENGTH];

						oInputStream.read(bArData, 0, Common.DRM_PREFIX_LENGTH);
						int j = 0;

						// DRM Check
						for (int i = 0; i < Common.DRM_PREFIX_LENGTH; i++) {
							bDRMPrefix[j++] = bArData[i];
						}

						drmPrefix = new String(bDRMPrefix);
						if (!drmPrefix.startsWith(Common.DRM_NAME)) {
							drmPrefix = "";
						}
						
						bArData = null;
						bDRMPrefix = null;
					}

				} catch (FileNotFoundException e) {
					Log.i(Common.TAG, e.getMessage());
				} catch (IOException e) {
					Log.i(Common.TAG, e.getMessage());
				} finally {
					if (oInputStream != null) {
						try {
							oInputStream.close();
							oInputStream = null;
						} catch (IOException e) {
							Log.i(Common.TAG, e.getMessage());
						}
					}
				}
			}
		}

		return drmPrefix;
	}
	
	public long fileToBytes(Context context) {
		byte[] bDRMCheck = null;
		long mp3_length = 0;
		
		if (mPath != null && mPath.length() > 0) {
			File oDatabFolder = new File(mPath);

			if (oDatabFolder != null && oDatabFolder.exists() == true
					&& oDatabFolder.isDirectory() == true) {

				String sFile = mPath + mFilename;
				FileInputStream oInputStream = null;
				
				try {
					oInputStream = new FileInputStream(sFile);
					int nCount = oInputStream.available();
					Log.i(Common.TAG, "avail:" + String.valueOf(nCount));

					if (nCount > 0) {
						bDRMCheck =  new byte[Common.DRM_NAME_LENGTH];
							
						// DRM Check
						oInputStream.read(bDRMCheck);
						String drmType = new String(bDRMCheck);
						Log.i(Common.TAG, "DRM:" + drmType);
						bDRMCheck = null;
						
						oInputStream.close();
						oInputStream = null;
						
						oInputStream = new FileInputStream(sFile);
						if (drmType.equals(Common.DRM_NAME)) {
							drmType = null;
							
							// DRM MP3
							mp3_length = nCount - Common.DRM_HEADER_LENGTH;
							oInputStream.skip(Common.DRM_HEADER_LENGTH);

						} else {
							// General MP3
							mp3_length = nCount;
						}
						
						long s1 = System.currentTimeMillis();
						// create temp file that will hold byte array
						File tempMp3 = new File(context.getExternalCacheDir() + "/" + Common.DRM_NAME + Common.DRM_EXTENSION);
						Log.i(Common.TAG, "@@@@@ temp1:" + tempMp3.getAbsoluteFile());
						
						tempMp3.delete();
						tempMp3.createNewFile();
						FileOutputStream fos = new FileOutputStream(tempMp3);
						
						int count = 0;
						byte bReadBuffer[] = new byte[Common.READ_BUFFER];
						
						while((count = oInputStream.read(bReadBuffer)) != -1) {
							fos.write(bReadBuffer, 0, count);
						}
												
						fos.close();
						fos = null;
						
						long e1 = System.currentTimeMillis();
					    
						Log.i(Common.TAG, "#########reverseBytes1:" + String.valueOf(e1 - s1));
					}

				} catch (FileNotFoundException e) {
					Log.i(Common.TAG, e.getMessage());
				} catch (IOException e) {
					Log.i(Common.TAG, e.getMessage());
				} finally {
					if (oInputStream != null) {
						try {
							oInputStream.close();
							oInputStream = null;
						} catch (IOException e) {
							Log.i(Common.TAG, e.getMessage());
						}
					}
				}
			}
		}

		return mp3_length;
	}
	
	public long reverseBytes(Context context) {
		
		File tempMp3 = new File(context.getExternalCacheDir() + "/" + Common.DRM_NAME + Common.DRM_EXTENSION);
		String tempFilePath = tempMp3.getAbsolutePath();
		Log.i(Common.TAG, "@@@@@ temp3:" + tempFilePath);

		int len = 0;
		FileInputStream oInputStream = null;
		FileOutputStream fos = null;
		File reverseFile = null;
		
		try { 
			oInputStream = new FileInputStream(tempMp3);
			len = oInputStream.available();
			Log.i(Common.TAG, "avail3:" + String.valueOf(len));
			oInputStream.close();
			oInputStream = null;
			
			if(len > 0) {
				// create temp file that will hold byte array
				reverseFile = new File(context.getExternalCacheDir() + "/" + Common.REVERSE_NAME + Common.DRM_EXTENSION);
				
				reverseFile.delete();
				reverseFile.createNewFile();
				fos = new FileOutputStream(reverseFile, true);
				
				int bpos = len - Common.DRM_REVERSE_LENGTH;
				int loop = len / Common.DRM_REVERSE_LENGTH;
				
				if(len > loop * Common.DRM_REVERSE_LENGTH) 
					loop++;
				
				long s1 = System.currentTimeMillis();
				
				
				int blen = 0;
				for(int i = 0; i < loop; i++) {
					
					if(bpos >= 0) {
						blen = Common.DRM_REVERSE_LENGTH;
					}
					else {
						blen = Common.DRM_REVERSE_LENGTH + bpos;
						bpos = 0;
					}

					fos.write(readCharsFromFile(tempFilePath, bpos, blen));
					bpos -= Common.DRM_REVERSE_LENGTH;
				}
				long e1 = System.currentTimeMillis();
			    fos.close();
			    fos = null;
			    
				Log.i(Common.TAG, "#########reverseBytes:" + String.valueOf(e1 - s1));
				tempMp3.delete();
				
				File from = new File(context.getExternalCacheDir() + "/" + Common.REVERSE_NAME + Common.DRM_EXTENSION);
				File to = new File(context.getExternalCacheDir() + "/" + Common.DRM_NAME + Common.DRM_EXTENSION);
				from.renameTo(to);
				
				from = null;
				to = null;
			}
			
		} catch (FileNotFoundException e) {
			Log.i(Common.TAG, e.getMessage());
		} catch (IOException e) {
			Log.i(Common.TAG, e.getMessage());
		} finally {
			if(fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (IOException e) {
					Log.i(Common.TAG, e.getMessage());
				}
			}
			
			if (oInputStream != null) {
				try {
					oInputStream.close();
					oInputStream = null;
				} catch (IOException e) {
					Log.i(Common.TAG, e.getMessage());
				}
			}
		}
		
		return len;
	}
	
	private byte[] readCharsFromFile(String filePath, int seek, int chars) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        
        if(seek > 0)
        	file.seek(seek);
        
        byte[] bytes = new byte[chars];
        file.read(bytes);
        file.close();
        file = null;
        return bytes;
    }
	
	public void doFirstDrmPlay(String host, String userid, String limit) {
		Log.i(Common.TAG, "LIMIT:" + limit);
		long limitResult = 0;
		
		try {
			limitResult = Long.valueOf(limit.replace("\r\n", "")); 
		}
		catch(NumberFormatException e) {
			return;
		}
		
		if(isFirstDrmPlay()) {
		
			// 새로운 DRM Prefix를 만듬
			String timestamp = CPDRMUtil.getTimestamp();
			Log.i(Common.TAG, "TS:" + timestamp);
			
			String strNewPrefix = "";
			strNewPrefix += getDRMPrefixAt(0) + ":"; 	// DRM스트링 + 출판사코드
			strNewPrefix += getDRMPrefixAt(1) + ":"; 	// 강좌코드
			strNewPrefix += getDRMPrefixAt(2) + ":"; 	// 강의코드
			strNewPrefix += userid + ":";               // 아이디
			strNewPrefix += limitResult + ":";			// 제한시간
			strNewPrefix += getDRMPrefixAt(5) + ":"; 	// 제한횟수
			strNewPrefix += getDRMPrefixAt(6) + ":"; 	// 복사제한횟수
			strNewPrefix += timestamp + ":"; 				// 최초제한시간
			strNewPrefix += timestamp + ":"; 				// 최종재생시간
			strNewPrefix += getDRMPrefixAt(9) + ":"; 	// 재생시간
			strNewPrefix += getDRMPrefixAt(10) + ":"; 	// 재생횟수
			strNewPrefix += getDRMPrefixAt(11); 		// 복사횟수
			strNewPrefix = strNewPrefix.trim();
			
			Log.i(Common.TAG, "NEW_PREFIX:" + strNewPrefix);
	
			String[] arrDecPrefix = strNewPrefix.split("[|]");
			
			String drmDecPostfix = arrDecPrefix[1].trim();
			String originEncPostfix = CPDRMUtil.getBase64encode(drmDecPostfix);
			Log.i(Common.TAG, "@@@CRYPTO:" + originEncPostfix);
			String drmEncPostfix = CPDRMUtil.getCPDRMBase64(originEncPostfix);

			String confirmEnc = CPDRMUtil.getOriginBase64(drmEncPostfix);
			String confirmDec = CPDRMUtil.getBase64decode(confirmEnc);
			Log.i(Common.TAG, "@@@CRYPTO:" + confirmDec);
			
			strNewPrefix = arrDecPrefix[0] + "| " + drmEncPostfix;
			
			strNewPrefix = CPDRMUtil.setPadding(strNewPrefix, " ", 1024);
			Log.i(Common.TAG, "PREFIX_LEN:" + String.valueOf(strNewPrefix.length()));
			
			
			// 파일에 아이디, 제한시간, 최초재생시간을 씀
			if(this.repackageDRMFile(strNewPrefix)) {
				Log.i(Common.TAG, "REPACKAGE1 : TRUE");
			}
				
		}
	}
	
	public void setLastPlayTime() {
		
		// 새로운 DRM Prefix를 만듬
		String timestamp = CPDRMUtil.getTimestamp();
		Log.i(Common.TAG, "TS2:" + timestamp);
		
		String strNewPrefix = "";
		strNewPrefix += getDRMPrefixAt(0) + ":"; 	// DRM스트링 + 출판사코드
		strNewPrefix += getDRMPrefixAt(1) + ":"; 	// 강좌코드
		strNewPrefix += getDRMPrefixAt(2) + ":"; 	// 강의코드
		strNewPrefix += getDRMPrefixAt(3) + ":";	// 아이디
		strNewPrefix += getDRMPrefixAt(4) + ":";	// 제한시간
		strNewPrefix += getDRMPrefixAt(5) + ":"; 	// 제한횟수
		strNewPrefix += getDRMPrefixAt(6) + ":"; 	// 복사제한횟수
		strNewPrefix += getDRMPrefixAt(7) + ":";	// 최초제한시간
		strNewPrefix += timestamp + ":"; 								// 최종재생시간
		strNewPrefix += getDRMPrefixAt(9) + ":"; 	// 재생시간
		strNewPrefix += getDRMPrefixAt(10) + ":"; 	// 재생횟수
		strNewPrefix += getDRMPrefixAt(11); 			// 복사횟수
		strNewPrefix = strNewPrefix.trim();
		
		Log.i(Common.TAG, "LAST_PLAY_PREFIX:" + strNewPrefix);
		
		String[] arrDecPrefix = strNewPrefix.split("[|]");
		
		String drmDecPostfix = arrDecPrefix[1].trim();
		String originEncPostfix = CPDRMUtil.getBase64encode(drmDecPostfix);
		Log.i(Common.TAG, "@@@CRYPTO:" + originEncPostfix);
		String drmEncPostfix = CPDRMUtil.getCPDRMBase64(originEncPostfix);

		String confirmEnc = CPDRMUtil.getOriginBase64(drmEncPostfix);
		String confirmDec = CPDRMUtil.getBase64decode(confirmEnc);
		Log.i(Common.TAG, "@@@CRYPTO:" + confirmDec);
		
		strNewPrefix = arrDecPrefix[0] + "| " + drmEncPostfix;
		Log.i(Common.TAG, "LAST_PLAY_EPREFIX:" + strNewPrefix);
		
		strNewPrefix = CPDRMUtil.setPadding(strNewPrefix, " ", 1024);
		Log.i(Common.TAG, "LAST_PLAY_PREFIX_LEN:" + String.valueOf(strNewPrefix.length()));
		
		
		// 파일에 최종재생시간을 씀
		if(this.repackageDRMFile(strNewPrefix)) {
			Log.i(Common.TAG, "REPACKAGE2 : TRUE");
		}
	}
	
	public boolean repackageDRMFile(String newPrefix) {

		RandomAccessFile raf = null;
		
		if (mPath != null && mPath.length() > 0) {
			File oDatabFolder = new File(mPath);

			if (oDatabFolder != null && oDatabFolder.exists() == true
					&& oDatabFolder.isDirectory() == true) {
				
				String sFile = mPath + mFilename;
				FileInputStream oInputStream = null;

				try {
					oInputStream = new FileInputStream(sFile);
					int nCount = oInputStream.available();
					oInputStream.close();
					oInputStream = null;
					
					if (nCount > 0) {
						raf = new RandomAccessFile(sFile, "rw");
						raf.seek(0);
						raf.writeBytes(newPrefix);
						raf.close();
						raf = null;
						
						return true;
					}

				} catch (FileNotFoundException e) {
					Log.i(Common.TAG, e.getMessage());
				} catch (IOException e) {
					Log.i(Common.TAG, e.getMessage());
				} finally {
					if(raf != null) {
						try {
							raf.close();
							raf = null;
						} catch (IOException e) {
							Log.i(Common.TAG, e.getMessage());
						}
					}
					
					if (oInputStream != null) {
						try {
							oInputStream.close();
							oInputStream = null;
						} catch (IOException e) {
							Log.i(Common.TAG, e.getMessage());
						}
					}
				}
			}
		}

		return false;
	}

}
