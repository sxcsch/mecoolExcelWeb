package com.mecool.util;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2/28/2018.
 */
public class ConstantsMecool {
    public static final String CANGKU_CODE="1241";
    public static final int dataDownLoadSize = 10000;
    public static final String dataDownTitleInfo = "文件导出失败提醒";
    public static final String dataDownContentInfo = "导出文件的数量不能超过10000条数据,请重新选择查询条件进行分批下载";

    public static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SimpleDateFormat SIMPLE_DATE_FORMAT1 = new SimpleDateFormat("yyyy/MM/dd");
    public static SimpleDateFormat SIMPLE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat SIMPLE_TIMENOSS_FORMAT = new SimpleDateFormat("HH:mm");
    /** type of PhoneApkVersion. */
    public enum PhoneApkVersion {
        ALL(0, "0", "USING"),
        AREA(1, "1", "AREA"),
        ORG(2, "2", "ORG"),
        AREA_ORG(3, "3", "AREA_ORG");  // 复合区域+部门范围的督导 取交集
        PhoneApkVersion(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;
        public long getType() {
            return type;
        }
        public void setType(long type) {
            this.type = type;
        }
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getTypeName() {
            return typeName;
        }
        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

    }

    /** type of DeleteFlag. */
    public enum DeleteFlag {
        USING(0, "0", "USING"),
        DELETE(1, "1", "DELETE");
        DeleteFlag(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;
        public long getType() {
            return type;
        }
        public void setType(long type) {
            this.type = type;
        }
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getTypeName() {
            return typeName;
        }
        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

    }

    /** Organize type of dm_user.org_type. */
    public enum OrgType {
        /** Organize type of dm_user.org_type is company. */
        COMPANY("0", "COMPANY"),
        /** Organize type of dm_user.org_type is customer. */
        CUSTOMER("1", "CUSTOMER");
        OrgType(String type, String typeName) {
            this.type = type;
            this.typeName = typeName;
        }

        private String type;
        private String typeName;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** Role type of dm_sysrole. */
    public enum SysRole {
        /** Role type of dm_sysrole is admin. */
        ADMIN(8291l, "ADMIN"),
        /** Role type of dm_sysrole is user. */
        USER(8290l, "USER");
        SysRole(Long type, String typeName) {
            this.type = type;
            this.typeName = typeName;
        }

        private Long type;
        private String typeName;

        public Long getType() {
            return type;
        }

