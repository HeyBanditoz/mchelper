package io.banditoz.mchelper.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;
import io.banditoz.mchelper.jda.JDAFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestScope
@Factory
public class JDAConfig {
    // TODO move this somewhere else!!!
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Bean
    public JDA jda() {
        JDA jda = mock(JDA.class);
        RestAction<ApplicationInfo> appInfoMock = mock(RestAction.class);
        when(appInfoMock.complete()).thenReturn(mock(ApplicationInfo.class));
        when(jda.retrieveApplicationInfo()).thenReturn(appInfoMock);
        RestAction mock = mock(RestAction.class);
        when(mock.complete()).thenReturn(Collections.emptyList());
        when(jda.retrieveApplicationEmojis()).thenReturn(mock);
        return jda;
    }

    @Bean
    public JDAFactory jdaFactory() {
        return mock(JDAFactory.class);
    }
}
