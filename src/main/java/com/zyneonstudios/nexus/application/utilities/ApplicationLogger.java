package com.zyneonstudios.nexus.application.utilities;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;

import javax.swing.*;

public class ApplicationLogger extends NexusLogger {

    public ApplicationLogger(String name) {
        super(name);
    }

    @Override
    public void err(String errorMessage) {
        err(errorMessage,true);
    }

    public void err(String errorMessage, boolean dialog) {
        super.err(errorMessage);
        if(dialog) {
            JOptionPane.showMessageDialog(NexusApplication.getInstance().getApplicationFrame(), errorMessage,
                    "NEXUS App (Error)", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void printErr(String prefix, String type, String message, String reason, StackTraceElement[] cause, String... possibleFixes) {
        StringBuilder output = new StringBuilder();
        if(message!=null) {
            if(prefix == null) {
                prefix = "AN ERROR OCCURRED";
            }
            if(type == null) {
                type = "ERROR";
            }
            String s1 = "===("+prefix+")===============================================================/"+type+"/===";
            output.append(s1).append("\n");
            err(s1,false);
            output.append(message).append("\n");
            err(message,false);
            if(reason!=null) {
                output.append("Reason: ").append(reason).append("\n");
                err("Reason: "+reason,false);
            }
            if(possibleFixes!=null) {
                String p = "Possible fix(es): ";
                for(String fix:possibleFixes) {
                    output.append(p).append(fix).append("\n");
                    err(p+fix,false);
                    p = "";
                }
            }
            if(cause!=null) {
                output.append("\nCaused by:\n");
                err(" ",false);
                err("Caused by:",false);
                for(StackTraceElement element:cause) {
                    output.append(" ").append(element.toString()).append("\n");
                    err(" "+element.toString(),false);
                }
            }
            String s2 = "===/"+type+"/===============================================================("+prefix+")===";
            output.append(s2).append("\n");
            err(s2,false);
        }
        JOptionPane.showMessageDialog(NexusApplication.getInstance().getApplicationFrame(), output.toString(),
                "NEXUS App (Error)", JOptionPane.ERROR_MESSAGE);
    }
}