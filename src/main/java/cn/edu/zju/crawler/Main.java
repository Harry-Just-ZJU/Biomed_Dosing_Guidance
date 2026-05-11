package cn.edu.zju.crawler;

/**
 * One-time data-crawl entry point.
 * Run this standalone (not as part of the web app) to populate the database
 * from PharmGKB before starting the web server.
 *
 * Usage:
 *   mvn exec:java -Dexec.mainClass=cn.edu.zju.crawler.Main
 *
 * Comment out steps that have already been completed.
 */
public class Main {
    public static void main(String[] args) {

        DrugLabelCrawler       drugLabelCrawler       = new DrugLabelCrawler();
        DosingGuidelineCrawler dosingGuidelineCrawler = new DosingGuidelineCrawler();

        // Step 1 – fetch master drug list
        drugLabelCrawler.doCrawlerDrug();

        // Step 2 – fetch drug labels for each drug
        drugLabelCrawler.doCrawlerDrugLabel();

        // Step 3 – fetch dosing guidelines
        dosingGuidelineCrawler.doCrawlerDosingGuidelineList();

        System.out.println("Crawl complete.");
    }
}
