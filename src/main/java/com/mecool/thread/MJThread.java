package com.mecool.thread;

import com.mecool.entity.MJEntity;
import com.mecool.service.MJExcelService;
import com.mecool.util.JedisUtil;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/8/24.
 */
public class MJThread extends Thread{

    private Vector<Map> pds = null;

    private MJEntity mjEntity = null;

    private MJExcelService mjExcelService = null;

    private CountDownLatch sCountDownLatch = null;

    private AtomicInteger atomicInteger;


    public MJThread(Vector<Map> pds, String mjEntityID,MJExcelService mjExcelService,CountDownLatch sCountDownLatch,AtomicInteger atomicInteger){
        this.pds = pds;
        this.mjEntity = JedisUtil.getMJEntity(mjEntityID);
        this.mjExcelService = mjExcelService;
        this.sCountDownLatch = sCountDownLatch;
        this.atomicInteger = atomicInteger;
    }

    private void method(){
        try {
            for (Map pd:pds) {
                mjExcelService.saveMj(pd,mjEntity);
                atomicInteger.decrementAndGet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sCountDownLatch.countDown();
        }
    }

    @Override
    public void run() {
        method();
    }
}
