package io.banditoz.mchelper.http.scraper;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RssScraper {
    private final RssReader reader;

    public RssScraper() {
        this.reader = new RssReader();
    }

    public Map<Channel, List<Item>> getRssForAll(String... urls) {
        return reader.read(List.of(urls))
                .collect(Collectors.groupingBy(Item::getChannel));
    }
}