        public void setType(Long type) {
            this.type = type;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** Organize kind of md_org.org_kind. */
    public enum OrgKindType {
        /** Organize kind of md_org.org_kind is 1000. */
        COMPANY("1000", "COMPANY"),
        /** Organize kind of md_org.org_kind is 1001. */
        SUB_COMPANY("1001", "SUB_COMPANY"),
        /** Organize kind of md_org.org_kind is 1002. */
        DEPARTMENT("1002", "DEPARTMENT");
        OrgKindType(String type, String typeName) {
            this.type = type;
            this.typeName = typeName;
        }

        private String type;
        private String typeName;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** Organize type of dm_user.org_type. */
    public enum JobType {
        /** Job type 巡店 is 1001. */
        STORE_PATROL("1001", "STORE_PATROL"),
        /** Job type 培训 is 1002. */
        TRAINING("1002", "CUSTOMER"),
        /** Job type 后勤 is 1003. */
        LOGISTICS("1003", "LOGISTICS"),
        /** Job type 卖进 is 1004. */
        SELLING("1004", "SELLING"),
        /** Job type 开店 is 1005. */
        STORE_OPEN("1005", "STORE_OPEN"),
        /** Job type 物料运输任务 is 1006. */
        MTRL_TRANSPORT("1006", "MTRL_TRANSPORT"),
        /** Job type 其他 is 1009. */
        OTHER("1009", "OTHER");
        JobType(String type, String typeName) {
            this.type = type;
            this.typeName = typeName;
        }

        private String type;
        private String typeName;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** Organize type of dm_user.org_type. */
    public enum JobTypeClass {
        /** Job type class is 1001. */
        CLASS_1001 ("1001", "CLASS_1001");
        JobTypeClass(String type, String typeName) {
            this.type = type;
            this.typeName = typeName;
        }

        private String type;
        private String typeName;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 项目类型  DM_PROJECT_CATEGORY_MASTER
     *
     */
    public enum ProjectCategoryMaster {
        CATEGORY_MASTER_STORE_PATROL(1, "1", "店检"),
        CATEGORY_MASTER_STORE_SALE(2, "2", "店销"),
        CATEGORY_MASTER_ROAD_SHOW(3, "3", "路演"),
        CATEGORY_MASTER_DISTRIBUTE(4, "4", "派发"),
        CATEGORY_MASTER_OTHER(5, "5", "其他");
        ProjectCategoryMaster(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 卖进警告标识
     * 0:没有警告信息; 1: 未按时卖进; 2: 开店前没有招聘促销员
     */
    public enum SellinWarningFlag {
        NO_WARNING(0, "0", "没有警告信息"),
        UN_ON_TIME(1, "1", "未按时卖进"),
        NO_SALES(2, "2", "开店前没有招聘促销员");
        SellinWarningFlag(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }
    /**
     */
    public enum SelectType {
        OPEN_STORE_STATUS(1, "1", "开店状态"),
        COLUMN_TYPE(2, "2", "字段类型"),
        SELLIN_STATUS(3, "3", "卖进状态"),
        SELLIN_EXEC_STATUS(4, "4", "卖进执行状态"),
        COLUMN_IS_MODIFY(5, "5", "字段修改标识"),
        SELLIN_AUDIT_STATUS(6, "6", "卖进审核状态"),
        SELLIN_SOURCE_TYPE(7, "7", "来源类型"),
        SELLIN_COLUMN_FIXED(8, "8", "卖进固定字段"),
        COM_TYPE(9, "9", "组件类型"),
        SELLIN_COLUMN_IS_SHOW(10, "10", "卖进字段是否显示"),
        SELLIN_COLUMN_IS_OPEN_CUSTOMER(11, "11", "卖进字段是否开发给客户"),
        SELLIN_COLUMN_IS_SETTING(12, "12", "卖进字段是否可配置"),
        DATA_OPTION_FLAG(13, "13", "数据操作标记"),
        SALES_WORK_TYPE(15, "15", "促销员排班方式"),
        PATROL_STATUS(19, "19", "巡店状态"),
        SALES_ATTENDANCE_STATUS(20, "20", "考勤状态"),
        QC_STATUS(21, "21", "QC状态"),
        POSITION_STATUS(22, "22", "位置状态"),
        OPEN_STORE_REPORT_TYPE(26, "26", "上报方式"),
        BANK_CODE(30, "30", "开户银行"),
        MUST_AUDI(33, "33", "是否需要审核"),
        SIGN_USER_IDENTITY(41, "41", "签收人身份")
        ;
        SelectType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** The status of store open. */
    public enum StoreOpenStatus {
        UN_OPEN(0, "0", "未开档"),
        OPENED(1, "1", "已开档"),
        UN_ONTIME(2, "2", "未按时开档"),
        UN_PLANED(3, "3", "非计划开档");
        StoreOpenStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }
//
//	/** 卖进表头字段类型.
//	 * 0：String；1：Integer； 2：Double； 3：Date；  4：DateTime； 5: image
//	 */
//	public enum SellinHeaderType {
//		STRING(5, "5", "String"),
//		INTEGER(6, "6", "Integer"),
//		DOUBLE(7, "7", "Double"),
//		DATE(8, "8", "Date"),
//		DATETIME(9, "9", "DateTime"),
//		IMAGE(10, "10", "Image");
//		SellinHeaderType(int type, String code, String typeName) {
//			this.type = type;
//			this.code = code;
//			this.typeName = typeName;
//		}
//
//		private int type;
//		private String code;
//		private String typeName;
//
//		public int getType() {
//			return type;
//		}
//
//		public void setType(int type) {
//			this.type = type;
//		}
//
//		public String getCode() {
//			return code;
//		}
//
//		public void setCode(String code) {
//			this.code = code;
//		}
//
//		public String getTypeName() {
//			return typeName;
//		}
//
//		public void setTypeName(String typeName) {
//			this.typeName = typeName;
//		}
//	}

    /** The status of sellin status.
     * 0：未卖进；1：卖进中； 2：卖进成功； 3：无法卖进；  4：卖进取消； 5：未上报
     */
    public enum SellinStatus {
        UN_SELLIN(11, "11", "未卖进"),
        SELLIN_ING(12, "12", "卖进中"),
        SELLIN_SUCCESSED(13, "13", "卖进成功"),
        CANNOT_SELLIN(14, "14", "无法卖进"),
        CANCEL(15, "15", "卖进取消");


        SellinStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** The status of sellin execute status.
     * 0：从未执行； 1：正在执行； 2： 执行过已终止 ； 3：下周可执行   4：执行完毕
     */
    public enum SellinExecuteStatus {
        NEVER_EXEC(17, "17", "从未执行"),
        EXEC_ING(18, "18", "正在执行"),
        EXEC_BUT_TERMINATED(19, "19", "执行过已终止"),
        NEXT_WEEK_EXEC(20, "20", "下周可执行"),
        EXEC_FINISHED(50, "50", "执行完毕");
        SellinExecuteStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 字段对应的卖进数据字段是否可以修改标识.
     * 0：可以修改； 1：不能修改
     */
    public enum SellinModifyFlag {
        MODIFY(21, "21", "可以修改"),
        NOT_MODIFY(22, "22", "不能修改");
        SellinModifyFlag(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** The status of sellin audit status.
     * 0:未审核；1：审核
     */
    public enum SellinAuditStatus {
        UN_AUDIT(23, "23", "未审核"),
        AUDITED(24, "24", "已审核");
        SellinAuditStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 卖进任务关系表   来源类型
     */
    public enum SellinSourceType {
        INIT_VALUE(701, "701", "初始化"),
        PHONE_INPUT(702, "702", "手机卖进上报"),
        JOB_STEP(703, "703", "手机任务"),
        JOB_AND_PHONE(704, "704", "手机卖进上报和手机任务"),
        STATIC_VALUE(705, "705", "固定值"),
        SALES_SYSTEM_STORE_OPEN(706, "706", "促销员开店");
        SellinSourceType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 卖进固定字段
     *
     */
    public enum SellinFields {
        PROJECT_ID("PROJECT_ID","项目名称"),
        CHANNEL_SYNC_ID("CHANNEL_SYNC_ID", "系统终端编号"),
        CHANNEL_NAME("CHANNEL_NAME", "系统终端名称"),
        CHANNEL_PROVINCE_AD_NAME("CHANNEL_PROVINCE_AD_NAME", "省份"),
        CHANNEL_CITY_AD_NAME("CHANNEL_CITY_AD_NAME", "城市"),
        SCHEDULE_NUM("SCHEDULE_NUM", "卖进序号"),
        OLD_PLAN_START_TIME("OLD_PLAN_START_TIME", "原计划开始日期"),
        OLD_PLAN_FINISHED_TIME("OLD_PLAN_FINISHED_TIME", "原计划结束日期"),
        PLAN_START_TIME("PLAN_START_TIME", "计划开始日期"),
        PLAN_FINISHED_TIME("PLAN_FINISHED_TIME", "计划结束日期"),
        EMP_CODE("EMP_CODE", "门店督导编号"),
        EMP_NAME("EMP_NAME", "门店督导姓名"),
        CITY_EMP_CODE("CITY_EMP_CODE", "城市督导编号"),
        CITY_EMP_NAME("CITY_EMP_NAME", "城市督导姓名"),
        AREA_MANAGER_CODE("AREA_MANAGER_CODE", "区域负责人编号"),
        AREA_MANAGER_NAME("AREA_MANAGER_NAME", "区域负责人姓名"),
        //		SCHEDULE_TYPE("SCHEDULE_TYPE", "档期类型"),
        STATUS("STATUS", "卖进状态"),
        EXEC_STATUS("EXEC_STATUS", "执行状态"),
        EXEC_DATE_TIME_COUNT("EXEC_DATE_TIME_COUNT", "计划执行店次(店*天)"),
        PLAN_STORE_OPEN_COUNT("PLAN_STORE_OPEN_COUNT", "开档计划"),
        AUDIT_STATUS("AUDIT_STATUS", "审核状态"),
        EXEC_DATE_TIME("EXEC_DATE_TIME", "执行日期"),
        UPDATE_USER("UPDATE_USER", "更新用户"),
        UPDATE_TIME("UPDATE_TIME", "更新时间"),
        PLAN_SALES_COUNT("PLAN_SALES_COUNT", "计划促销员人数"),
        CUST_DEPUTY_NAME("CUST_DEPUTY_NAME", "客户代表姓名"),
        CUST_DEPUTY_PHONE("CUST_DEPUTY_PHONE", "客户代表手机"),
        CUST_DIRECTOR_NAME("CUST_DIRECTOR_NAME", "客户主管"),
        CUST_DIRECTOR_PHONE("CUST_DIRECTOR_PHONE", "客户主管电话"),
        CUST_MANAGER_NAME("CUST_MANAGER_NAME", "客户经理"),
        CUST_MANAGER_PHONE("CUST_MANAGER_PHONE", "客户经理电话"),
        SELLIN_CHANNEL("SELLIN_CHANNEL", "渠道"),

        //--卖进字段新增20160808
        CUST_CHANNEL("CUST_CHANNEL", "客户门店渠道"),
        CUST_SYS("CUST_SYS", "客户门店系统"),
        CUST_CHANNEL_CODE("CUST_CHANNEL_CODE", "客户门店编码"),
        CUST_CHANNEL_NAME("CUST_CHANNEL_NAME", "客户门店名称"),
        PLAN_EXEC_DATE_TIME("PLAN_EXEC_DATE_TIME", "计划执行天数"),
        PLAN_COM_EXEC_DATE_TIME("PLAN_COM_EXEC_DATE_TIME", "计划常规执行天数"),
        PLAN_THREE_PAY_EXEC_DATE_TIME("PLAN_THREE_PAY_EXEC_DATE_TIME", "计划三薪执行天数"),
        PLAN_EXEC_STORE_TIME("PLAN_EXEC_STORE_TIME", "计划执行场次数"),
        PLAN_COM_EXEC_STORE_TIME("PLAN_COM_EXEC_STORE_TIME", "计划常规执行场次数"),
        PLAN_THREE_PAY_EXEC_STORE_TIME("PLAN_THREE_PAY_EXEC_STORE_TIME", "计划三薪执行场次数"),

        PLAN_EXHIBIT_TYPE("PLAN_EXHIBIT_TYPE", "计划陈列类型"), // 计划陈列类型(多个以/分割)
        PLAN_EXHIBIT_START_TIME("PLAN_EXHIBIT_START_TIME", "计划陈列开始日期"), //计划陈列开始时间 (所有陈列最早开始时间)
        PLAN_EXHIBIT_END_TIME("PLAN_EXHIBIT_END_TIME", "计划陈列结束日期"), //计划陈列结束时间(所有陈列最晚结束时间)
        PLAN_EXHIBIT_NUM("PLAN_EXHIBIT_NUM", "计划陈列数量/面积"), //计划成列数量/面积(多个以/分割)

        PLAN_STORE_PATROL_COUNT("PLAN_STORE_PATROL_COUNT", "计划巡店总次数"),
        PLAN_WEEK_STORE_PATROL_COUNT("PLAN_WEEK_STORE_PATROL_COUNT", "周巡店次数"),
        PLAN_MONTH_STORE_PATROL_COUNT("PLAN_MONTH_STORE_PATROL_COUNT", "月巡店次数"),
        TOTAL_SALES_TARGET("TOTAL_SALES_TARGET", "总销量目标"),
        DAILY_SOTRE_SALES_TARGET("DAILY_SOTRE_SALES_TARGET", "日店均销量目标");

        SellinFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 卖进表头字段标识
     * 1：DM_PROJECT_SELLIN_INFO；2：DM_PROJECT_SELLIN_SALES
     */
    public enum SellinHeaderFlag {
        DM_PROJECT_SELLIN_INFO(1, "DM_PROJECT_SELLIN_INFO", "卖进信息表"),
        DM_PROJECT_SELLIN_SALES(2, "DM_PROJECT_SELLIN_SALES", "卖进促销员信息表"),
        DM_SALES_SCHEDULE_CALENDAR(3, "DM_SALES_SCHEDULE_CALENDAR", "卖进促销员排班信息表"),
        DM_PROJECT_SELLIN_STORE_OPEN(4, "DM_PROJECT_SELLIN_STORE_OPEN", "卖进开店信息表"),
        DM_PROJECT_SELLIN_STORE_PATROL(5, "DM_PROJECT_SELLIN_STORE_PATROL", "卖进巡店信息表"),
        V_PROJECT_SELLIN_SALES_ATT(6, "V_PROJECT_SELLIN_SALES_ATT", "促销员考勤信息表"),
        INTF_PROJECT_QC_INFO(7, "INTF_PROJECT_QC_INFO", "QC信息表"),
        INTF_PROJECT_QC_ONLINE_INFO(8, "INTF_PROJECT_QC_ONLINE_INFO", "QC在线信息表"),
        DM_MTRL_OUT_WH_MNG(9, "DM_MTRL_OUT_WH_MNG", "物料出库维护表"),
        DM_MTRL_STORE_RECEIPT(10, "DM_MTRL_STORE_RECEIPT", "门店收货维护表"),
        DM_MTRL_INPUT_WH(11, "DM_MTRL_INPUT_WH", "入库维护表"),
        RPT_SUM_PROJECT_SELLIN(20, "RPT_SUM_PROJECT_SELLIN", "累计汇总信息表");
        SellinHeaderFlag(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 促销员固定字段
     * DM_PROJECT_SELLIN_SALES
     */
    public enum SellinSalesFields {
        SALES_CARD_ID("SALES_CARD_ID", "促销员身份证"),
        SALES_NAME("SALES_NAME", "促销员姓名"),
        SALES_PHONE("SALES_PHONE", "促销员电话"),
        SALES_ADDRESS("SALES_ADDRESS", "促销员地址"),
        SALES_SALARY_CARD("SALES_SALARY_CARD", "工资卡号"),
        SALES_BANK_CODE("SALES_BANK_CODE", "开户银行"),
        //恢复的卖进促销员字段。
        SALES_SCHEDULE_TYPE("SALES_SCHEDULE_TYPE", "促销员排班类型"),
        SALES_WORK_START("SALES_WORK_START", "促销员上班时间"),
        SALES_WORK_END("SALES_WORK_END", "促销员下班时间"),
        SALES_EAT_START("SALES_EAT_START", "促销员吃饭开始时间"),
        SALES_EAT_END("SALES_EAT_END", "促销员吃饭结束时间"),

        SALES_MEMO("SALES_MEMO", "促销员备注");
        SellinSalesFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 促销员排班日历表 固定字段
     * 3 DM_SALES_SCHEDULE_CALENDAR  DM_SALES_SCHEDULE_WORK
     */
    public enum SellinSalesScheduleCalendarFields {
        SW_BEGIN_TIME("SW_BEGIN_TIME", "促销员上班时间"),
        SW_END_TIME("SW_END_TIME", "促销员下班时间"),
        SW_REST_BEGIN_TIME1("SW_REST_BEGIN_TIME1", "促销员吃饭开始时间"),
        SW_REST_END_TIME1("SW_REST_END_TIME1", "促销员吃饭结束时间"),
        SW_REST_BEGIN_TIME2("SW_REST_BEGIN_TIME2", "促销员吃饭开始时间2"),
        SW_REST_END_TIME2("SW_REST_END_TIME2", "促销员吃饭结束时间2"),
        SW_WORK_TYPE("SW_WORK_TYPE", "排班类型"),
        SC_SCHEDULE_DATE("SC_SCHEDULE_DATE", "排班日期");
        SellinSalesScheduleCalendarFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 卖进开店表 固定字段
     * 4
     */
    public enum SellinStoreOpenFields {
        SO_STATUS("SO_STATUS", "开店状态"),
        SO_REPORT_TYPE("SO_REPORT_TYPE", "上报方式"),
        SO_UPDATE_USER("SO_UPDATE_USER", "上报人"),
        SO_PLAN_OPEN_TIME("SO_PLAN_OPEN_TIME", "计划开店日期"),
        SO_PLAN_FINISH_TIME("SO_PLAN_FINISH_TIME", "计划完成时间"),
        SO_OPEN_TIME("SO_OPEN_TIME", "开店日期"),
        SO_OPEN_TYPE("SO_OPEN_TYPE", "开店形式"),
        SO_EXHIBIT_TYPE("SO_EXHIBIT_TYPE", "陈列形式"),
        SO_ENTER_TIME("SO_ENTER_TIME", "进店时间"),
        SO_LEAVE_TIME("SO_LEAVE_TIME", "离店时间"),
        SO_PATROL_TIME("SO_PATROL_TIME", "开店花费时间"),
        SO_PATROL_POSITION_STATUS("SO_PATROL_POSITION_STATUS", "开店位置状态"),
        SO_EXHIBIT_PHOTOS("SO_EXHIBIT_PHOTOS", "陈列照片"),
        SO_SALES_PHOTOS("SO_SALES_PHOTOS", "促销员照片"),
        SO_EXEC_EMP_CODE("SO_EXEC_EMP_CODE", "开店督导"),
        SO_SPECIAL_SITUATION("SO_SPECIAL_SITUATION", "特殊报备"),
        SO_UPLOAD_TIME("SO_UPLOAD_TIME", "完成时间"),
        SO_SALES_EXHIBIT_TYPE("SO_SALES_EXHIBIT_TYPE", "陈列形式1"),
        SO_SALES_EXHIBIT_PHOTOS("SO_SALES_EXHIBIT_PHOTOS", "陈列照片1"),
        SO_SALES_OPEN_PHOTOS("SO_SALES_OPEN_PHOTOS", "促销员照片1"),
        SO_SALES_ACTION_PHOTOS("SO_SALES_ACTION_PHOTOS", "现场活动照片1"),
        SO_SALES_COMPET_PHOTOS("SO_SALES_COMPET_PHOTOS", "竞品照片1"),
        SO_SALES_OPEN_CODE("SO_SALES_OPEN_CODE", "促销员编号1"),
        SO_SALES_REPORT_TIME("SO_SALES_REPORT_TIME", "上报日期时间1"),
        SO_SALES_QUESTION_FEEDBACK("SO_SALES_QUESTION_FEEDBACK", "特殊报备1");
        SellinStoreOpenFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 卖进巡店表 固定字段
     * 5 DM_PROJECT_SELLIN_STORE_PATROL
     */
    public enum SellinStorePatrolFields {
        SP_ENTER_TIME("SP_ENTER_TIME", "进店时间"),
        SP_LEAVE_TIME("SP_LEAVE_TIME", "离店时间"),
        SP_PATROL_TIME("SP_PATROL_TIME", "巡店花费时间"),
        SP_PATROL_POSITION_STATUS("SP_PATROL_POSITION_STATUS", "巡店位置状态"),
        SP_EXHIBIT_PHOTOS("SP_EXHIBIT_PHOTOS", "陈列照片"),
        SP_SALES_PHOTOS("SP_SALES_PHOTOS", "促销员照片"),
        SP_SPOT_INTERACTION_PHOTOS("SP_SPOT_INTERACTION_PHOTOS", "现场互动照片"),
        SP_COMPETITION_PHOTOS("SP_COMPETITION_PHOTOS", "竞品照片"),
        SP_EXEC_EMP_CODE("SP_EXEC_EMP_CODE", "巡店督导"),
        SP_UPLOAD_TIME("SP_UPLOAD_TIME", "完成时间"),
        SP_JOB_NAME("SP_JOB_NAME", "任务名称");
        SellinStorePatrolFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** V_PROJECT_SELLIN_SALES_ATT 固定字段
     * 6
     */
    public enum SellinSalesAttFields {
        SA_SALES_NAME("SA_SALES_NAME", "促销员姓名"),
        SA_SALES_ATT_DATE("SA_SALES_ATT_DATE", "考勤日期"),
        SA_SALES_SCH_START_TIME("SA_SALES_SCH_START_TIME", "计划排班上班时间"),
        SA_SALES_SCH_END_TIME("SA_SALES_SCH_END_TIME", "计划排班下班时间"),
        SA_SALES_ATT_START_TIME("SA_SALES_ATT_START_TIME", "考勤上班时间"),
        SA_SALES_ATT_END_TIME("SA_SALES_ATT_END_TIME", "考勤下班时间"),
        SA_SALES_ATT_POSITION("SA_SALES_ATT_POSITION", "考勤位置");
        //		SA_TYPE("SA_TYPE", "结果类型");
//		SA_SALES_ATT_LONGITUDE("SA_SALES_ATT_LONGITUDE", "经度"),
//		SA_SALES_ATT_LATITUDE("SA_SALES_ATT_LATITUDE", "纬度");
        SellinSalesAttFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** INTF_PROJECT_QC_INFO 固定字段
     * 7 INTF_PROJECT_QC_INFO
     */
    public enum SellinQCFields {
        QC_SCORE("QC_SCORE", "本次巡检评分"),
        QC_TIME("QC_TIME", "巡检时间"),
        QC_ENTER_TIME("QC_ENTER_TIME", "进店时间"),
        QC_LEAVE_TIME("QC_LEAVE_TIME", "离店时间"),
        QC_CONSUMPTION_TIME("QC_CONSUMPTION_TIME", "店内时间"),
        QC_POSITION_STATUS("QC_POSITION_STATUS", "位置状态"),
        QC_EMP_CODE("QC_EMP_CODE", "巡检人"),
        QC_ATTACH_URLS("QC_ATTACH_URLS", "证据附件下载");
        //		QC_LONGITUDE("QC_LONGITUDE", "经度"),
//		QC_LATITUDE("QC_LATITUDE", "纬度");
        SellinQCFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** INTF_PROJECT_QC_ONLINE_INFO 固定字段
     * 7 INTF_PROJECT_QC_ONLINE_INFO
     */
    public enum SellinQCOnlineFields {
        QCO_SCORE("QCO_SCORE", "本次巡检评分"),
        QCO_TIME("QCO_TIME", "巡检时间"),
        QCO_ENTER_TIME("QCO_ENTER_TIME", "进店时间"),
        QCO_LEAVE_TIME("QCO_LEAVE_TIME", "离店时间"),
        QCO_CONSUMPTION_TIME("QCO_CONSUMPTION_TIME", "店内时间"),
        QCO_EMP_CODE("QCO_EMP_CODE", "巡检人"),
        QCO_ATTACH_URLS("QCO_ATTACH_URLS", "证据附件下载");
        SellinQCOnlineFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 物料出库维护表 固定字段
     * 9 DM_MTRL_OUT_WH_MNG
     */
    public enum MtrlOutWhMng {
        OW_OUT_TIME("OW_OUT_TIME", "出库时间"),
        OW_USER_ID("OW_USER_ID", "任务完成人"),
        OW_SCENE_PHOTO("OW_SCENE_PHOTO", "出库现场照片"),
        OW_SIGN_PHOTO("OW_SIGN_PHOTO", "出库签收照片"),
        OW_SIGN_DISTANCE("OW_SIGN_DISTANCE", "签收距离"),
        OW_FINISHED_TIME("OW_FINISHED_TIME", "任务完成时间"),
        OW_AUDI_STATUS("OW_AUDI_STATUS", "审核状态"),
        OW_AUDI_USER_ID("OW_AUDI_USER_ID", "审核人"),
        OW_AUDI_TIME("OW_AUDI_TIME", "审核时间"),
        OW_AUDI_OPINION("OW_AUDI_OPINION", "审核意见"),
        OW_REPORT_TYPE("OW_REPORT_TYPE", "上报方式"),
        OW_MTRL_VLOUME_SET("OW_MTRL_VLOUME_SET", "出库物料");
        MtrlOutWhMng(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 门店收货维护表 固定字段
     * 10 DM_MTRL_STORE_RECEIPT
     */
    public enum MtrlStoreReceipt {
        SR_CHANNEL_ID("SR_CHANNEL_ID", "门店"),
        SR_SIGN_TIME("SR_SIGN_TIME", "签收时间"),
        SR_SIGN_USER_NAME("SR_SIGN_USER_NAME", "签收人"),
        SR_USER_ID("SR_USER_ID", "任务完成人"),
        SR_SCENE_PHOTO("SR_SCENE_PHOTO", "收货现场照片"),
        SR_SIGN_PHOTO("SR_SIGN_PHOTO", "签收单照片"),
        SR_SIGN_USER_PHONE("SR_SIGN_USER_PHONE", "签收人电话"),
        SR_SIGN_USER_IDENTITY("SR_SIGN_USER_IDENTITY", "签收人身份"),
        SR_SIGN_DISTANCE("SR_SIGN_DISTANCE", "签收距离"),
        SR_FINISHED_TIME("SR_FINISHED_TIME", "任务完成时间"),
        SR_AUDI_STATUS("SR_AUDI_STATUS", "审核状态"),
        SR_AUDI_USER_ID("SR_AUDI_USER_ID", "审核人"),
        SR_AUDI_TIME("SR_AUDI_TIME", "审核时间"),
        SR_AUDI_OPINION("SR_AUDI_OPINION", "审核意见"),
        SR_REPORT_TYPE("SR_REPORT_TYPE", "上报方式"),
        SR_MTRL_VLOUME_SET("SR_MTRL_VLOUME_SET", "收货物料");
        MtrlStoreReceipt(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 入库维护表 固定字段
     * 11 DM_MTRL_INPUT_WH
     */
    public enum MtrlInputWarehouse {
        IW_CHANNEL_ID("IW_CHANNEL_ID", "仓库"),
        IW_INPUT_TICKETS("IW_INPUT_TICKETS", "入库单号"),
        IW_INPUT_TIME("IW_INPUT_TIME", "入库时间"),
        IW_INPUT_TYPE("IW_INPUT_TYPE", "入库类别"),
        IW_USER_ID("IW_USER_ID", "入库人"),
        IW_SCENE_PHOTO("IW_SCENE_PHOTO", "入库现场照片"),
        IW_SIGN_PHOTO("IW_SIGN_PHOTO", "入库签收单照片"),
        IW_SIGN_DISTANCE("IW_SIGN_DISTANCE", "签收距离"),
        IW_FINISHED_TIME("IW_FINISHED_TIME", "任务完成时间"),
        IW_ATTACHMENT("IW_ATTACHMENT", "附件"),
        IW_REPORT_TYPE("IW_REPORT_TYPE", "上报方式"),
        IW_AUDI_STATUS("IW_AUDI_STATUS", "审核状态"),
        IW_AUDI_USER_ID("IW_AUDI_USER_ID", "审核人"),
        IW_AUDI_TIME("IW_AUDI_TIME", "审核时间"),
        IW_AUDI_OPINION("IW_AUDI_OPINION", "审核意见"),
        IW_MTRL_VLOUME_SET("IW_MTRL_VLOUME_SET", "入库物料");
        MtrlInputWarehouse(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 累计 汇总表 固定字段
     * 20  RPT_SUM_PROJECT_SELLIN
     */
    public enum SellinSumFields {
        SUM_PLAN_STORE_OPEN_COUNT("SUM_PLAN_STORE_OPEN_COUNT", "计划开店次数"),
        SUM_STORE_OPEN_STATUS("SUM_STORE_OPEN_STATUS", "开店状态"),
        SUM_STORE_OPEN_COUNT("SUM_STORE_OPEN_COUNT", "开店次数"),
        SUM_STORE_PATROL_STATUS("SUM_STORE_PATROL_STATUS", "巡店状态"),
        SUM_PATROL_COUNT("SUM_PATROL_COUNT", "巡店次数"),
        SUM_SALES_ATT_STATUS("SUM_SALES_ATT_STATUS", "考勤状态"),
        SUM_ATT_DATE_COUNT("SUM_ATT_DATE_COUNT", "考勤天数"),
        SUM_ATT_SCH_DATE_COUNT("SUM_ATT_SCH_DATE_COUNT", "排班天数"),
        SUM_QC_STATUS("SUM_QC_STATUS", "是否实地QC"),
        SUM_QC_COUNT("SUM_QC_COUNT", "实地QC次数"),
        SUM_QC_AVG_SCORE("SUM_QC_AVG_SCORE", "QC评分"),
        SUM_QC_ONLINE_STATUS("SUM_QC_ONLINE_STATUS", "是否在线QC"),
        SUM_QC_ONLINE_COUNT("SUM_QC_ONLINE_COUNT", "在线QC次数");
        SellinSumFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** 卖进表头字段 COM_TYPE=9 组件类型
     */
    public enum SellinHeaderComType {
        TEXT(39L, "39", "输入文本"),
        INTEGER(40L, "40", "输入数字"),
        DOUBLE(41L, "41", "输入双精度数字"),
        PHONE_NUMBER(42L, "42", "输入电话号码"),
        ID_CARD(43L, "43", "输入身份证号码"),
        DATE(44L, "44", "日期选择"),
        DATE_TIME(45L, "45", "日期时间选择"),
        SELECT_LIST(46L, "46", "下拉列表选择"),
        TAKE_PHOTO(47L, "47", "拍摄图片"),
        EMP_STRUCTURE(48L, "48", "人员组织架构"),
        URL_TYPE(49L, "49", "图片类型"),
        RADIO(9050L, "9050", "单选类型"),
        DOWN_LOAD(9051L, "9051", "下载类型");
        SellinHeaderComType(Long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private Long type;
        private String code;
        private String typeName;

        public Long getType() {
            return type;
        }

        public void setType(Long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 卖进字段是否显示
     */
    public enum SellinColumnIsShow {
        SHOW(1001, "1001", "显示"),
        NOT_SHOW(1002, "1002", "不显示");
        SellinColumnIsShow(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 卖进字段是否开发给客户 IS_OPEN_CUSTOMER
     */
    public enum SellinColumnIsOpenCustomer {
        OPEN(1101, "1101", "开放"),
        NOT_OPEN(1102, "1102", "不开放");
        SellinColumnIsOpenCustomer(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 卖进字段是否可配置 IS_SETTING
     */
    public enum SellinColumnIsSetting {
        SETTING(1201, "1201", "可配置"),
        UN_SETTING(1202, "1202", "不可配置");
        SellinColumnIsSetting(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 数据操作标记 DATA_OPTION_FLAG
     */
    public enum DataOptionFlag {
        PHONE_ADD(1301, "1301", "手机增加"),
        PHONE_UPDATE(1302, "1302", "手机修改"),
        PHONE_DELETE(1303, "1303", "手机删除"),
        PAGE_ADD(1311, "1311", "页面增加"),
        PAGE_UPDATE(1312, "1312", "页面修改"),
        PAGE_DELETE(1313, "1313", "页面删除");
        DataOptionFlag(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 数据操作标记 DATA_OPTION_FLAG
     */
    public enum SellinOptionFlag {
        PHONE_SELLIN_MODIFY(3401, "3401", "手机卖进修改"),
        PHONE_EXECDATE_MODIFY(3402, "3402", "手机执行场次修改"),
        PHONE_SALES_ADD(3403, "3403", "手机促销员新增"),
        PHONE_SALES_MODIFY(3404, "3404", "手机促销员修改"),
        PHONE_SALES_DELETE(3405, "3405", "手机促销员删除"),

        PAGE_SELLIN_LOAD_ADD(3411, "3411", "页面卖进导入新增"),
        PAGE_SELLIN_LOAD_MODIFY(3412, "3412", "页面卖进导入修改"),
        PAGE_SELLIN_MODIFY(3413, "3413", "页面卖进修改"),
        PAGE_SELLIN_DELETE(3414, "3414", "页面卖进删除"),
        PAGE_EXECDATE_MODIFY(3415, "3415", "页面执行场次修改"),
        PAGE_SALES_ADD(3416, "3416", "页面促销员新增"),
        PAGE_SALES_MODIFY(3417, "3417", "页面促销员修改"),
        PAGE_SALES_DELETE(3418, "3418", "页面机促销员删除"),
        PAGE_SALES_SCHEDULE(3419, "3419", "页面促销员排班修改"),
        PAGE_STOREOPEN_LOAD(3420, "3420", "页面开档日导入"),
        PAGE_STOREOPEN_MODIFY(3421, "3421", "页面开档日修改");

        SellinOptionFlag(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 巡店状态
     */
    public enum PatrolStatus {
        PATROL(1901, "1901", "已巡店"),
        UN_PATROL(1902, "1902", "未巡店");
        PatrolStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 考勤状态
     */
    public enum SalesAttendanceStatus {
        Attendanced(2001, "2001", "已考勤"),
        UN_Attendance(2002, "2002", "未考勤");
        SalesAttendanceStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** QC状态
     */
    public enum QCStatus {
        QCed(2101, "2101", "已QC"),
        UN_QC(2102, "2102", "未QC"),
        LOCAL_QC(2103, "2103", "实地QC"),
        ONLINE_QC(2104, "2104", "在线QC");
        QCStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 位置状态
     */
    public enum PositionStatus {
        NORMAL(2201, "2201", "正常"),
        EXCEPTION(2202, "2202", "异常"),
        UN_GET_GPS(2203, "2203", "未获取GPS"),
        REFUSE_GPS(2204, "2204", "用户拒绝共享GPS"),
        NA(2205, "2205", "NA"),
        UN_ATT(2206, "2206", "未考勤"),
        UN_GET_9(2209, "2209", "未获取"),;
        PositionStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 位置状态
     */
    public enum MdPositionStatus {
        NORMAL(0, "0", "正常"),
        EXCEPTION(1, "1", "异常"),
        UN_GET_GPS(2, "2", "未获取GPS"),
        REFUSE_GPS(3, "3", "用户拒绝共享GPS"),
        NA(4, "4", "NA"),
        UN_ATT(5, "5", "未考勤"),
        UN_GET_9(9, "9", "未获取");
        MdPositionStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 开店形式
     */
    public enum OpenStoreType {
        UN_LOCALE_OS(2301, "2301", "非现场开店"),
        LOCALE_OS(2302, "2302", "现场开店");
        OpenStoreType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 巡店上报类型 （任务上报；手工上报）
     *
     */
    public enum PatrolStoreReportType {
        BY_JOB(2601, "2601", "任务上报"),
        BY_MANUAL(2602, "2602", "手工上报");
        PatrolStoreReportType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 上报类型 （任务上报；手工上报）
     *
     */
    public enum ReportType {
        BY_JOB(2601, "2601", "任务上报"),
        BY_MANUAL(2602, "2602", "手工上报");
        ReportType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 开店上报类型 （任务上报；手工上报）
     *
     */
    public enum OpenStoreReportType {
        BY_JOB(2601, "2601", "任务上报"),
        BY_MANUAL(2602, "2602", "手工上报");
        OpenStoreReportType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 促销员类型
     *
     */
    public enum SalesType {
        LONG(4301, "4301", "长促"),
        SHORT(4302, "4302", "短促");
        SalesType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 开店形式
     */

    public enum UrlValue {
        IMG_URL(1,"http://mcms.mecoolchina.com:18081/upload", "图片地址"),
        WX_IMG_URL(2,"http://120.55.166.247:9191/fileupload","微信图片地址");
        UrlValue(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }
        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 用户职位
     *
     */
    public enum EmpPosition {
        BOM(2701, "2701", "BOM"),
        BRL(2702, "2702", "BRL"),
        CL(2703, "2703", "CL"),
        EL(2704, "2704", "EL"),
        NEL(2105, "2105", "NEL");
        EmpPosition(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** GPS 记录
     *
     */
    public enum RecordGpsType {
        UN_RECORD(2901, "2901", "不记录"),
        RECORD(2902, "2902", "记录");
        RecordGpsType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 审核状态
     *
     */
    public enum AudiStatus {
        UN_AUDI(3101, "3101", "未审核"),
        AUDI_SUCCESSED(3102, "3102", "审核成功"),
        AUDI_FAILURE(3103, "3103", "审核不成功");
        AudiStatus(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 项目是否需要审核审核
     *
     */
    public enum MustAudi {
        UN_NEED(3301, "3301", "不必审核"),
        MUSTBE(3302, "3302", "必须审核");
        MustAudi(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 员工类型
     * 299  EmpTypeFields
     */
    public enum EmpTypeFields {
        EmpTypeFields_CXY("1001", "促销员"),
        EmpTypeFields_NBYG("1002", "内部员工"),
        EmpTypeFields_KHJKR("1003", "客户接口人"),
        EmpTypeFields_YYRY("1004", "演艺人员"),
        EmpTypeFields_QTLSYG("1005", "其他临时员工"),
        EmpTypeFields_QT("1006", "其他");

        EmpTypeFields(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private String code;
        private String name;
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

    }


    /** 员工状态
     * 299  EmpStatusFields
     */
    public enum EmpStatusFields {
        EmpStatusFields_ZAIGANG("3000", "在岗"),
        EmpStatusFields_DAIGANG("3001", "待岗"),
        EmpStatusFields_LIZHI("3002", "离职");

        EmpStatusFields(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }
    /**员工职级*/
    public enum EmpRank {

        EmpRank_GLC(4006,"4006","管理层"),
        EmpRank_ZJ(4007,"4007","总监"),
        EmpRank_ZLZJ(4008,"4008","助理总监"),
        EmpRank_GJJL(4009,"4009","高级经理"),
        EmpRank_JL(4010,"4010","经理"),
        EmpRank_ZLJL(4011,"4011","助理经理"),
        EmpRank_GJZG(4012,"4012","高级主管"),
        EmpRank_ZG(4013,"4013","主管"),
        EmpRank_ZLZG(4014,"4014","助理主管"),
        EmpRank_YG(4015,"4015","员工");

        EmpRank(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**员工所属职位*/
    public enum EmpPositionFields {
        EmpPosition_BOM(2701, "2701", "BOM"),
        EmpPosition_BRL(2702, "2702", "BRL"),
        EmpPosition_CL(2703, "2703", "CL"),
        EmpPosition_EL(2704, "2704", "EL"),
        EmpPosition_NEL(2705, "2705", "NEL");


        EmpPositionFields(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**
     *指标类型
     */
    public enum ZhiBiaoType {
        //页面显示汉字使用
        ZhiBiaoType_01("01", "当月执行门店数"),
        ZhiBiaoType_02("02", "开店率"),
        ZhiBiaoType_03("03", "促销员考勤打卡率"),
        ZhiBiaoType_04("04", "现场巡店任务数"),
        ZhiBiaoType_05("05", "非现场巡店任务数"),
        ZhiBiaoType_06("06", "责任门店巡店频率(场次覆盖率)"),
        ZhiBiaoType_07("07", "责任门店巡店覆盖率"),

        ZhiBiaoType_08("08", "现场开店任务数"),
        ZhiBiaoType_09("09", "非现场开店任务数"),
        ZhiBiaoType_10("10", "实地QC店次"),
        ZhiBiaoType_11("11", "实地QC平均分"),
        ZhiBiaoType_12("12", "缺岗率"),
        ZhiBiaoType_13("13", "在线QC店次"),
        ZhiBiaoType_14("14", "在线QC平均分"),

        ZhiBiaoType_15("15", "本月计划原始场次（人*店*天）"),
        ZhiBiaoType_16("16", "本月完成原始场次（人*店*天）"),
        ZhiBiaoType_17("17", "本月原始场次完成率"),
        ZhiBiaoType_18("18", "本月完成原始店次（店*天）"),
        ZhiBiaoType_19("19", "本月完成标准店次（店*天）");

        ZhiBiaoType(String columnName, String headerName) {
            this.columnName = columnName;
            this.headerName = headerName;
        }

        private String columnName;
        private String headerName;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getHeaderName() {
            return headerName;
        }
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /**设备覆盖状态*/
    public enum deviceCoverStatus {

        deviceCovered(0, "0", "设备已覆盖"),
        deviceUnCovered(1, "1", "设备未覆盖");


        deviceCoverStatus(Integer type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private Integer type;
        private String code;
        private String typeName;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**促销员排面状态*/
    public enum salesShrimpRowStatus {
        OutShrimpRow(0, "0", "不在排面"),
        InShrimpRow(1, "1", "在排面"),
        OutBluetooth(2, "2", "蓝牙未开启"),
        NoScanResults(3, "3", "没有扫描结果");


        salesShrimpRowStatus(Integer type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private Integer type;
        private String code;
        private String typeName;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }


    /**促销员在岗状态*/
    public enum salesOnJobStatus {
        Working(-1, "-1", "上班"),
        Resting(-2, "-2", "休息"),
        Training(-3, "-3", "参加培训"),
        SickLeave(-4, "-4", "病假"),
        Absentee(0, "0", "未上班"),
        OffDuty(1, "1", "下班"),
        OnShrimpRow(2, "2", "在排面"),
        offShrimpRow_Tally(3, "3", "不在排面-理货"),
        offShrimpRow_Dining(4, "4", "不在排面-用餐"),
        offShrimpRow_Other(5, "5", "不在排面-其他"),
        GotoMeeting(6, "6", "参加公司例会");


        salesOnJobStatus(Integer type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private Integer type;
        private String code;
        private String typeName;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }


    /**物料类别*/
    public enum MaterialTypes {
        materialTypes_01(3501,"赠品"),
        materialTypes_02(3502,"产品赠品"),
        materialTypes_03(3503,"POSM"),
        materialTypes_04(3504,"产品"),
        materialTypes_05(3505,"返货产品"),
        materialTypes_06(3506,"返货产品赠品"),
        materialTypes_07(3507,"制服");
        MaterialTypes(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
        private Integer id;
        private String name;
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    /** 入库类别
     *
     */
    public enum InputWarehouseType {
        DIRECT(3801, "3801", "到货"),
        ERGODIC_SUB_STEP(3802, "3802", "回仓");
        InputWarehouseType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 任务步骤解析类型
     *
     */
    public enum StepOptionType {
        DIRECT(3901, "3901", "直接解析"),
        ERGODIC_SUB_STEP(3902, "3902", "子步骤遍历");
        StepOptionType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 手机报表是否显示
     *
     */
    public enum IsShowPhoneType {
        SHOW(4201, "4201", "显示"),
        NOT_SHOW(4202, "4202", "不显示");
        IsShowPhoneType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 位置距离
     */
    public enum PositionDistance {
        SUPERVISOR_NORMAL(500, "正常"),
        SALES_NORMAL(800, "正常"),
        SALES_2000(2000, "2000");
        PositionDistance(int distance, String distanceName) {
            this.distance = distance;
            this.distanceName = distanceName;
        }

        private int distance;
        private String distanceName;
        public int getDistance() {
            return distance;
        }
        public void setDistance(int distance) {
            this.distance = distance;
        }
        public String getDistanceName() {
            return distanceName;
        }
        public void setDistanceName(String distanceName) {
            this.distanceName = distanceName;
        }

    }

    /** 报表区域限制
     */
    public enum UseADFilter {
        NO_FILTER(0, "0", "没有区域限制"),
        FILTER(1, "1", "区域限制");
        UseADFilter(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /** 执行报表
     */
    public enum CustCityExecReportType {
        SELLIN(1, "1", "卖进执行"),
        STORE_OPEN(2, "2", "开档统计"),
        EXEC_DATE(3, "3", "执行场次统计"),
        STORE_PATROL(4, "4", "巡店统计"),
        QC(5,"5","QC覆盖率统计");

        CustCityExecReportType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    public enum FaceCompareType{

        IMG_IMG(1, "1", "照片与照片比对"),
        IMG_VEDIO(3, "3", "照片与视频比对");

        FaceCompareType(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

    }
    public enum FaceCompareSys{

        DDAPP(1, "1", "督导APP"),
        MK(2, "2", "米咖");

        FaceCompareSys(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

    }
    public enum UserRole{
        BUL(1000, "1000", "BUL"),
        EXEC_MNG(1001, "1001", "执行网负责人"),
        PM(1002, "1002", "PM"),
        TL(1003, "1003", "TL"),
        PL(1004, "1004", "PL"),
        NPL(1005, "1005", "NPL"),
        AREA_EMP(1006, "1006", "区域督导"),
        CITY_EMP(1007, "1007", "城市督导"),
        STORE_EMP(1008, "1008", "门店督导"),
        CNETER_CUST(1009, "1009", "总部客户"),
        AREA_CUST(1010, "1010", "区域客户");

        UserRole(long type, String code, String typeName) {
            this.type = type;
            this.code = code;
            this.typeName = typeName;
        }

        private long type;
        private String code;
        private String typeName;

        public long getType() {
            return type;
        }

        public void setType(long type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

    }
}
