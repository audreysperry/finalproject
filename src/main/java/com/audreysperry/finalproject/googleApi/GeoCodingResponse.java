package com.audreysperry.finalproject.googleApi;

import java.util.List;

public class GeoCodingResponse {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
