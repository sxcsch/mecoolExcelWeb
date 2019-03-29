package com.mecool.service;

import com.mecool.entity.MJEntity;
import com.mecool.util.ConstantsMecool;
import com.mecool.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by me on 2018/10/12.
 */
@Service("heardExcelService")
public class HeardExcelService {

    @Resource(name = "mjExcelService")
    private MJExcelService mjExcelService;

    /**
     * 检查投数据是否完整
     * @param projectId
     * @param headerList
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public PageData checkHeader(String projectId, PageData headerList, MJEntity mjEntity) throws Exception {
        PageData data = new PageData();
        PageData pd = new PageData();
        pd.put("PROJECT_ID",projectId);
        pd.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_INFO.getType());
        PageData project  = mjEntity.getProject();

//        if (project == null || project.size() < 1) {
//            data.put("HeaderError", "没有此项目！");
//            return data;
//        }
        if (headerList == null || headerList.size() < 1) {
            data.put("HeaderError", "没有字段！");
            return data;
        } else if (headerList.size() > 100) {
            data.put("HeaderError", "字段总数超过80！");
            return data;
        }
        List<String> hlist = new ArrayList<String>();
        hlist.add(ConstantsMecool.SellinFields.CHANNEL_SYNC_ID.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.SCHEDULE_NUM.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.PLAN_START_TIME.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.PLAN_FINISHED_TIME.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.EMP_CODE.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.CITY_EMP_CODE.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.AREA_MANAGER_CODE.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.PLAN_SALES_COUNT.getHeaderName());
        Set<String> tmpSet = new HashSet<String>();
        for (int i = 0; i < headerList.size(); i++) {
            try {
                if (headerList.get("var"+i) == null) {
                    continue;
                }
                String header = headerList.get("var"+i).toString().trim();
                tmpSet.add(header);
                hlist.remove(header);
            } catch (Exception e) {
                throw e;
            }
        }
        if (hlist.size() > 0) {
            String h = "缺少关键字段: ";
            for (String s : hlist) {
                h = h + s+" ";
            }
            data.put("HeaderError", h);
            return data;
        }

        List<PageData> list = mjExcelService.findInfoHeaderByProjectIdOrderByColumnOrder(pd);
        if (list != null) {
            for (PageData p : list) {
                if (p == null) {
                    continue;
                }
                tmpSet.add(p.getString("HEADER_NAME"));
            }
        }
        if (tmpSet.size() > 100) {
            data.put("HeaderError", "新的字段与老的字段之和超过80！");
            return data;
        }
        try {
            updateHeader(project,headerList,mjEntity);
        } catch (Exception e) {
            data.put("HeaderError", "处理失败！");
            e.printStackTrace();
            return data;
        }
        return data;
    }
    /**
     *更新表头
     * @param project
     * @param headerList
     */
    @SuppressWarnings("unused")
    private void updateHeader(PageData project, PageData headerList, MJEntity mjEntity) throws Exception {
        PageData pd =new PageData();
        pd.put("PROJECT_ID",mjEntity.getProjectId());
        pd.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_INFO.getType());
        PageData countHeader = mjExcelService.getExcelHeardCountById(pd);
        if (Long.parseLong(countHeader.get("NUM").toString()) < 1) {
            // Don't exist headers.
            setInitHeaders(project, headerList);
        } else {
            // Exist headers.
            // Check sellin data.
            PageData countData = mjExcelService.getExcelHeardSellinInfoCountById(pd);
            if (Integer.parseInt(countData.get("NUM").toString()) < 1) {
                // Don't exist sellinInfo data.
                mjExcelService.deleteByProject(project);

                setInitHeaders(project, headerList);
            } else {
                // Exist sellinInfo data.
                project.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_INFO.getType());
                List<PageData> list = mjExcelService.findInfoHeaderByProjectIdOrderByColumnOrder(pd);
                // Save header that it isn't exist in DB.
                // Check header if or not exist, if it don't exist, save it.
                saveUnExistHeaders(project, list, headerList);
            }
        }

    }

    /**
     * Check header if or not exist, if it don't exist, save it.
     * @param project
     * @param oldHeaders  have existed in DB
     * @param headerList  new headerList.
     */
    @SuppressWarnings("unused")
    private void saveUnExistHeaders(PageData project, List<PageData> oldHeaders,PageData headerList)throws Exception {
        // Get max id.
//    	long id = 0;
        long order = 0;
        List<String> columns = new ArrayList<String>();
        for (PageData header : oldHeaders) {
            if (header == null) {
                continue;
            }
//			if (id < header.getId()) {
//				id = header.getId();
//			}
            if (order < Long.parseLong(header.get("COLUMN_ORDER").toString())) {
                order = Long.parseLong(header.get("COLUMN_ORDER").toString());
            }
            if (header.getString("SELLIN_C_NAME").startsWith("C")) {
                columns.add(header.getString("SELLIN_C_NAME"));
            }
        }
        Map<String, PageData> map = getSellinHeadersMap(project);
        for (int i = 0; headerList.size()<i; i++) {
            String h = headerList.getString("var"+i);
            if (h == null || h.trim().length() < 1) {
                continue;
            }
            h = h.trim();
            if (h != null && h.length()> 150) {
                h = h.substring(0, 149);
            }
            boolean flag = false;
            for (PageData header : oldHeaders) {
                if (header == null) {
                    continue;
                }
                if (h.equals(header.getString("HEADER_NAME"))) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                continue;
            }
            if (map.get(h.trim()) == null) {
                saveSellinHeader(project, h, order+2, columns, map);
            }
        }
    }

    @SuppressWarnings("unused")
    private void setInitHeaders(PageData project, PageData headerList)throws Exception {
        // Add Headers.
        saveSellinHeaders(project, headerList);

        // call procedure, set job and job step.
        mjExcelService.setJobStepByProcedure(project);

//		projectSellinHeaderService.setQCFieldsByProcedure(project);

        setInitIntegrated(project);
    }

    @SuppressWarnings("unused")
    private void setInitIntegrated(PageData project)throws Exception {
        project.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.RPT_SUM_PROJECT_SELLIN.getType());
        List<PageData> sellinList = mjExcelService.findInfoHeaderByProjectIdOrderByColumnOrder(project);
        for (PageData h : sellinList) {
            h.put("IS_INTEGRATED_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            mjExcelService.saveHeader(h);
        }
        project.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType());
        sellinList = mjExcelService.findInfoHeaderByProjectIdOrderByColumnOrder(project);
        for (PageData h : sellinList) {
            if (ConstantsMecool.SellinStorePatrolFields.SP_EXHIBIT_PHOTOS.equals(h.getString("SELLIN_C_NAME"))) {
                h.put("IS_INTEGRATED_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
                mjExcelService.saveHeader(h);
            } else if (ConstantsMecool.SellinStorePatrolFields.SP_SALES_PHOTOS.equals(h.getString("SELLIN_C_NAME"))) {
                h.put("IS_INTEGRATED_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
                mjExcelService.saveHeader(h);
            }
        }
    }

    @SuppressWarnings("unused")
    private void saveSellinHeaders(PageData project, PageData headerList) throws Exception{
        List<String> columns = new ArrayList<String>();
        Map<String, PageData> map = getSellinHeadersMap(project);
        saveSellinHeader(project, ConstantsMecool.SellinFields.STATUS.getHeaderName(), 1, columns, map);
        saveSellinHeader(project, ConstantsMecool.SellinFields.EXEC_STATUS.getHeaderName(), 2, columns, map);
        long index = 3;
        for (int i=0;i< headerList.size();i++) {
//    	for (int i = 0; i < headerList.size(); i++) {
//    		Cell cell = headerList.get(i);
            String h = headerList.getString("var"+i);
            if (h == null || h.trim().length() < 1) {
                continue;
            }
            if (ConstantsMecool.SellinFields.STATUS.getHeaderName().equals(h.trim())) {
                continue;
            }
            if (ConstantsMecool.SellinFields.EXEC_STATUS.getHeaderName().equals(h.trim())) {
                continue;
            }
            if (ConstantsMecool.SellinFields.UPDATE_TIME.getHeaderName().equals(h.trim())) {
                continue;
            }
            if (map.get(h.trim()) == null) {
                if (ConstantsMecool.SellinFields.PLAN_START_TIME.getHeaderName().equals(h.trim())) {
                    // 原计划开始日期
                    saveSellinHeader(project, ConstantsMecool.SellinFields.OLD_PLAN_START_TIME.getHeaderName(),
                            ConstantsMecool.SellinFields.OLD_PLAN_START_TIME.getColumnName(), index,
                            ConstantsMecool.SellinModifyFlag.MODIFY.getType(),
                            ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                            ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                            ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                            ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                            ConstantsMecool.StepOptionType.DIRECT.getType(),
                            ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                            ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_INFO.getType(),
                            ConstantsMecool.SellinHeaderComType.DATE.getType(),
                            null);
                    index++;
                    //原计划结束日期
                    saveSellinHeader(project, ConstantsMecool.SellinFields.OLD_PLAN_FINISHED_TIME.getHeaderName(),
                            ConstantsMecool.SellinFields.OLD_PLAN_FINISHED_TIME.getColumnName(), index,
                            ConstantsMecool.SellinModifyFlag.MODIFY.getType(),
                            ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                            ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                            ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                            ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                            ConstantsMecool.StepOptionType.DIRECT.getType(),
                            ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                            ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_INFO.getType(),
                            ConstantsMecool.SellinHeaderComType.DATE.getType(),
                            null);
                    index++;
                    saveSellinHeader(project, h.trim(), index, columns, map);
                } else {
                    saveSellinHeader(project, h.trim(), index, columns, map);
                }
            }
            index++;
        }
//    	long index = headerList.size() + 3;
        // Add sale header.
        saveHeadersSellinForSale(project, ConstantsMecool.SellinSalesFields.SALES_NAME.getHeaderName(),
                index++, ConstantsMecool.SellinSalesFields.SALES_NAME.getColumnName());
        saveHeadersSellinForSale(project, ConstantsMecool.SellinSalesFields.SALES_PHONE.getHeaderName(),
                index++, ConstantsMecool.SellinSalesFields.SALES_PHONE.getColumnName());
        saveHeadersSellinForSale(project, ConstantsMecool.SellinSalesFields.SALES_CARD_ID.getHeaderName(),
                index++, ConstantsMecool.SellinSalesFields.SALES_CARD_ID.getColumnName());
        saveHeadersSellinForSale(project, ConstantsMecool.SellinSalesFields.SALES_ADDRESS.getHeaderName(),
                index++, ConstantsMecool.SellinSalesFields.SALES_ADDRESS.getColumnName());
        saveHeadersSellinForSale(project, ConstantsMecool.SellinSalesFields.SALES_SALARY_CARD.getHeaderName(),
                index++, ConstantsMecool.SellinSalesFields.SALES_SALARY_CARD.getColumnName());
        saveHeadersSellinForSale(project, ConstantsMecool.SellinSalesFields.SALES_BANK_CODE.getHeaderName(),
                index++, ConstantsMecool.SellinSalesFields.SALES_BANK_CODE.getColumnName());
        saveHeadersSellinForSale(project, ConstantsMecool.SellinSalesFields.SALES_MEMO.getHeaderName(),
                index++, ConstantsMecool.SellinSalesFields.SALES_MEMO.getColumnName());

        index = saveHeadersSellinSalesScheduleCalendar(project, index);

        saveSellinHeader(project, ConstantsMecool.SellinFields.UPDATE_TIME.getHeaderName(),
                index++, columns, map);

        saveSellinOtherHeaders(project, index);
    }

    @SuppressWarnings("unused")
    private void saveSellinHeader(PageData project, String header, long order, List<String> columns, Map<String, PageData> map) throws Exception{
        if (map.get(header.trim()) != null) {
            return;
        }
        PageData sh = new PageData();
        sh.put("PROJECT_ID",project.get("PROJECT_ID"));
        sh.put("HEADER_NAME",header);
        if (ConstantsMecool.SellinFields.CHANNEL_SYNC_ID.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CHANNEL_SYNC_ID.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
        } else if (ConstantsMecool.SellinFields.SCHEDULE_NUM.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.SCHEDULE_NUM.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_START_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_START_TIME.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.DATE.getType());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_FINISHED_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_FINISHED_TIME.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.DATE.getType());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType());
        } else if (ConstantsMecool.SellinFields.EMP_CODE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.EMP_CODE.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType());
        } else if (ConstantsMecool.SellinFields.CITY_EMP_CODE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CITY_EMP_CODE.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType());
        } else if (ConstantsMecool.SellinFields.AREA_MANAGER_CODE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.AREA_MANAGER_CODE.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_SALES_COUNT.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_SALES_COUNT.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
        } else if (ConstantsMecool.SellinFields.STATUS.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.STATUS.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.PHONE_INPUT.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType());
            sh.put("COM_VALUE",ConstantsMecool.SelectType.SELLIN_STATUS.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
        } else if (ConstantsMecool.SellinFields.EXEC_STATUS.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.EXEC_STATUS.getColumnName());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.PHONE_INPUT.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType());
            sh.put("COM_VALUE",ConstantsMecool.SelectType.SELLIN_EXEC_STATUS.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
        } else if (ConstantsMecool.SellinFields.UPDATE_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.UPDATE_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.DATE_TIME.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.EMP_NAME.getHeaderName().equals(header)) {
            // 门店督导姓名
            // It is a table, isn't column.
            return;
        } else if (ConstantsMecool.SellinFields.CITY_EMP_NAME.getHeaderName().equals(header)) {
            // 城市督导姓名
            // It is a table, isn't column.
            return;
        } else if (ConstantsMecool.SellinFields.AREA_MANAGER_NAME.getHeaderName().equals(header)) {
            // 区域负责人姓名
            // It is a table, isn't column.
            return;
        } else if (ConstantsMecool.SellinFields.EXEC_DATE_TIME.getHeaderName().equals(header)) {
            // 执行日期
            // It is a table, isn't column.
            return;
        } else if (ConstantsMecool.SellinFields.EXEC_DATE_TIME_COUNT.getHeaderName().equals(header)) {
            // 执行场次
            // It is a table, isn't column.
            return;
        } else if (ConstantsMecool.SellinFields.PLAN_STORE_OPEN_COUNT.getHeaderName().equals(header)) {
            // 开店计划
            // It is a table, isn't column.
            return;
        } else if (ConstantsMecool.SellinFields.CUST_DEPUTY_NAME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_DEPUTY_NAME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_DEPUTY_PHONE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_DEPUTY_PHONE.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_DIRECTOR_NAME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_DIRECTOR_NAME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_DIRECTOR_PHONE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_DIRECTOR_PHONE.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_MANAGER_NAME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_MANAGER_NAME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_MANAGER_PHONE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_MANAGER_PHONE.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType());
        } else if (ConstantsMecool.SellinFields.SELLIN_CHANNEL.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.SELLIN_CHANNEL.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_CHANNEL.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_CHANNEL.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_SYS.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_SYS.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_CHANNEL_CODE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_CHANNEL_CODE.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.CUST_CHANNEL_NAME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.CUST_CHANNEL_NAME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_EXEC_DATE_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_EXEC_DATE_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_COM_EXEC_DATE_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_COM_EXEC_DATE_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_THREE_PAY_EXEC_DATE_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_THREE_PAY_EXEC_DATE_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_EXEC_STORE_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_EXEC_STORE_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_COM_EXEC_STORE_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_COM_EXEC_STORE_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_THREE_PAY_EXEC_STORE_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_THREE_PAY_EXEC_STORE_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_TYPE.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_EXHIBIT_TYPE.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_START_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_EXHIBIT_START_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.DATE.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_END_TIME.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_EXHIBIT_END_TIME.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.DATE.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_NUM.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_EXHIBIT_NUM.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_STORE_PATROL_COUNT.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_STORE_PATROL_COUNT.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_WEEK_STORE_PATROL_COUNT.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_WEEK_STORE_PATROL_COUNT.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.PLAN_MONTH_STORE_PATROL_COUNT.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.PLAN_MONTH_STORE_PATROL_COUNT.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinFields.TOTAL_SALES_TARGET.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.TOTAL_SALES_TARGET.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        }  else if (ConstantsMecool.SellinFields.DAILY_SOTRE_SALES_TARGET.getHeaderName().equals(header)) {
            sh.put("SELLIN_C_NAME",ConstantsMecool.SellinFields.DAILY_SOTRE_SALES_TARGET.getColumnName());
            sh.put("IS_MODIFY",ConstantsMecool.SellinModifyFlag.MODIFY.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.INIT_VALUE.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.INTEGER.getType());
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        }   else {
            sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.PHONE_INPUT.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
            String column = "C" + (columns.size() + 1);
            columns.add(column);
            sh.put("SELLIN_C_NAME",column);
        }
        sh.put("COLUMN_ORDER",order + 0l);
        if (sh.getString("HEADER_NAME") != null && sh.getString("HEADER_NAME").length()> 150) {
            sh.put("HEADER_NAME",sh.getString("HEADER_NAME").substring(0, 149));
        }
