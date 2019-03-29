package com.mecool.entity;

import com.mecool.util.PageData;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/4/12.
 */
public class KDEntity implements Serializable{

    //项目ID
    private String projectId;
    //头数据
    private LinkedHashMap<String,String> pdHeard;
    //
    private PageData heard;
    //全局JedisID
    private String jedisId;
    //emppk
    private String empPk;

    private List<String> excelHeard;

    private List<PageData> colHeaders;



    @Override
    public String toString() {
        return "KDEntity{" +
                "projectId='" + projectId + '\'' +
                ", pdHeard=" + pdHeard +
                ", heard=" + heard +
                ", jedisId='" + jedisId + '\'' +
                ", empPk='" + empPk + '\'' +
                ", excelHeard=" + excelHeard +
                ", colHeaders=" + colHeaders +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KDEntity mjEntity = (KDEntity) o;

        if (projectId != null ? !projectId.equals(mjEntity.projectId) : mjEntity.projectId != null) return false;
        if (pdHeard != null ? !pdHeard.equals(mjEntity.pdHeard) : mjEntity.pdHeard != null) return false;
        if (heard != null ? !heard.equals(mjEntity.heard) : mjEntity.heard != null) return false;
        if (jedisId != null ? !jedisId.equals(mjEntity.jedisId) : mjEntity.jedisId != null) return false;
        if (empPk != null ? !empPk.equals(mjEntity.empPk) : mjEntity.empPk != null) return false;
        if (excelHeard != null ? !excelHeard.equals(mjEntity.excelHeard) : mjEntity.excelHeard != null) return false;
        return colHeaders != null ? colHeaders.equals(mjEntity.colHeaders) : mjEntity.colHeaders == null;
    }

    @Override
    public int hashCode() {
        int result = projectId != null ? projectId.hashCode() : 0;
        result = 31 * result + (pdHeard != null ? pdHeard.hashCode() : 0);
        result = 31 * result + (heard != null ? heard.hashCode() : 0);
        result = 31 * result + (jedisId != null ? jedisId.hashCode() : 0);
        result = 31 * result + (empPk != null ? empPk.hashCode() : 0);
        result = 31 * result + (excelHeard != null ? excelHeard.hashCode() : 0);
        result = 31 * result + (colHeaders != null ? colHeaders.hashCode() : 0);
        return result;
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

    public PageData getHeard() {
        return heard;
    }

    public void setHeard(PageData heard) {
        this.heard = heard;
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
}
