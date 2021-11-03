package org.ships.config.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DebugFile {

    protected final List<String> messages = new ArrayList<>();
    protected final File file = new File("plugins" + File.pathSeparatorChar + "Ships" + File.pathSeparatorChar +
            "debug.txt");

    public void addMessage(String... messages){
        this.messages.addAll(Arrays.asList(messages));
    }

    public File getFile(){
        return this.file;
    }

    public void writeToDebug(){
        if(!this.file.exists()){
            return;
        }
        try {
            FileWriter writer = new FileWriter(this.file);
            this.messages.forEach(m -> {
                try {
                    writer.write(m + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
