package com.crewmeister.cmcodingchallenge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Configuration
public class MockServerConfig {

    @Autowired
    private RestTemplate restTemplate; // Ensure your app uses this RestTemplate

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void initMockServer() throws IOException {
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate)
                .ignoreExpectOrder(true)
                .build();

        String audResponse = loadXml("classpath:mock-responses/response-for-aud.xml");
        String tryResponse = loadXml("classpath:mock-responses/response-for-try.xml");
        String usdResponse = loadXml("classpath:mock-responses/response-for-usd.xml");

        mockServer.expect(requestTo("http://example.com/api/getData?Id=BBEX3.D.AUD.EUR.BB.AC.000"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(audResponse, MediaType.APPLICATION_XML));
        mockServer.expect(requestTo("http://example.com/api/getData?Id=BBEX3.D.TRY.EUR.BB.AC.000"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(tryResponse, MediaType.APPLICATION_XML));
        mockServer.expect(requestTo("http://example.com/api/getData?Id=BBEX3.D.USD.EUR.BB.AC.000"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(usdResponse, MediaType.APPLICATION_XML));
    }

    private String loadXml(final String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}