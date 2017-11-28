package com.hubertkaluzny.jcrawler.pagefilter;

import com.hubertkaluzny.jcrawler.pagefilter.webpage.WebFile;

public class Filter {

  private FilterType filterType;
  private String val;

  public boolean satisfied(WebFile webFile) {
    switch (filterType) {
      case MUST_CONTAIN:
        return webFile.getContent().contains(val.toLowerCase());
      case NOT_CONTAIN:
        return !webFile.getContent().contains(val.toLowerCase());
      case REFERENCES:
        for (String url : webFile.getUrls()) {
          if (url.toLowerCase().contains(val)) {
            return true;
          }
        }
        break;
      case NOT_REFERENCES:
        for (String url : webFile.getUrls()) {
          if (url.toLowerCase().contains(val)) {
            return false;
          }
        }
        return true;
      case IS_HOST:
        return webFile.getUrl().contains(val);
      case NOT_HOST:
        return !webFile.getUrl().contains(val);
    }
    return false;
  }

  public void setFilterType(FilterType filterType) {
    this.filterType = filterType;
  }

  public void setVal(String val) {
    if(filterType.equals(FilterType.NOT_CONTAIN) || filterType.equals(FilterType.MUST_CONTAIN)){
      this.val = " " + val + " ";
    }else{
      this.val = val;
    }
  }

  @Override
  public String toString() {
    return filterType.name() + ":" + val;
  }
}
