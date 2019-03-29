package com.mecool.entity;

import com.mecool.util.PageData;

import java.util.List;

/**
 * Created by Administrator on 2018/5/2.
 */
public class Cooperation {

    //全局JedisID
    private String jedisId;
    //emppk
    private String empPk;
    //头数据
    private List<String> excelHeard;
    //列具体信息
    private List<PageData> colHeaders;

}
