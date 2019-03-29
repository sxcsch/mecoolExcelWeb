package com.mecool.controller.excel;

import com.alibaba.druid.support.json.JSONUtils;
import com.mecool.controller.base.BaseController;
import com.mecool.entity.Application;
import com.mecool.entity.PBEntity;
import com.mecool.entity.Page;
import com.mecool.service.PBExcelService;
import com.mecool.thread.PBThread;
import com.mecool.util.*;
import net.sf.json.JSONObject;
import oracle.sql.TIMESTAMP;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Eddy on 3/14/2018.
 */
@Controller
@RequestMapping("/excel")
public class PBExcelController extends BaseController{

    protected Logger logger = Logger.getLogger(this.getClass());
    @Autowired
    private PBExcelService pbExcelService;

    private Application application = new Application();

    @RequestMapping("/pbUpload")
    public ModelAndView uploadFile(ModelAndView mv){
        mv.setViewName("service/uploadexcel");
        PageData pd = this.getPageData();
        mv.addObject("data", JSONUtils.toJSONString("pb"));
        mv.addObject("empPk",pd.getString("empPk"));
        mv.addObject("projectId",pd.getString("projectId"));
        return mv;
    }

    @RequestMapping(value="/readPBExcel")
    public ModelAndView readKDExcel(@RequestParam(value="service",required=false) MultipartFile file) throws Exception{
        ModelAndView mv = this.getModelAndView();
        PageData pd = this.getPageData();
        PBEntity pbEntity = new PBEntity();
        pbEntity.setProjectId(pd.getString("projectId"));
        pbEntity.setEmpPk(pd.getString("empPk"));
        List<PageData> listPd = null;
        if (null != file && !file.isEmpty()) {
            String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE;								//文件上传路径
            String fileName =  FileUpload.fileUp(file, filePath, "userexcel");							//执行上传
            listPd = (List) ObjectExcelRead.readExcel(filePath, fileName, 0, 0, 0);	//执行读EXCEL操作,读出的数据导入List 2:从第3行开始；0:从第A列开始；0:第0个sheet

            PageData heared = listPd.get(0);
            PageData projectD = new PageData();
            projectD.put("PROJECT_ID",pbEntity.getProjectId());
            projectD  = pbExcelService.findProjectById(projectD);
            if (projectD==null||projectD.size()==0){
                mv.setViewName("service/error");
                mv.addObject("msg","项目不存在！");
                mv.addObject("empPk",pbEntity.getEmpPk());
                mv.addObject("projectId",pbEntity.getProjectId());
                return mv;
            }
            //检测头信息，项目是否存在
            PageData checkoutHeader = checkHeader(heared);
            LinkedHashMap<String,String> mmp = new LinkedHashMap<String, String>();
            if (checkoutHeader!=null&&checkoutHeader.get("HeaderError")==null){
                //导入行
                saveSellins(listPd,mmp,pbEntity);
            }else {
                mv.setViewName("service/error");
                mv.addObject("msg",checkoutHeader);
                mv.addObject("empPk",pbEntity.getEmpPk());
                mv.addObject("projectId",pbEntity.getProjectId());
                return mv;
            }
            mv.addObject("msg","success");
        }
        mv.setViewName("service/transition");
        mv.addObject("data",JSONUtils.toJSONString("pb"));
        mv.addObject("jedis",JSONUtils.toJSONString(pbEntity.getJedisId()));
        return mv;
    }

    /**
     * 检查文档中Header的完整性，必须的header是否都存在
     * @return
     * @throws Exception
     */
    private PageData checkHeader(PageData headerList) throws Exception {
        PageData data = new PageData();
        if (headerList == null || headerList.size() < 1) {
            data.put("HeaderError", "没有字段！");
            return data;
        }
        List<String> hlist = new ArrayList<String>();
        hlist.add("姓名");
        hlist.add("电话");
        hlist.add("身份证号码");
        hlist.add("开户银行");
        hlist.add("工资卡号");
        hlist.add("考勤日期");
        hlist.add("系统终端编号");
        hlist.add("班次名称");
        hlist.add("开始时间");
        hlist.add("结束时间");
        hlist.add("排班类型");

        for (int i = 0; i < headerList.size(); i++) {
            try {
                if (headerList.get("var"+i) == null) {
                    continue;
                }
                String header = headerList.get("var"+i).toString().trim();
                hlist.remove(header);
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }
        }

        if (hlist.size() > 0) {
            String h = "缺少关键字段: ";
            for (String s : hlist) {
                h = h + s +" ";
            }
            data.put("HeaderError", h);
            return data;
        }
        return data;
    }

