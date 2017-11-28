package com.hubertkaluzny.jcrawler;

import com.hubertkaluzny.jcrawler.pagefilter.webpage.WebFile;
import com.hubertkaluzny.jcrawler.pagefilter.webpage.WebFileState;
import java.util.ArrayList;
import java.util.List;

public class CrawlerController implements Runnable {

  private List<WebFile> webFiles = new ArrayList<>();
  private List<Worker> workers = new ArrayList<>();
  private List<String> completedUrls = new ArrayList<>();
  private List<Thread> threads = new ArrayList<>();

  private boolean running = false;

  CrawlerController(List<String> seeds, int workerNum) {
    for (String s : seeds) {
      addWebFile(s);
    }
    for (int i = 0; i < workerNum; i++) {
      Worker w = new Worker();
      Thread t = new Thread(w);
      workers.add(w);
      threads.add(t);
      t.start();
    }
    running = true;
  }

  @Override
  public void run() {
    while (running) {
      for (int i = 0; i < webFiles.size(); i++) {
        WebFile wf = webFiles.remove(0);
        int workerID = getLeastfull(workers);
        Worker worker = workers.get(workerID);
        worker.webFiles.offer(wf);
        workers.set(workerID, worker);
      }

      for (Worker w : workers) {
        List<WebFile> completed = new ArrayList<>(w.completedWebFiles);
        w.completedWebFiles.clear();
        for (WebFile wr : completed) {
          if (wr.getFileState().equals(WebFileState.REMOVED) || wr.getFileState()
              .equals(WebFileState.PARSED)) {
            completedUrls.add(wr.getUrl());
          } else {
            webFiles.add(wr);
          }
        }

        List<String> newURLs = new ArrayList<>(w.newUrls);
        w.newUrls.clear();
        for(String url : newURLs){
          addWebFile(url);
        }
      }
    }
  }

  void stop() {
    for (Worker w : workers) {
      w.stop();
    }
    try {
      for (Thread t : threads) {
        t.join();
      }
    }catch (InterruptedException e){
      e.printStackTrace();
    }
    running = false;
  }

  void reduceWorkers(int n) {
    try {
      for (int i = 0; i < workers.size() && i < n; i++) {
        Worker w = workers.get(i);
        w.stop();
        Thread t = threads.get(i);
        t.join();
        workers.remove(i);
      }
    }catch (InterruptedException e){
      e.printStackTrace();
    }
  }

  String getStatus() {
    String status = "";
    if (running) {
      status += "[running] ";
      status += getWorforceSize() + " Workers, ";
      status += getFileQueueSize() + " In Queue, ";
      status += completedUrls.size() + " URLS completed.";
    } else {
      status = "[halted]";
    }
    return status;
  }

  int getFileQueueSize() {
    int webFileSum = 0;
    for (Worker w : workers) {
      webFileSum += w.webFiles.size();
    }
    return webFileSum;
  }

  int getWorforceSize() {
    return workers.size();
  }

  private void addWebFile(String url) {
    if (!completedUrls.contains(url) && !url.contains("@")) {
      webFiles.add(new WebFile(url));
    }
  }

  private int getLeastfull(List<Worker> workers) {
    int least = 0;
    for (int i = 0; i < workers.size(); i++) {
      if (workers.get(least).webFiles.size() > workers.get(i).webFiles.size()) {
        least = i;
      }
    }
    return least;
  }
}
