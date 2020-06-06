package com.fasterxml.jackson.databind.ser;

public class SamplePojo {
    private String settlementDate;
    private String getaways;
    private Boolean island;

    public void settlementDate(String v) { throw new Error("Should not get called"); }
    public String settlementDate() { throw new Error("Should not get called"); }
    public void setSettlementDate(String v) { settlementDate = v; }
    public String getSettlementDate() { return settlementDate; }

    public void getaways(String v) { throw new Error("Should not get called"); }
    public String getaways() { throw new Error("Should not get called"); }
    public void setGetaways(String v) { getaways = v ;}
    public String getGetaways() { return getaways; }

    public void island(Boolean v) { throw new Error("Should not get called"); }
    public Boolean island() { throw new Error("Should not get called"); }
    public void setIsland(Boolean v) { island = v; }
    public Boolean isIsland() { return island; }
}

