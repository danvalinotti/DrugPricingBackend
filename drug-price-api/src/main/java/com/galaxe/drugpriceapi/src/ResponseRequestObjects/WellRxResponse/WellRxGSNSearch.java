package com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxResponse;

public class WellRxGSNSearch {
    public String gsn;
    public String lat;
    public String lng;
    public String numdrugs;
    public String quantity;
    public String bgIndicator;
    public String bReference;
    public String ncpdps;

    public WellRxGSNSearch(){

    }
    public String getGSN(){
        return this.gsn;
    }
    public String getLat(){
        return this.lat;
    }
    public String getLng(){
        return this.lng;
    }
    public String getNumdrugs(){
        return this.numdrugs;
    }
    public String getQuantity(){
        return this.quantity;
    }
    public String getBgIndicator(){
        return this.bgIndicator;
    }
    public String getbReference(){
        return this.bReference;
    }
    public String getNcpdps(){
        return this.ncpdps;
    }

    public void setGSN(String newVal){
         this.gsn = newVal;
    }
    public void setLat(String newVal){
        this.lat = newVal;
    }
    public void setLng(String newVal){
        this.lng = newVal;
    }
    public void setNumdrugs(String newVal){
        this.numdrugs = newVal;
    }
    public void setQuantity(String newVal){
        this.quantity = newVal;
    }
    public void setBgIndicator(String newVal){
        this.bgIndicator = newVal;
    }
    public void setbReference(String newVal){
        this.bReference = newVal;
    }
    public void setNcpdps(String newVal){
        this.ncpdps = newVal;
    }
}