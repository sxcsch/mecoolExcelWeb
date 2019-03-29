package com.mecool.controller.excel;

import com.alibaba.druid.support.json.JSONUtils;
import com.mecool.controller.base.BaseController;
import com.mecool.entity.Application;
import com.mecool.entity.KDEntity;
import com.mecool.entity.Page;
import com.mecool.service.KDExcelService;
import com.mecool.thread.KDThread;
import com.mecool.util.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Eddy on 3/13/2018.
 */
@Controller
@RequestMapping("/excel")
public class KDExcelController extends BaseController{
    private final static Logger logger = LoggerFactory.getLogger(KDExcelController.class);

    @Autowired
    private KDExcelService kdExcelService;

    private Application application = new Application();

    @RequestMapping("/kdUpload")
    public ModelAndView uploadFile(ModelAndView mv){
        PageData pd = this.getPageData();
        mv.setViewName("service/uploadexcel");
        mv.addObject("data",JSONUtils.toJSONString("kd"));
        mv.addObject("empPk",pd.getString("empPk"));
        mv.addObject("projectId",pd.getString("projectId"));
        return mv;
    }

    @RequestMapping(value="/readKDExcel")
    public ModelAndView readKDExcel(@RequestParam(value="service",required=false) MultipartFile file) throws Exception{
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        KDEntity kdEntity = new KDEntity();
        kdEntity.setProjectId(pd.getString("projectId"));
        kdEntity.setEmpPk(pd.getString("empPk"));
        List<PageData> listPd = null;
        if (null != file && !file.isEmpty()) {
            String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE;								//文件上传路径
            String fileName =  FileUpload.fileUp(file, filePath, "userexcel");							//执行上传
            listPd = (List) ObjectExcelRead.readExcel(filePath, fileName, 0, 0, 0);	//执行读EXCEL操作,读出的数据导入List 2:从第3行开始；0:从第A列开始；0:第0个sheet

            PageData heared = listPd.get(0);
            PageData projectD = new PageData();
            projectD.put("PROJECT_ID",kdEntity.getProjectId());
            projectD  = kdExcelService.findProjectById(projectD);
            if (projectD==null||projectD.size()==0){
                mv.setViewName("service/error");
                mv.addObject("msg","项目不存在！");
                mv.addObject("empPk",kdEntity.getEmpPk());
                mv.addObject("projectId",kdEntity.getProjectId());
                return mv;
            }
            //检测头信息，项目是否存在
            PageData checkoutHeader = checkHeader(heared);
            if (checkoutHeader!=null&&checkoutHeader.get("HeaderError")==null){
                LinkedHashMap<String,String> mmp = new LinkedHashMap<String, String>();
                //导入行
                saveSellins(listPd,mmp,kdEntity);
            }else {
                mv.setViewName("service/error");
                mv.addObject("msg",checkoutHeader);
                mv.addObject("empPk",kdEntity.getEmpPk());
                mv.addObject("projectId",kdEntity.getProjectId());
                return mv;
            }
            mv.addObject("msg","success");
        }
        mv.setViewName("service/transition");
        mv.addObject("data",JSONUtils.toJSONString("kd"));
        mv.addObject("jedis",JSONUtils.toJSONString(kdEntity.getJedisId()));
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
        } else if (headerList.size() > 100) {
            data.put("HeaderError", "字段总数超过80！");
            return data;
        }
        List<String> hlist = new ArrayList<String>();
        hlist.add(ConstantsMecool.SellinFields.CHANNEL_SYNC_ID.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.SCHEDULE_NUM.getHeaderName());
        hlist.add(ConstantsMecool.SellinFields.PLAN_STORE_OPEN_COUNT.getHeaderName());

        for (int i = 0; i < headerList.size(); i++) {
            try {
                if (headerList.get("var"+i) == null) {
                    continue;
                }
                String header = headerList.get("var"+i).toString().trim();
                hlist.remove(header);
            } catch (Exception e) {
                throw e;
            }
        }

