package io.banditoz.mchelper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ThreadPoolExecutor;

import io.avaje.config.Config;
import io.banditoz.mchelper.http.PasteggClient;
import io.banditoz.mchelper.utils.paste.Paste;
import io.banditoz.mchelper.utils.paste.PasteResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FileUploadListener extends ListenerAdapter {
    private final ThreadPoolExecutor threadPoolExecutor;
    private final PasteggClient pasteggClient;
    private static final Logger log = LoggerFactory.getLogger(FileUploadListener.class);

    @Inject
    public FileUploadListener(ThreadPoolExecutor threadPoolExecutor,
                              PasteggClient pasteggClient) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.pasteggClient = pasteggClient;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        threadPoolExecutor.execute(() -> {
            Message m = event.getMessage();
            User u = event.getAuthor();
            if (!m.getAttachments().isEmpty()) {
                List<Message.Attachment> attachments = m.getAttachments();
                StringJoiner sj = new StringJoiner("\n");
                for (Message.Attachment a : attachments) {
                    try {
                        String c = a.getContentType();
                        if (c != null && (c.contains("text") || c.contains("json") || c.contains("xml") || c.contains("html"))) {
                            if (a.getSize() > 1024 * 512 ) { // 512 KB
                                log.warn("Attachment {} from message {} is too big, with size {}.", a, m, a.getSize());
                                continue;
                            }
                            try (InputStream is = a.getProxy().download().get()) {
                                String pasteContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                                Paste p = new Paste(pasteContent, a.getFileName());
                                p.setName(a.getFileName() + " by " + u.getName() + " (" + u.getId() + ")");
                                p.setDescription("Automatically generated paste from message " + event.getMessageId());
                                PasteResponse pasteResponse = pasteggClient.uploadPaste(p);
                                String baseUrl = Config.get("mchelper.pastegg.base-url");
                                baseUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
                                sj.add(a.getFileName() + ": " + baseUrl + pasteResponse.getPasteId());
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Could not download/upload attachment to paste service: " + a.toString(), ex);
                    }
                }
                if (!sj.toString().isEmpty()) {
                    event.getMessage().reply(sj.toString()).mentionRepliedUser(false).queue();
                }
            }
        });
    }
}
