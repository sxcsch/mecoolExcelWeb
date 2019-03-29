package com.mecool.thread;

import com.mecool.entity.KDEntity;
import com.mecool.service.KDExcelService;
import com.mecool.util.JedisUtil;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2018/8/24.
 */
public class KDThread implements Runnable{

    private Vector<Map> pds = null;

    private KDEntity kdEntity = null;

    private KDExcelService kdExcelService = null;

    private CountDownLatch sCountDownLatch = null;


    public KDThread(Vector<Map> pds, String kdEntityID, KDExcelService kdExcelService, CountDownLatch sCountDownLatch){
        this.pds = pds;
        this.kdEntity = JedisUtil.getKDEntity(kdEntityID);
        this.kdExcelService = kdExcelService;
        this.sCountDownLatch = sCountDownLatch;
    }

    private  void method(){
        try {
            for (Map pd:pds) {
                try {
                    kdExcelService.saveKd(pd,kdEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

        }
    }

    @Override
    public void run() {
        method();
        sCountDownLatch.countDown();
    }
}
