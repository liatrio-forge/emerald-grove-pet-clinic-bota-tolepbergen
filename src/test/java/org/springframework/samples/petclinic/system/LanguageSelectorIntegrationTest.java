package org.springframework.samples.petclinic.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
class LanguageSelectorIntegrationTest {

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private TestRestTemplate rest;

	private ResponseEntity<String> getHtml(String path) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.TEXT_HTML));
		return rest.exchange("http://localhost:" + port + path, HttpMethod.GET, new HttpEntity<>(headers),
				String.class);
	}

	@Test
	void shouldRenderLanguageSelectorOnHomepage() {
		ResponseEntity<String> resp = getHtml("/");
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resp.getBody()).contains("id=\"languageSelector\"");
		assertThat(resp.getBody()).contains("?lang=en");
		assertThat(resp.getBody()).contains("?lang=es");
		assertThat(resp.getBody()).contains("?lang=de");
	}

	@Test
	void shouldShowThreeLanguageOptions() {
		ResponseEntity<String> resp = getHtml("/");
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resp.getBody()).contains("English");
		assertThat(resp.getBody()).contains("Español");
		assertThat(resp.getBody()).contains("Deutsch");
	}

	@Test
	void shouldSwitchToSpanishWhenLangEsRequested() {
		ResponseEntity<String> resp = getHtml("/?lang=es");
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resp.getBody()).contains("Inicio");
		assertThat(resp.getBody()).contains("Buscar propietarios");
		assertThat(resp.getBody()).contains("Veterinarios");
	}

	@Test
	void shouldSwitchToGermanWhenLangDeRequested() {
		ResponseEntity<String> resp = getHtml("/?lang=de");
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resp.getBody()).contains("Startseite");
		assertThat(resp.getBody()).contains("Besitzer suchen");
		assertThat(resp.getBody()).contains("Tierärzte");
	}

}
