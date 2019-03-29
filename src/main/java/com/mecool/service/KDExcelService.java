package com.mecool.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mecool.dao.DaoSupport;
import com.mecool.entity.KDEntity;
import com.mecool.util.ConstantsMecool;
import com.mecool.util.JedisUtil;
import com.mecool.util.MecoolUtil;
import com.mecool.util.PageData;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2/7/2018.
 */
@Service("kdExcelService")
public class KDExcelService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    /*
  * 通过项目ID来寻找项目信息
  */
    public PageData findProjectById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findProjectById", pd);
    }
    /*
	* 通过项目ID来寻找头信息
	*/
    public List<PageData> findInfoHeaderByProjectIdOrderByColumnOrder(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("HeardExcelMapper.findInfoHeaderByProjectIdOrderByColumnOrder", pd);
    }
    /*
	* 通过项目ID来寻找头信息
	*/
    public List<PageData> getProjectSellinStoreOpenList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("ChannelExcelMapper.getProjectSellinStoreOpenList", pd);
    }
    /*
        * 通过项目ID来寻找头信息
        */
    public List<PageData> getSellinExecdateList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("KDExcelMapper.getSellinExecdateList", pd);
    }
    /*
  * 通过项目ID来寻找门店信息
  */
    public List<PageData> getChannelList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("ChannelExcelMapper.getChannelList", pd);
    }
    /*
 * 通过项目ID/门店/sn查询卖进信息
 */
    public List<PageData> findByMJinfoByProjectChannelSn(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findByMJinfoByProjectChannelSn", pd);
    }

    public void savePriectSellinStoreOpen(PageData pd) throws Exception {
        dao.save("KDExcelMapper.savePriectSellinStoreOpen",pd);
    }

    /*
   * 通过emp_pk寻找用户
   */
    public PageData findDmUserByEmppk(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findDmUserByEmppk", pd);
    }

    public void savePrjectSellinHisDetail(PageData pd) throws Exception {
        dao.save("KDExcelMapper.savePrjectSellinHisDetail",pd);
    }

    public void saveKd(Map mp, KDEntity kdEntity)throws Exception{
        PageData query = new PageData();
        query.put("EMP_PK",kdEntity.getEmpPk());
        PageData dmUser = findDmUserByEmppk(query);
        if (mp == null) {
            return;
        }
        Date today = MecoolUtil.getDateNoTime(new Date());
        Map<String, Date> ssm =  MecoolUtil.parseDaysStr(mp.get("PLAN_STORE_OPEN_COUNT").toString().trim());
        // 获得卖进
        PageData channel = null;
        try {
            PageData pd = new PageData();
            pd.put("CHANNEL_CODE",mp.get("CHANNEL_SYNC_ID").toString());
            List<PageData> channelList = getChannelList(pd);
            if (channelList != null && channelList.size() > 0) {
                channel = channelList.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException runtimeException = new RuntimeException();
            runtimeException.printStackTrace();
            return;
        }
        long sn = 0l;
        try {
            sn = (new BigDecimal(mp.get("SCHEDULE_NUM").toString())).longValue();
        } catch (Exception e) {

        }
        List<PageData> psi0 =null;
        if (null!=channel){
            query.put("SCHEDULE_NUM",sn);
            query.put("CHANNEL_SYNC_ID",channel.get("ID"));
            query.put("PROJECT_ID",kdEntity.getProjectId());
            psi0 = findByMJinfoByProjectChannelSn(query);
        }

        if (psi0==null||psi0.size()==0) {
            return;
        } else {
            List<PageData> oldPssoList = getProjectSellinStoreOpenList(psi0.get(0));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfs = new SimpleDateFormat("yyyy/MM/dd");
            if (oldPssoList != null && oldPssoList.size() > 0) {
                for (PageData projectSellinStoreOpen : oldPssoList) {
                    try {
                        if (projectSellinStoreOpen == null || projectSellinStoreOpen.get("SO_PLAN_OPEN_TIME") == null
                                || sdfs.parse(projectSellinStoreOpen.get("SO_PLAN_OPEN_TIME").toString()).before(today)) {
                            continue;
                        }
                    } catch (ParseException e) {
                        if (projectSellinStoreOpen == null || projectSellinStoreOpen.get("SO_PLAN_OPEN_TIME") == null
                                || sdf.parse(projectSellinStoreOpen.get("SO_PLAN_OPEN_TIME").toString()).before(today)) {
                            continue;
                        }
                    }
                    String d = null;
                    try {
                        Date so = sdf.parse(projectSellinStoreOpen.get("SO_PLAN_OPEN_TIME").toString());
                        d = ConstantsMecool.SIMPLE_DATE_FORMAT.format(so);
                    } catch (Exception e) {
                        d = ConstantsMecool.SIMPLE_DATE_FORMAT1.format(sdfs.parse(projectSellinStoreOpen.get("SO_PLAN_OPEN_TIME").toString()));
                    }
                    if (ssm.get(d) != null) {
                        ssm.remove(d);
                    }
                }
            }
            List<PageData> psedList = getSellinExecdateList(psi0.get(0));
            Collection<Date> c = ssm.values();
            Iterator<Date> it = c.iterator();
            for (; it.hasNext();) {
                Date d = it.next();
                if (d == null || d.before(today)) {
                    continue;
                }
                // 没有执行日不加开档日
                boolean isExec = false;
                if (psedList != null && psedList.size() > 0) {
                    for (PageData psed : psedList) {
                        if (psed == null || psed.get("EXEC_DATE") == null) {
                            continue;
                        }
                        if (DateUtils.isSameDay(d, sdf.parse(psed.get("EXEC_DATE").toString()))) {
                            isExec = true;
                            break;
                        }
                    }
                }
                if (!isExec) {
                    continue;
                }
                PageData DM_PROJECT_SELLIN_STORE_OPEN = new PageData();
                DM_PROJECT_SELLIN_STORE_OPEN.put("SO_CREATE_TIME",new Date());
                //TODO
                DM_PROJECT_SELLIN_STORE_OPEN.put("SO_CREATE_USER",dmUser.get("ID"));
                DM_PROJECT_SELLIN_STORE_OPEN.put("SO_PLAN_OPEN_TIME",d);
                DM_PROJECT_SELLIN_STORE_OPEN.put("PROJECT_SELLIN_INFO_ID",psi0.get(0).get("ID").toString());
                DM_PROJECT_SELLIN_STORE_OPEN.put("SO_STATUS",ConstantsMecool.StoreOpenStatus.UN_OPEN.getType());
                try{
                    savePriectSellinStoreOpen(DM_PROJECT_SELLIN_STORE_OPEN);
                }catch (Exception e){
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
            }
            //获得买进信息
            PageData psinfo = psi0.get(0);
            if(psinfo != null){
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss:SS").create();
                String str = "";
                try {
                    str = gson.toJson(psinfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
                //保存数据
                PageData json = new PageData();
                json.put("CREATE_TIME",new Date());
                json.put("PROJECT_SELLIN_INFO_ID",psi0.get(0).get("ID"));
                json.put("SELLIN_INFO",str);
                json.put("UPDATE_USER",dmUser.get("ID"));
                json.put("REMARKS","");
                json.put("OPTION_FLAG",ConstantsMecool.SellinOptionFlag.PAGE_STOREOPEN_LOAD.getType());
                try {
                    savePrjectSellinHisDetail(json);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
            }
            JedisUtil.delResource(mp,kdEntity.getJedisId());
        }
    }
}
