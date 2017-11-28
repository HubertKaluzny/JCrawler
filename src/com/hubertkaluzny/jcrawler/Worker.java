package com.hubertkaluzny.jcrawler;

import com.hubertkaluzny.jcrawler.pagefilter.webpage.WebFile;
import com.hubertkaluzny.jcrawler.pagefilter.webpage.WebFileState;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Worker implements Runnable {

  private boolean running = true;

  ConcurrentLinkedQueue<WebFile> webFiles = new ConcurrentLinkedQueue<>();
  ConcurrentLinkedQueue<WebFile> completedWebFiles = new ConcurrentLinkedQueue<>();
  ConcurrentLinkedQueue<String> newUrls = new ConcurrentLinkedQueue<>();

  void stop() {
    running = false;
  }

  @Override
  public void run() {
    while (running) {
      if (webFiles.size() > 0) {
        WebFile wf = webFiles.poll();

        if (wf.getFileState().equals(WebFileState.INSTANTIATED)) {
          try {
            Connection con = Jsoup.connect(wf.getUrl());
            Document doc = con.get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
              String url = link.attr("abs:href");
              wf.addUrl(url);
              newUrls.offer(url);
            }
            wf.setContent(doc.text().toLowerCase());

            if (!wf.applyFilterSet(Main.FILTERSET)) {
              wf.setFileState(WebFileState.REJECTED);
            } else {
              String filepath = con.response().url().getHost() + "/" + wf.getUrl()
                  .replaceAll("[\\?\\/\\:]", ".")
                  + ".html";
              wf.setFile(new File(filepath));

              if (!wf.getFile().exists()) {
                if (!new File(wf.getFile().getParent()).exists()) {
                  Files.createDirectories(Paths.get(wf.getFile().getParent()));
                }

                wf.getFile().createNewFile();
              }

              PrintWriter pw = new PrintWriter(wf.getFile());
              pw.print(doc.outerHtml());
              pw.close();
            }
          } catch (Exception e) {
            wf.setFileState(WebFileState.REJECTED);
          }
        }

        if (wf.getFileState().equals(WebFileState.REJECTED)) {
          wf.remove();
        } else {
          wf.setFileState(WebFileState.PARSED);
        }
        completedWebFiles.offer(wf);
      }
    }
  }
}