    private void saveSellins(List<PageData> listPd, LinkedHashMap<String,String> mmp,PBEntity pbEntity)throws Exception {
        //JedisId
        pbEntity.setJedisId(UUID.randomUUID().toString().replace("-",""));

        Jedis jedis = JedisUtil.getJedis();
        //数据长度
        LinkedHashMap pdHeard = new LinkedHashMap<String, String>();

        pbEntity.setHeard(listPd.get(0));
        pdHeard.put("uuid", "ID");
        pdHeard.put("opertion","操作");
        pdHeard.put("SALES_NAME","姓名");
        pdHeard.put("SALES_TYPE","促销员类型");
        pdHeard.put("PRODUCT","负责产品");
        pdHeard.put("SALES_PHONE","电话");
        pdHeard.put("SALES_CARD_ID","身份证号码");
        pdHeard.put("NAME","开户银行");
        pdHeard.put("SALES_SALARY_CARD","工资卡号");
        pdHeard.put("SALES_MEMO","备注");
        pdHeard.put("Sales_Schedule_Date","考勤日期");
        pdHeard.put("CHANNEL_SYNC_ID","系统终端编号");
        pdHeard.put("SW_NAME","班次名称");
        pdHeard.put("SW_BEGIN_TIME","开始时间");
        pdHeard.put("SW_END_TIME","结束时间");
        pdHeard.put("SW_REST_BEGIN_TIME1","休息开始时间1");
        pdHeard.put("SW_REST_END_TIME1","休息结束时间1");
        pdHeard.put("SW_REST_BEGIN_TIME2","休息开始时间2");
        pdHeard.put("SW_REST_END_TIME2","休息结束时间2");
        pdHeard.put("SwType","排班类型");
        pbEntity.setPdHeard(pdHeard);
        PageData ph = new PageData();
        Iterator<Map.Entry<String,String>> is = pdHeard.entrySet().iterator();
        for (int i=0;is.hasNext();i++){
            Map.Entry<String,String> entry = is.next();
            String value = entry.getValue();
            String key = entry.getKey();
            for (int k=0;k<pdHeard.size()-2;k++ ){
                if (value.equals(listPd.get(0).get("var"+k).toString().trim())){
                    ph.put(value,key);
                }
            }
        }
        pbEntity.setFzHeard(ph);
        mmp.put("heard",JSONUtils.toJSONString(pdHeard));
        LinkedHashMap<String,String> mmpBy = new LinkedHashMap<String,String>();
        for (int k = 1; k < listPd.size(); k++) {

            PageData data = listPd.get(k);
            if (data == null) {
                continue;
            }else {
                int lop = 0;
                for (int i=0;i<data.size();i++){
                    Object p = data.get("var"+i);
                    if (null==p||p.toString().equals("")){
                        lop+=1;
                    }
                }
                if (lop==data.size()){
                    continue;
                }
            }
            LinkedHashMap<String,String> mp = cellExcel(data,pbEntity.getHeard(),null,pbEntity);
            if (mp==null){
                continue;
            }else {
                if (k==1){
                    List cols = new ArrayList<String>();
                    Iterator list0 = mp.entrySet().iterator();
                    List stringList = new ArrayList<PageData>();
                    for (int i=0;i<mp.size();i++){
                        Map.Entry entryData = (Map.Entry) list0.next();
                        Iterator iterator = pdHeard.entrySet().iterator();
                        while (iterator.hasNext()){
                            Map.Entry entry = (Map.Entry) iterator.next();
                            if (entry.getKey().equals(entryData.getKey())){
                                PageData hd = new PageData();
                                if ("卖进序号".equals(entry.getValue())){
                                    hd.put("data",entry.getKey());
                                    hd.put("type","numeric");
                                }else{
                                    hd.put("data",entry.getKey());
                                    hd.put("type","text");
                                }
                                stringList.add(hd);
                                cols.add(entry.getValue());
                            }
                        }
                    }
                    pbEntity.setColHeaders(stringList);
                    pbEntity.setExcelHeard(cols);
                }
            }
            if (mp.get("opertion").equals("1")){
                mmpBy.put(mp.get("uuid").toString(), JSONUtils.toJSONString(mp));
            }
            mmp.put(mp.get("uuid").toString(), JSONUtils.toJSONString(mp));
        }
        /*存入数据库操作======================================*/
        pbEntity.setJedisId(pbEntity.getJedisId()+"ExcelPB");
        JSONObject json = JSONObject.fromObject(pbEntity);
        String str = json.toString();
        mmp.put("entry",str);
        jedis.hmset(pbEntity.getJedisId(),mmp);
    }

