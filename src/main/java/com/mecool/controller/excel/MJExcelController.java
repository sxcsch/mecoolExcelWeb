package com.mecool.controller.excel;

import com.alibaba.druid.support.json.JSONUtils;
import com.mecool.controller.base.BaseController;
import com.mecool.entity.Application;
import com.mecool.entity.MJEntity;
import com.mecool.entity.Page;
import com.mecool.service.HeardExcelService;
import com.mecool.service.MJExcelService;
import com.mecool.thread.MJThread;
import com.mecool.util.*;
import net.sf.json.JSONObject;
import oracle.sql.TIMESTAMP;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author eddy
 * Created by Eddy on 2/24/2018.
 */
@Controller
@RequestMapping("/excel")
public class MJExcelController extends BaseController {

    protected Logger logger = Logger.getLogger(this.getClass());
    @Autowired
    private MJExcelService mjExcelService;
    @Autowired
    private HeardExcelService heardExcelService;

    private Application application = new Application();
    @RequestMapping("/mjUpload")
    @SuppressWarnings("unused")
    public ModelAndView uploadFile(ModelAndView mv){
        mv.setViewName("service/uploadexcel");
        PageData pd = this.getPageData();
        mv.addObject("data",JSONUtils.toJSONString("mj"));
        mv.addObject("empPk",pd.getString("empPk"));
        mv.addObject("projectId",pd.getString("projectId"));
       return mv;
    }
    /**
     * 从EXCEL导入到数据库
     */
    @RequestMapping(value="/readMJExcel")
    @SuppressWarnings("unused")
    public ModelAndView readMJExcel(@RequestParam(value="service",required=false) MultipartFile file, HttpServletRequest request) throws Exception{
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        MJEntity mjEntity = new MJEntity();
        mjEntity.setProjectId(pd.getString("projectId"));
        mjEntity.setEmpPk(pd.getString("empPk"));
        List<PageData> listPd = null;
        if (null != file && !file.isEmpty()) {
            String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE;								//文件上传路径
            String fileName =  FileUpload.fileUp(file, filePath, "userexcel");							//执行上传
            listPd = (List) ObjectExcelRead.readExcel(filePath, fileName, 0, 0, 0);	//执行读EXCEL操作,读出的数据导入List 2:从第3行开始；0:从第A列开始；0:第0个sheet
            if (listPd.size()<1||listPd.size()==0){
                mv.setViewName("service/error");
                mv.addObject("msg","数据缺失！");
                mv.addObject("empPk",mjEntity.getEmpPk());
                mv.addObject("projectId",mjEntity.getProjectId());
                return mv;
            }
            if (mjEntity.getProjectId()!=null&&mjEntity.getProjectId().equals("")||mjEntity.getEmpPk()!=null&&mjEntity.getEmpPk().equals("")){
                mv.setViewName("service/error");
                mv.addObject("msg","异常打开方式，缺少参数！");
                mv.addObject("empPk",mjEntity.getEmpPk());
                mv.addObject("projectId",mjEntity.getProjectId());
                return mv;
            }
            PageData projectD = new PageData();
            projectD.put("PROJECT_ID",mjEntity.getProjectId());
            PageData projectDs  = mjExcelService.findProjectById(projectD);
            if (projectDs==null||projectDs.size()==0){
                mv.setViewName("service/error");
                mv.addObject("msg","项目不存在！");
                mv.addObject("empPk",mjEntity.getEmpPk());
                mv.addObject("projectId",mjEntity.getProjectId());
                return mv;
            }
            projectDs.put("START_DATE",projectDs.get("START_DATE").toString());
            projectDs.put("END_DATE",projectDs.get("END_DATE").toString());
            mjEntity.setProject(projectDs);
            PageData oldHeards = listPd.get(0);
            mjEntity.setOldHeards(oldHeards);
            projectD.put("HEADER_FLAG",ConstantsMecool.SellinHeaderFlag.DM_PROJECT_SELLIN_INFO.getType());
            //检测头信息，项目是否存在
            PageData checkoutHeader = heardExcelService.checkHeader(mjEntity.getProjectId(),oldHeards,mjEntity);
            if (checkoutHeader!=null&&checkoutHeader.get("HeaderError")==null){
                List<PageData> list = mjExcelService.findInfoHeaderByProjectIdOrderByColumnOrder(projectD);
                LinkedHashMap<String,String> mmp = new LinkedHashMap<String, String>();
                //导入行
                saveSellins(projectD,list,listPd,mmp,mjEntity);
            }else {
                mv.setViewName("service/error");
                mv.addObject("msg",checkoutHeader);
                mv.addObject("empPk",mjEntity.getEmpPk());
                mv.addObject("projectId",mjEntity.getProjectId());
                return mv;
            }
            mv.addObject("msg","success");
            //FileUtil.delFile(filePath+fileName);
        }

        mv.setViewName("service/transition");
        mv.addObject("data",JSONUtils.toJSONString("mj"));
        mv.addObject("jedis",JSONUtils.toJSONString(mjEntity.getJedisId()));
        return mv;
    }

