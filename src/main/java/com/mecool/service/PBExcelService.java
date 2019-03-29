package com.mecool.service;

import com.mecool.dao.DaoSupport;
import com.mecool.entity.PBEntity;
import com.mecool.util.ConstantsMecool;
import com.mecool.util.JedisUtil;
import com.mecool.util.MecoolUtil;
import com.mecool.util.PageData;
import oracle.sql.TIMESTAMP;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2/7/2018.
 */
@Service("pbExcelService")
@Transactional
public class PBExcelService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    /*
  * 保存
  */
    public void saveSalesScheduleWork(PageData pd)throws Exception {
        dao.save("PBExcelMapper.saveSalesScheduleWork", pd);
    }
    /*
  * 保存
  */
    public void saveProjectSellinSales(PageData pd)throws Exception {
        dao.save("PBExcelMapper.saveProjectSellinSales", pd);
    }
    /*
  * 保存排班
  */
    public void saveSalesScheduleCalendar(PageData pd)throws Exception {
        dao.save("PBExcelMapper.saveSalesScheduleCalendar", pd);
    }
    /*
 * 通过项目ID来寻找门店信息
 */
    public List<PageData> getChannelList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("ChannelExcelMapper.getChannelList", pd);
    }
    /*
   * 通过emp_pk寻找用户
   */
    public PageData findDmUserByEmppk(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findDmUserByEmppk", pd);
    }
    /*
  * 是否存在执行日
  */
    public List<PageData> findByProjectSellinExecdateAndChannel(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findByProjectSellinExecdateAndChannel", pd);
    }
    /*
  * 通过项目ID来寻找项目信息
  */
    public PageData findProjectById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findProjectById", pd);
    }
    /*
  * 通过ID
  */
    public PageData getChannelById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("ChannelExcelMapper.getChannelById", pd);
    }
    /*
  * 通过
  */
    public PageData findSelectValueById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("PBExcelMapper.findSelectValueById", pd);
    }
    /*
	* 通过
	*/
    public List<PageData> getSelectValueList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("PBExcelMapper.getSelectValueList", pd);
    }
    /*
	* 通过
	*/
    public List<PageData> getSelectValueNameList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("PBExcelMapper.getSelectValueNameList", pd);
    }
    /*
	* 通过
	*/
    public List<PageData> getScheduleWorkList(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("PBExcelMapper.getScheduleWorkList", pd);
    }
    /*
	* 通过
	*/
    public List<PageData> findByProjectSellinInfoIdAndDeleteFlag(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("PBExcelMapper.findByProjectSellinInfoIdAndDeleteFlag", pd);
    }
    /*
	* 通过
	*/
    public List<PageData> findByProjectSellinInfoIdAndDeleteFlagAndSales(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("PBExcelMapper.findByProjectSellinInfoIdAndDeleteFlagAndSales", pd);
    }
    /*
	* 通过
	*/
    public PageData findSalesBySalesIdAndDate(PageData pd)throws Exception {
        return (PageData)dao.findForObject("PBExcelMapper.findSalesBySalesIdAndDate", pd);
    }
    /*
	* 通过
	*/
    public List<PageData> getSalesScheduleCalendarByCardId(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("PBExcelMapper.getSalesScheduleCalendarByCardId", pd);
    }
    /*
 * 通过项目ID查询卖进信息
 */
    public List<PageData> findByMJinfoByProject(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findByMJinfoByProject", pd);
    }
    /*
 * 通过项目ID查询卖进信息
 */
    public List<PageData> findByMJinfoByProjectAndChannel(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("MJExcelMapper.findByMJinfoByProjectAndChannel", pd);
    }

    /*
 * 通过
 */
    public List<PageData> findSalesBySalesCardAndDate(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("PBExcelMapper.findSalesBySalesCardAndDate", pd);
    }

    public void savePb(Map mp, PBEntity pbEntity)throws Exception{
        HashMap<String,PageData> psiMap = new HashMap<String,PageData>();  // 保存卖进， key： 门店code
        PageData query = new PageData();
        query.put("EMP_PK",pbEntity.getEmpPk());
        PageData dmUser = findDmUserByEmppk(query);
        query.put("PROJECT_ID",pbEntity.getProjectId());
        List<PageData> psiList = findByMJinfoByProject(query);
        for (PageData psi : psiList) {
            String cc = psi.get("CHANNEL_SYNC_ID").toString();
            query.put("CHANNEL_SYNC_ID",cc);
            PageData channel = getChannelById(query);
            psiMap.put(channel.get("CHANNEL_CODE").toString(), psi);
        }
        HashMap<String, PageData> selectValueSWMap = new HashMap<String, PageData>();  //  key：SelectValue.Name

//        query.put("ID","15");
//        PageData st = pbExcelService.findSelectValueById(query);
        query.put("TYPE","15");
        List<PageData> svSWList = getSelectValueList(query);
        for (PageData sv : svSWList) {
            selectValueSWMap.put(sv.get("NAME").toString(), sv);
        }
        Map<String, PageData> selectValueBankMap = new HashMap<String, PageData>();  //  key：SelectValue.Name
//        query.put("ID","30");
//        st = pbExcelService.findSelectValueById(query);
        query.put("TYPE","30");
        List<PageData> svBankList = getSelectValueList(query);
        for (PageData sv : svBankList) {
            selectValueBankMap.put(sv.get("NAME").toString(), sv);
        }
        Map<String, PageData> sswMap = new HashMap<String, PageData>();  // 保存班次， key： 门店code+_+班次名称
        List<PageData> sswList = getScheduleWorkList(query);
        for (PageData salesScheduleWork : sswList) {
            if(salesScheduleWork== null || salesScheduleWork.get("CHANNEL_SYNC_ID")==null){
                continue;
            }
            // key： 门店code+_+班次名称
            String key = salesScheduleWork.get("CHANNEL_CODE")
                    + "_" + salesScheduleWork.get("SW_NAME");

            sswMap.put(key, salesScheduleWork);
        }
        String salesName = mp.get("SALES_NAME").toString();
        String salesType = mp.get("SALES_TYPE").toString();
        String product = mp.get("PRODUCT").toString();
        String salesPhone = mp.get("SALES_PHONE").toString();
        String salesIdCard = mp.get("SALES_CARD_ID").toString();
        String salesBankName = mp.get("NAME").toString();
        String salesSalaryCard = mp.get("SALES_SALARY_CARD").toString();
        String salesMemo = mp.get("SALES_MEMO").toString();
        String salesScheduleDate = mp.get("Sales_Schedule_Date").toString();
        String channelCode = mp.get("CHANNEL_SYNC_ID").toString();
        String swName = mp.get("SW_NAME").toString();
        String swBeginTime = mp.get("SW_BEGIN_TIME").toString();
        String swEndTime = mp.get("SW_END_TIME").toString();
        String swRestBeginTime1 = mp.get("SW_REST_BEGIN_TIME1").toString();
        String swRestEndTime1 = mp.get("SW_REST_END_TIME1").toString();
        String swRestBeginTime2 = mp.get("SW_REST_BEGIN_TIME2").toString();
        String swRestEndTime2 = mp.get("SW_REST_END_TIME2").toString();
        String swType = mp.get("SwType").toString();

        PageData pd = new PageData();
        pd.put("CHANNEL_CODE",mp.get("CHANNEL_SYNC_ID").toString());
        List<PageData> channel = getChannelList(pd);
        if (channel==null&&channel.size()<1){
            mp.put("CHANNEL_SYNC_ID","Error:没有对应门店!"+mp.get("CHANNEL_SYNC_ID").toString());
        }
        pd.put("CHANNEL_SYNC_ID",channel.get(0).get("ID").toString());
        pd.put("PROJECT_ID",pbEntity.getProjectId());
        List<PageData> psisw = findByMJinfoByProjectAndChannel(pd);
        Date today = MecoolUtil.getDateNoTime(new Date());
        pd.put("CHANNEL",channel.get(0).get("ID").toString());
        pd.put("PROJECT_SELLIN_INFO_ID",psisw.get(0).get("ID"));
        List<PageData> lpd = findByProjectSellinExecdateAndChannel(pd);
        Map<String, Date> ssmss = MecoolUtil.parseDaysStr(mp.get("Sales_Schedule_Date").toString());
        pd.put("DELETE_FLAG",ConstantsMecool.DeleteFlag.USING.getType());
        pd.put("SALES_CARD_ID",mp.get("SALES_CARD_ID"));
        List<PageData> saleses = findByProjectSellinInfoIdAndDeleteFlagAndSales(pd);
        for (Iterator<Date> iterator = ssmss.values().iterator(); iterator.hasNext();) {
            Date tmpDay = (Date) iterator.next();
            if (tmpDay.before(today)){
                continue;
            }else{
                for (PageData pe:lpd) {
                    TIMESTAMP da = (TIMESTAMP) pe.get("EXEC_DATE");
                    if (!da.dateValue().equals(tmpDay)){
                        continue;
                    }else  if (saleses != null && saleses.size() > 0) {
                        pd.put("SALES_CARD_ID", mp.get("SALES_CARD_ID"));
                        pd.put("SC_SCHEDULE_DATE", da.dateValue());
                        List<PageData> sale = findSalesBySalesCardAndDate(pd);
                        if (sale != null && sale.size() > 0) {
                            //mp.put("Sales_Schedule_Date", "Error:考勤日期冲突！");
                            JedisUtil.delResource(mp,pbEntity.getJedisId());
                            return;
                        }
                    }
            }

            }
        }

//        List<PageData> lpd = pbExcelService.findByProjectSellinExecdateAndChannel(pd);
//        if (lpd==null&&lpd.size()<1){
//            mp.put("Sales_Schedule_Date","Error:执行日为空！");
//        }else{
//            Date today = MecoolUtil.getDateNoTime(new Date());
//            Map<String, Date> ssm = null;
//            try {
//                ssm = MecoolUtil.parseDaysStr(mp.get("Sales_Schedule_Date").toString());
//            } catch (Exception e) {
//                mp.put("Sales_Schedule_Date","Error:考勤日期格式错误！");
//            }
//            if (mp.get("Sales_Schedule_Date").toString().indexOf("Error")==-1){
//                pd.put("DELETE_FLAG",ConstantsMecool.DeleteFlag.USING.getType());
//                pd.put("SALES_CARD_ID",mp.get("SALES_CARD_ID"));
//                List<PageData> saleses = pbExcelService.findByProjectSellinInfoIdAndDeleteFlagAndSales(pd);
//                for (Iterator<Date> iterator = ssm.values().iterator(); iterator.hasNext();) {
//                    boolean isError = false;
//                    Date tmpDay = (Date) iterator.next();
//                    if (tmpDay.before(today)) {
//                        mp.put("Sales_Schedule_Date","Error:考勤日期不能在当天之前！");
//                        isError = false;
//                        break;
//                    }else {
//                        for (PageData pe:lpd){
//                            TIMESTAMP da = (TIMESTAMP) pe.get("EXEC_DATE");
//                            if (!da.dateValue().equals(tmpDay)){
//                                isError = false;
//                            }else {
//                                if (saleses!=null&&saleses.size()>0){
//                                    pd.put("SALES_CARD_ID",mp.get("SALES_CARD_ID"));
//                                    pd.put("SC_SCHEDULE_DATE",da.dateValue());
//                                    List<PageData> sale = pbExcelService.findSalesBySalesCardAndDate(pd);
//                                    if (sale!=null&&sale.size()>0){
//                                        mp.put("Sales_Schedule_Date","Error:考勤日期冲突！");
//                                        isError = false;
//                                        break;
//                                    }
//                                }
//                                isError = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (isError){
//                        mp.put("Sales_Schedule_Date",mp.get("Sales_Schedule_Date").toString());
//                    }else if (mp.get("Sales_Schedule_Date").toString().indexOf("Error")!=-1){
//                        break;
//                    }else{
//                        mp.put("Sales_Schedule_Date","Error:考勤日期不在执行日！");
//                        break;
//                    }
//                }
//            }
//        }

        PageData psiBean = psiMap.get(mp.get("CHANNEL_SYNC_ID").toString());
        Date current = new Date();
        try {
            // 1. 处理班次
            PageData svWorkType = selectValueSWMap.get(swType);
//            if (svWorkType == null) {
//                //failList.add("第" + k + "条 : " + "排班类型不存在!");
//                mp.put("uuid","Error:排班类型不存在!");
//            }
            String sswKey = channelCode
                    + "_" + swName;
            PageData ssw = sswMap.get(sswKey);

            PageData sswNew = new PageData();
            if (ssw == null) { // 增加新的 排班类型
                // 保存新的班次名称
                sswNew.put("SW_NAME",swName);
                sswNew.put("PROJECT_ID",pbEntity.getProjectId());
                sswNew.put("CHANNEL_SYNC_ID",psiBean.get("CHANNEL_SYNC_ID").toString());
                sswNew.put("SW_WORK_TYPE",svWorkType.get("ID").toString());
                sswNew.put("SW_COLOR","#999999");
                sswNew.put("SW_BEGIN_TIME",swBeginTime);
                sswNew.put("SW_END_TIME",swEndTime);
                sswNew.put("SW_REST_BEGIN_TIME1",swRestBeginTime1);
                sswNew.put("SW_REST_END_TIME1",swRestEndTime1);
                sswNew.put("SW_REST_BEGIN_TIME2",swRestBeginTime2);
                sswNew.put("SW_REST_END_TIME2",swRestEndTime2);
                try {
                    saveSalesScheduleWork(sswNew);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
                sswMap.put(sswKey, sswNew);
            } else {
                String keyOld = "";
                String keyNew = "";
                // 校验： 门店code+_+班次名称+_+排班类型+_+开始时间+_+结束时间+_+休息开始时间1+_+休息结束时间1+_+休息开始时间2+_+休息结束时间2
                keyOld = ssw.get("CHANNEL_CODE").toString()
                        + "_" + ssw.get("SW_NAME").toString()
                        + "_" + ssw.get("SW_WORK_TYPE").toString()
                        + "_" + ssw.get("SW_BEGIN_TIME").toString()
                        + "_" + ssw.get("SW_END_TIME").toString();
                        if(ssw.get("SW_REST_BEGIN_TIME1")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_BEGIN_TIME1").toString();
                        }
                        if(ssw.get("SW_REST_END_TIME1")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_END_TIME1").toString();
                        }
                        if(ssw.get("SW_REST_BEGIN_TIME2")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_BEGIN_TIME2").toString();
                        }
                        if(ssw.get("SW_REST_END_TIME2")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_END_TIME2").toString();
                        }
                keyNew = channelCode
                        + "_" + swName
                        + "_" + svWorkType.get("ID")
                        + "_" + swBeginTime
                        + "_" + swEndTime;
                        if(ssw.get("SW_REST_BEGIN_TIME1")!=null){
                            keyNew = keyNew+"_"+swRestBeginTime1;
                        }
                        if(ssw.get("SW_REST_END_TIME1")!=null){
                            keyNew = keyNew+"_"+swRestEndTime1;
                        }
                        if(ssw.get("SW_REST_BEGIN_TIME2")!=null){
                            keyNew = keyNew+"_"+swRestBeginTime2;
                        }
                        if(ssw.get("SW_REST_END_TIME2")!=null){
                            keyNew = keyNew+"_"+swRestEndTime2;
                        }
                if (keyOld.equals(keyNew)) { // 这个班次可以使用
                    // ignore
                } else { //存在同名的班次
                    // 保存新的班次名称
                    swName = swName + current.getTime();
                    sswKey = channelCode + "_" + swName;
                    ssw = sswMap.get(sswKey);
                    if (ssw == null) { // 增加新的 排班类型
                        // 保存新的班次名称
                        sswNew.put("SW_NAME",swName);
                        sswNew.put("PROJECT_ID",pbEntity.getProjectId());
                        sswNew.put("CHANNEL_SYNC_ID",psiBean.get("CHANNEL_SYNC_ID").toString());
                        sswNew.put("SW_WORK_TYPE",svWorkType.get("ID").toString());
                        sswNew.put("SW_COLOR","#999999");
                        sswNew.put("SW_BEGIN_TIME",swBeginTime);
                        sswNew.put("SW_END_TIME",swEndTime);
                        sswNew.put("SW_REST_BEGIN_TIME1",swRestBeginTime1);
                        sswNew.put("SW_REST_END_TIME1",swRestEndTime1);
                        sswNew.put("SW_REST_BEGIN_TIME2",swRestBeginTime2);
                        sswNew.put("SW_REST_END_TIME2",swRestEndTime2);
                        try {
                            saveSalesScheduleWork(sswNew);
                        } catch (Exception e) {
                            e.printStackTrace();
                            RuntimeException runtimeException = new RuntimeException();
                            runtimeException.printStackTrace();
                            return;
                        }
                        sswMap.put(sswKey, sswNew);
                    } else {
                        keyOld = ssw.get("CHANNEL_CODE").toString()
                                + "_" + ssw.get("SW_NAME").toString()
                                + "_" + ssw.get("SW_WORK_TYPE").toString()
                                + "_" + ssw.get("SW_BEGIN_TIME").toString()
                                + "_" + ssw.get("SW_END_TIME").toString();
                        if(ssw.get("SW_REST_BEGIN_TIME1")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_BEGIN_TIME1").toString();
                        }
                        if(ssw.get("SW_REST_END_TIME1")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_END_TIME1").toString();
                        }
                        if(ssw.get("SW_REST_BEGIN_TIME2")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_BEGIN_TIME2").toString();
                        }
                        if(ssw.get("SW_REST_END_TIME2")!=null){
                            keyOld = keyOld+ "_" + ssw.get("SW_REST_END_TIME2").toString();
                        }
                        keyNew = channelCode
                                + "_" + swName
                                + "_" + svWorkType.get("ID")
                                + "_" + swBeginTime
                                + "_" + swEndTime;
                        if(ssw.get("SW_REST_BEGIN_TIME1")!=null){
                            keyNew = keyNew+"_"+swRestBeginTime1;
                        }
                        if(ssw.get("SW_REST_END_TIME1")!=null){
                            keyNew = keyNew+"_"+swRestEndTime1;
                        }
                        if(ssw.get("SW_REST_BEGIN_TIME2")!=null){
                            keyNew = keyNew+"_"+swRestBeginTime2;
                        }
                        if(ssw.get("SW_REST_END_TIME2")!=null){
                            keyNew = keyNew+"_"+swRestEndTime2;
                        }
                        if (keyOld.equals(keyNew)) { // 这个班次可以使用
                            // ignore
                        } else { //存在同名的班次
                            // 保存新的班次名称
                            swName = swName + (current.getTime()+1);
                            sswKey = channelCode + "_" + swName;
                            sswNew.put("SW_NAME",swName);
                            sswNew.put("PROJECT_ID",pbEntity.getProjectId());
                            sswNew.put("CHANNEL_SYNC_ID",psiBean.get("CHANNEL_SYNC_ID").toString());
                            sswNew.put("SW_WORK_TYPE",svWorkType.get("ID").toString());
                            sswNew.put("SW_COLOR","#999999");
                            sswNew.put("SW_BEGIN_TIME",swBeginTime);
                            sswNew.put("SW_END_TIME",swEndTime);
                            sswNew.put("SW_REST_BEGIN_TIME1",swRestBeginTime1);
                            sswNew.put("SW_REST_END_TIME1",swRestEndTime1);
                            sswNew.put("SW_REST_BEGIN_TIME2",swRestBeginTime2);
                            sswNew.put("SW_REST_END_TIME2",swRestEndTime2);
                            try {
                                saveSalesScheduleWork(sswNew);
                            } catch (Exception e) {
                                e.printStackTrace();
                                RuntimeException runtimeException = new RuntimeException();
                                runtimeException.printStackTrace();
                                return;
                            }
                            sswMap.put(sswKey, sswNew);
                        }
                    }
                }
            }

            // 2. 处理促销员   促销员存在卖进中就增加排班， 不存在卖进中先增加促销员在增加排班
            PageData sales = null;
            query.put("DELETE_FLAG", ConstantsMecool.DeleteFlag.USING.getType());
            query.put("PROJECT_SELLIN_INFO_ID",psiBean.get("ID"));
            List<PageData> salesesa = findByProjectSellinInfoIdAndDeleteFlag(query);
            for (PageData tmp : salesesa) {
                if (salesIdCard.equals(tmp.get("SALES_CARD_ID").toString())) {
                    sales = tmp;
                    break;
                }
            }
            if (sales == null) {
                sales = new PageData();
                sales.put("PROJECT_SELLIN_INFO_ID",psiBean.get("ID").toString());
                sales.put("SALES_CARD_ID",salesIdCard);
                sales.put("SALES_NAME",salesName);
                sales.put("SALES_PHONE",salesPhone);
                sales.put("SALES_SALARY_CARD",salesSalaryCard);
                String salesBankCode = "3001";
                PageData svBank = selectValueBankMap.get(salesBankName);
                if (svBank != null) {
                    salesBankCode = svBank.get("ID").toString();
                }
                sales.put("SALES_BANK_CODE",salesBankCode);
                sales.put("SALES_SCHEDULE_TYPE",svWorkType.get("ID").toString());
                sales.put("SALES_WORK_START",swBeginTime);
                sales.put("SALES_WORK_END",swEndTime);
                sales.put("SALES_EAT_START",swRestBeginTime1);
                sales.put("SALES_EAT_END",swRestEndTime1);
                sales.put("SALES_MEMO",salesMemo);
                sales.put("SALES_EAT_START2",swRestBeginTime2);
                sales.put("SALES_EAT_END2",swRestEndTime2);
                sales.put("CREATE_TIME",current);
                sales.put("CREATE_USER",dmUser.get("ID"));
                if (!product.equals("可以不填")){
                    sales.put("PRODUCT",product);
                }else {
                    sales.put("PRODUCT","");
                }
                if (ConstantsMecool.SalesType.LONG.getTypeName().equals(salesType)) {
                    sales.put("SALES_TYPE",ConstantsMecool.SalesType.LONG.getType());
                } else {
                    sales.put("SALES_TYPE",ConstantsMecool.SalesType.SHORT.getType());
                }
                try {
                    saveProjectSellinSales(sales);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
            }

            // 3. 处理排班
//            Date today = MecoolUtil.getDateNoTime(new Date());
            Map<String, Date> ssm = MecoolUtil.parseDaysStr(salesScheduleDate);
            for (Iterator<Date> iterator = ssm.values().iterator(); iterator.hasNext();) {
                Date tmpDay = (Date) iterator.next();
                //boolean isExist = false;
                PageData querys = new PageData();
                querys.put("SALES_CARD_ID",sales.get("ID").toString());
                querys.put("SC_SCHEDULE_DATE", DateUtils.addDays(new Date(), -1));
                List<PageData> sscSet = getSalesScheduleCalendarByCardId(querys);
                for (Iterator<PageData> iterator2 = sscSet.iterator(); iterator2.hasNext();) {
                    PageData ssc = (PageData) iterator2.next();
                    SimpleDateFormat sdf = new SimpleDateFormat();
                    Date d = sdf.parse(ssc.get("SC_SCHEDULE_DATE").toString());
                    if (d == null) {
                        continue;
                    }
                    d = MecoolUtil.getDateNoTime(d);
                    if (d.compareTo(tmpDay) == 0) {
                        //isExist = true;
                        break;
                    }
                }
//            if (isExist) {
//                continue;
//            }
                // 保存排班
                PageData ssc = new PageData();
                ssc.put("SALES_ID",sales.get("ID"));
                ssc.put("SC_SCHEDULE_DATE",tmpDay);
                if (ssw==null){
                    ssc.put("SCHEDULE_WORK_ID",sswNew.get("ID"));
                }else{
                    ssc.put("SCHEDULE_WORK_ID",ssw.get("ID"));
                }
                ssc.put("SC_CREATE_TIME",current);
                try {
                    saveSalesScheduleCalendar(ssc);
                } catch (Exception e) {
                    e.printStackTrace();
                    RuntimeException runtimeException = new RuntimeException();
                    runtimeException.printStackTrace();
                    return;
                }
                //salesScheduleCalendarService.saveAndFlush(ssc);
                //sscSet.add(ssc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException runtimeException = new RuntimeException();
            runtimeException.printStackTrace();
            return;
        }
        JedisUtil.delResource(mp,pbEntity.getJedisId());
    }
}
