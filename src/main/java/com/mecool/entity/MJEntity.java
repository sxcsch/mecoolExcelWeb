package com.mecool.entity;

import com.mecool.util.PageData;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2018/4/12.
 */
public class MJEntity implements Serializable{

    //项目ID
    private String projectId;
    //头数据
    private LinkedHashMap<String,String> pdHeard;
    //保存数据
    private PageData hfHeard;
    //
    private PageData heards;

    private PageData oldHeards;
    //全局JedisID
    private String jedisId;
    //emppk
    private String empPk;

    private List<String> excelHeard;

    private List<PageData> colHeaders;

    private PageData project;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MJEntity mjEntity = (MJEntity) o;
        return Objects.equals(projectId, mjEntity.projectId) &&
                Objects.equals(pdHeard, mjEntity.pdHeard) &&
                Objects.equals(hfHeard, mjEntity.hfHeard) &&
                Objects.equals(heards, mjEntity.heards) &&
                Objects.equals(oldHeards, mjEntity.oldHeards) &&
                Objects.equals(jedisId, mjEntity.jedisId) &&
                Objects.equals(empPk, mjEntity.empPk) &&
                Objects.equals(excelHeard, mjEntity.excelHeard) &&
                Objects.equals(colHeaders, mjEntity.colHeaders) &&
                Objects.equals(project, mjEntity.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, pdHeard, hfHeard, heards, oldHeards, jedisId, empPk, excelHeard, colHeaders, project);
    }

    @Override
    public String toString() {
        return "MJEntity{" +
                "projectId='" + projectId + '\'' +
                ", pdHeard=" + pdHeard +
                ", hfHeard=" + hfHeard +
                ", heards=" + heards +
                ", oldHeards=" + oldHeards +
                ", jedisId='" + jedisId + '\'' +
                ", empPk='" + empPk + '\'' +
                ", excelHeard=" + excelHeard +
                ", colHeaders=" + colHeaders +
                ", project=" + project +
                '}';
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public LinkedHashMap<String, String> getPdHeard() {
        return pdHeard;
    }

    public void setPdHeard(LinkedHashMap<String, String> pdHeard) {
        this.pdHeard = pdHeard;
    }

    public PageData getHfHeard() {
        return hfHeard;
    }

    public void setHfHeard(PageData hfHeard) {
        this.hfHeard = hfHeard;
    }

    public PageData getHeards() {
        return heards;
    }

    public void setHeards(PageData heards) {
        this.heards = heards;
    }

    public PageData getOldHeards() {
        return oldHeards;
    }

    public void setOldHeards(PageData oldHeards) {
        this.oldHeards = oldHeards;
    }

    public String getJedisId() {
        return jedisId;
    }

    public void setJedisId(String jedisId) {
        this.jedisId = jedisId;
    }

    public String getEmpPk() {
        return empPk;
    }

    public void setEmpPk(String empPk) {
        this.empPk = empPk;
    }

    public List<String> getExcelHeard() {
        return excelHeard;
    }

    public void setExcelHeard(List<String> excelHeard) {
        this.excelHeard = excelHeard;
    }

    public List<PageData> getColHeaders() {
        return colHeaders;
    }

    public void setColHeaders(List<PageData> colHeaders) {
        this.colHeaders = colHeaders;
    }

    public PageData getProject() {
        return project;
    }

    public void setProject(PageData project) {
        this.project = project;
    }
}
