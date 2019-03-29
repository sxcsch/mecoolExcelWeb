package com.mecool.service;

import com.mecool.dao.DaoSupport;
import com.mecool.util.PageData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2/7/2018.
 */
@Service("pxExcelService")
@Transactional
public class PXExcelService {
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    /*
  * 通过项目ID来寻找项目信息
  */
    public PageData findProjectById(PageData pd)throws Exception {
        return (PageData)dao.findForObject("MJExcelMapper.findProjectById", pd);
    }
    /*
 * 通过
 */
    public PageData findByProjectSalessCardIdTrainingDate(PageData pd)throws Exception {
        return (PageData)dao.findForObject("PXExcelMapper.findByProjectSalessCardIdTrainingDate", pd);
    }
    /*
 * 通过
 */
    public void removeProjectSalessCard(PageData pd)throws Exception {
        dao.delete("PXExcelMapper.removeProjectSalessCard", pd);
    }
    /*
 * 通过
 */
    public void saveProjectSalessCard(PageData pd)throws Exception {
        dao.save("PXExcelMapper.saveProjectSalessCard", pd);
    }
}
