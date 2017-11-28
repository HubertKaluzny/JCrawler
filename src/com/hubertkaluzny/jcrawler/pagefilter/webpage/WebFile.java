package com.hubertkaluzny.jcrawler.pagefilter.webpage;

import com.hubertkaluzny.jcrawler.pagefilter.Filter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WebFile {

  private String url = "";
  private String content = "";
  private File file = null;
  private List<String> urls = new ArrayList<>();
  private WebFileState fileState = WebFileState.INSTANTIATED;

  public WebFile(String url) {
    this.url = url;
  }

  public boolean applyFilterSet(List<Filter> filters) {
    for (Filter f : filters) {
      if (!f.satisfied(this)) {
        return false;
      }
    }
    return true;
  }

  public void remove() {
    fileState = WebFileState.REMOVED;
    if (file != null) {
      if (file.exists()) {
        file.delete();
      }
    }
  }

  public File getFile() {
    return file;
  }

  public List<String> getUrls() {
    return urls;
  }

  public String getContent() {
    return content;
  }

  public String getUrl() {
    return url;
  }

  public WebFileState getFileState() {
    return fileState;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileState(WebFileState fileState) {
    this.fileState = fileState;
  }

  public void addUrl(String url) {
    urls.add(url);
  }
}
