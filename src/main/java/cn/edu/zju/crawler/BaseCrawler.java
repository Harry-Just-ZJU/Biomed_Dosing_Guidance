package cn.edu.zju.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(BaseCrawler.class);

    public String getURLContent(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60_000);
            conn.setReadTimeout(60_000);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (PrecisionMedicine crawler; research use)");
            try (InputStream in = conn.getInputStream();
                 ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) >= 0) buf.write(buffer, 0, n);
                return buf.toString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Failed to fetch URL {}: {}", urlString, e.getMessage());
        }
        return null;
    }
}
