package com.conpo.audiorac.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.conpo.audiorac.adapter.FileAdapter;
import com.conpo.audiorac.model.Record;
import com.conpo.audiorac.player.MediaFile;
import com.conpo.audiorac.util.CPDRMUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * MP3 파일 검색 리스트 뷰
 * @author hansolo
 *
 */
public class FileListView extends ListView
				implements AdapterView.OnItemClickListener {

	private Context mContext = null;
	private ArrayList<String> mList = new ArrayList<String>();
    private ArrayList<String> mFolderList = new ArrayList<String>();
    private ArrayList<String> mFileList = new ArrayList<String>();
	private ArrayList<Record> mRecFileList = new ArrayList<Record>();

	private String mVolumnId = "";		// 앨범(강좌) ID
	private boolean mIsDRM = false;	// DRM 파일이 하나라도 있으면 true
	private int mExpiredDRMCnt = 0;	// DRM 기간 만료된 파일 수

	private String mApplicationDRMFolder = "";

	// Property 
	private String mPath = "";
	
	// Event
	private OnFileListEventListener mOnFileListEventListener = null;
	
	public interface OnFileListEventListener {
		public void onStartPathChange();
		public void onPathChanged(String path, String displayPath, boolean addHistory, String volumnId);
		public void onFileSelected(int selectedIndex, ArrayList<Record> recAllFileList);
		public void onFoundDRMExpired(int expiredCnt);
	}
	
	
	public interface OnNotifyEventListener {
		public void onNotify(Object sender);
	}
	
	public void setOnFileListEventListener(OnFileListEventListener listener) {
		mOnFileListEventListener = listener;
	}
	

	public void setApplicationDRMFolder(String folder) {
		mApplicationDRMFolder = folder;
	}

	public FileListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize(context);
	}

	public FileListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initialize(context);
	}

	public FileListView(Context context) {
		super(context);
		
		initialize(context);
	}
	
	private void initialize(Context context) {
		mContext = context;
		
		this.setOnItemClickListener(this);
	}

	/**
	 * 컨포 DRM 파일 다운로드 폴더 생성
	 * @return
	 */
	public String initializeDRMStorage() {
		String path = this.mApplicationDRMFolder;

		File tempPath = new File( path );
		if (!tempPath.exists()) {
            int result = mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			boolean res = tempPath.mkdirs();
		}

		return path;
	}

	/**
	 * 지정한 경로의 폴더 및 파일을 리스팅한다
	 * @param path
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private boolean listFiles(String path) {
		mFolderList.clear();
		mFileList.clear();
		mIsDRM = false;
		mVolumnId = "";
		
		
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
        	Toast.makeText(mContext, "접근할 수 없는 폴더입니다.", Toast.LENGTH_LONG).show();
        	return false;
        }
        
        for (int i=0; i<files.length; i++) {
        	
        	if (files[i].isDirectory()) {
        		mFolderList.add(files[i].getName());
        	} else {
        		if (files[i].getName().toLowerCase().endsWith(".mp3")) {
					mFileList.add(files[i].getName());
				}
        	}
        }
        
        Collections.sort(mFolderList);
        Collections.sort(mFileList);
        
        mRecFileList.clear();
        mFolderList.add(0, "..");
        
        for (String name : mFolderList) {
			int fileCnt = 0;
			int deletedCnt = 0;

        	if (!name.equals("..")) {
	    		File file1 = new File(path + "/" + name);
	            File[] files1 = file1.listFiles();
	            
	            for (int i=0; i < files1.length; i++) {
	            	
	            	if (files1[i].isFile()) {
	            		MediaFile mediaFile = new MediaFile(mContext);
	            		mediaFile.loadFile(path + name + "/", files1[i].getName());

	            		if (mediaFile.isCPDRM() && mediaFile.getRemainTime() == 0) {
	            			deletedCnt++;
							deleteFile(mediaFile.getFullPath());
	            		}

						fileCnt++;
	            	}
	            }

        	}

			if (!name.equals("..") && fileCnt == deletedCnt) {
				deleteFolder(path + "/" + name);
			} else {

				Record rec = new Record();

				rec.put("path", path);
				rec.put("name", name);
				rec.put("file_cnt", "" + (fileCnt - deletedCnt));
				rec.put("expired_cnt", "0");
				rec.put("type", "folder");

				mRecFileList.add(rec);
			}
    	} 
        
        mExpiredDRMCnt = 0;
        
        for (String name : mFileList) {
        	MediaFile mediaFile = new MediaFile(mContext);
        	
        	mediaFile.loadFile(path, name);

			if (mediaFile.getRemainTime() == 0) {	// DRM 기간 만료된 파일 --> 바로삭제 (2018.02.02 han)
				//mExpiredDRMCnt++;
				deleteFile( mediaFile.getFullPath() );

			} else {
				Record rec = new Record();
				rec.put("path", path);
				rec.put("name", name);
				rec.put("type", "mp3");
				rec.put("is_drm", ""+mediaFile.isCPDRM());
				rec.put("duration", CPDRMUtil.DurationToTimeString((int)mediaFile.getDuration()));
				rec.put("remain", ""+mediaFile.getRemainTime());
				rec.put("volumnid", ""+mediaFile.getDRMPrefixAt(1));
				rec.put("installmentid", ""+mediaFile.getDRMPrefixAt(2));

				if (!rec.get("volumnid").isEmpty())
					mVolumnId = rec.get("volumnid");

				if (rec.get("is_drm").equals("true")) {
					mIsDRM = true;
				}
        	
				mRecFileList.add(rec);
			}
    	}

        return true;
	}
	
	/**
	 * 현재 폴더의 전체 파일 리스트를 리턴합니다. 
	 * @return
	 */
	public ArrayList<Record> getAllFiles() {
		return mRecFileList;
	}
	
	/**
	 * 현재 폴더의 DRM 이용기간 만료된 파일을 삭제 
	 * @return 삭제한 파일의 수
	 */
	public boolean deleteExpiredFiles() {
		int deletedCnt = 0;
		for (String name : mFileList) {
        	MediaFile mediaFile = new MediaFile(mContext);
        	
        	mediaFile.loadFile(mPath, name);
      	
        	if (mediaFile.getRemainTime() == 0) {
        		deleteFile(mPath + "/" + name);
        		deletedCnt++;	// DRM 기간 만료된 파일 삭제
        	}
    	}
		

		if (deletedCnt == mFileList.size()) {
			deleteFile(mPath);
			
			return true;
		}
		
		return false;
	}
	
	public boolean deleteFile(String path) {
		File file = new File(path);
		return file.delete();
	}
	
	public boolean deleteFolder(String path) {
		File dir = new File(path);
		
		try {
			//FileUtils.deleteDirectory(dir);
			dir.delete();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * 현재 폴더의 하위 폴더 및 파일로 ListAdapter를 갱신하여 리스트를 다시 그린다
	 */
	public void updateAdapter() {
//		mList.clear();
//        mList.addAll(mFolderList);
//        mList.addAll(mFileList);
        
        ListAdapter adapter = new FileAdapter(mContext, mRecFileList);
	
        setAdapter(adapter);
	}

	public void updateDuration(Record rec) {
		for (Record file : mRecFileList) {
			String duration = rec.safeGet(file.safeGet("installmentid"));
			
			if (!duration.isEmpty()) {
				file.put("duration", duration);
			}
		}
	}
	
	public void refresh() {
		setPath(mPath, false);
	}
	
	public void setPath(String path, boolean addHistory) {
		if (path.length() == 0) {
			path = "/";
		} else if (!path.endsWith("/")) {
			path += "/"; 
		}

		File file = new File(path);
		if (file == null || !file.exists()) {
			path = mApplicationDRMFolder;
		}
		
		/*
		 * 경로변경 시작 알림
		 */
		if ( mOnFileListEventListener != null) 
			mOnFileListEventListener.onStartPathChange();
		
		if (listFiles(path)) {
			mPath = path;

			updateAdapter();
			
			/*
			 * 경로 변경 알림
			 */
			if (mOnFileListEventListener != null) {
				String displayPath = path.substring(Environment.getExternalStorageDirectory().getAbsolutePath().length());
			
				mOnFileListEventListener.onPathChanged(path, displayPath, addHistory, mVolumnId);
			}
			
			/*
			 * DRM 만료 파일 알림
			 */
			if (mExpiredDRMCnt > 0 && mOnFileListEventListener != null) 
				mOnFileListEventListener.onFoundDRMExpired(mExpiredDRMCnt);
		}
	}

	public String getPath() {
		return mPath;
	}

	public void moveToPath(String path) {
		setPath(getRealPathName(path), true);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> getFileList() {
		return (ArrayList<String>)mFileList.clone();
	}
	
//	public String DelteRight(String value, String border) {
//		String list[] = value.split(border);
//
//		String result = "";
//		
//		for (int i=0; i<list.length; i++) {
//			result = result + list[i] + border; 
//		}
//		
//		return result;
//	}
	
	private String delteLastFolder(String value) {
		String list[] = value.split("/");

		String result = "";
		
		for (int i=0; i<list.length-1; i++) {
			result = result + list[i] + "/"; 
		}
		
		return result;
	}
	
	private String getRealPathName(String newPath) {
		String path = newPath;
		if(!path.equals("..")) {
			path = path.substring(1, newPath.length());
		}
		
		if (path.equals("..")) {
			return delteLastFolder(mPath);
		} else {
			return mPath + path + "/";
		}
	}

	/**
	 * 지정한 인덱스 이후의 플레이 가능한 첫번째 파일을 선택
	 * @param index
	 */
	public void selectPlayableFile(int index) {
		if (index < 0)
			return;
		
		for (int i = index; i < this.mRecFileList.size(); i++) {
			Record rec = this.mRecFileList.get(i+1);

			if (rec.safeGet("type").equals("mp3")) {	// MP3 파일선택
				if (!rec.safeGet("remain").equals("0")) {
									
					if (mOnFileListEventListener != null) 
						mOnFileListEventListener.onFileSelected(i, mRecFileList);
					
					return;
				}
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Record rec = this.mRecFileList.get(position);
		
		if (rec.safeGet("type").equals("mp3")) {	// MP3 파일선택
			if (rec.safeGet("remain").equals("0")) {
				Toast.makeText(mContext, "이용기간이 만료되어 재생할 수 없습니다.", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (mOnFileListEventListener != null) 
				mOnFileListEventListener.onFileSelected(position-1, mRecFileList);
		}
		else {	// 폴더선택
			String path = rec.safeGet("path");
			String clicked = rec.safeGet("name");

			if (path.equals(mApplicationDRMFolder) && clicked.equals("..")) {
				// DRM 폴더 이상은 이동 못함
				return;
			}

			setPath(getRealPathName("/" + rec.safeGet("name")), true);
		}
	}

}