        if (hlist.size() > 0) {
            String h = "缺少关键字段: ";
            for (String s : hlist) {
                h = h + s;
            }
            data.put("HeaderError", h);
            return data;
        }
        return data;
    }

    /**
     * 保存行
     * @param listPd
     * @param mmp
     * @throws Exception
     */
    private void saveSellins(List<PageData> listPd, LinkedHashMap<String,String> mmp,KDEntity kdEntity)throws Exception {
        //JedisId
        kdEntity.setJedisId(UUID.randomUUID().toString().replace("-",""));
        Jedis jedis = JedisUtil.getJedis();
        LinkedHashMap pdHeard = new LinkedHashMap<String, String>();
        kdEntity.setHeard(listPd.get(0));
        pdHeard.put("uuid", "ID");
        pdHeard.put("opertion","操作");
        pdHeard.put("CHANNEL_SYNC_ID","系统终端编号");
        pdHeard.put("SCHEDULE_NUM","卖进序号");
        pdHeard.put("PLAN_STORE_OPEN_COUNT","开档计划");
        mmp.put("heard",JSONUtils.toJSONString(pdHeard));
        kdEntity.setPdHeard(pdHeard);
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

            LinkedHashMap<String,String> mp = cellExcel(data,kdEntity.getHeard(),null,kdEntity);
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
                    kdEntity.setColHeaders(stringList);
                    kdEntity.setExcelHeard(cols);
                }
            }
            if (mp.get("opertion").equals("1")){
                mmpBy.put(mp.get("uuid").toString(), JSONUtils.toJSONString(mp));
            }
            mmp.put(mp.get("uuid").toString(), JSONUtils.toJSONString(mp));
        }

        /*存入数据库操作======================================*/
        kdEntity.setJedisId(kdEntity.getJedisId()+"ExcelKD");
        JSONObject json = JSONObject.fromObject(kdEntity);
        String str = json.toString();
        mmp.put("entry",str);
        jedis.hmset(kdEntity.getJedisId(),mmp);
    }

    /**
     * Jedis分页展示数据
     * @param page
     * @return
     */
    @RequestMapping("/kdDataFY")
    public ModelAndView kdDataFY(Page page){
        ModelAndView mv = this.getModelAndView();
        PageData  pp  =  this.getPageData();
        if (null!=pp.getString("page.currentPage")&&!"".equals(pp.getString("page.currentPage"))){
            page.setCurrentPage(Integer.parseInt(pp.getString("page.currentPage")) );
            page.setShowCount(Integer.parseInt(pp.getString("page.showCount")) );
        }
        List<HashMap<String,String>> list =null;
        KDEntity kdEntity = JedisUtil.getKDEntity(pp.get("jedis").toString());
        int size = JedisUtil.getPageCountInt(kdEntity.getJedisId());
        page.setPd(pp);
        try {
            page.setTotalResult(size);
            list = JedisUtil.pagingResource(kdEntity.getJedisId(),page);
            if (list==null||list.size()==0){
                JedisUtil.delResource(kdEntity.getJedisId());
                mv.setViewName("service/error");
                //pp.put("HeaderError","无数据！");
                mv.addObject("msg","无数据！");
                mv.addObject("empPk",kdEntity.getEmpPk());
                mv.addObject("projectId",kdEntity.getProjectId());
                return mv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> excelHeard = kdEntity.getExcelHeard();
        List<PageData> colHeaders = kdEntity.getColHeaders();
        mv.addObject("empPk",kdEntity.getEmpPk());
        mv.addObject("projectId",kdEntity.getProjectId());
        mv.addObject("headString",JSONUtils.toJSONString(colHeaders));
        mv.addObject("pd", this.getPageData());
        mv.addObject("empPk",kdEntity.getEmpPk());
        mv.addObject("pds",JSONUtils.toJSONString(list));
        mv.addObject("jedis",JSONUtils.toJSONString(kdEntity.getJedisId()));
        mv.addObject("rh",JSONUtils.toJSONString(excelHeard));
        mv.setViewName("service/KDExcel");
        return mv;
    }

    /**
     * handsoontable无感提交（仅支持双击）
     * @return
     */
    @RequestMapping("/kdDataAjax")
    @ResponseBody
    public Object kdDataAjax(){
        PageData  pp  =  this.getPageData();
        String jedis = pp.getString("jedis");
        KDEntity kdEntity = JedisUtil.getKDEntity(jedis);
        if (null==pp.getString("uuid")||"".equals(pp.getString("uuid"))||"null".equals(pp.getString("uuid"))){
            JedisUtil.modifyKDEntityResource(kdEntity,kdEntity.getJedisId());
            String str=UUID.randomUUID().toString().replace("-","");
            pp.put("uuid",str);
            JedisUtil.addResource(pp,kdEntity.getJedisId());
            pp.put("success",str);
        }else if("1".equals(pp.getString("opertion"))||"0".equals(pp.getString("opertion"))||"null".equals(pp.getString("opertion"))){
            try{
                LinkedHashMap<String,String> newHeards = JedisUtil.getResourceHeard(kdEntity.getJedisId());
                LinkedHashMap<String,String> pps = cellExcel(pp,kdEntity.getHeard(),newHeards,kdEntity);
                JedisUtil.modifyResource(pps,kdEntity.getJedisId());
                pp.put("edit",JSONUtils.toJSONString(pps));
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try {
                pp.put("opertion",0);
                LinkedHashMap<String,String> newHeards = JedisUtil.getResourceHeard(kdEntity.getJedisId());
                LinkedHashMap<String,String> pps = cellExcel(pp,kdEntity.getHeard(),newHeards,kdEntity);
                JedisUtil.modifyResource(pps,kdEntity.getJedisId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return AppUtil.returnObject(new PageData(), pp);
    }

    /**
     * 行读取
     * @param data
     * @param heard
     */
    public LinkedHashMap<String, String> cellExcel(PageData data, PageData heard, LinkedHashMap<String, String> newHeards,KDEntity kdEntity)throws Exception{
        LinkedHashMap<String,String> mp = new LinkedHashMap<String,String>();
        String uuid=UUID.randomUUID().toString().replace("-","");
        Date today = MecoolUtil.getDateNoTime(new Date());
        Map<String, Date> ssm = null;
        boolean booleanData = false;
        PageData channel = null;
        if (data.get("uuid")==null||data.get("uuid").toString().equals("")){
            mp.put("uuid",uuid);
            mp.put("opertion","0");

        }else {
            mp.put("uuid",data.get("uuid").toString());
            mp.put("opertion",data.get("opertion").toString());
            booleanData = true;
        }
        Iterator he = null;
        if (newHeards!=null){
            he = newHeards.entrySet().iterator();
        }
        for (int i = 0; i < heard.size(); i++) {
            // 获得数据
            String header = null;
            Object cell = null;
            try {
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
                if (data.get("uuid")==null||data.get("uuid").toString().equals("")){
                    cell = data.get("var"+i);
                }
                if (ConstantsMecool.SellinFields.CHANNEL_SYNC_ID.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("CHANNEL_SYNC_ID")!=null||!data.get("CHANNEL_SYNC_ID").equals("null")){
                            cell = data.get("CHANNEL_SYNC_ID").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("CHANNEL_SYNC_ID",cell.toString());
                                continue;
                            }
                        }else{
                            mp.put("CHANNEL_SYNC_ID","Error:系统终端编号不能为空！");
                            continue;
                        }
                    }
                    // 系统终端编号
                    if (cell != null && !cell.toString().equals("")&& !cell.toString().equals("null")) {
                        mp.put("CHANNEL_SYNC_ID",cell.toString());
                        PageData query = new PageData();
                        query.put("CHANNEL_CODE",cell.toString());
                        List<PageData> channelList = kdExcelService.getChannelList(query);
                        if (channelList != null && channelList.size() > 0) {
                            channel = channelList.get(0);
                        }else {
                            mp.put("CHANNEL_SYNC_ID","Error:系统终端编号不存在。"+cell.toString());
                        }
                    }else {
                        mp.put("CHANNEL_SYNC_ID","Error:系统终端编号不能为空。");
                    }
                }else if (ConstantsMecool.SellinFields.SCHEDULE_NUM.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("SCHEDULE_NUM")!=null){
                            cell = data.get("SCHEDULE_NUM").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("SCHEDULE_NUM",cell.toString());
                                continue;
                            }
                        }else{
                            mp.put("SCHEDULE_NUM","Error:卖进序号不能为空！");
                            continue;
                        }
                    }
                    // 卖进序号
                    if (cell != null&& !cell.toString().equals("")) {
                        Long sn = 0l;
                        try {
                            sn = (new BigDecimal(cell.toString())).longValue();
                            mp.put("SCHEDULE_NUM",sn.toString());
                        } catch (Exception e) {
                            mp.put("SCHEDULE_NUM","Error:卖进序号必须为数字！");
                        }
                    }else {
                        mp.put("SCHEDULE_NUM","Error:卖进序号不能为空。");
                    }
                }else if (ConstantsMecool.SellinFields.PLAN_STORE_OPEN_COUNT.getHeaderName().equals(header)) {
                    if (booleanData){
                        if (data.get("PLAN_STORE_OPEN_COUNT")!=null){
                            cell = data.get("PLAN_STORE_OPEN_COUNT").toString();
                            if (cell.toString().indexOf("Error")!=-1){
                                mp.put("PLAN_STORE_OPEN_COUNT",cell.toString());
                                continue;
                            }
                        }else{
                            mp.put("PLAN_STORE_OPEN_COUNT","Error:开档计划不能为空！");
                            continue;
                        }
                    }
                    // 开档计划
                    if ( cell!= null && !cell.toString().equals("")) {
                        ssm =  MecoolUtil.parseDaysStr(cell.toString().trim());

                        if (ssm == null || ssm.size() < 1) {
                            mp.put("PLAN_STORE_OPEN_COUNT","Error:格式有误"+cell.toString());
                            continue;
                        }
                        // 去除过期日期
                        List<String> odList = new ArrayList<String>();
                        for (Iterator<String> iterator = ssm.keySet().iterator(); iterator.hasNext();) {
                            String strDate = iterator.next();
                            if (ssm.get(strDate).before(today)) {
                                odList.add(strDate);
                            }
                        }
                        if (odList.size() > 0) {
                            for (String strDate : odList) {
                                ssm.remove(strDate);
                            }
                        }
                        if (ssm.size() < 1) {
                            mp.put("PLAN_STORE_OPEN_COUNT","Error:开档计划时间要位于今日之后。");
                        }else {
                            mp.put("PLAN_STORE_OPEN_COUNT",cell.toString().trim());
                        }
                    }else {
                        mp.put("PLAN_STORE_OPEN_COUNT","Error:开档计划不能为空。");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PageData query = new PageData();
        List<PageData> psi0 =null;
        if (null!=channel){
            query.put("SCHEDULE_NUM",mp.get("SCHEDULE_NUM"));
            query.put("CHANNEL_SYNC_ID",channel.get("ID"));
            query.put("PROJECT_ID",kdEntity.getProjectId());
            psi0 = kdExcelService.findByMJinfoByProjectChannelSn(query);
        }
        String s = JSONUtils.toJSONString(mp);
        if (s.indexOf("Error")==-1){
            mp.put("opertion","1");
        }else {
            mp.put("opertion","0");
            return mp;
        }
        if (psi0==null||psi0.size()==0) {
            mp.put("opertion","0");
            mp.put("CHANNEL_SYNC_ID","Error:卖进中无此终端编号!"+mp.get("CHANNEL_SYNC_ID").toString());
            return mp;
        }else{
            Collection<Date> c = ssm.values();
            Iterator<Date> it = c.iterator();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            while (it.hasNext()) {
                Date d = it.next();
                List<PageData> psedList = kdExcelService.getSellinExecdateList(psi0.get(0));
                if (psedList != null && psedList.size() > 0) {
                    for (PageData psed : psedList) {
                        if (psed == null || psed.get("EXEC_DATE") == null) {
                            continue;
                        }
                        if (DateUtils.isSameDay(d,sdf.parse(psed.get("EXEC_DATE").toString()))) {
                            break;
                        }
                    }
                }else{
                    mp.put("opertion","0");
                    mp.put("PLAN_STORE_OPEN_COUNT","Error:没有执行日!");
                }
            }
        }
        return mp;
    }

    @RequestMapping("/kdSaveDateAll")
    @ResponseBody
    public int[] kdSaveDateAll(){
        PageData des = this.getPageData();
        KDEntity kdEntity = JedisUtil.getKDEntity(des.getString("jedis"));
        int excelThreadNum = application.getExcelThreadNum();
        int excelThreadOpen = application.getExcelThreadOpen();
        int[] it =new int[1];
        try {
            Long begin = System.currentTimeMillis();
            List<LinkedHashMap<String,String>> pds = JedisUtil.getResourceDXCDate(kdEntity.getJedisId());
            it[0] = 0;
            if (pds.size()==0){
                return it;
            }
            if (pds.size()>excelThreadOpen){
                final CountDownLatch sCountDownLatch = new CountDownLatch(excelThreadNum);
                //ThreadPoolExecutor threadpool=new ThreadPoolExecutor(2, excelThreadNum, 20, TimeUnit.SECONDS, new ArrayBlockingQueue(10));
                ExecutorService threadpool = Executors.newFixedThreadPool(excelThreadNum);
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
                        task.add(Collections.synchronizedMap(pds.get(i)));
                    }
                    final KDThread kdThread = new KDThread(task,kdEntity.getJedisId(),kdExcelService,sCountDownLatch);
                    threadpool.execute(kdThread);
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
                        kdExcelService.saveKd(pd,kdEntity);
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

    @RequestMapping("/kdSaveCount")
    @ResponseBody
    public int kdSaveCount(){
        PageData des = this.getPageData();
        String jedis = des.getString("jedis");
        int l = JedisUtil.getLength(jedis);
        return l;
    }

    @RequestMapping("/kdExportDateAll")
    public ModelAndView kdExportDateAll(){
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        KDEntity kdEntity = JedisUtil.getKDEntity(pd.getString("jedis"));
        List<LinkedHashMap<String,String>> list = JedisUtil.getResourceDXC(kdEntity.getJedisId());
        try {
            Map<String,Object> dataMap = new HashMap<String,Object>();
            List<String> titles = new ArrayList<String>();
            List<String> heards = new ArrayList<String>();
            LinkedHashMap<String,String> rh = kdEntity.getPdHeard();
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
            mv.addObject("empPk",kdEntity.getEmpPk());
            mv.addObject("projectId",kdEntity.getProjectId());
        }
        return mv;
    }
    @RequestMapping("/kdExportDateAllError")
    public ModelAndView kdExportDateAllError(){
        PageData pd = this.getPageData();
        ModelAndView mv = this.getModelAndView();
        KDEntity kdEntity = JedisUtil.getKDEntity(pd.getString("jedis"));
        List<LinkedHashMap<String,String>> list = JedisUtil.getResourceDXC(kdEntity.getJedisId());
        try {
            Map<String,Object> dataMap = new HashMap<String,Object>();
            List<String> titles = new ArrayList<String>();
            List<String> heards = new ArrayList<String>();
            LinkedHashMap<String,String> rh = kdEntity.getPdHeard();
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
            mv.addObject("empPk",kdEntity.getEmpPk());
            mv.addObject("projectId",kdEntity.getProjectId());
        }
        return mv;
    }

    @RequestMapping("importExcelKD")
    public ModelAndView importExcel(){
        PageData pageData =new PageData();
        pageData=this.getPageData();
        ModelAndView mv = new ModelAndView();
        mv.setViewName("service/importExcelKD");
        mv.addObject("jedis",JSONUtils.toJSONString(pageData.get("jedis").toString()));
        return mv;
    }
}
