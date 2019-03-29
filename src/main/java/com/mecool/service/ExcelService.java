package com.mecool.service;

import com.mecool.dao.DaoSupport;
import com.mecool.entity.Page;
import com.mecool.util.PageData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator on 2/7/2018.
 */
@Service("excelService")
@Transactional
public class ExcelService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /*
	* 新增
	*/
    public void save(PageData pd)throws Exception {
        dao.save("ExcelMapper.save", pd);
    }

    /*
    * 删除
    */
    public void delete(PageData pd)throws Exception {
        dao.delete("ExcelMapper.delete", pd);
    }

    /*
    * 修改
    */
    public void edit(PageData pd)throws Exception {
        dao.update("ExcelMapper.edit", pd);
    }

    /*
    *列表
    */
    public List<PageData> list(Page page)throws Exception {
        return (List<PageData>)dao.findForList("ExcelMapper.datalistPage", page);
    }

    /*
    *列表(全部)
    */
    public List<PageData> listAll(PageData pd)throws Exception {
        return (List<PageData>)dao.findForList("ExcelMapper.listAll", pd);
    }

    /*
    * 通过id获取数据
    */
    public PageData findById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("ExcelMapper.findById", pd);
    }
}