//        PageData headersh = null;
        try {
            mjExcelService.saveHeader(sh);
            //headersh = mjExcelService.findHeaderById(sh);
            map.put(sh.getString("HEADER_NAME"), sh);
        } catch (Exception e) {
            e.printStackTrace();
            map.put(sh.getString("HEADER_NAME"), sh);
        }
    }

    @SuppressWarnings("unused")
    private Map<String, PageData> getSellinHeadersMap(PageData project) throws Exception {
        Map<String, PageData> map = new HashMap<String, PageData>();
        project.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_INFO.getType());
        List<PageData> list = mjExcelService.findInfoHeaderByProjectIdOrderByColumnOrder(project);
        if (list == null) {
            return map;
        }
        for (PageData bean : list) {
            map.put(bean.getString("HEADER_NAME"), bean);
        }
        return map;
    }

    @SuppressWarnings("unused")
    private void saveSellinHeader(PageData project, String headerName, String sellinCName,
                                  long order, Long isModify, Long sourceType, Long isShow, Long isOpenCustomer, Long isSetting,
                                  Long stepOptionType, Long isShowPhone, Long headerFlag,
                                  Long comType, Long comValue) throws Exception {
        PageData sh = new PageData();
        sh.put("PROJECT_ID",project.get("PROJECT_ID"));
        sh.put("HEADER_NAME",headerName);
        sh.put("SELLIN_C_NAME",sellinCName);
        sh.put("COLUMN_ORDER",order);
        sh.put("IS_MODIFY",isModify);
        sh.put("SOURCE_TYPE",sourceType);
        sh.put("IS_SHOW",isShow);
        sh.put("IS_OPEN_CUSTOMER",isOpenCustomer);
        sh.put("IS_SETTING",isSetting);
        sh.put("HEADER_FLAG",headerFlag);
        sh.put("COM_TYPE",comType);
        sh.put("COM_VALUE",comValue);
        sh.put("STEP_OPTION_TYPE",stepOptionType);
        sh.put("IS_SHOW_PHONE",isShowPhone);
        mjExcelService.saveHeader(sh);
    }

    private void saveHeadersSellinForSale(PageData project, String header, long order, String saleColumnName) throws Exception {
        PageData sh = new PageData();
        sh.put("PROJECT_ID",project.get("PROJECT_ID"));
        sh.put("HEADER_NAME",header);
        sh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.PHONE_INPUT.getType());
        sh.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_SALES.getType());
        sh.put("SELLIN_C_NAME",saleColumnName);
        sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.TEXT.getType());
        if (ConstantsMecool.SellinSalesFields.SALES_ADDRESS.getColumnName().equals(saleColumnName)) {
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinSalesFields.SALES_MEMO.getColumnName().equals(saleColumnName)) {
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinSalesFields.SALES_PHONE.getColumnName().equals(saleColumnName)) {
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.PHONE_NUMBER.getType());
        } else if (ConstantsMecool.SellinSalesFields.SALES_CARD_ID.getColumnName().equals(saleColumnName)) {
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.ID_CARD.getType());
        } else if (ConstantsMecool.SellinSalesFields.SALES_SALARY_CARD.getColumnName().equals(saleColumnName)) {
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
        } else if (ConstantsMecool.SellinSalesFields.SALES_BANK_CODE.getColumnName().equals(saleColumnName)) {
            sh.put("IS_SHOW",ConstantsMecool.SellinColumnIsShow.SHOW.getType());
            sh.put("IS_OPEN_CUSTOMER",ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType());
            sh.put("COM_TYPE",ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType());
            sh.put("COM_VALUE",ConstantsMecool.SelectType.BANK_CODE.getType());
            sh.put("IS_SETTING",ConstantsMecool.SellinColumnIsSetting.SETTING.getType());
        }

        sh.put("COLUMN_ORDER",order);
        try {
            mjExcelService.saveHeader(sh);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 促销员排班日历表  header
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersSellinSalesScheduleCalendar(PageData project, long order) throws Exception {
        // 排班日期
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SC_SCHEDULE_DATE.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SC_SCHEDULE_DATE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.DATE.getType(),
                null);
        // 促销员上班时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SW_BEGIN_TIME.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SW_BEGIN_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 促销员下班时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SW_END_TIME.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SW_END_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 促销员吃饭开始时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_BEGIN_TIME1.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_BEGIN_TIME1.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 促销员吃饭结束时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_END_TIME1.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_END_TIME1.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 促销员吃饭开始时间2
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_END_TIME2.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_END_TIME2.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 促销员吃饭结束时间2
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_END_TIME2.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SW_REST_END_TIME2.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 排班类型
        saveSellinHeader(project, ConstantsMecool.SellinSalesScheduleCalendarFields.SW_WORK_TYPE.getHeaderName(),
                ConstantsMecool.SellinSalesScheduleCalendarFields.SW_WORK_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_SALES_SCHEDULE_CALENDAR.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.SelectType.SALES_WORK_TYPE.getType());
        return order;
    }

    /**
     * 保存其他行数
     * @param project
     * @param order
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private void saveSellinOtherHeaders(PageData project, long order) throws Exception {

        // 保存开店  header
        order = saveHeadersSellinStoreOpen(project, order);
        // 保存巡店 header
        order = saveHeadersSellinStorePatrol(project, order);

        // 促销员考勤 header
        order = saveHeadersSellinSalesAtt(project, order);

        // QC header
        order = saveHeadersSellinQC(project, order);
        order = saveHeadersSellinQCOnline(project, order);

        order = saveHeadersMtrlInputWarehouse(project, order);
        order = saveHeadersMtrlOutWhMng(project, order);
        order = saveHeadersMtrlStoreReceipt(project, order);
    }

    /**
     * 保存开店  header
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersSellinStoreOpen(PageData project, long order) throws Exception {
        // 开店状态
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_STATUS.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_STATUS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.SelectType.OPEN_STORE_STATUS.getType());
        // 上报方式
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_REPORT_TYPE.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_REPORT_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.PatrolStoreReportType.BY_JOB.getType());
        // 上报人
//    	saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_UPDATE_USER.getHeaderName(),
//    			ConstantsMecool.SellinStoreOpenFields.SO_UPDATE_USER.getColumnName(), order++,
//    			ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
//    			ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
//    			ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
//    			ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
//    			ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
//    			ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
//    			ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
//    			null);
        // 计划开始时间
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_PLAN_OPEN_TIME.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_PLAN_OPEN_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.DATE.getType(),
                null);
        // 计划完成时间
//    	saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.O_PLAN_FINISH_TIME.getHeaderName(),
//    			ConstantsMecool.SellinStoreOpenFields.O_PLAN_FINISH_TIME.getColumnName(), order++,
//    			ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
//    			ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
//    			ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
//    			ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
//    			ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
//    			ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
//    			ConstantsMecool.SellinHeaderComType.DATE.getType(),
//    			null);
        // 开店日期
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_OPEN_TIME.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_OPEN_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.DATE.getType(),
                null);
        // 开店形式
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_OPEN_TYPE.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_OPEN_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                null);
        // 陈列形式
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_EXHIBIT_TYPE.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_EXHIBIT_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                null);

        // 陈列照片
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_EXHIBIT_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_EXHIBIT_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 促销员照片
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 开店督导
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_EXEC_EMP_CODE.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_EXEC_EMP_CODE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 特殊报备
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SPECIAL_SITUATION.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SPECIAL_SITUATION.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 完成时间
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_UPLOAD_TIME.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_UPLOAD_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 促销员陈列形式
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_EXHIBIT_TYPE.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_EXHIBIT_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                null);
        // 促销员陈列照
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_EXHIBIT_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_EXHIBIT_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 促销员（开店）照片
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_OPEN_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_OPEN_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 促销员现场活动照片
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_ACTION_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_ACTION_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 促销员竞品照片
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_COMPET_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_COMPET_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 促销员（开店）姓名
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_OPEN_CODE.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_OPEN_CODE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 促销员上报日期时间
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_REPORT_TIME.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_REPORT_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 特殊报备1
        saveSellinHeader(project, ConstantsMecool.SellinStoreOpenFields.SO_SALES_QUESTION_FEEDBACK.getHeaderName(),
                ConstantsMecool.SellinStoreOpenFields.SO_SALES_QUESTION_FEEDBACK.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.SALES_SYSTEM_STORE_OPEN.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);

        for (int i = 1; i < 11; i++) {
            saveSellinHeader(project, "开店自定义字段" + i,
                    "SO" + i, order++,
                    ConstantsMecool.SellinModifyFlag.MODIFY.getType(),
                    ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                    ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                    ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                    ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                    ConstantsMecool.StepOptionType.DIRECT.getType(),
                    ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                    ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_OPEN.getType(),
                    ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                    null);
        }

        return order;
    }

    /**
     * 保存巡店 header
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersSellinStorePatrol(PageData project, long order) throws Exception {
        // 进店时间
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_ENTER_TIME.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_ENTER_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);

        // 离店时间
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_LEAVE_TIME.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_LEAVE_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 巡店花费时间
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_PATROL_TIME.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_PATROL_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.DOUBLE.getType(),
                null);
        // 巡店位置状态
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_PATROL_POSITION_STATUS.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_PATROL_POSITION_STATUS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.SelectType.POSITION_STATUS.getType());
        // 陈列照片
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_EXHIBIT_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_EXHIBIT_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 促销员照片
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_SALES_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_SALES_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 现场互动照片
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_SPOT_INTERACTION_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_SPOT_INTERACTION_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 竞品照片
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_COMPETITION_PHOTOS.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_COMPETITION_PHOTOS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);

        for (int i = 1; i < 101; i++) {
            saveSellinHeader(project, "巡店自定义字段" + i,
                    "SP" + i, order++,
                    ConstantsMecool.SellinModifyFlag.MODIFY.getType(),
                    ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                    ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                    ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                    ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                    ConstantsMecool.StepOptionType.DIRECT.getType(),
                    ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                    ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                    ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                    null);
        }
        // 巡店督导
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_EXEC_EMP_CODE.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_EXEC_EMP_CODE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 完成时间
        saveSellinHeader(project, ConstantsMecool.SellinStorePatrolFields.SP_UPLOAD_TIME.getHeaderName(),
                ConstantsMecool.SellinStorePatrolFields.SP_UPLOAD_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_STORE_PATROL.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        return order;
    }

    /**
     * 促销员考勤 header
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersSellinSalesAtt(PageData project, long order) throws Exception{
        // 促销员姓名
        saveSellinHeader(project, ConstantsMecool.SellinSalesAttFields.SA_SALES_NAME.getHeaderName(),
                ConstantsMecool.SellinSalesAttFields.SA_SALES_NAME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.V_PROJECT_SELLIN_SALES_ATT.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 考勤日期
        saveSellinHeader(project, ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_DATE.getHeaderName(),
                ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_DATE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.V_PROJECT_SELLIN_SALES_ATT.getType(),
                ConstantsMecool.SellinHeaderComType.DATE.getType(),
                null);
        // 计划排班上班时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesAttFields.SA_SALES_SCH_START_TIME.getHeaderName(),
                ConstantsMecool.SellinSalesAttFields.SA_SALES_SCH_START_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.V_PROJECT_SELLIN_SALES_ATT.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 计划排班下班时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesAttFields.SA_SALES_SCH_END_TIME.getHeaderName(),
                ConstantsMecool.SellinSalesAttFields.SA_SALES_SCH_END_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.V_PROJECT_SELLIN_SALES_ATT.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 考勤上班时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_START_TIME.getHeaderName(),
                ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_START_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.V_PROJECT_SELLIN_SALES_ATT.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 考勤下班时间
        saveSellinHeader(project, ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_END_TIME.getHeaderName(),
                ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_END_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.V_PROJECT_SELLIN_SALES_ATT.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 考勤位置
        saveSellinHeader(project, ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_POSITION.getHeaderName(),
                ConstantsMecool.SellinSalesAttFields.SA_SALES_ATT_POSITION.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.V_PROJECT_SELLIN_SALES_ATT.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.SelectType.POSITION_STATUS.getType());
        return order;
    }

    /**
     * QC header
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersSellinQC(PageData project, long order)throws Exception {
        // 本次巡检评分
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_SCORE.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_SCORE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 巡检时间
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_TIME.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 进店时间
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_ENTER_TIME.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_ENTER_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 离店时间
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_LEAVE_TIME.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_LEAVE_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 店内花费时间
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_CONSUMPTION_TIME.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_CONSUMPTION_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DOUBLE.getType(),
                null);
        // 位置状态
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_POSITION_STATUS.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_POSITION_STATUS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.SelectType.POSITION_STATUS.getType());
        // 巡检人
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_EMP_CODE.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_EMP_CODE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 证据附件下载
        saveSellinHeader(project, ConstantsMecool.SellinQCFields.QC_ATTACH_URLS.getHeaderName(),
                ConstantsMecool.SellinQCFields.QC_ATTACH_URLS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DOWN_LOAD.getType(),
                null);

        return order;
    }

    /**
     * QC header
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersSellinQCOnline(PageData project, long order)throws Exception {
        // 本次巡检评分
        saveSellinHeader(project, ConstantsMecool.SellinQCOnlineFields.QCO_SCORE.getHeaderName(),
                ConstantsMecool.SellinQCOnlineFields.QCO_SCORE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_ONLINE_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 巡检时间
        saveSellinHeader(project, ConstantsMecool.SellinQCOnlineFields.QCO_TIME.getHeaderName(),
                ConstantsMecool.SellinQCOnlineFields.QCO_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_ONLINE_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 进店时间
        saveSellinHeader(project, ConstantsMecool.SellinQCOnlineFields.QCO_ENTER_TIME.getHeaderName(),
                ConstantsMecool.SellinQCOnlineFields.QCO_ENTER_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_ONLINE_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 离店时间
        saveSellinHeader(project, ConstantsMecool.SellinQCOnlineFields.QCO_LEAVE_TIME.getHeaderName(),
                ConstantsMecool.SellinQCOnlineFields.QCO_LEAVE_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_ONLINE_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 店内花费时间
        saveSellinHeader(project, ConstantsMecool.SellinQCOnlineFields.QCO_CONSUMPTION_TIME.getHeaderName(),
                ConstantsMecool.SellinQCOnlineFields.QCO_CONSUMPTION_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_ONLINE_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DOUBLE.getType(),
                null);
        // 巡检人
        saveSellinHeader(project, ConstantsMecool.SellinQCOnlineFields.QCO_EMP_CODE.getHeaderName(),
                ConstantsMecool.SellinQCOnlineFields.QCO_EMP_CODE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_ONLINE_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 证据附件下载
        saveSellinHeader(project, ConstantsMecool.SellinQCOnlineFields.QCO_ATTACH_URLS.getHeaderName(),
                ConstantsMecool.SellinQCOnlineFields.QCO_ATTACH_URLS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.INTF_PROJECT_QC_ONLINE_INFO.getType(),
                ConstantsMecool.SellinHeaderComType.DOWN_LOAD.getType(),
                null);
        return order;
    }

    /**
     * 入库维护表 header  DM_MTRL_INPUT_WH
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersMtrlInputWarehouse(PageData project, long order)throws Exception {
        // 入库单号
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_INPUT_TICKETS.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_INPUT_TICKETS.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                ConstantsMecool.PatrolStoreReportType.BY_JOB.getType());
        // 入库时间
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_INPUT_TIME.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_INPUT_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 入库类别
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_INPUT_TYPE.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_INPUT_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.RADIO.getType(),
                ConstantsMecool.InputWarehouseType.DIRECT.getType());
        // 入库人
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_USER_ID.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_USER_ID.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 入库现场照片
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_SCENE_PHOTO.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_SCENE_PHOTO.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 入库签收照片
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_SIGN_PHOTO.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_SIGN_PHOTO.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);

        // 签收距离
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_SIGN_DISTANCE.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_SIGN_DISTANCE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.DOUBLE.getType(),
                null);

        // 上报方式
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_REPORT_TYPE.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_REPORT_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.PatrolStoreReportType.BY_JOB.getType());

        // 入库物料
        saveSellinHeader(project, ConstantsMecool.MtrlInputWarehouse.IW_MTRL_VLOUME_SET.getHeaderName(),
                ConstantsMecool.MtrlInputWarehouse.IW_MTRL_VLOUME_SET.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.ERGODIC_SUB_STEP.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_INPUT_WH.getType(),
                ConstantsMecool.SellinHeaderComType.RADIO.getType(),
                null);

        return order;
    }

    /**
     * 物料出库维护表 header
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersMtrlOutWhMng(PageData project, long order)throws Exception {
        // 出库时间
        saveSellinHeader(project, ConstantsMecool.MtrlOutWhMng.OW_OUT_TIME.getHeaderName(),
                ConstantsMecool.MtrlOutWhMng.OW_OUT_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_OUT_WH_MNG.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 任务完成人
        saveSellinHeader(project, ConstantsMecool.MtrlOutWhMng.OW_USER_ID.getHeaderName(),
                ConstantsMecool.MtrlOutWhMng.OW_USER_ID.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_OUT_WH_MNG.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 出库现场照片
        saveSellinHeader(project, ConstantsMecool.MtrlOutWhMng.OW_SCENE_PHOTO.getHeaderName(),
                ConstantsMecool.MtrlOutWhMng.OW_SCENE_PHOTO.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_OUT_WH_MNG.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 出库签收照片
        saveSellinHeader(project, ConstantsMecool.MtrlOutWhMng.OW_SIGN_PHOTO.getHeaderName(),
                ConstantsMecool.MtrlOutWhMng.OW_SIGN_PHOTO.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_OUT_WH_MNG.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);

        // 签收距离
        saveSellinHeader(project, ConstantsMecool.MtrlOutWhMng.OW_SIGN_DISTANCE.getHeaderName(),
                ConstantsMecool.MtrlOutWhMng.OW_SIGN_DISTANCE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_OUT_WH_MNG.getType(),
                ConstantsMecool.SellinHeaderComType.DOUBLE.getType(),
                null);

        // 上报方式
        saveSellinHeader(project, ConstantsMecool.MtrlOutWhMng.OW_REPORT_TYPE.getHeaderName(),
                ConstantsMecool.MtrlOutWhMng.OW_REPORT_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_OUT_WH_MNG.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.PatrolStoreReportType.BY_JOB.getType());

        // 出库物料
        saveSellinHeader(project, ConstantsMecool.MtrlOutWhMng.OW_MTRL_VLOUME_SET.getHeaderName(),
                ConstantsMecool.MtrlOutWhMng.OW_MTRL_VLOUME_SET.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.ERGODIC_SUB_STEP.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_OUT_WH_MNG.getType(),
                ConstantsMecool.SellinHeaderComType.RADIO.getType(),
                null);

        return order;
    }

    /**
     * 门店收货维护表 header  DM_MTRL_STORE_RECEIPT
     * @param project
     * @param order
     * @return
     */
    @SuppressWarnings("unused")
    private long saveHeadersMtrlStoreReceipt(PageData project, long order)throws Exception {
        // 签收时间
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_SIGN_TIME.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_SIGN_TIME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
                null);
        // 签收人
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_SIGN_USER_NAME.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_SIGN_USER_NAME.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.TEXT.getType(),
                null);
        // 任务完成人
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_USER_ID.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_USER_ID.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.EMP_STRUCTURE.getType(),
                null);
        // 收货现场照片
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_SCENE_PHOTO.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_SCENE_PHOTO.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 签收单照片
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_SIGN_PHOTO.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_SIGN_PHOTO.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.TAKE_PHOTO.getType(),
                null);
        // 签收人电话
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_SIGN_USER_PHONE.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_SIGN_USER_PHONE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.PHONE_NUMBER.getType(),
                null);
        // 签收人身份
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_SIGN_USER_IDENTITY.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_SIGN_USER_IDENTITY.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.SelectType.SIGN_USER_IDENTITY.getType());

        // 签收距离
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_SIGN_DISTANCE.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_SIGN_DISTANCE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.DOUBLE.getType(),
                null);

