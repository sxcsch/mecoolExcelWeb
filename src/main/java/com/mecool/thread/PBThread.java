package com.mecool.thread;

import com.mecool.entity.PBEntity;
import com.mecool.service.PBExcelService;
import com.mecool.util.JedisUtil;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2018/8/24.
 */
public class PBThread implements Runnable{

    private Vector<Map> pds = null;

    private PBEntity pbEntity = null;

    private PBExcelService pbExcelService = null;

    private CountDownLatch sCountDownLatch = null;


    public PBThread(Vector<Map> pds, String pbEntityID, PBExcelService pbExcelService, CountDownLatch sCountDownLatch){
        this.pds = pds;
        this.pbEntity = JedisUtil.getPBEntity(pbEntityID);
        this.pbExcelService = pbExcelService;
        this.sCountDownLatch = sCountDownLatch;
    }

    private  void method(){
        try {
            for (Map pd:pds) {
                pbExcelService.savePb(pd,pbEntity);
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
