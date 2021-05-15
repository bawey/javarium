package foo.bawey.streamer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import reactor.core.publisher.Flux;

public class Streamer implements Runnable {
    private static final Logger logger = Logger.getLogger(Streamer.class.getName());

    private URL playlistUrl;

    private boolean goOn = true;
    private Set<URI> fetched = new HashSet<>();
    private File output;

    public Streamer(URL playlistUrl) {
        this(playlistUrl, Paths.get(System.getProperty("user.home"), Instant.now().getEpochSecond() + ".mp4"));
    }

    public Streamer(URL playlistUrl, Path pathToTarget) {
        this.playlistUrl = playlistUrl;
        this.output = pathToTarget.toFile();
    }

    public static void main(String[] args) throws Exception {
        String url = "https://1f3mguoorurpkou6.ezcdn654.net:8443/hls/mo722nr.m3u8?s=Vw1lsAF5B9BpVltzn2HqnQ&e=1621128762";
        Streamer streamer = new Streamer(new URL(url));
        streamer.run();
    }

    public List<URL> fetchPlaylistAndGrabUrls(URL url) {
        List<URL> newUrls = new LinkedList<>();
        logger.info("Requesting playlist from: " + url);
        try (InputStream stream = url.openStream()) {

            byte[] bytes = stream.readAllBytes();
            String content = new String(bytes, Charset.defaultCharset().name());
            for (String line : content.split(System.lineSeparator())) {
                if (line.endsWith(".ts")) {
                    String subPath = url.getPath();
                    subPath = subPath.substring(0, subPath.lastIndexOf("/") + 1);
                    newUrls.add(new URL(url.getProtocol(), url.getHost(), url.getPort(), subPath + line));
                }
            }
            logger.info(String.format("Fetched %d segment URLs", newUrls.size()));
            return newUrls;

        } catch (IOException ioe) {
            logger.warning("Something went wrong" + ioe.getMessage());
        }
        return newUrls;
    }

    public void consumeSegmentUrl(URL url) {
        logger.info("Consuming segment URL: " + url);
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
            return;
        }

        if (fetched.contains(uri)) {
            logger.info(() -> "Skipping segment " + uri);
            return;
        }
        try (InputStream stream = url.openStream(); FileOutputStream fos = new FileOutputStream(output, true);) {
            logger.info("Will try fetching: " + url + ", fetched so far: " + fetched.size());
            this.fetched.add(url.toURI());
            byte[] content = stream.readAllBytes();
            logger.info("Done fetching, will write...");
            fos.write(content);
            logger.info(String.format("Done writing, fetched so far %d", fetched.size()));

        } catch (Exception e) {
            fetched.remove(uri);
            logger.log(java.util.logging.Level.SEVERE, "Got a problem", e);
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        logger.info(String.format("Launching to fetch from: %s", this.playlistUrl.toString()));
        Flux<URL> urlFlux = Flux.interval(Duration.ofMillis(4000)).takeWhile((l) -> this.goOn)
                .flatMapIterable(l -> this.fetchPlaylistAndGrabUrls(this.playlistUrl)).distinct().doOnComplete(() -> {
                    this.goOn = false;
                }).doOnError((Throwable error) -> {
                    this.goOn = false;
                });
        urlFlux.subscribe(this::consumeSegmentUrl);
        while (goOn) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("App is about to terminate!");
    }
}