package com.mecool.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mecool.dao.DaoSupport;
import com.mecool.entity.MJEntity;
import com.mecool.util.ConstantsMecool;
import com.mecool.util.JedisUtil;
import com.mecool.util.MecoolUtil;
import com.mecool.util.PageData;
import oracle.sql.TIMESTAMP;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2/7/2018.
 */
@Service("mjExcelService")
@SuppressWarnings("unused")
public class MJExcelService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    /*
	* 通过项目ID来寻找头信息
	*/
    public List<PageData> findInfoHeaderByProjectIdOrderByColumnOrder(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("HeardExcelMapper.findInfoHeaderByProjectIdOrderByColumnOrder", pd);
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
    /*
  * 通过
  */
    public List<PageData> getProjectSellinExecdateList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.getProjectSellinExecdateList", pd);
    }
    /*
  * 通过
  */
    public List<PageData> getProjectSellinExecdateListByInfoExec(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.getProjectSellinExecdateListByInfoExec", pd);
    }
    /*
   * 通过项目ID来寻找项目信息
   */
    public PageData findProjectById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findProjectById", pd);
    }
    /*
   * 通过emp_pk寻找用户
   */
    public PageData findDmUserByEmppk(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findDmUserByEmppk", pd);
    }
    /*
   * 通过项目ID来寻找项目信息
   */
    public List<PageData> findByMJinfoByProject(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findByMJinfoByProject", pd);
    }
    /*
   * 通过项目ID来寻找项目信息
   */
    public PageData findProjectSellinInfoById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findProjectSellinInfoById", pd);
    }
    /*
  * 通过
  */
    public PageData getOnUsePosCountByCode(PageData pd)throws Exception {
        return (PageData)dao.findForObject("ChannelExcelMapper.getOnUsePosCountByCode", pd);
    }

    /*
  * 通过项目ID来统计count
  */
    public PageData getExcelHeardCountById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("HeardExcelMapper.getExcelHeardCountById", pd);
    }
    /*
  * 通过项目ID来统计count
  */
    public PageData getExcelHeardSellinInfoCountById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("HeardExcelMapper.getExcelHeardSellinInfoCountById", pd);
    }
    /*
  * 通过项目ID来统计count
  */
    public PageData findDmProjectStoreByProjectAndChannel(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findDmProjectStoreByProjectAndChannel", pd);
    }
    /*
  * 通过项目ID来统计count
  */
    public List<PageData> findProjectStoreByProjectAndChannel(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findProjectStoreByProjectAndChannel", pd);
    }
    /*
  * 是否存在执行日
  */
    public List<PageData> findByProjectSellinExecdateAndChannel(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findByProjectSellinExecdateAndChannel", pd);
    }
    /*
  * 通过
  */
    public PageData findMdEmpByEmpCode(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findMdEmpByEmpCode", pd);
    }
    /*
  * 通过
  */
    public PageData findMdEmpByEmpCodeIs(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findMdEmpByEmpCodeIs", pd);
    }
    /*
  * 通过
  */
    public PageData findHeaderById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("HeardExcelMapper.findHeaderById", pd);
    }
    /*
  * 通过
  */
    public PageData findProjectEmployeeByProjectAndEmpId(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findProjectEmployeeByProjectAndEmpId", pd);
    }
    /*
  * 通过
  */
    public PageData getSalesScheduleCalendarByProjectId(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.getSalesScheduleCalendarByProjectId", pd);
    }

    /*
    * 通过项目ID来删除数据
    */
    public void deleteByProject(PageData pd)throws Exception {
        dao.delete("HeardExcelMapper.deleteByProject", pd);
    }
    /*
        * 通过保存头
        */
    public void saveHeader(PageData pd)throws Exception {
        dao.save("HeardExcelMapper.saveHeader", pd);
    }
    /*
        * 通过保存头
        */
    public void saveProjectEmployee(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveProjectEmployee", pd);
    }
    /*
        * 通过
        */
    public void saveDmProjectEmployeeStore(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveDmProjectEmployeeStore", pd);
    }
    /*
       * 通过
       */
    public void saveProjectSellinExecdate(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveProjectSellinExecdate", pd);
    }
    /*
    * 保存开店信息
    */
    public void saveProjectStore(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveProjectStore", pd);
    }
    /*
    * 通过保存开店信息
    */
    public void saveProjectSellinStore(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveProjectSellinStore", pd);
    }
    /*
    * 通过保存开店信息
    */
    public void ProjectSellinStoreOpenRemove(PageData pd)throws Exception {
        dao.delete("MJExcelMapper.ProjectSellinStoreOpenRemove", pd);
    }
    /*
    * 通过保存头
    */
    public void saveSellinHistoryDetail(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveSellinHistoryDetail", pd);
    }
    /*
        * 通过保存
        */
    public void saveSellinInfoLog(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveSellinInfoLog", pd);
    }

    /*
        * 通过保存
        */
    public void saveProjectSellinInfo(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveProjectSellinInfo", pd);
    }
    /*
            * 通过保存
            */
    public void saveSellinHistory(PageData pd)throws Exception {
        dao.save("MJExcelMapper.saveSellinHistory", pd);
    }
    /*
        *
        */
    public void setJobStepByProcedure(PageData pd)throws Exception {
        dao.findForObject("HeardExcelMapper.setJobStepByProcedure", pd);
    }
    /*
        *
        */
    public void updateProjectSellinInfo(PageData pd)throws Exception {
        dao.update("MJExcelMapper.updateProjectSellinInfo", pd);
    }

    /*
   *
   */
    public List<PageData> findProjectEmployeeByProject(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("EmployeeExcelMapper.findProjectEmployeeByProject", pd);
    }
    /*
   *
   */
    public List<PageData> findSellinHistory(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findSellinHistory", pd);
    }

    public void saveMj(Map mp, MJEntity mjEntity)throws Exception{
        PageData query = new PageData();
        PageData channel = null;
        PageData newPage = new PageData();
        query.put("EMP_PK",mjEntity.getEmpPk());
        PageData dmUser = findDmUserByEmppk(query);
        query.put("CHANNEL_CODE",mp.get("CHANNEL_SYNC_ID").toString());
        List<PageData> channelList = getChannelList(query);
        if (channelList != null && channelList.size() > 0) {
            channel = channelList.get(0);
        }
        query.put("SCHEDULE_NUM",mp.get("SCHEDULE_NUM"));
        query.put("CHANNEL_SYNC_ID",channel.get("ID"));
        query.put("PROJECT_ID",mjEntity.getProjectId());

        List<PageData> fmbpc = findByMJinfoByProjectChannelSn(query);
        try {

            Timestamp PLAN_START_TIME = null;
            Timestamp PLAN_FINISHED_TIME = null;
//            if (mp.get("EXEC_DATE_TIME")!=null&&!mp.get("EXEC_DATE_TIME").toString().equals("")){
//                Map<String, Date> sw = new HashMap<String, Date>();
//                if (fmbpc!=null&&fmbpc.size()>0){
//                    for (PageData ppd:fmbpc) {
//                        if (ppd.get("EXEC_DATE_TIME")!=null&&!ppd.get("EXEC_DATE_TIME").toString().equals("")){
//                            Map<String, Date> ssm = MecoolUtil.parseDaysStr(ppd.get("EXEC_DATE_TIME").toString());
//                            sw.putAll(ssm);
//                        }
//                    }
//                }
//                Map<String, Date> ssm = MecoolUtil.parseDaysStr(mp.get("EXEC_DATE_TIME").toString());
//                sw.putAll(ssm);
//                //最大时间
//                Date start_plan = null;
//                //最小时间
//                Date end_plan = null;
//                int count = 1;
//                for (String key : sw.keySet()) {
//                    Date d = sw.get(key);
//                    if (d == null) {
//                        continue;
//                    }else{
//                        if (count==1){
//                            start_plan = d;
//                            end_plan =d;
//                            count++;
//                        }else{
//
//                            if (start_plan.before(d)) {
//                                start_plan = d;
//                            }else if (d.before(end_plan)){
//                                end_plan = d;
//                            }
//
//                        }
//
//                    }
//                }
//                newPage.put("PLAN_FINISHED_TIME",start_plan);
//                newPage.put("PLAN_START_TIME",end_plan);
//            }

            try {
                PLAN_START_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_START_TIME").toString()).getTime());
                PLAN_FINISHED_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).getTime());
            } catch (ParseException e) {
                PLAN_START_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_START_TIME").toString()).getTime());
                PLAN_FINISHED_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).getTime());
            }
            mp.put("CHANNEL_SYNC_ID",channel.get("ID").toString());
            mp.put("PROJECT_ID",mjEntity.getProjectId());
            mp.put("STATUS",ConstantsMecool.SellinStatus.UN_SELLIN.getType()+"");
            mp.put("EXEC_STATUS",ConstantsMecool.SellinExecuteStatus.NEVER_EXEC.getType()+"");
            mp.put("AUDIT_STATUS",ConstantsMecool.SellinAuditStatus.UN_AUDIT.getType()+"");
            Iterator it = mp.entrySet().iterator();

            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                Object obj = entry.getValue();
                if (obj!=null&&!obj.toString().equals("")&&!obj.toString().equals("null")){
                    try {
                        newPage.put(entry.getKey(),new BigDecimal(obj.toString()));
                    }catch (Exception e){
                        newPage.put(entry.getKey(),obj.toString());
                    }
                }else{
                    it.remove();
                }
            }
            Date date = new Date();
            newPage.put("OLD_PLAN_FINISHED_TIME",PLAN_FINISHED_TIME);
            newPage.put("OLD_PLAN_START_TIME",PLAN_START_TIME);
            if(mp.get("PLAN_EXHIBIT_START_TIME")!=null){
                Timestamp PLAN_EXHIBIT_START_TIME = null;
                try {
                    PLAN_EXHIBIT_START_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("PLAN_EXHIBIT_START_TIME").toString()).getTime());
                } catch (ParseException e) {
                    PLAN_EXHIBIT_START_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("PLAN_EXHIBIT_START_TIME").toString()).getTime());
                }
                newPage.put("PLAN_EXHIBIT_START_TIME",PLAN_EXHIBIT_START_TIME);
            }
            if(mp.get("PLAN_EXHIBIT_END_TIME")!=null){
                Timestamp PLAN_EXHIBIT_END_TIME = null;
                try {
                    PLAN_EXHIBIT_END_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("PLAN_EXHIBIT_END_TIME").toString()).getTime());
                } catch (ParseException e) {
                    PLAN_EXHIBIT_END_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("PLAN_EXHIBIT_END_TIME").toString()).getTime());
                }
                newPage.put("PLAN_EXHIBIT_END_TIME",PLAN_EXHIBIT_END_TIME);
            }
            if(mp.get("OLD_PLAN_START_TIME")!=null){
                Timestamp OLD_PLAN_START_TIME = null;
                try {
                    OLD_PLAN_START_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_START_TIME").toString()).getTime());
                } catch (ParseException e) {
                    OLD_PLAN_START_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_START_TIME").toString()).getTime());
                }
                newPage.put("OLD_PLAN_START_TIME",OLD_PLAN_START_TIME);
            }
            if(mp.get("OLD_PLAN_FINISHED_TIME")!=null){
                Timestamp OLD_PLAN_FINISHED_TIME = null;
                try {
                    OLD_PLAN_FINISHED_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).getTime());
                } catch (ParseException e) {
                    OLD_PLAN_FINISHED_TIME = new Timestamp(ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).getTime());
                }
                newPage.put("OLD_PLAN_FINISHED_TIME",OLD_PLAN_FINISHED_TIME);
            }
            newPage.put("CREATE_TIME",new Timestamp(date.getTime()));
            newPage.put("CREATE_USER",mjEntity.getEmpPk());

