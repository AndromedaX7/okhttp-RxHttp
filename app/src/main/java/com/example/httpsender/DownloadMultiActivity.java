package com.example.httpsender;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.httpsender.DownloadMultiAdapter.OnItemClickListener;
import com.example.httpsender.entity.DownloadTask;
import com.example.httpsender.vm.MultiTaskDownloader;

import java.util.ArrayList;

/**
 * 多任务下载
 * User: ljx
 * Date: 2019-06-07
 * Time: 11:02
 */
public class DownloadMultiActivity extends ToolBarActivity implements OnItemClickListener<DownloadTask> {


    private DownloadMultiAdapter mAdapter;

    private String[] downloadUrl = {
        "http://update.9158.com/miaolive/Miaolive.apk",//喵播
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk",//探探
        "https://o8g2z2sa4.qnssl.com/android/momo_8.18.5_c1.apk",//陌陌
        "http://s9.pstatp.com/package/apk/aweme/app_aweGW_v6.6.0_2905d5c.apk"//抖音
    };

    private MultiTaskDownloader mMultiTaskDownloader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_multi_activity);

        ArrayList<DownloadTask> allTask = new ArrayList<>();  //所有下载任务
        for (int i = 0; i < 20; i++) {
            DownloadTask task = new DownloadTask(downloadUrl[i % downloadUrl.length]);
            task.setTaskId(i);
            task.setLocalPath(getExternalCacheDir() + "/" + i + ".apk");
            allTask.add(task);
        }
        mAdapter = new DownloadMultiAdapter(allTask);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setHasStableIds(true);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter);

        mMultiTaskDownloader = new ViewModelProvider(this).get(MultiTaskDownloader.class);
        mMultiTaskDownloader.addTasks(allTask);
        mMultiTaskDownloader.getAllLiveTask().observe(this, tasks -> {
            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download_all) {
            if ("全部下载".contentEquals(item.getTitle())) {
                mMultiTaskDownloader.startAllDownloadTask();
                item.setTitle("全部取消");
            } else if ("全部取消".contentEquals(item.getTitle())) {
                mMultiTaskDownloader.cancelAllTask();
                item.setTitle("全部下载");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, DownloadTask task, int position) {
        switch (view.getId()) {
            case R.id.bt_pause:
                int curState = task.getState();  //任务当前状态
                if (curState == MultiTaskDownloader.IDLE         //未开始->开始下载
                    || curState == MultiTaskDownloader.PAUSED    //暂停下载->继续下载
                    || curState == MultiTaskDownloader.CANCEL    //已取消->重新开始下载
                    || curState == MultiTaskDownloader.FAIL      //下载失败->重新下载
                ) {
                    mMultiTaskDownloader.download(task);
                } else if (curState == MultiTaskDownloader.WAITING) {       //等待中->取消下载
                    mMultiTaskDownloader.removeWaitTask(task);
                } else if (curState == MultiTaskDownloader.DOWNLOADING) {   //下载中->暂停下载
                    mMultiTaskDownloader.pauseTask(task);
                } else if (curState == MultiTaskDownloader.COMPLETED) {   //任务已完成
                    Tip.show("该任务已完成");
                }
                break;
        }
    }
}
