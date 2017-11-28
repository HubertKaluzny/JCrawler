package com.hubertkaluzny.jcrawler;

import com.hubertkaluzny.jcrawler.pagefilter.Filter;
import com.hubertkaluzny.jcrawler.pagefilter.FilterType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

  static List<Filter> FILTERSET;
  private static List<String> SEEDS;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    System.out.println("JCrawler - Configuration at config.txt");
    System.out.println("How many workers do you want to run?");
    int workers = sc.nextInt();
    if (!parseConfig()) {
      System.out.println("config.txt is misconfigured!");
      System.out.println("Allowed expressions: ");
      System.out.println("SEED: <single seeding url>");
      System.out.println("FILTER:\\n");
      System.out.println(
          "TYPE: <MUST_CONTAIN | NOT_CONTAIN | REFERENCES | NOT_REFERENCES | IS_HOST | NOT_HOST>");
      System.out.println("VAL: <filter value>");
    }
    boolean running = true;
    CrawlerController cc = new CrawlerController(SEEDS, workers);
    Thread ccThread = new Thread(cc);
    ccThread.start();
    while (running) {
      String cmd = sc.nextLine();
      if (cmd.equalsIgnoreCase("stop")) {
        System.out.println("Stopping...");
        cc.stop();
        running = false;
      } else if (cmd.equalsIgnoreCase("reduce")) {
        System.out.println("How many workers do you want to stop?");
        int num = sc.nextInt();
        cc.reduceWorkers(num);
        System.out.println("New worker size: " + cc.getWorforceSize());
      } else if (cmd.equalsIgnoreCase("status")) {
        System.out.println(cc.getStatus());
      } else {
        System.out.println("Command list: ");
        System.out.println("Stop - stops entire crawler");
        System.out.println("Reduce - reduces the number of workers");
        System.out.println("Status - prints current status");
      }
    }
    System.out.println("Exiting.");
  }

  private static boolean parseConfig() {
    try {
      File configFile = new File("config.txt");
      BufferedReader br = new BufferedReader(new FileReader(configFile));
      String line;
      List<Filter> filters = new ArrayList<>();
      List<String> seeds = new ArrayList<>();
      boolean inFilter = false;
      Filter f = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("SEED: ")) {
          seeds.add(line.replace("SEED: ", ""));
        } else if (line.startsWith("FILTER:")) {
          if (inFilter) {
            return false;
          } else {
            f = new Filter();
            inFilter = true;
          }
        } else if (line.startsWith("TYPE: ")) {
          if (inFilter) {
            f.setFilterType(FilterType.valueOf(line.replace("TYPE: ", "").toUpperCase()));
          } else {
            return false;
          }
        } else if (line.startsWith("VAL: ")) {
          if (inFilter) {
            f.setVal(line.replace("VAL: ", "").toLowerCase());
            filters.add(f);
            System.out.println("Added filter: " + f.toString());
            inFilter = false;
          } else {
            return false;
          }
        }
      }
      SEEDS = seeds;
      FILTERSET = filters;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
