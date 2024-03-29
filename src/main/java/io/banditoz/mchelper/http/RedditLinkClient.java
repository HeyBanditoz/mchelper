package io.banditoz.mchelper.http;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

public interface RedditLinkClient {
    @RequestLine("GET /{url}")
    @Headers("User-Agent: Mozilla/5.0 (X11; Linux; I HATE YOUR WEBISTE) Well well well... It seems as though you have once again, despite your best efforts, become a pawn in one of my rather elaborate ruses, and it seems as though the end result of said ruse has left your state of being of a lesser quality than before you were dealt the card hidden up my sleeve, while being none the wiser! And yes, while it is true that you will eventually recover from this recent turn of events, it remains unclear whether or not your social status on this website will remain at its current level, or if it will take a turn for the worst! After all is said and done, at the end of the day, you will have to accept the fact that you just got the short end of the deal! I hold no remorse or regret, for I am and always shall be........ someone who believes any link coming from your damned website should redirect to reddit, not amp reddit, not mobile reddit, just good old reddit")
    Response extractRealLinkFromRedditAppLink(@Param("url") String url);
}
