import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AddTextToMessages {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        File folder = new File("");//Path to your export folder

        try (FileReader reader = new FileReader(folder.getAbsolutePath() + "\\result.json", StandardCharsets.UTF_8)) {
            // Parse JSON file
            JsonNode rootNode = objectMapper.readTree(reader);

            // Get the "messages" array
            JsonNode messagesNode = rootNode.path("messages");

            // Iterate through each message
            for (int i = 0; i < messagesNode.size(); i++) {
                JsonNode messageNode = messagesNode.get(i);

                // Get the file name from the message
                String fileName = messageNode.path("file").asText();

                // Check if it is a video or voice message
                if (fileName != null && fileName.contains("round_video_messages")) {
                    // Extract text from corresponding txt file
                    String textFileName = fileName.replace(".mp4", ".txt");
                    String text = readTextFromFile(textFileName, folder);

                    // Add the text to the message
                    ((com.fasterxml.jackson.databind.node.ObjectNode) messageNode).put("text", text);
                } else if (fileName != null && fileName.contains("voice_messages")) {
                    // Extract text from corresponding txt file
                    String textFileName = fileName.replace(".ogg", ".txt");
                    String text = readTextFromFile(textFileName, folder);

                    // Add the text to the message
                    ((com.fasterxml.jackson.databind.node.ObjectNode) messageNode).put("text", text);
                }
                System.out.println("Processing message at index " + i);
            }

            try (FileWriter fileWriter = new FileWriter(folder.getAbsolutePath() + "\\modified_json_output.json", StandardCharsets.UTF_8)) {
                ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
                objectWriter.writeValue(fileWriter, rootNode);
                System.out.println("Modified JSON saved to modified_json_output.json");
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readTextFromFile(String fileName, File folder) {
        String filePath = folder.getPath() + File.separator + fileName;
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content.toString();
    }
}