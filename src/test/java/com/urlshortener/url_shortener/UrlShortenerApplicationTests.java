package com.urlshortener.url_shortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = {
				"spring.data.redis.host=localhost",
				"spring.data.redis.port=6379"
		}
)
class UrlShortenerApplicationTests {

	@Test
	void contextLoads() {
	}
}