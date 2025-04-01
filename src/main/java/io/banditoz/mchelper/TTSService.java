package io.banditoz.mchelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.config.Config;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.SplitUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TTSService {
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(TTSService.class);
    private static final Random random = new Random();

    // yeah yeah yeah this is REALLY bad practice, but I don't want to have to refactor more code for this april fools :D
    private static final TTSService SINGLETON = new TTSService(new OkHttpClient(), new ObjectMapper());

    public TTSService(OkHttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public static TTSService getInstance() {
        return SINGLETON;
    }

    /**
     * new TIKTOK feature!!! for when you need the tiktok TTS voice!!!!!!
     * @param text The text?
     * @return TTSResponse object containing everything u need to TTS-ize your message :D
     */
    public TTSResponse generateTTSFileFromString(String text) throws Exception {
        String fileName = UUID.randomUUID().toString();
        byte[] bytes = processTextToSpeech(text);
        Files.write(Path.of(fileName + ".mp3"), bytes);
        // -err_detect ignore_error...... Idk why this is needed, idk what im doing really
        // reencode to mp3 again to fix weird errors i may have caused by concatenation
        // since despite JDA docs recommending .ogg to use in their example
        // it seems iphones actually cannot play it
        Process exec = new ProcessBuilder("ffmpeg", "-hide_banner", "-loglevel", "error", "-err_detect", "ignore_err", "-i", fileName + ".mp3", fileName + "_corrected.mp3").start();
        exec.waitFor();
        // the file has been encoded :) maybe
        // guess we'll find out lmao

        // we need duration here because discord doesn't know how to run ffprobe or otherwise get audio file length on
        // their backend, so we gotta do it, smh discord
        exec = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", fileName + "_corrected.mp3").start();
        exec.waitFor();
        double duration = Double.parseDouble(new String(exec.getInputStream().readAllBytes()));

        // CLEANUP OLD FILES but in the WORST WAY POSSIBLE!!!
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(180_000); // WAIT TO CLEAN UP!!!! WHAT IS GOOD CODE DESIGN?
                Files.delete(Path.of(fileName + ".mp3"));
                Files.delete(Path.of(fileName + "_corrected.mp3"));
            } catch (InterruptedException e) {
                // what?
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("Error while deleting files!!!! CRAP!!!!!!!!!!!", e);
            }
        });
        // NOW generate random bytes for waveform
        // couldn't figure out how to get a real waveform from ffmpeg in time
        // sorry :(
        // and idk why discord cant just compute this on their backend
        // guess audio processing is expensive these days
        byte[] waveform = new byte[255];
        random.nextBytes(waveform);
        return new TTSResponse(Path.of(fileName + "_corrected.mp3"), duration, waveform);
    }

    public static String getMessageEmbedForTts(MessageEmbed me) {
        // don't look too closely... i know this is horrifying, but i have less than two hours to finish this before
        // it is april fools
        StringBuilder ttsText = new StringBuilder();
        ttsText.append(me.getAuthor() == null ? "" : me.getAuthor().getName() + ". ");
        ttsText.append(me.getTitle() == null ? "" : me.getTitle() + ". ");
        ttsText.append(me.getDescription() == null ? "" : me.getDescription() + ". ");
        // inline fields first, since they render above non-inlined ones
        for (MessageEmbed.Field field : me.getFields()) {
            ttsText.append(field.getName()).append(". ").append(field.getValue()).append(". ");
        }
        ttsText.append(me.getFooter() == null ? "" : me.getFooter().getText() + ". ");
        ttsText.append(me.getTimestamp() == null ? "" : me.getTimestamp().toString() + ". ");
        return ttsText.toString();
    }

    private byte[] processTextToSpeech(String text) {
        List<String> chunks = SplitUtil.split(text, 300, SplitUtil.Strategy.WHITESPACE);

        List<String> base64Strings = new ArrayList<>();
        for (String chunk : chunks) {
            String base64String = generateAudio(chunk);
            base64Strings.add(base64String);
        }

        return concatenateBase64Mp3(base64Strings);
    }

    private String generateAudio(String chunk) {
        RequestBody formBody = new FormBody.Builder()
                .add("req_text", chunk)
                .add("speaker_map_type", "0")
                .add("aid", "1233")
                .add("text_speaker", "en_us_001") // the CLASSIC horrible tiktok female voice
                .build();

        Request request = new Request.Builder()
                .url("https://api.tiktokv.com/media/api/text/speech/invoke/")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .header("User-Agent", "com.zhiliaoapp.musically/2022600030 (Linux; U; Android 7.1.2; es_ES; SM-G988N; Build/NRD90M;tt-ok/3.12.13.1)")
                .header("Cookie", "sessionid=" + Config.get("mchelper.tiktok-session-id"))
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Code " + response.code() + " while generating TTS.");
            }

            // oh well, should have done java records. too late :D
            JsonNode responseJson = objectMapper.readTree(response.body().string());

            // when an HTTP status code isn't enough, let's _also_ add one in the JSON! because WHY NOT!
            int jsonStatus = responseJson.get("status_code").asInt();
            if (jsonStatus == 0) {
                return responseJson.get("data").get("v_str").asText();
            }
            else {
                throw new RuntimeException("Invalid API response code: " + jsonStatus);
            }
        } catch (Exception ex) {
            // actual java devs, don't look at what's going on here
            throw new RuntimeException(ex);
        }
    }

    /** This method does magic */
    private byte[] concatenateBase64Mp3(List<String> encodedFiles) {
        byte[] finalAudioData = new byte[0];

        for (int i = 0; i < encodedFiles.size(); i++) {
            byte[] decodedData = Base64.getDecoder().decode(encodedFiles.get(i));

            if (i == 0) {
                finalAudioData = decodedData;
            } else {
                if (decodedData.length > 10 && decodedData[0] == 'I' && decodedData[1] == 'D' && decodedData[2] == '3') {
                    int tagSize = ((decodedData[6] & 0x7F) << 21) |
                            ((decodedData[7] & 0x7F) << 14) |
                            ((decodedData[8] & 0x7F) << 7) |
                            (decodedData[9] & 0x7F);

                    byte[] dataWithoutID3 = new byte[decodedData.length - (10 + tagSize)];
                    System.arraycopy(decodedData, 10 + tagSize, dataWithoutID3, 0, dataWithoutID3.length);
                    decodedData = dataWithoutID3;
                }

                byte[] newFinalAudioData = new byte[finalAudioData.length + decodedData.length];
                System.arraycopy(finalAudioData, 0, newFinalAudioData, 0, finalAudioData.length);
                System.arraycopy(decodedData, 0, newFinalAudioData, finalAudioData.length, decodedData.length);
                finalAudioData = newFinalAudioData;
            }
        }

        return finalAudioData;
    }

    /**
     * @param path Path of the .ogg. It will get cleaned up in 3 minutes. so HURRY!!!!!!
     * @param length Length of the file in seconds.
     * @param waveform Randomized waveform.
     */
    public record TTSResponse(Path path, double length, byte[] waveform) {}
}