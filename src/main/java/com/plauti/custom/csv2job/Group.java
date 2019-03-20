package com.plauti.custom.csv2job;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Group {

    public static AtomicInteger groupCounter = new AtomicInteger(10);

    public Group(String master) {
        this.matchedRecords = new HashSet<String>();
        this.masterId = master;
        this.groupNumber = groupCounter.incrementAndGet();
    }

    private String masterId;
    private String groupId;
    private Integer groupNumber;
    private Set<String> matchedRecords;

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public static AtomicInteger getGroupCounter() {
        return groupCounter;
    }

    public static void setGroupCounter(AtomicInteger groupCounter) {
        Group.groupCounter = groupCounter;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(Integer groupNumber) {
        this.groupNumber = groupNumber;
    }

    public Set<String> getMatchedRecords() {
        return matchedRecords;
    }

    public void setMatchedRecords(Set<String> matchedRecords) {
        this.matchedRecords = matchedRecords;
    }

    @Override
    public String toString() {
        return "Group{" +
                "masterId='" + masterId + '\'' +
                ", groupNumber=" + groupNumber +
                ", matchedRecords=" + matchedRecords +
                '}';
    }
}
