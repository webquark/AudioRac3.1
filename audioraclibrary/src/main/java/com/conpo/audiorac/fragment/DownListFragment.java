package com.conpo.audiorac.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.conpo.audiorac.activity.MainActivity;
import com.conpo.audiorac.activity.PlayerActivity;
import com.conpo.audiorac.adapter.FileListAdapter;
import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.application.PlayList;
import com.conpo.audiorac.helper.SlideUpHelper;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.model.DrmFile;
import com.conpo.audiorac.model.ModelBase;
import com.conpo.audiorac.model.Record;
import com.conpo.audiorac.player.MediaFile;
import com.conpo.audiorac.util.CPDRMUtil;
import com.conpo.audiorac.util.FileUtil;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 다운로드 목록 액티비티
 */
public class DownListFragment extends FragmentBase
        implements SwipeRefreshLayout.OnRefreshListener,
                    FileListAdapter.OnItemClickListener {
    private static final String LOG_TAG = "DownList";

    private View mView;

    private TextView mTvPath;
    private SwipeRefreshLayout mSwipeContainer;

    private FileListAdapter mAdapter;

    private final ArrayList<DrmFile> mDrmFileList = new ArrayList<>();    // DRM 파일 목록

    protected ArrayList<String> mPathHistory = new ArrayList<String>();

    private String mCourseId = "";		// 앨범(강좌) ID
    private boolean mIsDrm = false;	    // DRM 파일이 하나라도 있으면 true
    private int mExpiredDRMCnt = 0;	    // DRM 기간 만료된 파일 수

    private String mCurrentPath = "";

    /**
     * 다운로드 파일 저장 기본 폴더
     */
    private String DRM_FOLDER_PATH = "";


    public interface OnListCompleteListener {
        public void onListComplete();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_down_list, container, false);

        if (mContext == null) {
            mContext = getContext();
        }

        mMainActivity = (MainActivity)getActivity();
        DRM_FOLDER_PATH = mMainActivity.getAudioRacApplication().getDRMFolder();

        mTvPath = mView.findViewById(R.id.tv_path);

        mSwipeContainer = mView.findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);

        initializeFileList();
        initializeDRMStorage();

        Log.d(LOG_TAG, "DownList Initialized");

        return mView;
    }

    public ActivityResultLauncher<Intent> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        mMainActivity.setNavigationItem(R.id.navigation_down_list);
                        onRefresh();

                    } else {
                        mMainActivity.setNavigationTab(0);
                        Alert(R.string.msg_permission_nenied);
                    }
                }
            });

    public ActivityResultLauncher<Intent> getPermissionLauncher() {
        return permissionLauncher;
    }


    /**
     * 파일 리스트 초기화
     */
    private void initializeFileList() {
        RecyclerView recyclerView = mView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mAdapter = new FileListAdapter(mContext, mDrmFileList);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void initializeDRMStorage() {
        mCurrentPath = DRM_FOLDER_PATH;
        FileUtil.makeFolder(mCurrentPath);

        setCurrentPath(mCurrentPath, true, null);
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }

    public void setCurrentPath(String path, boolean addHistory, OnListCompleteListener listener) {
        path = FileUtil.validatePath(path);

        File file = new File(path);
        if (file == null || !file.exists()) {
            path = DRM_FOLDER_PATH;
        }

        if (listFolderFiles(path)) {
            mCurrentPath = path;
            mAdapter.notifyDataSetChanged();

            displayCurrentPath(addHistory);

            if (mCourseId != null && !mCourseId.isEmpty()) {
                new RequestDurationTask().execute(mCourseId);
            }

            if (listener != null)
                listener.onListComplete();
        }
    }

    public void displayCurrentPath(boolean addHistory) {
        if (addHistory)
            mPathHistory.add(mCurrentPath);

        String displayPath = mCurrentPath.substring(Environment.getExternalStorageDirectory().getAbsolutePath().length());
        mTvPath.setText(displayPath);
    }

    @Override
    public void onRefresh() {
        mSwipeContainer.setRefreshing(true);
        setCurrentPath(mCurrentPath, false, null);
        mSwipeContainer.setRefreshing(false);
    }

    /**
     * 파일 리스트의 아이템 클릭 처리
     * @param item
     */
    @Override
    public void onItemClick(ModelBase item) {
        DrmFile drmFile = (DrmFile)item;
        Log.d(LOG_TAG, drmFile.name);

        if (drmFile.type.equals("mp3")) {	// MP3 파일선택
            if (drmFile.remain == 0) {
                showToast(R.string.msg_cannot_play_expired);
                return;
            }

            PlayList.resetPlayList();
            PlayList.addFile(drmFile);

            mMainActivity.stopWebAudioPlayer();

            Intent intent = new Intent(mContext, PlayerActivity.class);
            this.startActivityForResult(intent, 1000);


        } else {	// 폴더선택
            if (drmFile.name.equals("..") && drmFile.path.equals(DRM_FOLDER_PATH)) {
                // DRM 폴더 이상은 이동 못함
                return;
            }

            String path;
            if (drmFile.name.equals("..")) {
                path = FileUtil.getParentFolderPath(mCurrentPath);
            } else {
                path = mCurrentPath + drmFile.name + File.separator;
            }

            setCurrentPath(path, true, null);
        }
    }

    @Override
    public void onFolderMenu(ModelBase item) {
        final DrmFile drmFile = (DrmFile)item;

        SlideUpHelper.showMenu(drmFile.name, R.menu.menu_folder, false,
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        SlideUpHelper.hide();

                        int id = menuItem.getItemId();
                        if (id == R.id.action_open) {
                            String path = mCurrentPath + drmFile.name + File.separator;
                            setCurrentPath(path, true, null);

                        } else if (id == R.id.action_delete) {
                            final String path = drmFile.path + drmFile.name;

                            Confirm(R.string.delete_folder,
                                    R.string.msg_delete_folder_files,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();

                                            if (FileUtil.deleteFolder(path)) {
                                                showToast(R.string.msg_folder_deleted);
                                                onRefresh();
                                            }
                                        }
                                    });

                            return true;
                        }

                        return false;
                    }
                });
    }

    @Override
    public void onFileMenu(ModelBase item) {
        final DrmFile drmFile = (DrmFile)item;

        SlideUpHelper.showMenu(drmFile.name, R.menu.menu_mp3, false,
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        SlideUpHelper.hide();

                        int id = menuItem.getItemId();
                        if (id == R.id.action_listen) {
                            onItemClick(drmFile);

                        } else if (id == R.id.action_delete) {
                            final String path = drmFile.path + drmFile.name;

                            Confirm(R.string.delete_file,
                                    R.string.msg_delete_file,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();

                                            if (FileUtil.deleteFile(path)) {
                                                showToast(R.string.msg_file_deleted);
                                                onRefresh();
                                            }
                                        }
                                    });

                            return true;
                        }

                        return false;
                    }
                });
    }

    /**
     * 지정한 폴더의 모든 파일을 읽어들인다.
     * @param folderPath
     * @return
     */
    private boolean listFolderFiles(String folderPath) {
        File[] topLevelFiles = FileUtil.listFiles(folderPath);
        if (topLevelFiles == null) {
            showToast(R.string.drm_folder_access_denied);
            return false;
        }

        mDrmFileList.clear();

        ArrayList<String> folderNameList = new ArrayList<>();
        ArrayList<String> fileNameList = new ArrayList<>();

        for (File file : topLevelFiles) {
            if (file.isDirectory()) {
                folderNameList.add(file.getName());
            } else {
                if (file.getName().toLowerCase().endsWith(".mp3")) {
                    fileNameList.add(file.getName());
                }
            }
        }

        Collections.sort(folderNameList);
        Collections.sort(fileNameList);

        folderNameList.add(0, "..");

        for (String folderName : folderNameList) {
            int fileCnt = 0;
            int deletedCnt = 0;

            if (!folderName.equals("..")) {
                File[] files = FileUtil.listFiles(folderPath + "/" + folderName);
                for (File file : files) {
                    if (file.isFile()) {
                        MediaFile mediaFile = new MediaFile(mContext);
                        mediaFile.loadFile(folderPath + folderName + "/", file.getName());

                        if (mediaFile.isCPDRM() && mediaFile.getRemainTime() == 0) {
                            deletedCnt++;
                            FileUtil.deleteFile(mediaFile.getFullPath());
                        }

                        fileCnt++;
                    }
                }
            }

            if (!folderName.equals("..") && fileCnt == deletedCnt) {
                FileUtil.deleteFolder(folderPath + "/" + folderName);

            } else {
                DrmFile file = new DrmFile();
                file.path = folderPath;
                file.name = folderName;
                file.fileCnt = fileCnt - deletedCnt;
                file.expiredCnt = 0;
                file.type = "folder";

                mDrmFileList.add(file);
            }
        }

        mIsDrm = false;
        mCourseId = "";
        for (String fileName : fileNameList) {
            MediaFile mediaFile = new MediaFile(mContext);

            mediaFile.loadFile(folderPath, fileName);

            if (mediaFile.getRemainTime() == 0) {	// DRM 기간 만료된 파일 --> 바로삭제 (2018.02.02 han)
                FileUtil.deleteFile( mediaFile.getFullPath() );

            } else {
                DrmFile file = new DrmFile();
                file.path = folderPath;
                file.name = fileName;
                file.type = "mp3";
                file.isDrmFile = mediaFile.isCPDRM();
                file.duration = CPDRMUtil.DurationToTimeString((int)mediaFile.getDuration());
                file.remain = mediaFile.getRemainTime();
                file.courseId = mediaFile.getDRMPrefixAt(1);
                file.chapterId = mediaFile.getDRMPrefixAt(2);

                if (file.courseId != null) {
                    mCourseId = file.courseId;
                }

                if (file.isDrmFile) {
                    mIsDrm = true;
                }

                mDrmFileList.add(file);
            }
        }

        return true;
    }

    /**
     * 현재 폴더의 모든 파일들을 차례로 플레이함
     */
    public void playCurrentPath() {
        selectPlayableFile(0);
    }

    /**
     * 지정한 인덱스 이후의 플레이 가능한 첫번째 파일을 선택
     * @param index
     */
    public void selectPlayableFile(int index) {
        if (index < 0)
            return;

        for (int i = index; i < this.mDrmFileList.size(); i++) {
            DrmFile drmFile = this.mDrmFileList.get(i+1);

            if (drmFile.type.equals("mp3")) {	// MP3 파일선택
                if (drmFile.remain != 0) {
                    PlayList.resetPlayList();
                    PlayList.addFiles(mDrmFileList);

                    return;
                }
            }
        }
    }

    /**
     * 서버에서 받아온 Play Time 업데이트
     * @param rec
     * @return
     */
    public int updateDuration(Record rec) {
        int changedCnt = 0;

        for (DrmFile file : mDrmFileList) {
            String duration = rec.safeGet(file.chapterId);

            if (!duration.isEmpty()) {
                file.duration = duration;
                changedCnt++;
            }
        }

        return changedCnt;
    }

    /**
     * 서버에 앨범 트랙들의 Play Time을 요청
     * @author hansolo
     *
     */
    public class RequestDurationTask extends AsyncTask<String, Void, Record> {

        protected Record doInBackground(String... params) {
            return AudioRacApplication.requestDuration(params[0]);
        }

        protected void onPostExecute(Record result) {
            if (result == null)
                return;

            if (updateDuration(result) > 0) {
                mAdapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public boolean onBackPressed() {
        int historySize = mPathHistory.size();

        if (historySize > 1) {
            mPathHistory.remove(historySize - 1);

            String path = mPathHistory.get(historySize - 2);
            setCurrentPath(path, false, null);
            return true;

        } else {
            return false;
        }
    }
}
