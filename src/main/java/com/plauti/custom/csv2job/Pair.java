package com.plauti.custom.csv2job;

import com.opencsv.bean.CsvBindByPosition;

public class Pair {

    @CsvBindByPosition(required = true, position = 0)
    private String sourceId;

    @CsvBindByPosition(required = true, position = 1)
    private String matchId;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
}
