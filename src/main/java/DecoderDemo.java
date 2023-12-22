import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.vosk.LogLevel;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.Model;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

public class DecoderDemo {

    public static void main(String[] argv) throws IOException, UnsupportedAudioFileException {
        LibVosk.setLogLevel(LogLevel.DEBUG);

        File folder = new File("");//Path to your audio messages folder
        File[] listOfFiles = folder.listFiles();

        Model model = new Model("model");//vosk model folder name/path
        Recognizer recognizer = new Recognizer(model, 16000);

        int index = 0;
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && (file.getName().endsWith(".ogg")||file.getName().endsWith(".mp4"))) {
                System.out.println("File: " + file.getName() + ", Index: " + ++index);
                String name = file.getName().substring(0, file.getName().length() - 4);

                if (new File(folder.getAbsolutePath() + "\\" + name + ".txt").exists()) {
                    System.out.println("Skipped");
                    continue;
                }

                // Create attributes for encoding
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec("pcm_s16le");
                audio.setBitRate(16000);  // 16kHz
                audio.setChannels(1);  // mono
                audio.setSamplingRate(16000);  // 16kHz

                // Encoding attributes
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setOutputFormat("wav");
                attrs.setAudioAttributes(audio);

                // Encoder
                Encoder encoder = new Encoder();
                File target = new File(folder.getAbsolutePath() + "\\output.wav");
                try {
                    encoder.encode(new MultimediaObject(file), target, attrs);
                } catch (IllegalArgumentException | EncoderException e) {
                    e.printStackTrace();
                }

                //Conversion

                InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(target.getAbsolutePath())));

                StringBuilder sb = new StringBuilder();
                int nbytes;
                byte[] b = new byte[4096];
                while ((nbytes = ais.read(b)) >= 0) {
                    if (recognizer.acceptWaveForm(b, nbytes)) {
                        sb.append(getPrettyString(recognizer.getResult(), index));
                    }
                }

                ais.close();

                sb.append(getPrettyString(recognizer.getFinalResult(), index));

                try (PrintWriter out = new PrintWriter(folder.getAbsolutePath() + "\\" + name + ".txt")) {
                    out.println(sb.toString().trim().substring(0, sb.length()-2)+".");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                File deleteThis = new File(folder.getAbsolutePath() + "\\output.wav");
                System.out.println("Deleted temp files: " + deleteThis.delete());
            }
        }
    }
    public static String getPrettyString(String input, int index){
        try{
            return input.split("\"text\" : \"")[1].split("\"")[0] + ",\n";
        } catch (Exception e){
            System.out.println("Err at " + index);
            return input;
        }
    }
}
