package org.zyneonstudios.apex.nexusapp.search.curseforge;

import org.zyneonstudios.apex.nexusapp.downloads.Download;
import org.zyneonstudios.apex.nexusapp.downloads.DownloadManager;
import org.zyneonstudios.apex.nexusapp.events.DownloadEndEvent;
import org.zyneonstudios.apex.nexusapp.main.NexusApplication;
import org.zyneonstudios.apex.nexusapp.search.curseforge.resource.CurseForgeResource;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CurseForgeDownload extends Download {

    private DownloadManager.DownloadState state = DownloadManager.DownloadState.PREPARING;
    private String percentString = "0%";
    private Instant startTime;
    private Instant finishTime = null;
    private long lastBytesRead = 0;
    private int fileSize = 0;
    private boolean finished = false;
    private double percent = 0;
    private DownloadEndEvent event = null;
    private Collection<Download> fileDownloads;

    public CurseForgeDownload(CurseForgeResource project, Collection<Download> fileDownloads, Path basePath) throws MalformedURLException {
        super(UUID.randomUUID(), project.getName(), new URL(project.getUrl()), basePath);
        this.fileDownloads = fileDownloads;
        state = DownloadManager.DownloadState.WAITING;
    }

    public CurseForgeDownload(CurseForgeResource project, Path basePath) throws MalformedURLException {
        super(UUID.randomUUID(), project.getName(), new URL(project.getUrl()), basePath);
        fileDownloads = null;
    }

    @Override
    public void setPreparing(boolean preparing) {
        if(state==DownloadManager.DownloadState.WAITING||state==DownloadManager.DownloadState.PREPARING) {
            if (preparing) {
                state = DownloadManager.DownloadState.PREPARING;
            } else {
                state = DownloadManager.DownloadState.WAITING;
            }
        }
    }

    public Collection<Download> getFileDownloads() {
        return fileDownloads;
    }

    public void setFileDownloads(Collection<Download> fileDownloads) {
        this.fileDownloads = fileDownloads;
    }

    @Override
    public boolean isPreparing() {
        return state.equals(DownloadManager.DownloadState.PREPARING);
    }

    @Override
    public void setFinishEvent(DownloadEndEvent event) {
        this.event = event;
    }

    @Override
    public boolean start() {
        if (state == DownloadManager.DownloadState.WAITING) {
            state = DownloadManager.DownloadState.RUNNING;
            startTime = Instant.now();

            try {
                double weight = 100.0 / fileDownloads.size();
                final int[] finished = {0};
                for (Download download : fileDownloads) {
                    download.setFinishEvent(new DownloadEndEvent(download) {
                        @Override
                        public boolean onFinish() {
                            percent += weight;
                            String s = (int) percent + "%";
                            if (!percentString.equals(s)) {
                                percentString = s;
                            }
                            finished[0] += 1;
                            return false;
                        }
                    });
                    CompletableFuture.runAsync(() -> {
                        download.start();
                    });
                }
                while (finished[0] < fileDownloads.size()) {
                    Thread.sleep(1000);
                }
                setPercent(100);
                return true;
            } catch (Exception e) {
                NexusApplication.getLogger().err("Couldn't download \"" + getUrl() + "\" to \"" + getPath().toString() + "\": " + e.getMessage());
            }
        }
        state = DownloadManager.DownloadState.FAILED;
        if (event != null) {
            event.execute();
        }
        return false;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public int getFileSize() {
        return fileSize;
    }

    @Override
    public long getLastBytesRead() {
        return lastBytesRead;
    }

    @Override
    public String getPercentString() {
        return percentString;
    }

    @Override
    public DownloadManager.DownloadState getState() {
        return state;
    }

    @Override
    public Duration getElapsedTime() {
        if (finishTime != null) {
            return Duration.between(startTime, finishTime);
        }
        return Duration.between(startTime, Instant.now());
    }

    @Override
    public double getSpeedMbps() {
        return -1;
    }


    @Override
    public Duration getEstimatedRemainingTime() {
        return Duration.ofSeconds(-1);
    }

    @Override
    public double getPercent() {
        return percent;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void setPercent(double percent) {
        this.percent = percent;
        if (percent >= 100) {
            setFinished();
        } else if (percent < 0) {
            this.percent = 0;
        }
    }

    private void setFinished() {
        this.finished = true;
        if (event != null) {
            event.execute();
        }
        finishTime = Instant.now();
        percent = 100;
        percentString = "100%";
        state = DownloadManager.DownloadState.FINISHED;
    }
}