package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.RowFilter.Entry;

/**
 * staging area
 */
public class Stage {
    // store the file's sha1 name, and sha1 blob
    static HashMap<String, String> stagedAddition;
    static HashMap<String, String> stagedRemoval;
    static File stageAdditionFile = Utils.join(Repositories.STAGE_FOLDER, "stageaddition");
    static File stageRemovalFile = Utils.join(Repositories.STAGE_FOLDER, "stagedremoval");

    // init addition dn removal
    private static void load() throws IOException {
        if (!stageAdditionFile.exists()) {
            stagedAddition = new HashMap<>();
            Utils.writeObject(stageAdditionFile, stagedAddition);
        }
        if (!stageRemovalFile.exists()) {
            stagedRemoval = new HashMap<>();
            Utils.writeObject(stageRemovalFile, stagedRemoval);
        }
        stagedAddition = (HashMap<String, String>) Utils.readObject(stageAdditionFile, HashMap.class);
        stagedRemoval = (HashMap<String, String>) Utils.readObject(stageRemovalFile, HashMap.class);
    }

    private static void save() {
        Utils.writeObject(stageAdditionFile, stagedAddition);
        Utils.writeObject(stageRemovalFile, stagedRemoval);
    }

    // save file in stage Addition
    public static void addFile(String filename) throws IOException {
        load();

        Blob blob = new Blob(filename, Utils.join(Repositories.CWD, filename));
        String fileNameSHA1 = Utils.sha1(filename);
        File blobFile = Utils.join(Repositories.BLOB_FOLDER, blob.getSha1());
        if (blobFile.exists()) {
            return;
        }
        Utils.writeObject(blobFile, blob);
        // add to stage
        stagedAddition.put(fileNameSHA1, blob.getSha1());
        // save it
        save();
    }

    public static void showAdditionFiles() throws IOException {
        load();
        Prompt.logTitle("Staged Files");
        for (Map.Entry<String, String> pair : stagedAddition.entrySet()) {
            File blobFile = Utils.join(Repositories.BLOB_FOLDER, pair.getValue());
            Blob blob = Utils.readObject(blobFile, Blob.class);
            Prompt.log(blob.filename);
        }
    }

    public static boolean isStageEmpty() throws IOException {
        load();
        return stagedAddition.isEmpty() && stagedRemoval.isEmpty();
    }

    /**
     * clear called when we finish commit, all remove files gones
     * 
     * @throws IOException
     */
    public static void clear() throws IOException {
        load();
        stagedAddition.clear();
        for (Map.Entry<String, String> pair : stagedRemoval.entrySet()) {
            File blobFile = Utils.join(Repositories.BLOB_FOLDER, pair.getValue());
            blobFile.delete();
        }
        stagedRemoval.clear();
        save();
    }

}
