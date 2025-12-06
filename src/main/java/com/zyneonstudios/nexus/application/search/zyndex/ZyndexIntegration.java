package com.zyneonstudios.nexus.application.search.zyndex;

import com.zyneonstudios.nexus.application.downloads.Download;
import com.zyneonstudios.nexus.application.events.DownloadFinishEvent;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.search.zyndex.local.LocalInstance;
import com.zyneonstudios.nexus.instance.Instance;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ZyndexIntegration {

    public static boolean install(Instance instance, Path installDirPath) {
        return install(instance,installDirPath.toString());
    }

    public static boolean install(Instance instance, String installDirPathString) {
        return install(instance,new File(installDirPathString));
    }

    public static boolean install(Instance instance, File installDir) {
        installDir = getInstallDir(installDir, instance.getId());
        if(installInstance(instance,installDir)) {
            NexusApplication.getInstance().getInstanceManager().addInstance(new LocalInstance(installDir+"/zyneonInstance.json"));
            System.gc();
            return true;
        }
        System.gc();
        return false;
    }

    @SuppressWarnings("all")
    private static boolean installInstance(Instance instance, File installDir) {
        try {
            Path path = Paths.get(NexusApplication.getInstance().getWorkingDir() +"/temp/"+ UUID.randomUUID() +".zip");
            Download download = new Download(instance.getName(), URI.create(instance.getDownloadUrl()).toURL(),path);
            NexusApplication.getInstance().getDownloadManager().addDownload(download);
            while (!download.isFinished()) {
                Thread.sleep(1000);
            }
            File zip = path.toFile();
            if(FileExtractor.unzipFile(zip.getAbsolutePath(),installDir.getAbsolutePath())) {
                zip.delete();
                return FileGetter.downloadFile(instance.getLocation(),installDir.getAbsolutePath()+"/zyneonInstance.json").exists();
            }
            zip.delete();
            if(NexusApplication.getInstance().getApplicationFrame().getBrowser().getURL().toLowerCase().contains("page=library")) {
                NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
            }
        } catch (Exception e) {
            NexusApplication.getLogger().err("[Minecraft] (ZyndexIntegration) Couldn't install instance "+instance.getId()+" v"+instance.getVersion()+": "+e.getMessage());
        }
        return false;
    }

    public static File getInstallDir(File installDir, String id) {
        File bak = installDir;
        if(!installDir.getName().equalsIgnoreCase(id)) {
            installDir = new File(installDir.getAbsolutePath() + "/" + id.replace("/","-")+"/");
        }
        if(!installDir.exists()) {
            if(!installDir.mkdirs()) {
                throw new NullPointerException("Could not find or create instance directory \""+installDir.getAbsolutePath()+"\"");
            }
        } else {
            return getInstallDir(bak, id+"-"+ StringGenerator.generateAlphanumericString(8));
        }
        return installDir;
    }
}
