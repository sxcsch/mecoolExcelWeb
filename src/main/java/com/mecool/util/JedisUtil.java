package com.mecool.util;

import com.alibaba.druid.support.json.JSONUtils;
import com.mecool.entity.KDEntity;
import com.mecool.entity.MJEntity;
import com.mecool.entity.PBEntity;
import com.mecool.entity.Page;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class JedisUtil {

    private static JedisPool jedisPool;

    private static final String PROPERTIES_PATH = "redis.properties";

    private JedisUtil() {
    }
    static{

        try {
            //加载配置文件
            InputStream in = JedisUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
            Properties p = new Properties();
            p.load(in);
            String host = p.getProperty("redis.host") == null ? "localhost" : p.getProperty("redis.host");
            int port = p.getProperty("redis.port") == null ? 6379 : Integer.parseInt(p.getProperty("redis.port"));
            int maxIdle = p.getProperty("redis.maxIdle") == null ?20:Integer.parseInt(p.getProperty("redis.maxIdle"));
            int maxTotal = p.getProperty("redis.maxTotal") == null ?50:Integer.parseInt(p.getProperty("redis.maxTotal"));
            String auth = p.getProperty("redis.auth");
            //初始化操作
            //1、设置连接池的配置对象
            JedisPoolConfig config = new JedisPoolConfig();
            //设置池中最大连接数【可选】
            config.setMaxTotal(maxTotal);
            //设置空闲时池中保有的最大连接数【可选】
            config.setMaxIdle(maxIdle);
            //2、设置连接池对象
            if (null!=auth&&!auth.equals("")){
                jedisPool= new JedisPool(config,host, port,10000,auth);
            }else {
                jedisPool= new JedisPool(config,host, port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 简单的
     * @return
     */
    public synchronized  static Jedis getJedis(){
        Jedis jds = null;
        try {
            jds = jedisPool.getResource();
            return jds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 简单的Get
     * @param <T>
     * @param key
     * @param requiredType
     * @return
     */
    public static <T> T get(String key , Class<T>...requiredType){
        Jedis jds = null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.select(0);
            byte[] skey = SerializeUtil.serialize(key);
            return SerializeUtil.deserialize(jds.get(skey),requiredType);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        } finally {
            returnResource(jds, isBroken);
        }
        return null;
    }
    /**
     * 简单的set
     * @param key
     * @param value
     */
    public static void set(Object key ,Object value){
        Jedis jds = null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.select(0);
            byte[] skey = SerializeUtil.serialize(key);
            byte[] svalue = SerializeUtil.serialize(value);
            jds.set(skey, svalue);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        } finally {
            returnResource(jds, isBroken);
        }
    }
    /**
     * 过期时间的
     * @param key
     * @param value
     * @param timer （秒）
     */
    public static void setex(Object key, Object value, int timer) {
        Jedis jds = null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.select(0);
            byte[] skey = SerializeUtil.serialize(key);
            byte[] svalue = SerializeUtil.serialize(value);
            jds.setex(skey, timer, svalue);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        } finally {
            returnResource(jds, isBroken);
        }

    }
    /**
     * 判断是否存在
     * @param existskey
     * @return
     */
    public static boolean exists(String existskey){
        Jedis jds = null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.select(0);
            byte[] lkey = SerializeUtil.serialize(existskey);
            return jds.exists(lkey);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        } finally {
            returnResource(jds, isBroken);
        }
        return false;
    }
    /**
     * 释放
     * @param jedis
     * @param isBroken
     */
    public static void returnResource(Jedis jedis, boolean isBroken) {
        if (jedis == null)
            return;
        jedis.close();
    }

    public static List<HashMap<String,String>> pagingResource(String jedisId, Page page) {
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            Set<String> map = jds.hkeys(jedisId);
            Map<String,String> mm = jds.hgetAll(jedisId);
            List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();

            List<HashMap<String,String>> sortBefore = new ArrayList<>();
            List<HashMap<String,String>> sortLast = new ArrayList<>();
            List<HashMap<String,String>> sort = new ArrayList<>();
            map.remove("entry");
            map.remove("heard");
            for (String key:map) {
                String str = mm.get(key);
                LinkedHashMap<String,String> pds = (LinkedHashMap<String,String>) JSONUtils.parse(str);
                if(pds.get("opertion").toString().equals("0")){
                    sortBefore.add(pds);
                }else {
                    sortLast.add(pds);
                }
            }
            sort.addAll(sortBefore);
            sort.addAll(sortLast);
            Integer sc = page.getShowCount();
            Integer cp = page.getCurrentPage();
            Integer numLast = sc*cp;
            Integer numBefore = (cp-1)*sc;
            if(cp!=page.getTotalPage()){
                for (int i=numBefore;i<numLast;i++){
                    list.add(sort.get(i));
                }
            }else{
                for (int i=numBefore;i<page.getTotalResult();i++){
                    list.add(sort.get(i));
                }
            }
            return list;
        }catch (ArrayIndexOutOfBoundsException e){

        } catch(Exception e) {
            isBroken = true;
            e.printStackTrace();
        } finally {
            returnResource(jds, isBroken);
        }
        return null;
    }

    public static LinkedHashMap<String,String> getResourceHeard(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        LinkedHashMap<String,String> pd = null;
        try {
            jds = jedisPool.getResource();
            Map<String,String> mm = jds.hgetAll(jedisId);
            String str = mm.get("heard");
            pd= (LinkedHashMap<String,String>) JSONUtils.parse(str);
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return pd;
    }

    public static MJEntity getMJEntity(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        MJEntity pd = new MJEntity();
        try {
            jds = jedisPool.getResource();
            Map<String,String> mm = jds.hgetAll(jedisId);
            if (mm==null||mm.size()==0){
                return null;
            }
            String str = mm.get("entry");
            JSONObject jsonObject = JSONObject.fromObject(str);
            pd = (MJEntity) JSONObject.toBean(jsonObject, MJEntity.class);

            PageData heards = pd.getHeards();
            Iterator it = heards.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                MorphDynaBean morphDynaBean = (MorphDynaBean) entry.getValue();
                JSONObject jobj = JSONObject.fromObject(morphDynaBean);
                PageData s = (PageData) JSONObject.toBean(jobj,PageData.class);
                heards.put(entry.getKey(),s);
            }

            if (pd.getColHeaders()==null||pd.getColHeaders().size()==0){
                return pd;
            }
            List<PageData> colHeaders = new ArrayList<PageData>();
            Iterator its = pd.getColHeaders().iterator();
            while (its.hasNext()){
                MorphDynaBean morphDynaBean = (MorphDynaBean) its.next();
                JSONObject jobj = JSONObject.fromObject(morphDynaBean);
                PageData s = (PageData) JSONObject.toBean(jobj,PageData.class);
                colHeaders.add(s);
            }
            pd.setColHeaders(colHeaders);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return pd;
    }

    public static KDEntity getKDEntity(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        KDEntity pd = new KDEntity();
        try {
            jds = jedisPool.getResource();
            Map<String,String> mm = jds.hgetAll(jedisId);
            if (mm==null||mm.size()==0){
                return null;
            }
            String str = mm.get("entry");
            JSONObject jsonObject = JSONObject.fromObject(str);
            pd = (KDEntity) JSONObject.toBean(jsonObject, KDEntity.class);
            List<PageData> colHeaders = new ArrayList<PageData>();
            Iterator its = pd.getColHeaders().iterator();
            while (its.hasNext()){
                MorphDynaBean morphDynaBean = (MorphDynaBean) its.next();
                JSONObject jobj = JSONObject.fromObject(morphDynaBean);
                PageData s = (PageData) JSONObject.toBean(jobj,PageData.class);
                colHeaders.add(s);
            }
            pd.setColHeaders(colHeaders);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return pd;
    }

    public static PBEntity getPBEntity(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        PBEntity pd = new PBEntity();
        try {
            jds = jedisPool.getResource();
            Map<String,String> mm = jds.hgetAll(jedisId);
            if (mm==null||mm.size()==0){
                return null;
            }
            String str = mm.get("entry");
            JSONObject jsonObject = JSONObject.fromObject(str);
            pd = (PBEntity) JSONObject.toBean(jsonObject, PBEntity.class);

            List<PageData> colHeaders = new ArrayList<PageData>();
            Iterator its = pd.getColHeaders().iterator();
            while (its.hasNext()){
                MorphDynaBean morphDynaBean = (MorphDynaBean) its.next();
                JSONObject jobj = JSONObject.fromObject(morphDynaBean);
                PageData s = (PageData) JSONObject.toBean(jobj,PageData.class);
                colHeaders.add(s);
            }
            pd.setColHeaders(colHeaders);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return pd;
    }

    public static void addResource(PageData hashMap,String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.hset(jedisId,hashMap.get("uuid").toString(),JSONUtils.toJSONString(hashMap));
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void modifyResource(LinkedHashMap<String, String> hashMap,String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.hset(jedisId,hashMap.get("uuid").toString(),JSONUtils.toJSONString(hashMap));
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void modifyResourceBy(LinkedHashMap<String, String> hashMap,String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.hset(jedisId,hashMap.get("uuid").toString(),JSONUtils.toJSONString(hashMap));
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void modifyMJEntityResource(MJEntity hashMap, String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            JSONObject json = JSONObject.fromObject(hashMap);
            String str = json.toString();
            jds.hset(jedisId,"entry",str);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void modifyKDEntityResource(KDEntity hashMap, String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            JSONObject json = JSONObject.fromObject(hashMap);
            String str = json.toString();
            jds.hset(jedisId,"entry",str);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void modifyPBEntityResource(PBEntity hashMap, String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            JSONObject json = JSONObject.fromObject(hashMap);
            String str = json.toString();
            jds.hset(jedisId,"entry",str);
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void delResource(LinkedHashMap<String, String> hashMap,String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.hdel(jedisId, hashMap.get("uuid").toString());
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void delResource(Map hashMap,String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.hdel(jedisId, hashMap.get("uuid").toString());
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void delResource(PageData hashMap,String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.hdel(jedisId, hashMap.get("uuid").toString());
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void delResource(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.del(jedisId);
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static void delRow(String jedis, String uuid) {
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            jds.hdel(jedis,uuid);
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
    }

    public static int getLength(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            int li = 0;
            Set<String> map = jds.hkeys(jedisId);
            Map<String,String> mm = jds.hgetAll(jedisId);
            map.remove("entry");
            map.remove("heard");
            for (String key:map) {
                String str = mm.get(key);
                LinkedHashMap<String,String> pds = (LinkedHashMap<String,String>) JSONUtils.parse(str);
                if (pds.get("opertion").toString().equals("1")){
                    li++;
                }
            }
            return li;
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return -1;
    }

    public static int getPageCountInt(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            int li = 0;
            Set<String> map = jds.hkeys(jedisId);
            if (map!=null){
                int size = map.size();
                li=size-2;
            }else{
                li=0;
            }
            return li;
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return -1;
    }

    public static List<LinkedHashMap<String,String>> getResourceDXC(String jedisId){
        Jedis jds= null;
        List list = null;
        boolean isBroken = false;
        try {
            jds = jedisPool.getResource();
            Set<String> map = jds.hkeys(jedisId);
            list = new ArrayList<LinkedHashMap<String,String>>();
            Map<String,String> mm = jds.hgetAll(jedisId);
            map.remove("entry");
            map.remove("heard");
            for (String key:map) {
                String str = mm.get(key);
                LinkedHashMap<String,String> pds = (LinkedHashMap<String,String>) JSONUtils.parse(str);
                list.add(pds);
            }
        } catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return list;
    }

    public static List<LinkedHashMap<String,String>> getResourceDXCDate(String jedisId){
        Jedis jds= null;
        boolean isBroken = false;
        List<LinkedHashMap<String,String>> list = null;
        try {
            jds = jedisPool.getResource();
            Set<String> map = jds.hkeys(jedisId);
            list = new Vector<LinkedHashMap<String,String>>();
            Map<String,String> mm = jds.hgetAll(jedisId);
            map.remove("entry");
            map.remove("heard");
            for (String key:map) {
                String str = mm.get(key);
                LinkedHashMap<String,String> pds = (LinkedHashMap<String,String>) JSONUtils.parse(str);
                if (pds.get("opertion").toString().equals("1")){
                    list.add(pds);
                }
            }
        }catch (Exception e) {
            isBroken = true;
            e.printStackTrace();
        }finally {
            returnResource(jds, isBroken);
        }
        return list;
    }


}