    /**
     * Jedis分页展示数据
     * @param page
     * @return
     */
    @RequestMapping("/pbDataFY")
    public ModelAndView pbDataFY(Page page){
        ModelAndView mv = this.getModelAndView();
        PageData  pp  =  this.getPageData();
        if (null!=pp.getString("page.currentPage")&&!"".equals(pp.getString("page.currentPage"))){
            page.setCurrentPage(Integer.parseInt(pp.getString("page.currentPage")) );
            page.setShowCount(Integer.parseInt(pp.getString("page.showCount")) );
        }
        PBEntity pbEntity = JedisUtil.getPBEntity(pp.get("jedis").toString());
        int size = JedisUtil.getPageCountInt(pbEntity.getJedisId());
        List<HashMap<String,String>> list =null;
        page.setPd(pp);
        try {
            page.setTotalResult(size);
            list = JedisUtil.pagingResource(pbEntity.getJedisId(),page);
            if (list==null||list.size()==0){
                JedisUtil.delResource(pbEntity.getJedisId());
                mv.setViewName("service/error");
                //pp.put("HeaderError","无数据！");
                mv.addObject("msg","无数据！");
                mv.addObject("empPk",pbEntity.getEmpPk());
                mv.addObject("projectId",pbEntity.getProjectId());
                return mv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mv.setViewName("service/PBExcel");
        List<String> excelHeard = pbEntity.getExcelHeard();
        List<PageData> colHeaders = pbEntity.getColHeaders();
        mv.addObject("empPk",pbEntity.getEmpPk());
        mv.addObject("projectId",pbEntity.getProjectId());
        mv.addObject("headString",JSONUtils.toJSONString(colHeaders));
        mv.addObject("pd", this.getPageData());
        mv.addObject("empPk",pbEntity.getEmpPk());
        mv.addObject("pds",JSONUtils.toJSONString(list));
        mv.addObject("jedis",JSONUtils.toJSONString(pbEntity.getJedisId()));
        mv.addObject("rh",JSONUtils.toJSONString(excelHeard));
        return mv;
    }

    public LinkedHashMap<String, String> cellExcel(PageData data, PageData heard,LinkedHashMap<String,String> newHeards,PBEntity pbEntity)throws Exception{
        LinkedHashMap<String,String> mp = new LinkedHashMap<String,String>();
        String uuid = UUID.randomUUID().toString().replace("-","");
        boolean booleanData = false;
        if (data.get("uuid")==null||data.get("uuid").toString().equals("")){
            mp.put("uuid",uuid);
            mp.put("opertion","0");
        }else {
            mp.put("uuid",data.get("uuid").toString());
            mp.put("opertion",data.get("opertion").toString());
            booleanData = true;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        for (int i = 0; i < heard.size(); i++) {
            // 获得数据
            String header = null;
            try {
                if (heard.get("var"+i) == null) {
                    continue;
                }
                header = heard.get("var"+i).toString().trim();
                Object cell = null;
                if (data.get("uuid")==null||data.get("uuid").toString().equals("")){
                    cell = data.get("var"+i);
                }
                PageData ph = pbEntity.getFzHeard();
                if(header.equals("姓名")){
                    if (booleanData){
                        if (data.get("SALES_NAME")!=null||!data.get("SALES_NAME").equals("")){
                            cell = data.get("SALES_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SALES_NAME",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:姓名不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:姓名不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("促销员类型")){
                    if (booleanData){
                        if (data.get("SALES_TYPE")!=null||!data.get("SALES_TYPE").equals("")){
                            cell = data.get("SALES_TYPE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SALES_TYPE",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:促销员类型不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:促销员类型不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("负责产品")){
                    if (booleanData){
                        if (data.get("PRODUCT")!=null||!data.get("PRODUCT").equals("")){
                            cell = data.get("PRODUCT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("PRODUCT",cell.toString());
                                continue;
                            }
                        }
//                        else {
//                            mp.put(ph.getString(header),"Error:负责产品不能为空！");
//                            continue;
//                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"");
//                        mp.put(ph.getString(header),"Error:负责产品不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("电话")){
                    if (booleanData){
                        if (data.get("SALES_PHONE")!=null||!data.get("SALES_PHONE").equals("")){
                            cell = data.get("SALES_PHONE").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SALES_PHONE",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:电话不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:电话不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("身份证号码")){
                    if (booleanData){
                        if (data.get("SALES_CARD_ID")!=null||!data.get("SALES_CARD_ID").equals("")){
                            cell = data.get("SALES_CARD_ID").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SALES_CARD_ID",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:身份证号码不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:身份证号码不能为空！");
                        continue;
                    }
                    if (cell.toString().trim().length()==18){
                        mp.put(ph.getString(header),cell.toString());
                    }else {
                        mp.put(ph.getString(header),"Error:身份证号码位数错误！");
                    }
                }else if (header.equals("开户银行")){
                    if (booleanData){
                        if (data.get("NAME")!=null||!data.get("NAME").equals("")){
                            cell = data.get("NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("NAME",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:开户银行不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:开户银行不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("工资卡号")){
                    if (booleanData){
                        if (data.get("SALES_SALARY_CARD")!=null||!data.get("SALES_SALARY_CARD").equals("")){
                            cell = data.get("SALES_SALARY_CARD").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SALES_SALARY_CARD",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:工资卡号不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:工资卡号不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("备注")){
                    if (booleanData){
                        if (data.get("SALES_MEMO")!=null||!data.get("SALES_NAME").equals("")){
                            cell = data.get("SALES_MEMO").toString();
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("考勤日期")){
                    if (booleanData){
                        if (data.get("Sales_Schedule_Date")!=null||!data.get("Sales_Schedule_Date").equals("")){
                            cell = data.get("Sales_Schedule_Date").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("Sales_Schedule_Date",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:考勤日期不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:考勤日期不能为空！");
                        continue;
                    }
                    Date today = MecoolUtil.getDateNoTime(new Date());
                    Map<String, Date> ssm = null;
                    ssm = MecoolUtil.parseDaysStr(cell.toString());
                    if (ssm==null||ssm.size()<1){
                        mp.put("Sales_Schedule_Date","Error:考勤日期格式错误！");
                    }
                    boolean isError = false;
                    for (Iterator<Date> iterator = ssm.values().iterator(); iterator.hasNext();) {
                        Date tmpDay = (Date) iterator.next();
                        if (tmpDay.before(today)) {
                            mp.put("Sales_Schedule_Date","Error:考勤日期不能在当天之前！");
                            isError = false;
                            break;
                        }else {
                            isError = true;
                        }
                    }
                    if (isError){
                        mp.put(ph.getString(header),cell.toString());
                    }
                }else if (header.equals("系统终端编号")){
                    if (booleanData){
                        if (data.get("CHANNEL_SYNC_ID")!=null||!data.get("CHANNEL_SYNC_ID").equals("")){
                            cell = data.get("CHANNEL_SYNC_ID").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("CHANNEL_SYNC_ID",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:系统终端编号不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:系统终端编号不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("班次名称")){
                    if (booleanData){
                        if (data.get("SW_NAME")!=null||!data.get("SW_NAME").equals("")){
                            cell = data.get("SW_NAME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SW_NAME",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:班次名称不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:班次名称不能为空！");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("开始时间")){
                    if (booleanData){
                        if (data.get("SW_BEGIN_TIME")!=null||!data.get("SW_BEGIN_TIME").equals("")){
                            cell = data.get("SW_BEGIN_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SW_BEGIN_TIME",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:开始时间不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:开始时间不能为空！");
                        continue;
                    }
                    try {
                        if(StringUtils.isNotBlank(cell.toString())){
                            LocalTime.parse(cell.toString(), formatter);
                        }
                    } catch (Exception e) {
                        mp.put(ph.getString(header),"Error:休息时间设置有问题,时间区间/格式只能为(00:00 - 23:59)!");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("结束时间")){
                    if (booleanData){
                        if (data.get("SW_END_TIME")!=null||!data.get("SW_END_TIME").equals("")){
                            cell = data.get("SW_END_TIME").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SW_END_TIME",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:结束时间不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:结束时间不能为空！");
                        continue;
                    }
                    try {
                        if(StringUtils.isNotBlank(cell.toString())){
                            LocalTime.parse(cell.toString(), formatter);
                        }
                    } catch (Exception e) {
                        mp.put(ph.getString(header),"Error:休息时间设置有问题,时间区间/格式只能为(00:00 - 23:59)!");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("休息开始时间1")){
                    if (booleanData){
                        if (data.get("SW_REST_BEGIN_TIME1")!=null){
                            cell = data.get("SW_REST_BEGIN_TIME1").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SW_REST_BEGIN_TIME1",cell.toString());
                                continue;
                            }
                        }
//                        else {
//                            mp.put(ph.getString(header),"Error:休息开始时间1不能为空！");
//                            continue;
//                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"");
//                        mp.put(ph.getString(header),"Error:休息开始时间1不能为空！");
                        continue;
                    }
                    try {
                        if(StringUtils.isNotBlank(cell.toString())){
                            LocalTime.parse(cell.toString(), formatter);
                        }
                    } catch (Exception e) {
                        mp.put(ph.getString(header),"Error:休息时间设置有问题,时间区间/格式只能为(00:00 - 23:59)!");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("休息结束时间1")){
                    if (booleanData){
                        if (data.get("SW_REST_END_TIME1")!=null){
                            cell = data.get("SW_REST_END_TIME1").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("休息结束时间1",cell.toString());
                                continue;
                            }
                        }
//                        else {
//                            mp.put(ph.getString(header),"Error:休息结束时间1不能为空！");
//                            continue;
//                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"");
//                        mp.put(ph.getString(header),"Error:休息结束时间1不能为空！");
                        continue;
                    }
                    try {
                        if(StringUtils.isNotBlank(cell.toString())){
                            LocalTime.parse(cell.toString(), formatter);
                        }
                    } catch (Exception e) {
                        mp.put(ph.getString(header),"Error:休息时间设置有问题,时间区间/格式只能为(00:00 - 23:59)!");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("休息开始时间2")){
                    if (booleanData){
                        if (data.get("SW_REST_BEGIN_TIME2")!=null){
                            cell = data.get("SW_REST_BEGIN_TIME2").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SW_REST_BEGIN_TIME2",cell.toString());
                                continue;
                            }
                        }
//                        else {
//                            mp.put(ph.getString(header),"Error:休息开始时间2不能为空！");
//                            continue;
//                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"");
//                        mp.put(ph.getString(header),"Error:休息开始时间2不能为空！");
                        continue;
                    }
                    try {
                        if(StringUtils.isNotBlank(cell.toString())){
                            LocalTime.parse(cell.toString(), formatter);
                        }
                    } catch (Exception e) {
                        mp.put(ph.getString(header),"Error:休息时间设置有问题,时间区间/格式只能为(00:00 - 23:59)!");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("休息结束时间2")){
                    if (booleanData){
                        if (data.get("SW_REST_END_TIME2")!=null){
                            cell = data.get("SW_REST_END_TIME2").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SW_REST_END_TIME2",cell.toString());
                                continue;
                            }
                        }
//                        else {
//                            mp.put(ph.getString(header),"Error:休息结束时间2不能为空！");
//                            continue;
//                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"");
//                        mp.put(ph.getString(header),"Error:休息结束时间2不能为空！");
                        continue;
                    }
                    try {
                        if(StringUtils.isNotBlank(cell.toString())){
                            LocalTime.parse(cell.toString(), formatter);
                        }
                    } catch (Exception e) {
                        mp.put(ph.getString(header),"Error:休息时间设置有问题,时间区间/格式只能为(00:00 - 23:59)!");
                        continue;
                    }
                    mp.put(ph.getString(header),cell.toString());
                }else if (header.equals("排班类型")){
                    if (booleanData){
                        if (data.get("SwType")!=null||!data.get("SwType").equals("")){
                            cell = data.get("SwType").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SwType",cell.toString());
                                continue;
                            }
                        }else {
                            mp.put(ph.getString(header),"Error:排班类型不能为空！");
                            continue;
                        }
                    }
                    if (cell==null||cell.toString().equals("")||cell.toString().equals("null")){
                        mp.put(ph.getString(header),"Error:排班类型不能为空！");
                        continue;
                    }
                    PageData pds = new PageData();
//                    pds.put("NAME",cell.toString());
//                    List<PageData> selectValue = pbExcelService.getSelectValueNameList(pds);
                    pds.put("TYPE","15");
                    List<PageData> svSWList = pbExcelService.getSelectValueList(pds);
                    PageData ppp = new PageData();
                    for (PageData sv : svSWList) {
                        ppp.put(sv.get("NAME").toString(),sv);
                    }
                    PageData selectValue = (PageData) ppp.get(cell.toString().trim());
                    if (selectValue == null) {
                        mp.put(ph.getString(header),"Error:排班类型不存在!"+cell.toString());
                    }else{
                        mp.put(ph.getString(header),cell.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mp.get("CHANNEL_SYNC_ID").toString().indexOf("Error")==-1){
            PageData pd = new PageData();
            pd.put("CHANNEL_CODE",mp.get("CHANNEL_SYNC_ID").toString());
            List<PageData> channel = pbExcelService.getChannelList(pd);
            if (channel==null&&channel.size()<1){
                mp.put("CHANNEL_SYNC_ID","Error:没有对应门店!"+mp.get("CHANNEL_SYNC_ID").toString());
            }
            pd.put("CHANNEL_SYNC_ID",channel.get(0).get("ID").toString());
            pd.put("PROJECT_ID",pbEntity.getProjectId());
            List<PageData> psis = pbExcelService.findByMJinfoByProjectAndChannel(pd);
            if (psis == null || psis.size() < 1) {
                mp.put("CHANNEL_SYNC_ID","Error:没有对应卖进!"+mp.get("CHANNEL_SYNC_ID").toString());
            }else {
                if (mp.get("Sales_Schedule_Date").toString().indexOf("Error")==-1){
                    pd.put("CHANNEL",channel.get(0).get("ID").toString());
                    pd.put("PROJECT_SELLIN_INFO_ID",psis.get(0).get("ID"));
                    List<PageData> lpd = pbExcelService.findByProjectSellinExecdateAndChannel(pd);
                    if (lpd==null&&lpd.size()<1){
                        mp.put("Sales_Schedule_Date","Error:执行日为空！");
                    }else{
                        Date today = MecoolUtil.getDateNoTime(new Date());
                        Map<String, Date> ssm = null;
                        try {
                            ssm = MecoolUtil.parseDaysStr(mp.get("Sales_Schedule_Date").toString());
                        } catch (Exception e) {
                            mp.put("Sales_Schedule_Date","Error:考勤日期格式错误！");
                        }
                        if (mp.get("Sales_Schedule_Date").toString().indexOf("Error")==-1){
                            pd.put("DELETE_FLAG",ConstantsMecool.DeleteFlag.USING.getType());
                            //pd.put("PROJECT_SELLIN_INFO_ID",psis.get(0).get("ID"));
                            pd.put("SALES_CARD_ID",mp.get("SALES_CARD_ID"));
                            List<PageData> saleses = pbExcelService.findByProjectSellinInfoIdAndDeleteFlagAndSales(pd);
                            for (Iterator<Date> iterator = ssm.values().iterator(); iterator.hasNext();) {
                                boolean isError = false;
                                Date tmpDay = (Date) iterator.next();
                                if (tmpDay.before(today)) {
                                    mp.put("Sales_Schedule_Date","Error:考勤日期不能在当天之前！");
                                    isError = false;
                                    break;
                                }else {
                                    for (PageData pe:lpd){
                                        TIMESTAMP da = (TIMESTAMP) pe.get("EXEC_DATE");
                                        if (!da.dateValue().equals(tmpDay)){
                                            isError = false;
                                        }else {
                                            if (saleses!=null&&saleses.size()>0){
                                                pd.put("SALES_CARD_ID",mp.get("SALES_CARD_ID"));
                                                pd.put("SC_SCHEDULE_DATE",da.dateValue());
                                                List<PageData> sale = pbExcelService.findSalesBySalesCardAndDate(pd);
                                                if (sale!=null&&sale.size()>0){
                                                    mp.put("Sales_Schedule_Date","Error:考勤日期冲突！");
                                                    isError = false;
                                                    break;
                                                }
                                            }
                                            isError = true;
                                            break;
                                        }
                                    }
                                }
                                if (isError){
                                    mp.put("Sales_Schedule_Date",mp.get("Sales_Schedule_Date").toString());
                                }else if (mp.get("Sales_Schedule_Date").toString().indexOf("Error")!=-1){
                                    break;
                                }else{
                                    mp.put("Sales_Schedule_Date","Error:考勤日期不在执行日！");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }


        String s = JSONUtils.toJSONString(mp);
        if (s.indexOf("Error")==-1){
            mp.put("opertion","1");
        }else {
            mp.put("opertion","0");
            return mp;
        }
        return mp;
    }

    /**
     * handsoontable无感提交（仅支持双击）
     * @return
     */
    @RequestMapping("/pbDataAjax")
    @ResponseBody
    public Object pbDataAjax(){
        PageData  pp  =  this.getPageData();
        String jedis = pp.getString("jedis");
        PBEntity pbEntity = JedisUtil.getPBEntity(jedis);
        if (null==pp.getString("uuid")||"".equals(pp.getString("uuid"))||"null".equals(pp.getString("uuid"))){
            JedisUtil.modifyPBEntityResource(pbEntity,pbEntity.getJedisId());
            String str=UUID.randomUUID().toString().replace("-","");
            pp.put("uuid",str);
            JedisUtil.addResource(pp,pbEntity.getJedisId());
            pp.put("success",str);
        }else if("1".equals(pp.getString("opertion"))||"0".equals(pp.getString("opertion"))||"null".equals(pp.getString("opertion"))){
            try{
                LinkedHashMap<String,String> newHeards = JedisUtil.getResourceHeard(pbEntity.getJedisId());
                LinkedHashMap<String,String> pps = cellExcel(pp,pbEntity.getHeard(),newHeards,pbEntity);
                JedisUtil.modifyResource(pps,pbEntity.getJedisId());
                pp.put("edit",JSONUtils.toJSONString(pps));
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try {
                pp.put("opertion",0);
                LinkedHashMap<String,String> newHeards = JedisUtil.getResourceHeard(pbEntity.getJedisId());
                LinkedHashMap<String,String> pps = cellExcel(pp,pbEntity.getHeard(),newHeards,pbEntity);
                JedisUtil.modifyResource(pps,pbEntity.getJedisId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pp.put("jedis",pbEntity.getJedisId());
        return AppUtil.returnObject(new PageData(), pp);
    }

    @RequestMapping("/pbSaveDateAll")
    @ResponseBody
    public int[] pbSaveDateAll(){
        PageData des = this.getPageData();
        PBEntity pbEntity = JedisUtil.getPBEntity(des.getString("jedis"));
        int excelThreadNum = application.getExcelThreadNum();
        int excelThreadOpen = application.getExcelThreadOpen();
        int[] it =new int[1];
        try {
            Long begin = System.currentTimeMillis();
            List<LinkedHashMap<String,String>> pds = JedisUtil.getResourceDXCDate(pbEntity.getJedisId());
            it[0] = 0;
            if (pds.size()==0){
                return it;
            }

            if (pds.size()>excelThreadOpen){
                final CountDownLatch sCountDownLatch = new CountDownLatch(excelThreadNum);
                //ThreadPoolExecutor threadpool=new ThreadPoolExecutor(2, excelThreadNum, 20, TimeUnit.SECONDS, new ArrayBlockingQueue(10));
                ExecutorService threadpool = Executors.newFixedThreadPool(excelThreadNum);
                //ExecutorService pool = Executors.newFixedThreadPool(excelThreadNum);
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
                    //final List<LinkedHashMap<String,String>> task =Collections.synchronizedList(pds.subList(start,count));
                    final Vector<Map> task = new Vector<Map>();
                    for (int i=start;i<count;i++){
                        task.add(Collections.synchronizedMap(pds.get(i)));
                    }
                    final PBThread pbThread = new PBThread(task,pbEntity.getJedisId(),pbExcelService,sCountDownLatch);
                    threadpool.execute(pbThread);
                }
                try {
                    sCountDownLatch.await();
                    threadpool.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    for (LinkedHashMap<String,String> pd : pds) {
                        pbExcelService.savePb(pd,pbEntity);
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
        it[0]=1;
        return it;
    }

    @RequestMapping("/pbSaveCount")
    @ResponseBody
    public int pbSaveCount(){
        PageData des = this.getPageData();
        String jedis = des.getString("jedis");
        int l = JedisUtil.getLength(jedis);
        return l;
    }


    @RequestMapping("/pbExportDateAll")
    public ModelAndView pbExportDateAll(){
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        PBEntity pbEntity = JedisUtil.getPBEntity(pd.getString("jedis"));
        List<LinkedHashMap<String,String>> list = JedisUtil.getResourceDXC(pbEntity.getJedisId());
        try {
            Map<String,Object> dataMap = new HashMap<String,Object>();
            List<String> titles = new ArrayList<String>();
            List<String> heards = new ArrayList<String>();
            LinkedHashMap<String,String> rh = pbEntity.getPdHeard();
            List<PageData> varList = new ArrayList<PageData>();
            for (int i=0;i<list.size();i++){
                LinkedHashMap<String,String> link = list.get(i);
                PageData p = new PageData();
                int ps = 1;
                if (i==0){
                    Iterator it = list.get(i).entrySet().iterator();
                    while (it.hasNext()){
                        Map.Entry entry = (Map.Entry) it.next();
                        if (entry.getKey().equals("uuid") || entry.getKey().equals("opertion")){
                        }else{
                            String str = rh.get(entry.getKey());
                            titles.add(str);
                            heards.add(entry.getKey().toString());
                        }
                    }
                }
                for (String heard:heards) {
                    p.put("var"+ps,link.get(heard));
                    ps++;
                }
                varList.add(p);
            }
            dataMap.put("titles",titles);
            dataMap.put("varList",varList);
            ObjectExcelView erv = new ObjectExcelView();
            mv = new ModelAndView(erv,dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            mv.setViewName("service/error");
            mv.addObject("msg","导出错误！");
            mv.addObject("empPk",pbEntity.getEmpPk());
            mv.addObject("projectId",pbEntity.getProjectId());
        }
        return mv;
    }

    @RequestMapping("/pbExportDateAllError")
    public ModelAndView pbExportDateAllError(){
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        PBEntity pbEntity = JedisUtil.getPBEntity(pd.getString("jedis"));
        List<LinkedHashMap<String,String>> list = JedisUtil.getResourceDXC(pbEntity.getJedisId());
        try {
            Map<String,Object> dataMap = new HashMap<String,Object>();
            List<String> titles = new ArrayList<String>();
            List<String> heards = new ArrayList<String>();
            LinkedHashMap<String,String> rh = pbEntity.getPdHeard();
            List<PageData> varList = new ArrayList<PageData>();
            for (int i=0;i<list.size();i++){
                LinkedHashMap<String,String> link = list.get(i);
                String opertion = list.get(i).get("opertion");
                if(i==0){
                    Iterator it = list.get(i).entrySet().iterator();
                    while (it.hasNext()){
                        Map.Entry entry = (Map.Entry) it.next();
                        if (entry.getKey().equals("uuid") || entry.getKey().equals("opertion")){

                        }else{
                            String str = rh.get(entry.getKey());
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
            ObjectExcelView erv = new ObjectExcelView();
            mv = new ModelAndView(erv,dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            mv.setViewName("service/error");
            mv.addObject("msg","导出错误！");
            mv.addObject("empPk",pbEntity.getEmpPk());
            mv.addObject("projectId",pbEntity.getProjectId());
        }
        return mv;
    }

    @RequestMapping("importExcelPB")
    public ModelAndView importExcel(){
        PageData pageData =new PageData();
        pageData=this.getPageData();
        ModelAndView mv = new ModelAndView();
        mv.setViewName("service/importExcelPB");
        mv.addObject("jedis",JSONUtils.toJSONString(pageData.get("jedis").toString()));
        return mv;
    }

    @RequestMapping("delReturn")
    public void delReturn(){
        PageData pd = this.getPageData();
        JedisUtil.delResource(pd.get("jedis").toString());
    }
}
