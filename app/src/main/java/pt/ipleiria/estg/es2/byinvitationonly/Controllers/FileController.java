package pt.ipleiria.estg.es2.byinvitationonly.Controllers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Models.Conference;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;

public class FileController {


    public static final String CONFERENCE_FILE = "DadosConferencia.csv";
    public static final String SESSIONS_FILE = "DadosSessoes.csv";

    public static Conference importConference(Context context) throws IOException {
        File file = new File(context.getFilesDir(), CONFERENCE_FILE);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            HashMap<String, String> conferenceData = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                conferenceData.put(parts[0], parts[1]);
            }
            br.close();

            if (!conferenceData.isEmpty()) {

                return new Conference(
                        conferenceData.get(FirebaseController.CONFERENCE_ATTRIBUTES[0]),
                        conferenceData.get(FirebaseController.CONFERENCE_ATTRIBUTES[3]),
                        conferenceData.get(FirebaseController.CONFERENCE_ATTRIBUTES[4]),
                        conferenceData.get(FirebaseController.CONFERENCE_ATTRIBUTES[2]),
                        conferenceData.get(FirebaseController.CONFERENCE_ATTRIBUTES[5]),
                        conferenceData.get(FirebaseController.CONFERENCE_ATTRIBUTES[6]),
                        conferenceData.get(FirebaseController.CONFERENCE_ATTRIBUTES[1]),
                        Float.valueOf(conferenceData.get("Rating")),
                        conferenceData.get("FirebaseNode"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LinkedList<Session> importSessions(Context context) {
        File file = new File(context.getFilesDir(), SESSIONS_FILE);
        LinkedList<Session> sessions = new LinkedList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String[] campos = br.readLine().split("\\|");
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                //Garante que todas as linhas têm o formato correto
                if (parts.length == campos.length) {
                    sessions.add(new Session(parts[0], parts[1], parts[2], parts[3],
                            parts[4], parts[5], parts[6], parts[7], parts[8], parts[9], parts[10]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return sessions;
    }

    public static void copyAssets(final AssetManager am, Context context) {
        String[] files;
        try {
            files = am.list("");
            for (String filename : files) {
                if (filename.equals("images") || filename.equals("kioskmode") ||
                        filename.equals("sounds") || filename.equals("webkit")) {
                    Log.i("Copy Assets", "Skipping folder: " + filename);
                    continue;
                }

                InputStream in = null;
                OutputStream out = null;
                try {
                    in = am.open(filename);
                    File outFile = new File(context.getFilesDir(), filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static boolean existFiles(Context context) {
        File outFile1 = new File(context.getFilesDir(), CONFERENCE_FILE);
        File outFile2 = new File(context.getFilesDir(), SESSIONS_FILE);
        return outFile1.exists() && outFile2.exists();
    }

    public static void exportConference(Context context, Conference conference) {
        PrintWriter out = null;
        try {
            File outFile = new File(context.getFilesDir(), CONFERENCE_FILE);
            out = new PrintWriter(outFile);
            out.println(FirebaseController.CONFERENCE_ATTRIBUTES[0] + "|" + conference.getAbbreviation());
            out.println(FirebaseController.CONFERENCE_ATTRIBUTES[3] + "|" + conference.getFullName());
            out.println(FirebaseController.CONFERENCE_ATTRIBUTES[4] + "|" + conference.getLocation());
            out.println(FirebaseController.CONFERENCE_ATTRIBUTES[2] + "|" + conference.getDates());
            out.println(FirebaseController.CONFERENCE_ATTRIBUTES[5] + "|" + conference.getLogoURL());
            out.println(FirebaseController.CONFERENCE_ATTRIBUTES[6] + "|" + conference.getWebsite());
            out.println(FirebaseController.CONFERENCE_ATTRIBUTES[1] + "|" + conference.getCallForPapers());
            out.println("Rating" + "|" + conference.getMyRating());
            out.println("FirebaseNode" + "|" + conference.getFirebaseConferenceNode());
            out.flush();
        } catch (IOException e) {
            Log.e("tag", "Failed to write file: ", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void exportSessions(Context context, LinkedList<Session> sessions) {
        PrintWriter out = null;
        int counter = 0;
        try {
            File outFile = new File(context.getFilesDir(), SESSIONS_FILE);
            out = new PrintWriter(new FileWriter(outFile));
            String text = "Date|Start|End|Room|Track|Title|Presenter|Abstract|Rating|FirebaseNode|OnAgenda";
            out.println(text);
            for (Session session : sessions) {
                text = session.getDateFormattedString() + "|" +
                        session.getStartHour() + "|" +
                        session.getEndHour() + "|" +
                        session.getRoom() + "|" +
                        session.getTrack() + "|" +
                        session.getTitle() + "|" +
                        session.getPresenter() + "|" +
                        session.getAbstracts() + "|" +
                        session.getMyRating() + "|" +
                        session.getFirebaseSessionNode() + "|" +
                        session.isOnAgenda();
                counter++;
                if (counter < sessions.size()) {
                    out.println(text);
                } else {
                    out.print(text);
                }
            }

        } catch (IOException e) {
            Log.e("tag", "Failed to write file: ", e);
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    public static void updateSessionStateOnAgenda(Context context, Session session) {

        File inFile = new File(context.getFilesDir(), SESSIONS_FILE);
        if (!inFile.isFile()) {
            System.out.println("Parameter is not an existing file");
            return;
        }

        File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
        BufferedReader br = null;
        PrintWriter pw = null;

        try {

            br = new BufferedReader(new FileReader(inFile));
            pw = new PrintWriter(new FileWriter(tempFile));

            String line;
            int lines = 0;
            while ((line = br.readLine()) != null) {
                lines++;
            }
            br.close();

            String lineToRemove = session.getDateFormattedString() + "|" +
                    session.getStartHour() + "|" +
                    session.getEndHour() + "|" +
                    session.getRoom() + "|" +
                    session.getTrack() + "|" +
                    session.getTitle() + "|" +
                    session.getPresenter() + "|" +
                    session.getAbstracts() + "|" +
                    session.getMyRating() + "|" +
                    session.getFirebaseSessionNode() + "|" +
                    !session.isOnAgenda();

            br = new BufferedReader(new FileReader(inFile));
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals(lineToRemove)) {
                    pw.println(line);
                }
            }

            String lineToAdd = session.getDateFormattedString() + "|" +
                    session.getStartHour() + "|" +
                    session.getEndHour() + "|" +
                    session.getRoom() + "|" +
                    session.getTrack() + "|" +
                    session.getTitle() + "|" +
                    session.getPresenter() + "|" +
                    session.getAbstracts() + "|" +
                    session.getMyRating() + "|" +
                    session.getFirebaseSessionNode() + "|" +
                    session.isOnAgenda();
            pw.print(lineToAdd);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            pw.flush();
            pw.close();
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (!inFile.delete()) {
            return;
        }

        if (!tempFile.renameTo(inFile)) {
            System.out.println("Could not rename file");
        }


    }

    public static boolean isSessionOnAgenda(Context context, Session session) {
        try {
            File inFile = new File(context.getFilesDir(), SESSIONS_FILE);
            if (!inFile.isFile()) {
                System.out.println("Parameter is not an existing file");
                return false;
            }

            String line;
            String[] partsSession = {session.getDateFormattedString(), session.getStartHour(), session.getEndHour(),
                    session.getRoom(), session.getTrack(), session.getTitle(), session.getPresenter(), session.getAbstracts()};
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts[0].equals(partsSession[0]) && parts[1].equals(partsSession[1]) &&
                        parts[2].equals(partsSession[2]) && parts[3].equals(partsSession[3]) &&
                        parts[4].equals(partsSession[4]) && parts[5].equals(partsSession[5]) &&
                        parts[6].equals(partsSession[6]) && parts[7].equals(partsSession[7])) {
                    br.close();
                    return Boolean.valueOf(parts[10]);
                }
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