    /**
     * Jedis分页展示数据
     * @param page
     * @return
     */
    @RequestMapping("/mjDataFY")
    @SuppressWarnings("unused")
    public ModelAndView mjDataFY(Page page){
        ModelAndView mv = this.getModelAndView();
        PageData  pp  =  this.getPageData();
        if (null!=pp.getString("page.currentPage")&&!"".equals(pp.getString("page.currentPage"))){
            page.setCurrentPage(Integer.parseInt(pp.getString("page.currentPage")));
            page.setShowCount(Integer.parseInt(pp.getString("page.showCount")) );
        }else{
            page.setShowCount(15);
        }
        List<HashMap<String,String>> list =null;
        String jedis = pp.getString("jedis");
        int size = JedisUtil.getPageCountInt(jedis);
        MJEntity mjEntity = JedisUtil.getMJEntity(jedis);
        page.setPd(pp);
        try {
            page.setTotalResult(size);
            list = JedisUtil.pagingResource(jedis,page);
            if (list==null||list.size()==0){
                JedisUtil.delResource(jedis);
                mv.setViewName("service/error");
                //pp.put("HeaderError","无数据！");
                mv.addObject("msg","无数据！");
                mv.addObject("empPk",mjEntity.getEmpPk());
                mv.addObject("projectId",mjEntity.getProjectId());
                return mv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> excelHeard = mjEntity.getExcelHeard();
        List<PageData> colHeaders = mjEntity.getColHeaders();
        mv.addObject("projectId",mjEntity.getProjectId());
        mv.addObject("headString",JSONUtils.toJSONString(colHeaders));
        mv.addObject("pd", this.getPageData());
        mv.addObject("empPk",mjEntity.getEmpPk());
        mv.addObject("pds",JSONUtils.toJSONString(list));
        mv.addObject("jedis",JSONUtils.toJSONString(jedis));
        mv.addObject("rh",JSONUtils.toJSONString(excelHeard));
        mv.setViewName("service/MJExcel");
        return mv;
    }

    /**
     * handsoontable无感提交（仅支持双击）
     * @return
     */
    @RequestMapping("/mjDataAjax")
    @ResponseBody
    @SuppressWarnings("unused")
    public Object mjDataAjax(){
        PageData  pp  =  this.getPageData();
        String jedis = pp.getString("jedis");
        MJEntity mjEntity = JedisUtil.getMJEntity(jedis);
        if (null==pp.getString("uuid")||"".equals(pp.getString("uuid"))||"null".equals(pp.getString("uuid"))){
            JedisUtil.modifyMJEntityResource(mjEntity,mjEntity.getJedisId());
            String str=UUID.randomUUID().toString().replace("-","");
            pp.put("uuid",str);
            JedisUtil.addResource(pp,mjEntity.getJedisId());
            pp.put("success",str);
        }else if("1".equals(pp.getString("opertion"))||"0".equals(pp.getString("opertion"))||"null".equals(pp.getString("opertion"))){
            try{
                LinkedHashMap<String,String> newHeards = JedisUtil.getResourceHeard(mjEntity.getJedisId());
                LinkedHashMap<String,String> pps = cellExcel(pp,mjEntity.getOldHeards(),newHeards,mjEntity);
                JedisUtil.modifyResource(pps,mjEntity.getJedisId());
                pp.put("edit",JSONUtils.toJSONString(pps));
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try {
                pp.put("opertion",0);
                LinkedHashMap<String,String> newHeards = JedisUtil.getResourceHeard(mjEntity.getJedisId());
                LinkedHashMap<String,String> pps = cellExcel(pp,mjEntity.getOldHeards(),newHeards,mjEntity);
                JedisUtil.modifyResource(pps,mjEntity.getJedisId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return AppUtil.returnObject(new PageData(), pp);
    }

    @SuppressWarnings("unused")
    public LinkedHashMap<String, String> cellExcel(PageData data, PageData heard, LinkedHashMap<String, String> newHeards,MJEntity mjEntity)throws Exception{
        Map<String, PageData> channelsMap = new HashMap<String, PageData>();
        String uuid=UUID.randomUUID().toString().replace("-","");
        //PageData bean = null;
        PageData beanDB = null;
        long sn = 0l;
        String channelStr = null;
        //保存要用
        PageData channel = null;
        int flag = 0;
        //容器
        LinkedHashMap mp = new LinkedHashMap<String,String>();
        PageData hfHeard = mjEntity.getHfHeard();
        PageData project = new PageData();
        project.put("PROJECT_ID",mjEntity.getProjectId());
        project  = mjEntity.getProject();
        boolean booleanData = false;
        //唯一标识符UUID+操作标识符
        if (data.get("uuid")==null||data.get("uuid").toString().equals("")){
            mp.put("uuid",uuid);
            mp.put("opertion","0");
        }else {
            mp.put("uuid",data.get("uuid").toString());
            mp.put("opertion",data.get("opertion").toString());
            booleanData = true;
        }

        //日期使用
        Date START_DATE = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(project.get("START_DATE").toString());
        Date END_DATE =ConstantsMecool.SIMPLE_DATE_FORMAT.parse(project.get("END_DATE").toString());
        Date today = new Date();
        /**
         * 第二循环(一)
         */
        String header ="";
        Object cell= null;
        boolean isNew = false;
        Iterator he = null;
        if (newHeards!=null){
            he = newHeards.entrySet().iterator();
        }
        for (int i=0;i<heard.size();i++){
            cell= null;
            if (data.get("uuid")==null||data.get("uuid").toString().equals("")){
                cell = data.get("var"+i);
            }
            if(heard.get("var"+i)==null){
                Map.Entry en = (Map.Entry) he.next();
                if (en==null){
                    continue;
                }else {
                    header = en.getValue().toString().trim();
                }
            }else{
                header = heard.get("var"+i).toString().trim();
            }

            if(hfHeard.getString(header)==null){
                continue;
            }
            /**
             * 门店信息
             */
            try {

                if (ConstantsMecool.SellinFields.CHANNEL_SYNC_ID.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CHANNEL_SYNC_ID")!=null){
                            cell = data.get("CHANNEL_SYNC_ID").toString();
                        }else {
                            mp.put(hfHeard.getString(header),"Error:系统终端编号不能为空！");
                            continue;
                        }
                    }
                    if (cell == null || "".equals(cell.toString().trim())||cell.toString().equals("null")) {
                        mp.put(hfHeard.getString(header),"Error:系统终端编号不能为空！");
                    }
                    channelStr = cell.toString();

                    try {
                        if (channelsMap==null){

                        }else{
                            channel = channelsMap.get(channelStr);
                        }
                        if (channel == null) {
                            PageData pd = new PageData();
                            pd.put("CHANNEL_CODE",channelStr);
                            List<PageData> channelList = mjExcelService.getChannelList(pd);
                            if (channelList == null ) {

                            }else if (channelList.size() > 0){
                                channel = channelList.get(0);
                            }
                            if (channel == null) {
                                mp.put(hfHeard.getString(header),"Error:系统终端编号不存在"+channelStr);
                            }else if (channel.size()>0){
                                channelsMap.put(channelStr, channel);
                                channel.put("CHANNEL_CODE",channelStr);
                            }
                        }else if (channel.size()>0){
                            mp.put(hfHeard.getString(header),channelStr);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (channel != null && sn > 0) {
                        continue;
                    }
                } else if (ConstantsMecool.SellinFields.SCHEDULE_NUM.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("SCHEDULE_NUM")!=null){
                            cell = data.get("SCHEDULE_NUM").toString();
                        }
                    }
                    try {
                        sn = (new BigDecimal(cell.toString())).longValue();
                    } catch (Exception e) {
                        mp.put(hfHeard.getString(header),"Error:卖进序号必须为数字！");
                    }
                    if (channel != null && sn > 0) {
                        continue;
                    }
                    mp.put(hfHeard.getString(header),channelStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mp.put(hfHeard.getString(header),"Error:"+e.getMessage()+channelStr);
            }
        }
        if (channel == null) {
            if (StringUtils.isBlank(channelStr)) {

            } else {
                mp.put("CHANNEL_SYNC_ID","Error:门店不存在！"+channelStr);
            }
        } else {
            PageData query = new PageData();
            query.put("SCHEDULE_NUM",sn);
            query.put("CHANNEL_SYNC_ID",channel.get("ID"));
            query.put("PROJECT_ID",mjEntity.getProjectId());
            List<PageData> fmbpc = mjExcelService.findByMJinfoByProjectChannelSn(query);
            if(fmbpc!=null&&fmbpc.size()>0){
                beanDB = fmbpc.get(0);
            }else {
                beanDB = null;
            }
        }
        if (beanDB == null) {
            //校验门店是否可用( num<1 查不到可用门店信息)
            PageData query = new PageData();
            query.put("POS_CODE",channelStr);
            Long num = 0l;
            if (channelStr!=null&&!channelStr.equals("")){
                num = Long.parseLong(mjExcelService.getOnUsePosCountByCode(query).get("NUM").toString()) ;
            }
            if(num < 1){
                mp.put("CHANNEL_SYNC_ID","Error:门店编号不存在！"+channelStr);
            }

//            bean = new PageData();
//            bean.put("PROJECT_ID",mjEntity.getProjectId());
//            bean.put("CREATE_TIME",new Date());
//            bean.put("CREATE_USER","");
//            bean.put("STATUS",ConstantsMecool.SellinStatus.UN_SELLIN.getType());
//            bean.put("EXEC_STATUS",ConstantsMecool.SellinExecuteStatus.NEVER_EXEC.getType());
//            bean.put("AUDIT_STATUS",ConstantsMecool.SellinAuditStatus.UN_AUDIT.getType());
//            bean.put("SCHEDULE_NUM",sn);

            isNew = true;
        }
        String execDateStr = null;
        /**
         * 第二循环（二）
         */
        PageData query = new PageData();
        for (int i = 0; i < heard.size(); i++) {
            cell = null;
            if (data.get("uuid")==null||data.get("uuid").toString().equals("")){
                cell = data.get("var"+i);
            }
            header = "";
            PageData headerList = heard;
            try {
                if (headerList.get("var"+i) == null) {
                    continue;
                }
                header = headerList.get("var"+i).toString().trim();
                if(hfHeard.getString(header)==null){
                    continue;
                }
                if (ConstantsMecool.SellinFields.EXEC_DATE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("EXEC_DATE_TIME")!=null){
                            cell = data.get("EXEC_DATE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            cell=cell+"";
                        }
                    }
                    if (cell!=null){
                        try {
                            Map<String, Date> ssm = MecoolUtil.parseDaysStr(cell.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            mp.put(hfHeard.getString(header),"Error:执行日期格式错误！");
                            continue;
                        }
                        // 执行日期
                        mp.put(hfHeard.getString(header),cell.toString());
                    }
                }
                //TODO
                PageData psh = (PageData) mjEntity.getHeards().get(header);
                if (psh == null||hfHeard.getString(header)==null) {
                    continue;
                }
                if (ConstantsMecool.SellinFields.CHANNEL_SYNC_ID.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CHANNEL_SYNC_ID")!=null){
                            cell = data.get("CHANNEL_SYNC_ID").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell == null || "".equals(cell.toString().trim())||cell.toString().equals("null")) {
                        mp.put(hfHeard.getString(header),"Error:系统终端编号不能为空！");
                    }
                    if (channel == null) {
                        try {
                            if (channelsMap==null){

                            }else {
                                channel = channelsMap.get(channelStr);
                            }
                            query.put("CHANNEL_CODE",channelStr);
                            List<PageData> pd = mjExcelService.getChannelList(query);
                            if (pd!=null&&pd.size()>0){
                                channel = pd.get(0);
                            }else {
                                channel = null;
                                mp.put(hfHeard.getString(header),"Error:门店不存在"+cell.toString());
                            }
                            if (channel != null) {
                                channelsMap.put(channelStr, channel);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (channel == null) {
                        continue;
                    }else {
//                        if (beanDB!=null){
//                            mp.put("CHANNEL_SYNC_ID","Error:已存在卖进中。"+channelStr);
//                        }else{
                            mp.put(hfHeard.getString(header),cell.toString());
                           // bean.put("CHANNEL_SYNC_ID",channel);
//                        }
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_START_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("OLD_PLAN_START_TIME")!=null){
                            cell = data.get("OLD_PLAN_START_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put("OLD_PLAN_START_TIME","Error:计划开始日期不能为空！");
                            continue;
                        }
                    }
                    //mp.put(hfHeard.getString(header),cell.toString());
                    if(cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put("OLD_PLAN_START_TIME","Error:计划开始日期不能为空！");
                        continue;
                    }
//                    if (!includedOldPlanStartTime) {
                    Date d = null;
                    String strd = cell.toString();
                    if (!strd.equals("") && strd.trim().length() > 6) {
                        try {
                            d = ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(strd);
                        } catch (Exception e) {
                            try {
                                d = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(cell.toString());
                            } catch (Exception e1) {
                                mp.put("OLD_PLAN_START_TIME","Error:不是日期值！"+cell.toString());
                                continue;
                            }
                        }
                    }
                    if (d != null) {
                        if (isNew) {
                            if (mp.get("OLD_PLAN_START_TIME") == null) {
                                if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                    // Set static value.
                                    try {
                                    //    bean.put("OLD_PLAN_START_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                        mp.put("OLD_PLAN_START_TIME",psh.get("FIX_VALUE").toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mp.put("OLD_PLAN_START_TIME","Error:"+e.getMessage()+cell.toString());
                                        continue;
                                    }
                                } else {
                                    // The data value from job step. Don't need deal it.
                                 //   bean.put("OLD_PLAN_START_TIME",cell.toString());
                                    mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                }
                            } else {
                                if (d.before(today)) {
                                    mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                    continue;
                                } else {
                                    if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                        // Set static value.
                                        try {
                                         //   bean.put("OLD_PLAN_START_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                            mp.put("OLD_PLAN_START_TIME",psh.get("FIX_VALUE").toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            mp.put("OLD_PLAN_START_TIME","Error:"+e.getMessage()+cell.toString());
                                            continue;
                                        }
                                    } else {
                                        // The data value from job step. Don't need deal it.
                                        mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                    }
                                }
                            }
                        } else {
                            if (d.before(today)) {
                                mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                continue;
                            } else {
                                if (isNew) {
                                    // New data
                                    if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                        // Set static value.
                                        try {
                                            //												bean.setPlanStartTime(Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                       //     bean.put("OLD_PLAN_START_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                            mp.put("OLD_PLAN_START_TIME",psh.get("FIX_VALUE").toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            mp.put("OLD_PLAN_START_TIME","Error:"+e.getMessage()+cell.toString());
                                            continue;
                                        }
                                    } else {
                                        // The data value from job step. Don't need deal it.
                                        //											bean.setPlanStartTime(new Timestamp(d.getTime()).toString());
                                    //    bean.put("OLD_PLAN_START_TIME",cell.toString());
                                        mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                    }
                                } else {
                                    // 入卖进的时候，给原计划的开始和结束日期赋值，以后不做修改
                                    // Old data, but projectSellinJob != null. so must be use old data.
                                    mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                }
                            }
                        }
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_FINISHED_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("OLD_PLAN_FINISHED_TIME")!=null){
                            cell = data.get("OLD_PLAN_FINISHED_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put("OLD_PLAN_FINISHED_TIME","Error:计划结束日期不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put("OLD_PLAN_FINISHED_TIME","Error:计划结束日期不能为空！");
                        continue;
                    }
                    Date d = null;
                    if (cell.toString() != null && cell.toString().trim().length() > 6) {
                        try {
                            d = ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(cell.toString());
                        } catch (Exception e) {
                            try {
                                d = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(cell.toString());
                            } catch (Exception e1) {
                                mp.put("OLD_PLAN_FINISHED_TIME","Error:不是日期值"+cell.toString());
                                continue;
                            }
                        }
                    }
                    if (d != null) {
                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                                try {
                                //    bean.put("OLD_PLAN_FINISHED_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                    mp.put("OLD_PLAN_FINISHED_TIME",psh.get("FIX_VALUE").toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mp.put("OLD_PLAN_FINISHED_TIME","Error:"+e.getMessage()+cell.toString());
                                }
                            } else {
                                // The data value from job step. Don't need deal it.
                              //  bean.put("OLD_PLAN_FINISHED_TIME",cell.toString());
                                mp.put("OLD_PLAN_FINISHED_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                            }
                        } else {
                            // Old data, but projectSellinJob != null. so must be use old data.
                            if (d == null) {
                                // New data
                                if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                    // Set static value.
                                    try {
                                     //   bean.put("OLD_PLAN_FINISHED_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                        mp.put("OLD_PLAN_FINISHED_TIME",psh.get("FIX_VALUE").toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                  //      bean.put("OLD_PLAN_FINISHED_TIME",cell.toString());
                                        mp.put("OLD_PLAN_FINISHED_TIME","Error:"+e.getMessage()+cell.toString());
                                    }
                                } else {
                                    // The data value from job step. Don't need deal it.
                                 //   bean.put("OLD_PLAN_FINISHED_TIME",new Timestamp(d.getTime()).toString());
                                    mp.put("OLD_PLAN_FINISHED_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                }
                            } else {
                                if (d.before(today)) {
                                    mp.put("OLD_PLAN_FINISHED_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                  //  bean.put("OLD_PLAN_FINISHED_TIME",cell.toString());
                                    continue;
                                } else {
                                    // New data
                                    if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                        // Set static value.
                                        try {
                                         //   bean.put("OLD_PLAN_FINISHED_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                            mp.put("OLD_PLAN_FINISHED_TIME",cell.toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                         //   bean.put("OLD_PLAN_FINISHED_TIME",cell.toString());
                                            mp.put("OLD_PLAN_FINISHED_TIME","Error:"+e.getMessage()+cell.toString());
                                        }
                                    } else {
                                        // The data value from job step. Don't need deal it.
                                      //  bean.put("OLD_PLAN_FINISHED_TIME",cell.toString());
                                        mp.put("OLD_PLAN_FINISHED_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                    }
                                }
                            }

                        }
                    }
                } else if (ConstantsMecool.SellinFields.OLD_PLAN_START_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("OLD_PLAN_START_TIME")!=null){
                            cell = data.get("OLD_PLAN_START_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:原计划开始日期不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:原计划开始日期不能为空！");
                        continue;
                    }
                    Date d = null;
                    String strd = cell.toString();
                    if (!strd.equals("") && strd.trim().length() > 6) {
                        try {
                            d = ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(strd);
                        } catch (Exception e) {
                            mp.put(hfHeard.getString(header),"Error:不是日期值"+cell.toString());
                        }
                    }
                    if (d != null) {
                        // PlanStartDate must be after today.
                        if (isNew) {
                            // New data
                            if (d.before(today)) {
                                mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                continue;
                            } else {
                                if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                    // Set static value.
                                    try {
                                     //   bean.put("OLD_PLAN_START_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                        mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mp.put("OLD_PLAN_START_TIME","Error:"+e.getMessage()+cell.toString());
                                        continue;
                                    }
                                } else {
                                    // The data value from job step. Don't need deal it.
                               //     bean.put("OLD_PLAN_START_TIME",new Timestamp(d.getTime()).toString());
                                    mp.put("OLD_PLAN_START_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                }
                            }
                        } else {
                            // 入卖进的时候，给原计划的开始和结束日期赋值，以后不做修改
                        }
                    }
                } else if (ConstantsMecool.SellinFields.OLD_PLAN_FINISHED_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("OLD_PLAN_FINISHED_TIME")!=null){
                            cell = data.get("OLD_PLAN_FINISHED_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:计划结束日期为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划结束日期为空！");
                        continue;
                    }
                    Date d = null;
                    //mp.put(hfHeard.getString(header),cell.toString());
                    String strd = cell.toString();
                    if (!strd.equals("") && strd.trim().length() > 6) {
                        try {
                            d = ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(strd);
                        } catch (Exception e) {
                            mp.put(hfHeard.getString(header),"Error:不是日期值"+cell.toString());
                        }
                    }
                    if (d != null) {
                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                                try {
                                  //  bean.put("OLD_PLAN_FINISHED_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                    mp.put("OLD_PLAN_FINISHED_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mp.put(hfHeard.getString(header),"Error:"+e.getMessage()+cell.toString());
                                }
                            } else {
                                // The data value from job step. Don't need deal it.
                              //  bean.put("OLD_PLAN_FINISHED_TIME",new Timestamp(d.getTime()).toString());
                                mp.put("OLD_PLAN_FINISHED_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                            }
                        } else {
                            // Old data, but projectSellinJob != null. so must be use old data.
                            mp.put("OLD_PLAN_FINISHED_TIME",ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                        }
                    }
                } else if (ConstantsMecool.SellinFields.EMP_CODE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("EMP_CODE")!=null){
                            cell = data.get("EMP_CODE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:门店督导编号不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:门店督导编号不能为空！");
                        continue;
                    }
                    //mp.put(hfHeard.getString(header),cell.toString());
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if ("".equals(s) || s == null) {
                       // bean.put("EMP_CODE","");
                    } else {
                        PageData queryCell = new PageData();
                        queryCell.put("EMP_CODE",cell);
                        PageData emp = mjExcelService.findMdEmpByEmpCodeIs(queryCell);
                        if(emp!=null&&emp.size()>0){

                        }else{
                            mp.put(hfHeard.getString(header),"Error:门店督导编号不存在！");
                            continue;
                        }
                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                             //   bean.put("EMP_CODE",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } else {
                                // The data value from job step. Don't need deal it.
                            //    bean.put("EMP_CODE",cell.toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // Old data, but projectSellinJob != null. so must be use old data.
                            if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            //    bean.put("EMP_CODE",s);
                                mp.put(hfHeard.getString(header),cell.toString());
                            }else{
                                mp.put(hfHeard.getString(header), cell.toString());
                            }
                        }
                    }
                } else if (ConstantsMecool.SellinFields.CITY_EMP_CODE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CITY_EMP_CODE")!=null){
                            cell = data.get("CITY_EMP_CODE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:城市督导编号不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:城市督导编号不能为空！");
                        continue;
                    }
                    //mp.put(hfHeard.getString(header),cell.toString());
                    // 不能为null，必须存在。  这里转成 emp_pk 保存
                    if (cell!=null) {
                        String s = cell.toString();
                        s = s == null ? "" : s.trim();
                        PageData queryCell = new PageData();
                        queryCell.put("EMP_CODE",cell);
                        PageData emp = mjExcelService.findMdEmpByEmpCodeIs(queryCell);
                        if(emp!=null&&emp.size()>0){

                        }else{
                            mp.put(hfHeard.getString(header),"Error:城市督导编号不存在！");
                            continue;
                        }
                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() == Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                            //    bean.put("CITY_EMP_CODE", psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header), psh.get("FIX_VALUE").toString());
                            } else {
                                // The data value from job step. Don't need deal it.
                            //    bean.put("CITY_EMP_CODE", s);
                                mp.put(hfHeard.getString(header), cell.toString());
                            }
                        } else {
                            // Old data, but projectSellinJob != null. so must be use old data.
                            if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() != Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            //    bean.put("CITY_EMP_CODE", s);
                                mp.put(hfHeard.getString(header), cell.toString());
                            }else{
                                mp.put(hfHeard.getString(header), cell.toString());
                            }
                        }
                    }
                } else if (ConstantsMecool.SellinFields.AREA_MANAGER_CODE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("AREA_MANAGER_CODE")!=null){
                            cell = data.get("AREA_MANAGER_CODE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:区域负责人编号不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:区域负责人编号不能为空！");
                        continue;
                    }
                    // 不能为null，必须存在。  这里转成 emp_pk 保存
                    if (cell!=null){
                        String s = cell.toString();
                        s = s == null ? "" : s.trim();
                        PageData queryCell = new PageData();
                        queryCell.put("EMP_CODE",cell);
                        PageData emp = mjExcelService.findMdEmpByEmpCodeIs(queryCell);
                        if(emp!=null&&emp.size()>0){

                        }else{
                            mp.put(hfHeard.getString(header),"Error:城市督导编号不存在！");
                            continue;
                        }
                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                               // bean.put("AREA_MANAGER_CODE",psh.get("FIX_VALUE").toString());
                                mp.put("AREA_MANAGER_CODE",psh.get("FIX_VALUE").toString());
                            } else {
                                // The data value from job step. Don't need deal it.
                              //  bean.put("AREA_MANAGER_CODE",s);
                                mp.put("AREA_MANAGER_CODE",cell.toString());
                            }
                        } else {
                            // Old data, but projectSellinJob != null. so must be use old data.
                            if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                              //  bean.put("AREA_MANAGER_CODE",s);
                                mp.put("AREA_MANAGER_CODE",cell.toString());
                            }else {
                                mp.put("AREA_MANAGER_CODE",cell.toString());
                            }
                        }
                    }
                }else if (ConstantsMecool.SellinFields.AREA_MANAGER_NAME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("AREA_MANAGER_NAME")!=null){
                            cell = data.get("AREA_MANAGER_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put("AREA_MANAGER_NAME","Error:区域负责人姓名不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put("AREA_MANAGER_NAME","Error:区域负责人姓名不能为空！");
                        continue;
                    }else{
                        mp.put("AREA_MANAGER_NAME",cell.toString());
                    }
                }  else if (ConstantsMecool.SellinFields.SCHEDULE_NUM.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("SCHEDULE_NUM")!=null){
                            cell = data.get("SCHEDULE_NUM").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put(hfHeard.getString(header),"Error:卖进序号不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:卖进序号不能为空！");
                        continue;
                    }
                    if (cell!=null){
                        try {
                            sn = (new BigDecimal(cell.toString())).longValue();
                            mp.put(hfHeard.getString(header),sn);
                        } catch (Exception e) {
                            mp.put(hfHeard.getString(header),"Error:卖进序号必须为数字！");
                        }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_STORE_OPEN_COUNT.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_STORE_OPEN_COUNT")!=null){
                            cell = data.get("PLAN_STORE_OPEN_COUNT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                        mp.put("PLAN_STORE_OPEN_COUNT",cell.toString());
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_SALES_COUNT.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_SALES_COUNT")!=null){
                            cell = data.get("PLAN_SALES_COUNT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                        try {
                            int count = Integer.parseInt(cell.toString());
                            if (count>0){

                            }else{
                                mp.put(hfHeard.getString(header),"Error:计划促销员人数必须大于0！");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            mp.put(hfHeard.getString(header),"Error:计划促销员人数必须为数字！");
                            continue;
                        }
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                              //  bean.put("PLAN_SALES_COUNT",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } catch (Exception e) {
                              //  bean.put("PLAN_SALES_COUNT","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
                              //  bean.put("PLAN_SALES_COUNT",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                              //  bean.put("PLAN_SALES_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
                             //   bean.put("PLAN_SALES_COUNT",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                             //   bean.put("PLAN_SALES_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_DEPUTY_NAME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_DEPUTY_NAME")!=null){
                            cell = data.get("CUST_DEPUTY_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                              //  bean.put("CUST_DEPUTY_NAME",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } catch (Exception e) {
                              //  bean.put("CUST_DEPUTY_NAME","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                          //  bean.put("CUST_DEPUTY_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                          //  bean.put("CUST_DEPUTY_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_DEPUTY_PHONE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_DEPUTY_PHONE")!=null){
                            cell = data.get("CUST_DEPUTY_PHONE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){


                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                               // bean.put("CUST_DEPUTY_PHONE",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                             //   bean.put("CUST_DEPUTY_PHONE","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                          //  bean.put("CUST_DEPUTY_PHONE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                          //  bean.put("CUST_DEPUTY_PHONE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_DIRECTOR_NAME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_DIRECTOR_NAME")!=null){
                            cell = data.get("CUST_DIRECTOR_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if(cell!=null){
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                               // bean.put("CUST_DIRECTOR_NAME",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                             //   bean.put("CUST_DIRECTOR_NAME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                           // bean.put("CUST_DIRECTOR_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                           // bean.put("CUST_DIRECTOR_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_DIRECTOR_PHONE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_DIRECTOR_PHONE")!=null){
                            cell = data.get("CUST_DIRECTOR_PHONE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if(cell!=null){

                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                              //  bean.put("CUST_DIRECTOR_PHONE",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } catch (Exception e) {
                              //  bean.put("CUST_DIRECTOR_PHONE","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                           // bean.put("CUST_DIRECTOR_PHONE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                           // bean.put("CUST_DIRECTOR_PHONE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_MANAGER_NAME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_MANAGER_NAME")!=null){
                            cell = data.get("CUST_MANAGER_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){

                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                                //bean.put("CUST_MANAGER_NAME",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                               // bean.put("CUST_MANAGER_NAME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            //bean.put("CUST_MANAGER_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                           // bean.put("CUST_MANAGER_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.CUST_MANAGER_PHONE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_MANAGER_PHONE")!=null){
                            cell = data.get("CUST_MANAGER_PHONE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                              //  bean.put("CUST_MANAGER_PHONE",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                              //  bean.put("CUST_MANAGER_PHONE","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            //bean.put("CUST_MANAGER_PHONE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                          //  bean.put("CUST_MANAGER_PHONE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.SELLIN_CHANNEL.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("SELLIN_CHANNEL")!=null){
                            cell = data.get("SELLIN_CHANNEL").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                               // bean.put("SELLIN_CHANNEL",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                              //  bean.put("SELLIN_CHANNEL","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                           // bean.put("SELLIN_CHANNEL",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                           // bean.put("SELLIN_CHANNEL",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_CHANNEL.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_CHANNEL")!=null){
                            cell = data.get("CUST_CHANNEL").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){

                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                              //  bean.put("CUST_CHANNEL",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                              //  bean.put("CUST_CHANNEL","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                           // bean.put("CUST_CHANNEL",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                           // bean.put("CUST_CHANNEL",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.CUST_SYS.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_SYS")!=null){
                            cell = data.get("CUST_SYS").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if(cell!=null){
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
                                //bean.put("CUST_SYS",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                               // bean.put("CUST_SYS","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            //bean.put("CUST_SYS",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                           // bean.put("CUST_SYS",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_CHANNEL_CODE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_CHANNEL_CODE")!=null){
                            cell = data.get("CUST_CHANNEL_CODE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("CUST_CHANNEL_CODE",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("CUST_CHANNEL_CODE","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
//                            bean.put("CUST_CHANNEL_CODE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
//                            bean.put("CUST_CHANNEL_CODE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }
                    }
                } else if (ConstantsMecool.SellinFields.CUST_CHANNEL_NAME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CUST_CHANNEL_NAME")!=null){
                            cell = data.get("CUST_CHANNEL_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:客户门店名称不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:客户门店名称不能为空！");
                        continue;
                    }
                    if (cell!=null){
                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("CUST_CHANNEL_NAME",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("CUST_CHANNEL_NAME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
//                            bean.put("CUST_CHANNEL_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
//                            bean.put("CUST_CHANNEL_NAME",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }

                    }else{
                        mp.put(hfHeard.getString(header),"Error:客户门店名称不能为空！");
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_EXEC_DATE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_EXEC_DATE_TIME")!=null){
                            cell = data.get("PLAN_EXEC_DATE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put(hfHeard.getString(header),"Error:计划执行天数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划执行天数不能为空！");
                        continue;
                    }else{
                        try {
                            int count = Integer.parseInt(cell.toString());
                            if (count>0){

                            }else{
                                mp.put(hfHeard.getString(header),"Error:计划执行天数必须大于0！");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            mp.put(hfHeard.getString(header),"Error:计划执行天数必须为数字！");
                            continue;
                        }
                    }
                    if(cell!=null){
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_EXEC_DATE_TIME",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_EXEC_DATE_TIME",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_EXEC_DATE_TIME",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_COM_EXEC_DATE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_COM_EXEC_DATE_TIME")!=null){
                            cell = data.get("PLAN_COM_EXEC_DATE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put(hfHeard.getString(header),"Error:计划常规执行天数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划常规执行天数不能为空！");
                        continue;
                    } else{
                        try {
                            int count = Integer.parseInt(cell.toString());
                            /*if (count>0){

                            }else{
                                mp.put(hfHeard.getString(header),"Error:计划常规执行天数必须大于0！");
                                continue;
                            }*/
                        } catch (NumberFormatException e) {
                            mp.put(hfHeard.getString(header),"Error:计划常规执行天数必须为数字！");
                            continue;
                        }
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_COM_EXEC_DATE_TIME",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_COM_EXEC_DATE_TIME",null);
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_COM_EXEC_DATE_TIME",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_COM_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_COM_EXEC_DATE_TIME",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_COM_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_THREE_PAY_EXEC_DATE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_THREE_PAY_EXEC_DATE_TIME")!=null){
                            cell = data.get("PLAN_THREE_PAY_EXEC_DATE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:计划三薪执行天数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划三薪执行天数不能为空！");
                        continue;
                    }else{
                        try {
                            int count = Integer.parseInt(cell.toString());
                            /*if (count>0){

                            }else{
                                mp.put(hfHeard.getString(header),"Error:计划常规执行天数必须大于0！");
                                continue;
                            }*/
                        } catch (NumberFormatException e) {
                            mp.put(hfHeard.getString(header),"Error:计划三薪执行天数天数必须为数字！");
                            continue;
                        }
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_THREE_PAY_EXEC_DATE_TIME",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_THREE_PAY_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_THREE_PAY_EXEC_DATE_TIME",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_THREE_PAY_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_THREE_PAY_EXEC_DATE_TIME",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_THREE_PAY_EXEC_DATE_TIME","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_EXEC_STORE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_EXEC_STORE_TIME")!=null){
                            cell = data.get("PLAN_EXEC_STORE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:计划执行场次数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划执行场次数不能为空！");
                        continue;
                    }else{
                        try {
                            int count = Integer.parseInt(cell.toString());
                            if (count>0){

                            }else{
                                mp.put(hfHeard.getString(header),"Error:计划执行场次数必须大于0！");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            mp.put(hfHeard.getString(header),"Error:计划执行场次数必须为数字！");
                            continue;
                        }
                    }
                    if (cell!=null){

                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                                try {
//                                    bean.put("PLAN_EXEC_STORE_TIME",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                    mp.put(hfHeard.getString(header),cell.toString());
                                } catch (Exception e) {
//                                    bean.put("PLAN_EXEC_STORE_TIME","");
                                    mp.put(hfHeard.getString(header),cell.toString());
                                }
                            } else {
                                // The data value from job step. Don't need deal it.
                                try {
                                    String s = cell.toString();
                                    if (s.contains(".")) {
                                        s = s.substring(0, s.indexOf("."));
                                    }
//                                    bean.put("PLAN_EXEC_STORE_TIME",Long.parseLong(s));
                                    mp.put(hfHeard.getString(header),cell.toString());
                                } catch (Exception e) {
//                                    bean.put("PLAN_EXEC_STORE_TIME","");
                                    mp.put(hfHeard.getString(header),"");
                                }
                            }
                        } else {
                            // Old data, but projectSellinJob != null. so must be use old data.
                            if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                try {
//                                    bean.put("PLAN_EXEC_STORE_TIME",Long.parseLong(cell.toString()));
                                    mp.put(hfHeard.getString(header),cell.toString());
                                } catch (Exception e) {
//                                    bean.put("PLAN_EXEC_STORE_TIME","");
                                    mp.put(hfHeard.getString(header),cell.toString());
                                }
                            }
                        }
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_COM_EXEC_STORE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_COM_EXEC_STORE_TIME")!=null){
                            cell = data.get("PLAN_COM_EXEC_STORE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:计划常规执行场次数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划常规执行场次数不能为空！");
                        continue;
                    }else{
                        try {
                            int count = Integer.parseInt(cell.toString());
                            /*if (count>0){

                            }else{
                                mp.put(hfHeard.getString(header),"Error:计划执行天数必须大于0！");
                                continue;
                            }*/
                        } catch (NumberFormatException e) {
                            mp.put(hfHeard.getString(header),"Error:计划常规执行场次数必须为数字！");
                            continue;
                        }
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_COM_EXEC_STORE_TIME",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_COM_EXEC_STORE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_COM_EXEC_STORE_TIME",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_COM_EXEC_STORE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_COM_EXEC_STORE_TIME",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_COM_EXEC_STORE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_THREE_PAY_EXEC_STORE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_THREE_PAY_EXEC_STORE_TIME")!=null){
                            cell = data.get("PLAN_THREE_PAY_EXEC_STORE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put(hfHeard.getString(header),"Error:计划三薪执行场次数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划三薪执行场次数不能为空！");
                        continue;
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_THREE_PAY_EXEC_STORE_TIME",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_THREE_PAY_EXEC_STORE_TIME","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_THREE_PAY_EXEC_STORE_TIME",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_THREE_PAY_EXEC_STORE_TIME","");
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_THREE_PAY_EXEC_STORE_TIME",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_THREE_PAY_EXEC_STORE_TIME","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_TYPE.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_EXHIBIT_TYPE")!=null){
                            cell = data.get("PLAN_EXHIBIT_TYPE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){

                    String s = cell.toString();
                    s = s == null ? "" : s.trim();
                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_EXHIBIT_TYPE",psh.get("FIX_VALUE").toString());
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_EXHIBIT_TYPE","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
//                            bean.put("PLAN_EXHIBIT_TYPE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
//                            bean.put("PLAN_EXHIBIT_TYPE",s);
                            mp.put(hfHeard.getString(header),cell.toString());
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_START_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_EXHIBIT_START_TIME")!=null){
                            cell = data.get("PLAN_EXHIBIT_START_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"");
                        continue;
                    }
                    Date d = null;
                    String strd = cell.toString();
                    if (!strd.equals("") && strd.trim().length() > 6) {
                        try {
                            d = ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(strd);
                        } catch (Exception e) {
                            try{
                                d = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(strd);
                            }catch (Exception es){
                                mp.put(hfHeard.getString(header),"Error:不是日期值"+cell.toString());
                            }
                        }
                    }
                    if (d != null) {
                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                                try {
//                                    bean.put("PLAN_EXHIBIT_START_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                    mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mp.put(hfHeard.getString(header),"Error:"+e.getMessage()+cell.toString());
                                }
                            } else {
                                // The data value from job step. Don't need deal it.
//                                bean.put("PLAN_EXHIBIT_START_TIME",new Timestamp(d.getTime()).toString());
                                mp.put(hfHeard.getString(header),ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                            }
                        } else {
//                            bean.put("PLAN_EXHIBIT_START_TIME",new Timestamp(d.getTime()).toString());
                            mp.put(hfHeard.getString(header),ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                        }
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_END_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_EXHIBIT_END_TIME")!=null){
                            cell = data.get("PLAN_EXHIBIT_END_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"");
                        continue;
                    }
                    Date d = null;
                    String strd = cell.toString();
                    if (strd != null && strd.trim().length() > 6) {
                        try {
                            d = ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(strd);
                        } catch (Exception e) {
                            try {
                                d = ConstantsMecool.SIMPLE_DATE_FORMAT.parse(strd);
                            } catch (Exception e1) {
                                mp.put(hfHeard.getString(header),"Error:不是日期值"+cell.toString());
                                continue;
                            }
                        }
                    }
                    if (d != null) {
                        if (isNew) {
                            // New data
                            if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                                // Set static value.
                                try {
//                                    bean.put("PLAN_EXHIBIT_END_TIME",Timestamp.valueOf(psh.get("FIX_VALUE").toString()));
                                    mp.put(hfHeard.getString(header),ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mp.put(hfHeard.getString(header),"Error:"+e.getMessage()+cell.toString());
                                    continue;
                                }
                            } else {
                                // The data value from job step. Don't need deal it.
//                                bean.put("PLAN_EXHIBIT_END_TIME",new Timestamp(d.getTime()).toString());
                                mp.put(hfHeard.getString(header),ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                            }
                        } else {
//                            bean.put("PLAN_EXHIBIT_END_TIME",new Timestamp(d.getTime()).toString());
                            mp.put(hfHeard.getString(header),ConstantsMecool.SIMPLE_DATE_FORMAT.format(d));
                        }
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_EXHIBIT_NUM.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_EXHIBIT_NUM")!=null){
                            cell = data.get("PLAN_EXHIBIT_NUM").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                        mp.put("PLAN_EXHIBIT_NUM",cell.toString());
                    }
                } else if (ConstantsMecool.SellinFields.PLAN_STORE_PATROL_COUNT.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_STORE_PATROL_COUNT")!=null){
                            cell = data.get("PLAN_STORE_PATROL_COUNT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:计划巡店总次数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:计划巡店总次数不能为空！");
                        continue;
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_STORE_PATROL_COUNT",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_STORE_PATROL_COUNT",null);
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_STORE_PATROL_COUNT",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_STORE_PATROL_COUNT",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_WEEK_STORE_PATROL_COUNT.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_WEEK_STORE_PATROL_COUNT")!=null){
                            cell = data.get("PLAN_WEEK_STORE_PATROL_COUNT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:周巡店次数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:周巡店次数不能为空！");
                        continue;
                    }
                    if(cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_WEEK_STORE_PATROL_COUNT",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),psh.get("FIX_VALUE").toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_WEEK_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_WEEK_STORE_PATROL_COUNT",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_WEEK_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_WEEK_STORE_PATROL_COUNT",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_WEEK_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.PLAN_MONTH_STORE_PATROL_COUNT.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_MONTH_STORE_PATROL_COUNT")!=null){
                            cell = data.get("PLAN_MONTH_STORE_PATROL_COUNT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:月巡店次数不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:月巡店次数不能为空！");
                        continue;
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("PLAN_MONTH_STORE_PATROL_COUNT",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_MONTH_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("PLAN_MONTH_STORE_PATROL_COUNT",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_MONTH_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("PLAN_MONTH_STORE_PATROL_COUNT",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("PLAN_MONTH_STORE_PATROL_COUNT","");
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.TOTAL_SALES_TARGET.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("TOTAL_SALES_TARGET")!=null){
                            cell = data.get("TOTAL_SALES_TARGET").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:总销量目标不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:总销量目标不能为空！");
                        continue;
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("TOTAL_SALES_TARGET",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("TOTAL_SALES_TARGET",null);
                                mp.put(hfHeard.getString(header),"");
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
//                                bean.put("TOTAL_SALES_TARGET",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("TOTAL_SALES_TARGET",null);
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
//                                bean.put("TOTAL_SALES_TARGET",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("TOTAL_SALES_TARGET",null);
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.DAILY_SOTRE_SALES_TARGET.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("DAILY_SOTRE_SALES_TARGET")!=null){
                            cell = data.get("DAILY_SOTRE_SALES_TARGET").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(hfHeard.getString(header),"Error:日店均销量目标不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put(hfHeard.getString(header),"Error:日店均销量目标不能为空！");
                        continue;
                    }
                    if (cell!=null){

                    if (isNew) {
                        // New data
                        if (ConstantsMecool.SellinSourceType.STATIC_VALUE.getType() ==Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            // Set static value.
                            try {
//                                bean.put("DAILY_SOTRE_SALES_TARGET",Long.parseLong(psh.get("FIX_VALUE").toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
//                                bean.put("DAILY_SOTRE_SALES_TARGET",null);
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        } else {
                            // The data value from job step. Don't need deal it.
                            try {
                                String s = cell.toString();
                                if (s.contains(".")) {
                                    s = s.substring(0, s.indexOf("."));
                                }
                                //bean.put("DAILY_SOTRE_SALES_TARGET",Long.parseLong(s));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                                //bean.put("DAILY_SOTRE_SALES_TARGET",null);
                                mp.put(hfHeard.getString(header),cell.toString());
                            }
                        }
                    } else {
                        // Old data, but projectSellinJob != null. so must be use old data.
                        if (ConstantsMecool.SellinSourceType.JOB_STEP.getType() !=Long.parseLong(psh.get("SOURCE_TYPE").toString())) {
                            try {
                                //bean.put("DAILY_SOTRE_SALES_TARGET",Long.parseLong(cell.toString()));
                                mp.put(hfHeard.getString(header),cell.toString());
                            } catch (Exception e) {
                                //bean.put("DAILY_SOTRE_SALES_TARGET",null);
                                mp.put(hfHeard.getString(header),"");
                            }
                        }
                    }

                    }
                } else if (ConstantsMecool.SellinFields.EMP_NAME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("EMP_NAME")!=null){
                            cell = data.get("EMP_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put("EMP_NAME","Error:门店督导姓名不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put("EMP_NAME","Error:门店督导姓名不能为空！");
                        continue;
                    }
                    if (cell!=null){
                        mp.put("EMP_NAME",cell.toString());
                    }
                } else if (ConstantsMecool.SellinFields.CITY_EMP_NAME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CITY_EMP_NAME")!=null){
                            cell = data.get("CITY_EMP_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }else{
                            mp.put("CITY_EMP_NAME","Error:城市督导姓名不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put("CITY_EMP_NAME","Error:城市督导姓名不能为空！");
                        continue;
                    }
                    if (cell!=null){
                        mp.put("CITY_EMP_NAME",cell.toString());
                    }
                } else if (ConstantsMecool.SellinFields.EXEC_DATE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("EXEC_DATE_TIME")!=null){
                            cell = data.get("EXEC_DATE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                        mp.put("EXEC_DATE_TIME",cell.toString());
                    }
                } else if (ConstantsMecool.SellinFields.EXEC_DATE_TIME_COUNT.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("EXEC_DATE_TIME_COUNT")!=null){
                            cell = data.get("EXEC_DATE_TIME_COUNT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell==null||"".equals(cell.toString())||"null".equals(cell.toString())){
                        mp.put("EXEC_DATE_TIME_COUNT","Error:计划执行店次(店*天)不能为空！");
                        continue;
                    }
                    if (cell!=null){
                        mp.put("EXEC_DATE_TIME_COUNT",cell.toString());
                    }
                } else if (ConstantsMecool.SellinFields.STATUS.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("STATUS")!=null){
                            cell = data.get("STATUS").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                        mp.put(hfHeard.getString(header),cell.toString());
                    }
                    // Ignore  卖进状态
                } else if (ConstantsMecool.SellinFields.EXEC_STATUS.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("EXEC_STATUS")!=null){
                            cell = data.get("EXEC_STATUS").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                        mp.put(hfHeard.getString(header),cell.toString());
                    }
                    // Ignore  执行状态
                } else if (ConstantsMecool.SellinFields.UPDATE_TIME.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("UPDATE_TIME")!=null){
                            cell = data.get("UPDATE_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put(hfHeard.getString(header),cell.toString());
                                continue;
                            }
                        }
                    }
                    if (cell!=null){
                        mp.put(hfHeard.getString(header),cell.toString());
                    }
                    // Ignore  更新时间
                } else {
                    if (cell!=null&&!cell.equals("null")){
                        mp.put(hfHeard.getString(header),cell.toString());
                    }
                    // Other fields
                }
            } catch (Exception e) {
                e.printStackTrace();
                mp.put(hfHeard.getString(header),"Error:出错");
            }
        }
//        if (checkNullRow(bean)) {
//            return mp;
//        }
        if (mp.get("PLAN_EXEC_DATE_TIME").toString().indexOf("Error")==-1){
            boolean dx = true;
            if (mp.get("EXEC_DATE_TIME")!=null&&mp.get("EXEC_DATE_TIME").toString().indexOf("Error")==-1&&mp.get("PLAN_EXEC_STORE_TIME").toString().indexOf("Error")==-1){
                try {
                    Map<String, Date> ssm = MecoolUtil.parseDaysStr(mp.get("EXEC_DATE_TIME").toString());
                    //校验计划执行天数是否大于执行日(天数)
                    boolean b = checkExecDate(beanDB,isNew,mp.get("EXEC_DATE_TIME").toString(),Long.parseLong(mp.get("PLAN_EXEC_DATE_TIME").toString()));
                    if (b){
                        mp.put("PLAN_EXEC_DATE_TIME","Error:计划执行天数不能小于执行日(天数)!");
                        dx = false;
                    }
                } catch (Exception e) {
                    mp.put("EXEC_DATE_TIME","Error:执行日期格式错误！");
                    dx = false;
                }
                if (dx){
                    //促销员排班<=计划执行场次数
                    boolean b = checkSalesSchedule(beanDB,isNew,Long.parseLong(mp.get("PLAN_EXEC_DATE_TIME").toString()));
                    if (b){
                        mp.put("PLAN_EXEC_DATE_TIME","Error:计划执行场次数不能小于促销员排班总数!");
                    }else{
                        if (Long.parseLong(mp.get("PLAN_EXEC_STORE_TIME").toString())<Long.parseLong(mp.get("PLAN_EXEC_DATE_TIME").toString())){
                            mp.put("PLAN_EXEC_STORE_TIME","Error:计划执行场次数不能小于计划执行天数!");
                        }
                    }
                }
            }
        }
        try{
            if (null == mp.get("OLD_PLAN_START_TIME") || "".equals(mp.get("OLD_PLAN_START_TIME").toString())) {
                mp.put("OLD_PLAN_START_TIME","Error:卖进开始日期不能为空" );
            }else if(mp.get("OLD_PLAN_START_TIME").toString().indexOf("Error")!=-1){

            }else if (mp.get("OLD_PLAN_START_TIME").toString().equals(DateUtil.getDay())){

            }else if (ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_START_TIME").toString()).before(today)
                    || ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_START_TIME").toString()).after(END_DATE)) {
                mp.put("OLD_PLAN_START_TIME","Error:卖进开始日期必须在当前日期之后，在项目结束时间之前" );
            }
        }catch (Exception e){
            if (null == mp.get("OLD_PLAN_START_TIME") || "".equals(mp.get("OLD_PLAN_START_TIME").toString())) {
                mp.put("OLD_PLAN_START_TIME","Error:卖进开始日期不能为空" );
            }else if(mp.get("OLD_PLAN_START_TIME").toString().indexOf("Error")!=-1){

            }else if (mp.get("OLD_PLAN_START_TIME").toString().equals(DateUtil.getDays())){

            }else if (ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_START_TIME").toString()).before(today)
                    || ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_START_TIME").toString()).after(END_DATE)
                    ) {
                mp.put("OLD_PLAN_START_TIME","Error:计划开始日期必须在当前日期之后，在项目结束时间之前" );
            }
        }
        try{
            if (null == mp.get("OLD_PLAN_FINISHED_TIME") || "".equals(mp.get("OLD_PLAN_FINISHED_TIME").toString())) {
                mp.put("OLD_PLAN_FINISHED_TIME","Error:计划结束日期不能为空" );
            }else if(mp.get("OLD_PLAN_FINISHED_TIME").toString().indexOf("Error")!=-1){

            }else if (mp.get("OLD_PLAN_START_TIME").toString().indexOf("Error")!=-1) {
//                mp.put("OLD_PLAN_FINISHED_TIME","Error:开始日期错误" );
            }else if (ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).before(today)
                    ||ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).after(END_DATE)
                    ){

                mp.put("OLD_PLAN_FINISHED_TIME","Error:卖进结束日期必须在当前日期之后， 在项目结束时间之前" );

            }else if (ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_START_TIME").toString()).
                    after(ConstantsMecool.SIMPLE_DATE_FORMAT.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()))) {
                mp.put("OLD_PLAN_FINISHED_TIME", "Error:卖进结束日期必须在卖进开始日期之后");
            }
        }catch (Exception e){
            if (null == mp.get("OLD_PLAN_FINISHED_TIME") || "".equals(mp.get("OLD_PLAN_FINISHED_TIME").toString())) {
                mp.put("OLD_PLAN_FINISHED_TIME","Error:计划结束日期不能为空" );
            }else if(mp.get("OLD_PLAN_FINISHED_TIME").toString().indexOf("Error")!=-1){

            }else if (mp.get("OLD_PLAN_START_TIME").toString().indexOf("Error")!=-1) {
//                mp.put("OLD_PLAN_FINISHED_TIME","Error:开始日期错误" );
            }else if (ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).before(today)
                    ||ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()).after(END_DATE)
                    ){
                mp.put("OLD_PLAN_FINISHED_TIME","Error:卖进结束日期必须在当前日期之后， 在项目结束时间之前" );
            }else if (ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_START_TIME").toString()).
                    after(ConstantsMecool.SIMPLE_DATE_FORMAT1.parse(mp.get("OLD_PLAN_FINISHED_TIME").toString()))){
                mp.put("OLD_PLAN_FINISHED_TIME","Error:卖进结束日期必须在卖进开始日期之后" );
            }
        }
        String s = JSONUtils.toJSONString(mp);
        if (s.indexOf("Error")==-1){
            mp.put("opertion","1");
        }else{
            mp.put("opertion","0");
        }
        return mp;
    }

    @RequestMapping("/mjSaveDateAll")
    @ResponseBody
    public int[] mjSaveDateAll(){
        PageData des = this.getPageData();
        MJEntity mjEntity = JedisUtil.getMJEntity(des.getString("jedis"));
        int excelThreadNum = application.getExcelThreadNum();
        int excelThreadOpen = application.getExcelThreadOpen();
        // 开始时间
        Long begin = System.currentTimeMillis();
        int[] it =new int[1];
        try {
            List<LinkedHashMap<String,String>> pds = JedisUtil.getResourceDXCDate(mjEntity.getJedisId());
            AtomicInteger atomicInteger = new AtomicInteger(pds.size());
            it[0] = 0;
            if (pds.size()==0){
                return it;
            }
           if (pds.size()>excelThreadOpen){
               final CountDownLatch sCountDownLatch = new CountDownLatch(excelThreadNum);
               //ThreadPoolExecutor threadpool=new ThreadPoolExecutor(2, excelThreadNum, 20, TimeUnit.SECONDS, new ArrayBlockingQueue(20));
               ExecutorService threadpool = Executors.newFixedThreadPool(excelThreadNum);
               Vector<MJThread> thread = new Vector<MJThread>();
               for(int s=1;s<=excelThreadNum;s++){
                    int count;
                    int start;
                    int size = pds.size();
                    if (s == excelThreadNum) {
                        start = (s - 1) * (size / excelThreadNum);
                        count = s * (size / excelThreadNum )+ (size % excelThreadNum);
                    } else {
                        start = (s - 1) * (size / excelThreadNum);
                        count = s * (size / excelThreadNum);
                    }
                   final Vector<Map> task = new Vector<Map>();
                    for (int i=start;i<count;i++){
                        Map map = Collections.synchronizedMap(pds.get(i));
                        task.add(map);
                    }
                   final MJThread mjThread = new MJThread(task,mjEntity.getJedisId(),mjExcelService,sCountDownLatch,atomicInteger);
                   //mjThread.start();
                   thread.add(mjThread);
                   //threadpool.execute(mjThread);
               }

               try {
                   int i = 0;
                   for (MJThread mt:thread){
                       //mt.join();
                       threadpool.execute(mt);
                       //new Thread(mt,"xiancheng"+i++).start();
                   }
                   sCountDownLatch.await();
                   threadpool.shutdown();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
              while(true){//等待所有任务都结束了继续执行
                   try {
                       if(threadpool.isTerminated()){
                           System.out.println("所有的子线程都结束了！");
                           break;
                       }
                       Thread.sleep(1000);
                   }catch (Exception e){
                       e.printStackTrace();
                   }
               }
           }else {
                try {
                    for (LinkedHashMap<String,String> pd : pds) {
                        mjExcelService.saveMj(pd,mjEntity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
          }
            // 结束时间
            Long end = System.currentTimeMillis();
            // 耗时
            System.out.println(pds.size()+"条数据插入花费时间 : " + (end - begin) / 1000 + " s"+"  插入完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
        it[0] = 1;
        return it;
    }

    @RequestMapping("/mjSaveCount")
    @ResponseBody
    public int mjSaveCount(){
        PageData des = this.getPageData();
        String jedis = des.getString("jedis");
        int l = JedisUtil.getLength(jedis);
        return l;
    }

    @RequestMapping(value = "/excelMjExportDateAll")
    public ModelAndView mjExportDateAll(){
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        MJEntity mjEntity = JedisUtil.getMJEntity(pd.getString("jedis"));
        List<LinkedHashMap<String,String>> list = JedisUtil.getResourceDXC(mjEntity.getJedisId());
        try {
            Map<String,Object> dataMap = new HashMap<String,Object>();
            List<String> titles = new ArrayList<String>();
            List<String> heards = new ArrayList<String>();
            LinkedHashMap<String,String> rh = mjEntity.getPdHeard();
            List<PageData> varList = new ArrayList<PageData>();
            for (int i=0;i<list.size();i++){
                LinkedHashMap<String,String> link = list.get(i);
                if (i==0){
                    Iterator it = list.get(i).entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry) it.next();
                        if (entry.getKey().equals("uuid") || entry.getKey().equals("opertion")){
                        }else{
                            String str ="";
                            if (entry.getKey().equals("OLD_PLAN_START_TIME")||entry.getKey().equals("PLAN_START_TIME")){
                                str = "计划开始日期";
                            }else if (entry.getKey().equals("OLD_PLAN_FINISHED_TIME")||entry.getKey().equals("PLAN_FINISHED_TIME")){
                                str = "计划结束日期";
                            }else{
                                str = rh.get(entry.getKey());
                            }
                            titles.add(str);
                            heards.add(entry.getKey().toString());
                        }
                    }
                }
                PageData p = new PageData();
                int ps = 1;
                for (String heard:heards) {
                    p.put("var"+ps,link.get(heard));
                    ps++;
                }
                varList.add(p);
            }
            dataMap.put("titles",titles);
            dataMap.put("varList",varList);
            /*HSSFWorkbook iop = new HSSFWorkbook();
            ExcelExport eex = new ExcelExport();
            eex.excelExport(dataMap,iop,response);*/
            ObjectExcelView erv = new ObjectExcelView();
            mv = new ModelAndView(erv,dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            mv.setViewName("service/error");
            mv.addObject("msg","导出错误！");
            mv.addObject("empPk",mjEntity.getEmpPk());
            mv.addObject("projectId",mjEntity.getProjectId());
        }
        return mv;
    }

    @RequestMapping(value = "/excelMjExportDateAllError")
    public ModelAndView excelMjExportDateAllError(){
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        MJEntity mjEntity = JedisUtil.getMJEntity(pd.getString("jedis"));
        List<LinkedHashMap<String,String>> list = JedisUtil.getResourceDXC(mjEntity.getJedisId());
        try {
            Map<String,Object> dataMap = new HashMap<String,Object>();
            List<String> titles = new ArrayList<String>();
            List<String> heards = new ArrayList<String>();
            LinkedHashMap<String,String> rh = mjEntity.getPdHeard();
            List<PageData> varList = new ArrayList<PageData>();
            for (int i=0;i<list.size();i++){
                LinkedHashMap<String,String> link = list.get(i);
                String opertion = list.get(i).get("opertion");
                if (i==0){
                    Iterator it = list.get(i).entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry) it.next();
                        if (entry.getKey().equals("uuid") || entry.getKey().equals("opertion")){
                        }else {
                            String str = "";
                            if (entry.getKey().equals("OLD_PLAN_START_TIME") || entry.getKey().equals("PLAN_START_TIME")) {
                                str = "计划开始日期";
                            } else if (entry.getKey().equals("OLD_PLAN_FINISHED_TIME") || entry.getKey().equals("PLAN_FINISHED_TIME")) {
                                str = "计划结束日期";
                            } else {
                                str = rh.get(entry.getKey());
                            }
                            titles.add(str);
                            heards.add(entry.getKey().toString());
                        }

                    }
                }
                if (opertion.equals("1")){
                    continue;
                }
                PageData p = new PageData();
                int ps = 1;
                for (String heard:heards) {
                    p.put("var"+ps,link.get(heard));
                    ps++;
                }
                varList.add(p);
            }
            dataMap.put("titles",titles);
            dataMap.put("varList",varList);
            /*HSSFWorkbook iop = new HSSFWorkbook();
            ExcelExport eex = new ExcelExport();
            eex.excelExport(dataMap,iop,response);*/
            ObjectExcelView erv = new ObjectExcelView();
            mv = new ModelAndView(erv,dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            mv.setViewName("service/error");
            mv.addObject("msg","导出错误！");
            mv.addObject("empPk",mjEntity.getEmpPk());
            mv.addObject("projectId",mjEntity.getProjectId());
        }
        return mv;
    }

    @RequestMapping("/delRow")
    @ResponseBody
    public String delRow(){
        PageData pd = this.getPageData();
        String jedis = pd.getString("jedis");
        String uuid = pd.getString("uuid");
        String del = "success";
        try {
            JedisUtil.delRow(jedis,uuid);
        } catch (Exception e) {
            e.printStackTrace();
            del = "error";
        }
        return del;
    }

    /**
     * 校验计划执行天数是否大于执行日(天数)
     * @param psi
     * @param isNewData
     * @param execDateStr
     * @param planExecDateTime 计划执行天数
     */
    public boolean checkExecDate(PageData psi, boolean isNewData, String execDateStr, Long planExecDateTime){
        Map<String, Date> ssm = new HashMap<String, Date>();
        if(StringUtils.isNotBlank(execDateStr)){
            ssm = MecoolUtil.parseDaysStr(execDateStr);
        }
        if(!isNewData){
            try {
                PageData query = new PageData();
                query.put("PROJECT_SELLIN_INFO_ID",Long.parseLong(psi.get("ID").toString()));
                List<PageData> oldDate = mjExcelService.getProjectSellinExecdateList(query);
                if(oldDate!=null && oldDate.size()>0) {
                    for (PageData projectSellinExecdate : oldDate) {
                        Date date = new Date();
                        TIMESTAMP dateTime =(TIMESTAMP)projectSellinExecdate.get("EXEC_DATE");
                        date = dateTime.dateValue();
                        String d = ConstantsMecool.SIMPLE_DATE_FORMAT1.format(date);
                        ssm.put(d, date);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(ssm.keySet()!=null && ssm.keySet().size()>planExecDateTime.intValue()){
            return true;
        }else{
            return false;
        }

    }

    /**
     *  促销员排班<=计划执行场次数
     * @param psi
     * @param isNewData
     * @param planExecDateTime
     * @return
     */
    public boolean checkSalesSchedule(PageData psi, boolean isNewData, Long planExecDateTime){
        if(isNewData){
            return false;
        }
        if(psi!=null && psi.get("ID")!=null){
            try {
                PageData query = new PageData();
                query.put("ID",BigDecimal.valueOf(Long.parseLong(psi.get("ID").toString())));
                PageData pageData = mjExcelService.getSalesScheduleCalendarByProjectId(query);
                if(Long.parseLong(pageData.get("NUM").toString())>planExecDateTime){
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    @RequestMapping("importExcel")
    public ModelAndView importExcel(){
        PageData pageData =new PageData();
        pageData=this.getPageData();
        ModelAndView mv = new ModelAndView();
        mv.setViewName("service/importExcel");
        mv.addObject("jedis",JSONUtils.toJSONString(pageData.get("jedis").toString()));
        return mv;
    }

    /**
     * 保存行
     * @param project
     * @param headerListDB
     * @param listPd
     * @param mmp
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private void saveSellins(PageData project, List<PageData> headerListDB, List<PageData> listPd, LinkedHashMap<String,String> mmp, MJEntity mjEntity)throws Exception {
        //JedisId
        mjEntity.setJedisId(UUID.randomUUID().toString().replace("-",""));
        Jedis jedis = JedisUtil.getJedis();
        //数据长度
        LinkedHashMap<String, String> pdHeard = new LinkedHashMap<String, String>();
        PageData heards= new PageData();
        PageData hfHeard = new PageData();
        pdHeard.put("uuid", "ID");
        pdHeard.put("opertion","操作");
        //循环操作头信息
        for (PageData psh : headerListDB) {
            heards.put(psh.getString("HEADER_NAME"), psh);
            if (psh.getString("SOURCE_TYPE") == null) {
                psh.put("SOURCE_TYPE",ConstantsMecool.SellinSourceType.PHONE_INPUT.getType());
            }
            for (int i=0;i<listPd.get(0).size();i++){
                if(listPd.get(0).get("var"+i).equals(psh.getString("HEADER_NAME"))){
//                    if (!psh.getString("HEADER_NAME").equals("C1")||!psh.getString("HEADER_NAME").equals("C2")){
//
//                    }
                    pdHeard.put(psh.getString("SELLIN_C_NAME"),psh.getString("HEADER_NAME"));
                    hfHeard.put(psh.getString("HEADER_NAME"),psh.getString("SELLIN_C_NAME"));
                }
            }
        }
        PageData he = listPd.get(0);
        for (int i=0;i<he.size();i++){
            String str = he.get("var"+i).toString();
            if (str.equals("计划陈列数量/面积")){
                pdHeard.put("PLAN_EXHIBIT_NUM","计划陈列数量/面积");
                hfHeard.put("计划陈列数量/面积","PLAN_EXHIBIT_NUM");
            }else
            if (str.equals("城市督导姓名")){
                pdHeard.put("CITY_EMP_NAME", "城市督导姓名");
                hfHeard.put("城市督导姓名","CITY_EMP_NAME");
            }else
            if (str.equals("门店督导姓名")){
                pdHeard.put("EMP_NAME", "门店督导姓名");
                hfHeard.put("门店督导姓名","EMP_NAME");
            }else
            if (str.equals("区域负责人姓名")){
                pdHeard.put("AREA_MANAGER_NAME", "区域负责人姓名");
                hfHeard.put("区域负责人姓名","AREA_MANAGER_NAME");
            }else
            if (str.equals("区域负责人编号")){
                pdHeard.put("AREA_MANAGER_CODE", "区域负责人编号");
                hfHeard.put("区域负责人编号","AREA_MANAGER_CODE");
            }else
            if (str.equals("执行日期")){
                pdHeard.put("EXEC_DATE_TIME", "执行日期");
                hfHeard.put("执行日期","EXEC_DATE_TIME");
            }else
            if (str.equals("计划执行店次(店*天)")){
                pdHeard.put("EXEC_DATE_TIME_COUNT", "计划执行店次(店*天)");
                hfHeard.put("计划执行店次(店*天)","EXEC_DATE_TIME_COUNT");
            }else
            if (str.equals("开档计划")){
                pdHeard.put("PLAN_STORE_OPEN_COUNT", "开档计划");
                hfHeard.put("开档计划","PLAN_STORE_OPEN_COUNT");
            }
        }

        mjEntity.setHeards(heards);
        mjEntity.setHfHeard(hfHeard);
        mmp.put("heard", JSONUtils.toJSONString(pdHeard));
        mjEntity.setPdHeard(pdHeard);

        for (int k = 1; k < listPd.size(); k++) {
            PageData data = listPd.get(k);
            if (data == null||data.size()==0) {
                continue;
            }
            LinkedHashMap<String,String> mp = cellExcel(data,he, null,mjEntity);
            if (mp==null){
                continue;
            }else {
                if (k==1){
                    List listrh = new ArrayList<String>();
                    Iterator list0 = mp.entrySet().iterator();
                    List stringList = new ArrayList<PageData>();
                    for (int i=0;i<mp.size();i++){
                        Map.Entry entryData = (Map.Entry) list0.next();
                        Iterator iterator = pdHeard.entrySet().iterator();
                        while (iterator.hasNext()){
                            Map.Entry entry = (Map.Entry) iterator.next();
                            if (entry.getKey().equals(entryData.getKey())
                                    ||entryData.getKey().equals("OLD_PLAN_START_TIME")
                                    ||entryData.getKey().equals("OLD_PLAN_FINISHED_TIME")){
                                PageData hd = new PageData();
                                if ("计划执行天数".equals(entry.getValue())||"计划常规执行天数".equals(entry.getValue())||"卖进序号".equals(entry.getValue())
                                        ||"计划三薪执行天数".equals(entry.getValue())||"计划执行场次数".equals(entry.getValue())||"计划三薪执行场次数".equals(entry.getValue())
                                        ||"计划巡店总次数".equals(entry.getValue())||"周巡店次数".equals(entry.getValue())||"月巡店次数".equals(entry.getValue())
                                        ||"总销量目标".equals(entry.getValue())||"日店均销量目标".equals(entry.getValue())||"计划常规执行场次数".equals(entry.getValue())){
                                    hd.put("data",entry.getKey());
                                    hd.put("type","numeric");
                                }else if ("计划开始日期".equals(entry.getValue())||"计划结束日期".equals(entry.getValue())||"计划陈列开始日期".equals(entry.getValue())
                                        ||"计划陈列结束日期".equals(entry.getValue())||entryData.getKey().equals("OLD_PLAN_START_TIME")||entryData.getKey().equals("OLD_PLAN_FINISHED_TIME")){
                                    hd.put("data",entryData.getKey());
                                    hd.put("type","date");
                                    hd.put("dateFormat","YYYY-MM-DD");
                                    hd.put("correctFormat","true");
                                }else{
                                    hd.put("data",entry.getKey());
                                    hd.put("type","text");
                                }
                                stringList.add(hd);
                                if (entryData.getKey().equals("OLD_PLAN_START_TIME")) {
                                    listrh.add("计划开始日期");
                                }else if (entryData.getKey().equals("OLD_PLAN_FINISHED_TIME")){
                                    listrh.add("计划结束日期");
                                }else{
                                    listrh.add(entry.getValue());
                                }
                                break;
                            }
                        }
                    }
                    mjEntity.setColHeaders(stringList);
                    mjEntity.setExcelHeard(listrh);
                }
                mmp.put(mp.get("uuid"), JSONUtils.toJSONString(mp));
            }
        }

        /*存入数据库操作======================================*/
        mjEntity.setJedisId(mjEntity.getJedisId()+"ExcelMJ");
        JSONObject json = JSONObject.fromObject(mjEntity);
        String str = json.toString();
        mmp.put("entry",str);
        //执行
        jedis.hmset(mjEntity.getJedisId(),mmp);
    }

}
