package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.paste.Paste;
import io.banditoz.mchelper.utils.paste.PasteggUploader;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

public class FileUploadListener extends ListenerAdapter {
    private final MCHelper MCHELPER;
    private final PasteggUploader UPLOADER;
    private final Logger LOGGER = LoggerFactory.getLogger(FileUploadListener.class);

    public FileUploadListener(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
        this.UPLOADER = new PasteggUploader(mcHelper);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MCHELPER.getThreadPoolExecutor().execute(() -> {
            Message m = event.getMessage();
            User u = event.getAuthor();
            if (!m.getAttachments().isEmpty()) {
                List<Message.Attachment> attachments = m.getAttachments();
                StringJoiner sj = new StringJoiner("\n");
                for (Message.Attachment a : attachments) {
                    try {
                        String c = a.getContentType();
                        if (c != null && (c.contains("text") || c.contains("json") || c.contains("xml") || c.contains("html"))) {
                            try (InputStream is = a.getProxy().download().get()) {
                                String pasteContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                                Paste p = new Paste(pasteContent, a.getFileName());
                                p.setName(a.getFileName() + " by " + u.getName() + " (" + u.getId() + ")");
                                p.setDescription("Automatically generated paste from message " + event.getMessageId());
                                String pasteUrl = UPLOADER.uploadToPastegg(p);
                                sj.add(a.getFileName() + ": " + pasteUrl);
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Could not download/upload attachment to paste service: " + a.toString(), ex);
                    }
                }
                if (!sj.toString().isEmpty()) {
                    event.getMessage().reply(sj.toString()).mentionRepliedUser(false).queue();
                }
            }
        });
    }
}
