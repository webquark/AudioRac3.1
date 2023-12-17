package com.conpo.audiorac.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.fragment.BarcodeFragment;
import com.conpo.audiorac.fragment.DownListFragment;
import com.conpo.audiorac.fragment.FragmentBase;
import com.conpo.audiorac.fragment.HomeViewFragment;
import com.conpo.audiorac.fragment.MyAudioFragment;
import com.conpo.audiorac.fragment.SettingsFragment;
import com.conpo.audiorac.helper.SlideUpHelper;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.model.DrmFile;
import com.conpo.audiorac.util.FileUtil;
import com.conpo.audiorac.util.Utils;
import com.conpo.audiorac.widget.SimpleProgressDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.client.android.CaptureActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * 메인 액티비티
 * @author hansolo
 *
 */
public class MainActivity extends ActivityBase
        implements View.OnClickListener,
                    BarcodeFragment.OnISBNSearchListener {

    private static final String LOG_TAG = "Main";

    private BottomNavigationView mNavView;
    private HomeViewFragment mHomeView = new HomeViewFragment();
    private DownListFragment mDownListView = new DownListFragment();
    private MyAudioFragment mMyAudioView = new MyAudioFragment();
    private BarcodeFragment mBarcodeView = new BarcodeFragment();
    private SettingsFragment mSettingsView = new SettingsFragment();

    /**
     * 네비게이션 탭 목록
     */
    private int[] mNavigationIds = {R.id.navigation_home, R.id.navigation_down_list, R.id.navigation_my_audio,
            R.id.navigation_barcode, R.id.navigation_settings};

    private FragmentBase mActiveView = mHomeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUIMode();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvSiteName = (TextView)findViewById(R.id.tv_site_name);
        tvSiteName.setText(LoginInfo.getSiteName());
        tvSiteName.setOnClickListener(this);

        ImageView ivLogo = findViewById(R.id.iv_logo);
        if (LoginInfo.getUIMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            Glide.with(this).load(R.drawable.title_logo).into(ivLogo);
        } else {
            Glide.with(this).load(R.drawable.title_logo_dark).into(ivLogo);
        }

        mNavView = findViewById(R.id.bottom_nav_view);
        mNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            return setNavigationItem(id);
        });

        setFragment(mHomeView, "1", 0);

        initializeSlideUpMenu();

        if (!checkAppPermission()) {
            /*
             * 앱 권한요청 액티비티 런치
             */
            launchPermission(new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
//                            Alert(R.string.msg_permission_nenied,
//                                    new AlertDialogCallback() {
//                                        @Override
//                                        public void onOKClick() {
//                                            MainActivity.this.finishAndRemoveTask();
//                                        }
//                                    });
                }
            });
        }
    }

    public void setUIMode() {
        LoginInfo.loadPreferences(this);

        if (LoginInfo.getUIMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public void restart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

     /**
     * 아이템 Id로 네비게이션 탭 선택
     * @param id 아이템 Id
     * @return
     */
    public boolean setNavigationItem(@NonNull int id) {
        if (SlideUpHelper.isVisible()) {
            return false;
        }

        if (id == R.id.navigation_home) {   // 홈
            if (mNavView.getSelectedItemId() == R.id.navigation_home) {
                mHomeView.gotoHome();
            } else {
                setFragment(mHomeView, "1", 0);
            }

        } else if (id == R.id.navigation_down_list) {   // 다운로드
            if (!checkAppPermission()) {
                Intent intent = new Intent(this, PermissionActivity.class);
                mDownListView.getPermissionLauncher().launch(intent);

            } else {
                setFragment(mDownListView, "2", 1);
                mDownListView.onRefresh();
            }

        } else if (id == R.id.navigation_my_audio) {       // 마이 오디오
            if (mNavView.getSelectedItemId() == R.id.navigation_my_audio) {
                mMyAudioView.gotoHome();
            } else {
                setFragment(mMyAudioView, "3", 2);
            }

        } else if (id == R.id.navigation_barcode) {     // 바코드
            mBarcodeView.setOnISBNSearchListener(MainActivity.this);
            
            if (!checkAppPermission()) {
                Intent intent = new Intent(this, PermissionActivity.class);
                mBarcodeView.getPermissionLauncher().launch(intent);

            } else {
                setFragment(mBarcodeView, "4", 3);
            }

        } else if (id == R.id.navigation_settings) {     // 설정
            setFragment(mSettingsView, "5", 4);
        }

        return false;
    }

    /**
     * 인덱스로 네비게이션 탭 선택
     * @param index
     * @return
     */
    public boolean setNavigationTab(int index) {
        return setNavigationItem( mNavigationIds[index] );
    }

    /**
     * 네비게이션 호스트 프래그먼트에 표시될 뷰 프래그먼트를 설정
     * @param fragment 뷰 프래그먼트
     * @param tag 뷰 구분 태그
     * @param position 탭 위치
     */
    public void setFragment(FragmentBase fragment, String tag, int position) {
        final FragmentManager fm = getSupportFragmentManager();
        String action = "";

        if (fragment.isAdded()) {
            fm.beginTransaction().hide(mActiveView).commit();
            fm.beginTransaction().show(fragment).commit();
            action = "show";

        } else {
            fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, fragment, tag).commit();
            action = "add";
        }

        mNavView.getMenu().getItem(position).setChecked(true);
        mActiveView = fragment;

        Log.d(LOG_TAG, String.format("TAB[%d]: %s: %s", position, action, fragment.getClass().toString()) );
    }

    /**
     * 슬라이드-업 메뉴 초기화
     */
    protected void initializeSlideUpMenu() {
        final ViewGroup viewGroup = findViewById(R.id.container);

        new SlideUpHelper().initialize(this, viewGroup);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.tv_site_name) {
            if (mNavView.getSelectedItemId() == R.id.navigation_home) {
                mHomeView.gotoHome();
            } else {
                mNavView.setSelectedItemId(R.id.navigation_home);
            }
        }
    }

    /**
     * 바코드 스캐너 런처
     */
    ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (!Utils.checkNetworkState(this)) {
                        showToast(R.string.msg_qr_network_err);
                        return;
                    }

                    // QR코드/바코드를 스캔한 결과 값을 가져옵니다.
                    if (result.getData() == null) {
                        showToast(R.string.msg_qr_invalid_barcode);
                        return;
                    }

                    String barcode = result.getData().getStringExtra("SCAN_RESULT");
                    mBarcodeView.setBarcode(barcode);
                }
            }
    );

    /**
     * ZXing Library의 QR코드 캡처 액티비티 실행하기
     */
    public void scanBarcode(String type) {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        scannerLauncher.launch(intent);
    }

    @Override
    public void showCourse(String csCode) {
        mNavView.setSelectedItemId(R.id.navigation_home);
        mHomeView.gotoCourse(csCode);
    }

    /**
     * 웹페이지의 오디오 플레이어 멈추기
     */
    public void stopWebAudioPlayer() {
        mHomeView.stopAudioPlayer();
        mMyAudioView.stopAudioPlayer();
    }

    public void stopStreaming() {
        mHomeView.gotoHome();
    }

    public void onFileDownload() {
        new RequestFileListTask().execute();
    }

    private void executeDownload(ArrayList<DrmFile> drmFiles) {
        if (drmFiles == null || drmFiles.size() == 0)
            return;

        if (!Utils.checkNetworkState(this)) {
            showToast(R.string.msg_download_network_err);
            return;
        }

        // 멀티파일들 중 하나라도 존재한다면 얼럿
        boolean isExistAlready = false;
        for (DrmFile drmFile : drmFiles) {
            File file = new File(drmFile.path);
            if (file.exists()) {
                isExistAlready = true;
                break;
            }
        }

        if (isExistAlready) {
            Confirm(R.string.msg_download_duplicate,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            new FileDownloadTask().execute(drmFiles);
                        }
                    });
        } else {
            // 앨범 폴더 생성
            FileUtil.makeFolder(drmFiles.get(0).albumPath);

            new FileDownloadTask().execute(drmFiles);
        }
    }

    /**
     * 서버에 로그인한 사용자의 다운로드할 파일이 있는지 확인한다
     * @author hansolo
     *
     */
    public class RequestFileListTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showProgress();
        }

        protected String doInBackground(Void... params) {
            return AudioRacApplication.requestFileList();
        }

        protected void onPostExecute(String result) {
            if (result == null) {
                showToast(R.string.msg_no_download_file);
                return;
            }

            String filelist = result;
            Log.i(Common.TAG, "FileContent:" + filelist);

            String[] arrFileList = filelist.split("\r\n");
            if (arrFileList.length <= 0) {
                hideProgress();
                return;
            }

            /*
             * 다운로드한 목록을 파싱하여 array에 저장
             */
            ArrayList<DrmFile> arrDrmFile = new ArrayList<DrmFile>();
            for (String str : arrFileList) {
                // http://www.audiorac.kr/drmmobile/conpo/2014/sosul/conpo_1001/mp3/01.mp3|봄·봄|01_오프닝
                String[] arrFileInfo = str.split("[|]");

                if (arrFileInfo != null && arrFileInfo.length == 3) {
                    String url = arrFileInfo[0];
                    String album = arrFileInfo[1];
                    String title = arrFileInfo[2];
                    String fileName = url.substring(url.lastIndexOf("/") + 1);
                    String albumPath = MainActivity.this.getAudioRacApplication().getDRMFolder();

                    if (album != null && !album.equals("")) {
                        albumPath += album + "/";
                    }

                    String localPath = albumPath + title + fileName;

                    DrmFile drmFile = new DrmFile();
                    drmFile.url = arrFileInfo[0];       // 다운로드 경로
                    drmFile.album = arrFileInfo[1];     // 앨범 타이틀명 - 저장폴더
                    drmFile.name = arrFileInfo[2];      // 트렉 타이틀
                    drmFile.path = localPath;           // 로컬 저장경로 (file pullpath)
                    drmFile.albumPath = albumPath;      // 로컬 앨범 폴더

                    arrDrmFile.add(drmFile);
                }
            }

            hideProgress();
            executeDownload(arrDrmFile);
        }
    }

    /**
     * 파일 다운로드 태스크
     * @author hansolo
     *
     */
    public class FileDownloadTask extends AsyncTask<ArrayList<DrmFile>, String, String> {

        private boolean mIsCanceled = false;
        private int mDownIndex = 0;
        private ArrayList<DrmFile> mArrDrmFile;

        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(getString(R.string.msg_download_proceeding), 1);
            setProgressDlgListener(new SimpleProgressDialog.ProgressDlgListener() {
                @Override
                public void onProgressStop() {
                    Confirm(R.string.msg_download_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mIsCanceled = true;
                                }
                            });
                }
            });
        }

        protected String doInBackground(ArrayList<DrmFile>...params) {
            mArrDrmFile = params[0];
            publishProgress("" + 0 );

            int count;

            for (DrmFile drmFile : mArrDrmFile) {
                String fileUrl = drmFile.url.trim();
                String localPath = drmFile.path.trim();

                if (mIsCanceled) {
                    cancel(true);
                    return null;
                }

                try {
                    URL url = new URL(fileUrl);
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    long lengthOfFile = connection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(), 1024 * 1000);
                    OutputStream output = new FileOutputStream(localPath);

                    long total = 0;
                    int pos = 0;
                    byte data[] = new byte[1024 * 1000];

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        pos = (int) ((total * 100) / lengthOfFile);
                        publishProgress("" + pos );
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();

                    data = null;
                    output = null;
                    input = null;

                } catch(Exception e) {
                    Log.i(Common.TAG, getString(R.string.msg_file_download_fail) + e.getMessage());
                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            int pos = Integer.parseInt(progress[0]);
            setProgressPos(pos);

            if (pos == 100) {
                mDownIndex++;
            }

            setProgressMessage( getString(R.string.msg_file_download_cnt, mDownIndex+1, mArrDrmFile.size()) );
        }

        protected void onPostExecute(String file_url) {
            hideProgress();

            showToast(getString(R.string.msg_file_download_complete,  mArrDrmFile.size()));

            /*
             * 플레이 리스트 세팅 및 플레이
             */
            onFileDownloadComplete(mArrDrmFile);
        }

        protected void onCancelled() {
            hideProgress();
        }
    }

    protected void onFileDownloadComplete(ArrayList<DrmFile> arrDrmFile) {
        mNavView.setSelectedItemId(R.id.navigation_down_list);
        mDownListView.setCurrentPath(arrDrmFile.get(0).albumPath, true,
                new DownListFragment.OnListCompleteListener() {
                    @Override
                    public void onListComplete() {
                        mDownListView.playCurrentPath();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (SlideUpHelper.isVisible() && !SlideUpHelper.isGlobal()) {
            SlideUpHelper.hide();
            return;
        }

        if (mActiveView.onBackPressed()) {
            return;
        } else {
            if (mActiveView != mHomeView) {
                mNavView.setSelectedItemId(R.id.navigation_home);
                mActiveView = mHomeView;
                return;
            }
        }

        SlideUpHelper.showMenu(getString(R.string.msg_exit_app), R.menu.menu_app, true,
                menuItem -> {
                    int id = menuItem.getItemId();

                    SlideUpHelper.hide();

                    if (id == R.id.action_cancel_exit) {
                        return true;

                    } else if (id == R.id.action_exit_app) {
                        if (mSettingsView != null) {
                            mSettingsView.cancelAlarm();

                            LoginInfo.setAlarm(0, 0, false);
                            LoginInfo.savePreferences(this);
                        }
                        MainActivity.this.finishAndRemoveTask();
                    }

                    return false;
                });

//        if (CPDRMCloseEvent.Handler(this)) {
//            if (mSettingsView != null) {
//                mSettingsView.cancelAlarm();
//
//                LoginInfo.setAlarm(0, 0, false);
//                LoginInfo.savePreferences(this);
//            }
//
//            finish();
//        }
    }
}