//            mp.put("SCHEDULE_NUM",sn);

            if(fmbpc!=null&&fmbpc.size()>0){
                PageData news = fmbpc.get(0);
                if (news.get("OLD_PLAN_FINISHED_TIME")!=null&&news.get("OLD_PLAN_START_TIME")!=null){
                    //原计划开始结束时间
                    newPage.remove("OLD_PLAN_FINISHED_TIME");
                    newPage.remove("OLD_PLAN_START_TIME");
                    //卖进状态
                    newPage.remove("STATUS");
                    //执行状态
                    newPage.remove("EXEC_STATUS");
                    //审核状态、审核人、审核时间
                    newPage.remove("AUDIT_STATUS");
                    newPage.remove("AUDI_USER_ID");
                    newPage.remove("AUDI_DATE");
                    //创建人、创建时间
                    newPage.remove("CREATE_USER");
                    newPage.remove("CREATE_TIME");
                }
                newPage.put("ID",news.get("ID"));
                try {
                    updateProjectSellinInfo(newPage);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
            }else {
                //保存
                try {
                    saveProjectSellinInfo(newPage);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
            }

            //保存日志
            PageData sellinInfoLog = new PageData();
            Iterator iterator = newPage.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                sellinInfoLog.put(entry.getKey(),entry.getValue());
            }
            sellinInfoLog.put("SOURCE_ID",newPage.get("ID").toString());
            sellinInfoLog.put("OPTION_FLAG",ConstantsMecool.DataOptionFlag.PAGE_ADD.getType());
            sellinInfoLog.put("UPDATE_TIME",new Timestamp(new Date().getTime()));
            sellinInfoLog.put("UPDATE_USER",dmUser.get("ID").toString());
            sellinInfoLog.put("CREATE_USER",mjEntity.getEmpPk());
            try {
                saveSellinInfoLog(sellinInfoLog);
            } catch (Exception e) {
                e.printStackTrace();
                RuntimeException runtimeException = new RuntimeException();
                runtimeException.printStackTrace();
                return;
            }
            PageData sellinHistory = new PageData();
            List<PageData> listSellinHistory = findSellinHistory(newPage);
            if (listSellinHistory == null || listSellinHistory.size() < 1) {
                sellinHistory.put("CHANGE_TIMES",1);
            } else {
                sellinHistory.put("CHANGE_TIMES",Integer.parseInt(listSellinHistory.get(listSellinHistory.size() -1).get("CHANGE_TIMES").toString())+1);
            }


            sellinHistory.put("CREATE_TIME",new Timestamp(new Date().getTime()));
            sellinHistory.put("PROJECT_SELLIN_INFO_LOG_ID",sellinInfoLog.get("ID").toString());
            // 保存DM_SELLIN_HISTORY
            saveSellinHistory(sellinHistory);
            writeLogJson(Long.parseLong(newPage.get("ID").toString()),0l, "", ConstantsMecool.SellinOptionFlag.PAGE_SELLIN_LOAD_ADD.getType());
        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException runtimeException = new RuntimeException();
            runtimeException.printStackTrace();
            return;
        }
        query.put("PROJECT",mjEntity.getProjectId());
        query.put("CHANNEL",channel.get("ID").toString());
        query.put("CHANNEL_CODE",channel.get("CHANNEL_CODE").toString());
        //project, channel
        PageData ps = findDmProjectStoreByProjectAndChannel(query);
        if (ps == null) {
            PageData psBean = new PageData();
            psBean.put("CHANNEL",channel.get("ID").toString());
            psBean.put("CHANNEL_CODE",channel.get("CHANNEL_CODE").toString());
            psBean.put("PROJECT",mjEntity.getProjectId());
            try {
                saveProjectStore(psBean);
            } catch (Exception e) {
                e.printStackTrace();
                RuntimeException runtimeException = new RuntimeException();
                runtimeException.printStackTrace();
                return;
            }
            ps = findDmProjectStoreByProjectAndChannel(query);
        }

        // DmProjectEmployee 人员
        query.put("PROJECT_ID",mjEntity.getProjectId());
        List<PageData> pe = findProjectEmployeeByProject(query);
        PageData peManager = null;
        if (pe!=null&&pe.size()>0){
            peManager = pe.get(0);
        }
        query.put("EMP_CODE",mp.get("AREA_MANAGER_CODE").toString());
        PageData mdemp = findMdEmpByEmpCodeIs(query);
        //区域负责人
        PageData peArea = null;
        if (mdemp!=null&&mdemp.size()>0){
            if (mp.get("AREA_MANAGER_CODE")!=null&&!mp.get("AREA_MANAGER_CODE").toString().equals("")){
                query.put("EMP_CODE",mdemp.get("EMP_CODE").toString());
                PageData emp = findMdEmpByEmpCodeIs(query);
                if (emp!=null&&emp.size()>0){
                    query.put("EMP_ID",emp.get("EMP_PK").toString());
                    peArea = findProjectEmployeeByProjectAndEmpId(query);
                    if (peArea == null) {
                        peArea = new PageData();
                        peArea.put("PROJECT",mjEntity.getProjectId());
                        peArea.put("EMP_ID",emp.get("EMP_PK").toString());
                        peArea.put("EMP_NAME",emp.get("EMP_NAME").toString());
                        peArea.put("CREATE_TIME",new Timestamp(new Date().getTime()));
                        peArea.put("ROLE",ConstantsMecool.UserRole.AREA_EMP.getCode());
                        if (peManager == null) {
                            peArea.put("RANK_TYPE",1);
                            peArea.put("TITLE_WITHIN_THE_PROJECT","项目负责人");
                        } else {
                            peArea.put("PARENT",peManager.get("ID").toString());
                        }
                        try {
                            saveProjectEmployee(peArea);
                        } catch (Exception e) {
                            e.printStackTrace();
                            RuntimeException runtimeException = new RuntimeException();
                            runtimeException.printStackTrace();
                            return;
                        }
                    }
                }
            }
        }

        // CityEmpPk
        // 修改部分
        query.put("EMP_CODE",mp.get("CITY_EMP_CODE").toString());
        PageData emp = findMdEmpByEmpCodeIs(query);
        PageData peCity = null;
        if (emp != null) {
            query.put("EMP_ID",emp.get("EMP_PK").toString());
            peCity = findProjectEmployeeByProjectAndEmpId(query);
            if (peCity == null) {
                peCity = new PageData();
                peCity.put("PROJECT",mjEntity.getProjectId());
                peCity.put("EMP_ID",emp.get("EMP_PK").toString());
                peCity.put("EMP_NAME",emp.get("EMP_NAME").toString());
                peCity.put("CREATE_TIME",new Timestamp(new Date().getTime()));
                peCity.put("ROLE",ConstantsMecool.UserRole.CITY_EMP.getCode());
                if (peArea == null) {
                    return;
                } else {
                    peCity.put("PARENT",peArea.get("ID").toString());
                }
                try {
                    saveProjectEmployee(peCity);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
            }
        }
//        // EmpPk
        query.put("EMP_CODE",mp.get("EMP_CODE").toString());
        PageData mdEmp = findMdEmpByEmpCode(query);
        if (mdEmp != null) {
            query.put("EMP_ID",mdEmp.get("EMP_PK").toString());
            PageData pes = findProjectEmployeeByProjectAndEmpId(query);
            if (pes == null) {
                pes = new PageData();
                pes.put("PROJECT",mjEntity.getProjectId());
                pes.put("EMP_ID",mdEmp.get("EMP_PK").toString());
                pes.put("EMP_NAME",mdEmp.get("EMP_NAME").toString());
                pes.put("CREATE_TIME",new Timestamp(new Date().getTime()));
                pes.put("ROLE",ConstantsMecool.UserRole.STORE_EMP.getCode());
                if (peCity == null) {
                    return;
                } else {
                    pes.put("PARENT",peCity.get("ID").toString());
                }
                try {
                    saveProjectEmployee(pes);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
            }
            // DmProjectEmployeeStore 门店分配
            if (ps != null && ps.size()>0 && pes != null && pes.size()>0) {
                try {
                    PageData dmProjectEmployeeStore = new PageData();
                    BigDecimal PROJEMP_ID =  new BigDecimal(pes.get("ID").toString());
                    BigDecimal PROJSTORE_ID =  new BigDecimal(ps.get("ID").toString());
                    dmProjectEmployeeStore.put("PROJEMP_ID",PROJEMP_ID);
                    dmProjectEmployeeStore.put("PROJSTORE_ID",PROJSTORE_ID);
                    saveDmProjectEmployeeStore(dmProjectEmployeeStore);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (emp!=null){
                query.put("EMP_ID",emp.get("EMP_PK").toString());
                PageData pes = findProjectEmployeeByProjectAndEmpId(query);
                if (ps != null &&ps.size()>0 && pes != null&&pes.size()>0){
                    PageData dmProjectEmployeeStore = new PageData();
                    dmProjectEmployeeStore.put("PROJEMP_ID",new BigDecimal(pes.get("ID").toString()));
                    dmProjectEmployeeStore.put("PROJSTORE_ID",new BigDecimal(ps.get("ID").toString()));
                    try {
                        saveDmProjectEmployeeStore(dmProjectEmployeeStore);
                    } catch (Exception e) {
                        e.printStackTrace();
                        RuntimeException runtimeException = new RuntimeException();
                        runtimeException.printStackTrace();
                        return;
                    }
                }
            }
        }
        Map<String, Date> ssm = null;
        if (mp.get("EXEC_DATE_TIME")!=null){
            String daysStr = mp.get("EXEC_DATE_TIME").toString();
            ssm = MecoolUtil.parseDaysStr(daysStr);
            Map<String, Date> ssmTmp = new HashMap<String, Date>(ssm);
            for (String key : ssmTmp.keySet()) {
                Date d = ssmTmp.get(key);
                if (d == null) {
                    continue;
                }
            }
        }

        query.put("PROJECT_SELLIN_INFO_ID",newPage.get("ID").toString());

        List<PageData> oldDate = getProjectSellinExecdateList(query);
        if (oldDate != null && oldDate.size() > 0) {
            for (PageData execDate : oldDate) {
                String d = null;
                TIMESTAMP timestamp = (TIMESTAMP) execDate.get("EXEC_DATE");
                Date EXEC_DATE = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(timestamp.dateValue().toString());
                d = ConstantsMecool.SIMPLE_DATE_FORMAT1.format(EXEC_DATE);


                if (ssm!=null){
                    ssm.remove(d);
                }
            }
        }
        if (ssm!=null&&ssm.size()>0){
            Collection<Date> c = ssm.values();
            PageData projectSellinExecdate = null;
            if (CollectionUtils.isNotEmpty(c)) {
                for (Date date : c) {
                    projectSellinExecdate = new PageData();
                    projectSellinExecdate.put("PROJECT_SELLIN_INFO_ID",newPage.get("ID").toString());
                    projectSellinExecdate.put("EXEC_DATE",date);
                    try {
                        if (date.before(new Date())){
                           continue;
                        }
                        saveProjectSellinExecdate(projectSellinExecdate);
                    } catch (Exception e) {
                        e.printStackTrace();
                        RuntimeException runtimeException = new RuntimeException();
                        runtimeException.printStackTrace();
                    }
                }
            }
        }
        writeLogJson(Long.parseLong(newPage.get("ID").toString()), 0l, "", ConstantsMecool.SellinOptionFlag.PAGE_SALES_MODIFY.getType());
        query.put("PROJECT_SELLIN_INFO_ID",newPage.get("ID").toString());
        List<PageData> oldDates = getProjectSellinExecdateListByInfoExec(query);
        if (oldDates != null && oldDates.size() > 0) {
            PageData ed1 = oldDates.get(0);
            newPage.put("PLAN_START_TIME",ed1.get("EXEC_DATE"));
            PageData ed2 = oldDates.get(oldDates.size() - 1);
            newPage.put("PLAN_FINISHED_TIME",ed2.get("EXEC_DATE"));
            try {
                updateProjectSellinInfo(newPage);
            } catch (Exception e) {
                e.printStackTrace();
                RuntimeException runtimeException = new RuntimeException();
                runtimeException.printStackTrace();
                return;
            }
        }
        //项目门店开店记录
        query.put("PROJECT",mjEntity.getProjectId());
        query.put("CHANNEL",channel.get("ID").toString());
        List<PageData> pssoList = findProjectStoreByProjectAndChannel(query);
        //项目门店执行日
        query.put("PROJECT_SELLIN_INFO_ID",newPage.get("ID").toString());
        List<PageData> pseList = findByProjectSellinExecdateAndChannel(query);
        // Get ProjectSellinStoreOpen of the channel.
        if (pssoList == null || pssoList.size() < 1) {
            if (pseList != null && pseList.size() > 0) {//没有开店记录, 但有门店执行日 ,则生成卖进开店日
                for (PageData ed : pseList) {
                    if (ed == null || ed.get("EXEC_DATE") == null) {
                        continue;
                    }
                        PageData storeOpen = new PageData();
                        storeOpen.put("PROJECT_SELLIN_INFO_ID",newPage.get("ID").toString());
                        storeOpen.put("SO_CREATE_USER",dmUser.get("ID").toString());
                        storeOpen.put("SO_STATUS",ConstantsMecool.StoreOpenStatus.UN_OPEN.getType());
                        storeOpen.put("SO_PLAN_OPEN_TIME",ed.get("EXEC_DATE"));
                        storeOpen.put("SO_CREATE_TIME",new Timestamp(new Date().getTime()));
                        try {
                            if(fmbpc!=null&&fmbpc.size()>0){

                            }else{
                                //ProjectSellinStoreOpenRemove(storeOpen);
                                saveProjectSellinStore(storeOpen);
                                break;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            RuntimeException runtimeException = new RuntimeException();
                            runtimeException.printStackTrace();
                            return;
                        }

                }
            } else { //没有开店记录, 没有门店执行日, 忽略.
                // ignore
            }
            delResource(1,mp,mjEntity.getJedisId());
            return;
        } else {
            // Check the status of ProjectSellinStoreOpen.
            boolean isOpened = false;
            for (PageData projectSellinStoreOpen : pssoList) {
                if (projectSellinStoreOpen != null
                        && (ConstantsMecool.StoreOpenStatus.UN_OPEN.getType() != Long.parseLong(projectSellinStoreOpen.get("SO_STATUS").toString()))) {
                    isOpened = true;
                }
            }
            //未开过店
            if (!isOpened) {
                pseList = findByProjectSellinExecdateAndChannel(query);
                if (pseList != null && pseList.size() > 0) {
                    if (DateUtils.isSameDay(getDate(pssoList.get(0).get("SO_PLAN_OPEN_TIME")),getDate(pseList.get(0).get("EXEC_DATE")))){

                    } else {
                        for (PageData ed : pseList) {
                            if (ed == null || ed.get("EXEC_DATE") == null) {
                                continue;
                            }
                            PageData psso = pssoList.get(0);
                            if(DateUtils.isSameDay(getDate(ed.get("EXEC_DATE")),getDate(psso.get("SO_PLAN_OPEN_TIME")))){
                                //ignore
                            }else{

                                // (若新增的执行日在已有的执行日之前, 则新增个开店日,同时删除已有的第一天的开店日)
                                PageData storeOpen = new PageData();
                                storeOpen.put("PROJECT_SELLIN_INFO_ID",newPage.get("ID").toString());
                                storeOpen.put("SO_CREATE_USER",dmUser.get("ID").toString());
                                storeOpen.put("SO_STATUS",ConstantsMecool.StoreOpenStatus.UN_OPEN.getType());
                                storeOpen.put("SO_PLAN_OPEN_TIME",ed.get("EXEC_DATE").toString());
                                storeOpen.put("SO_CREATE_TIME",new Timestamp(new Date().getTime()));
                                try {
                                    if(fmbpc!=null&&fmbpc.size()>0){

                                    }else{
                                        //ProjectSellinStoreOpenRemove(storeOpen);
                                        saveProjectSellinStore(storeOpen);
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    RuntimeException runtimeException = new RuntimeException();
                                    runtimeException.printStackTrace();
                                    return;
                                }

                            }
                        }
                    }
                } else {
                    // It don't exist ProjectSellinExecdate, so it don't exist ProjectSellinStoreOpen
//                    if (pssoList != null && pssoList.size() > 0) {
//                        for (PageData psso : pssoList) {
//                            try {
                                ProjectSellinStoreOpenRemove(newPage);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                RuntimeException runtimeException = new RuntimeException();
//                                runtimeException.printStackTrace();
//                                return;
//                            }
//                        }
//                    }
                }
            }
            delResource(1,mp,mjEntity.getJedisId());
            return;
        }
    }

    private void writeLogJson (Long sellinId, Long updateUser, String remarks,
                               Long optionFlag)throws Exception{
        //获得买进信息
        PageData query = new PageData();
        query.put("ID",sellinId);
        //List<PageData> psinfo = mjExcelService.findByMJinfoByProject(query);
        PageData psinfo = findProjectSellinInfoById(query);
        if(psinfo != null){
            psinfo.put("PROJECT_ID",psinfo.get("PROJECT_ID").toString());
            psinfo.put("CHANNEL_SYNC_ID",psinfo.get("CHANNEL_SYNC_ID").toString());
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss:SS").create();
            String str = "";
            try {
                str = gson.toJson(psinfo);
            } catch (Exception e) {
                e.printStackTrace();
                RuntimeException runtimeException = new RuntimeException();
                runtimeException.printStackTrace();
            }
            //保存数据
            PageData json = new PageData();
            json.put("CREATE_TIME",new Timestamp(new Date().getTime()));
            json.put("PROJECT_SELLIN_INFO_ID",sellinId);
            json.put("SELLIN_INFO",str);
            json.put("UPDATE_USER",updateUser);
            json.put("REMARKS",remarks);
            json.put("OPTION_FLAG",optionFlag);
            try {
                saveSellinHistoryDetail(json);
            } catch (Exception e) {
                e.printStackTrace();
                RuntimeException runtimeException = new RuntimeException();
                runtimeException.printStackTrace();
            }
        }
    }

    private void delResource(int count,Map pd,String id){
        if (count==1){
            JedisUtil.delResource(pd,id);
        }
    }

    private Date getDate(Object value) {

        Timestamp timestamp = null;
        try {
            timestamp = (Timestamp) value;
        } catch (Exception e) {
            timestamp = getOracleTimestamp(value);
        }
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(timestamp!=null) {
            Date dates = new Date();
            try {
                dates = sdf.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(timestamp));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dates;
        }
            return null;
    }
    /**
     * @reference oracle.sql.Datum.timestampValue();
     * @return
     */
    private Timestamp getOracleTimestamp(Object value) {
        try {
            Class clz = value.getClass();
            Method m = clz.getMethod("timestampValue", null);
            //m = clz.getMethod("timeValue", null); 时间类型
            //m = clz.getMethod("dateValue", null); 日期类型
            return (Timestamp) m.invoke(value, null);

        } catch (Exception e) {
            return null;
        }
    }
}