//    	// 任务完成日期
//    	saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_FINISHED_TIME.getHeaderName(),
//    			ConstantsMecool.MtrlStoreReceipt.SR_FINISHED_TIME.getColumnName(), order++,
//    			ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
//    			ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
//    			ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
//    			ConstantsMecool.SellinColumnIsOpenCustomer.OPEN.getType(),
//    			ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
//    			ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
//    			ConstantsMecool.SellinHeaderComType.DATE_TIME.getType(),
//    			null);
        // 上报方式
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_REPORT_TYPE.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_REPORT_TYPE.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.INIT_VALUE.getType(),
                ConstantsMecool.SellinColumnIsShow.SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.UN_SETTING.getType(),
                ConstantsMecool.StepOptionType.DIRECT.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.SELECT_LIST.getType(),
                ConstantsMecool.PatrolStoreReportType.BY_JOB.getType());

        // 收货物料
        saveSellinHeader(project, ConstantsMecool.MtrlStoreReceipt.SR_MTRL_VLOUME_SET.getHeaderName(),
                ConstantsMecool.MtrlStoreReceipt.SR_MTRL_VLOUME_SET.getColumnName(), order++,
                ConstantsMecool.SellinModifyFlag.NOT_MODIFY.getType(),
                ConstantsMecool.SellinSourceType.JOB_STEP.getType(),
                ConstantsMecool.SellinColumnIsShow.NOT_SHOW.getType(),
                ConstantsMecool.SellinColumnIsOpenCustomer.NOT_OPEN.getType(),
                ConstantsMecool.SellinColumnIsSetting.SETTING.getType(),
                ConstantsMecool.StepOptionType.ERGODIC_SUB_STEP.getType(),
                ConstantsMecool.IsShowPhoneType.NOT_SHOW.getType(),
                ConstantsMecool.SellinHeaderFlag.DM_MTRL_STORE_RECEIPT.getType(),
                ConstantsMecool.SellinHeaderComType.RADIO.getType(),
                null);

        return order;
    }



}
